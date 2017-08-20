package space.edhits.edtrust;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import space.edhits.edtrust.data.CmdrList;
import space.edhits.edtrust.data.ListSubscription;
import space.edhits.edtrust.data.UserProfileData;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static space.edhits.edtrust.Constants.*;

/**
 * Created by inb on 31/07/2017.
 */
@Controller
public class WebUiController {

    static final String REGISTRATION_TEMPLATE = "register";

    @Autowired
    protected UserApiContextFactory userFactory;

    @Autowired
    protected UserProfileData users;

    @Autowired
    private CmdrList lists;

    @Autowired
    private ListApiContextFactory listFactory;


    String getUserEmail(Principal principal, Model model) {
        if (principal != null) {
            LinkedHashMap details = (LinkedHashMap) ((OAuth2Authentication) principal).getUserAuthentication().getDetails();
            if (details != null) {
                model.addAttribute("userFullName", details.getOrDefault("name", "no name"));
                String email = (String) details.getOrDefault("email", "no name");
                model.addAttribute("email", email);
                return email;
            }
        }
        return null;
    }

    void addUserError(Model model, String error) {
        model.addAttribute("userError", error);
    }

    UserApiContext getUserContext(String email) throws UnknownUser {
        return userFactory.getUserByEmail(email);
    }

    UserApiContext getRegistered(String email, Model model) throws UnknownUser, UnknownList {
        if (email == null) throw new UnknownUser();

        UserApiContext user = getUserContext(email);
        model.addAttribute("user", user);
        model.addAttribute("registered", true);
        model.addAttribute("ownedLists", user.getOwnedLists());
        return user;
    }

    boolean checkRegistered(String email, Model model) {
        try {
            getRegistered(email, model);
            return true;
        } catch (UnknownUser | UnknownList ignored) {
            return false;
        }
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Principal principal, Model model) {
        checkRegistered(getUserEmail(principal, model), model);
        return "index";
    }

    @RequestMapping(value = "/about", method = RequestMethod.GET)
    public String about(Principal principal, Model model) {
        checkRegistered(getUserEmail(principal, model), model);
        return "about";
    }

    @RequestMapping(value = "/lists", method = RequestMethod.GET)
    public String lists(Principal principal, Model model) throws UnknownUser {
        if (checkRegistered(getUserEmail(principal, model), model)) {
            return "lists";
        }
        return REGISTRATION_TEMPLATE;
    }

    @RequestMapping(value = "/lists", method = RequestMethod.POST)
    public String listsCreate(Principal principal, Model model,
                              @RequestParam(value = "newListName", required = true) String newName) throws UnknownUser, UnknownList {
        UserApiContext user = getRegistered(getUserEmail(principal, model), model);
        newName = Sanitizer.listName(newName);

        if (!user.admin) {
            if (user.getOwnedLists().size() > ListApiContext.MAX_LISTS_COUNT) {
                addUserError(model, "Max lists limit reached");
                return "lists";
            }
        }

        try {
            lists.createList(user.userId, newName);
        } catch (NameExists nameExists) {
            addUserError(model, "that name is not allowed or already exists.");
        }
        // refresh the list values
        getRegistered(getUserEmail(principal, model), model);

        return "lists";
    }

    @RequestMapping(value = "/list/{listName}/delete", method = RequestMethod.POST)
    public RedirectView deleteList(Principal principal, Model model,
                                   @PathVariable("listName") String listName) throws UnknownUser, UnknownList {
        UserApiContext user = getRegistered(getUserEmail(principal, model), model);
        lists.deleteList(lists.getList(listName));
        return new RedirectView("/lists");
    }

    @RequestMapping(value = "/list/{listName}", method = RequestMethod.GET)
    public String viewList(Principal principal, Model model,
                           @PathVariable("listName") String listName,
                           @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset
    ) throws UnknownUser, UnknownList {

        UserApiContext user = getRegistered(getUserEmail(principal, model), model);
        ListApiContext list = listFactory.getList(listName);

        model.addAttribute("list", list);
        model.addAttribute("offset", offset);

        model.addAttribute(Constants.RESPONSE_STATUS_HOSTILE, list.getItems(offset, 50, Constants.RESPONSE_STATUS_HOSTILE));
        model.addAttribute(Constants.RESPONSE_STATUS_FRIENDLY, list.getItems(offset, 50, Constants.RESPONSE_STATUS_FRIENDLY));

        return "list";
    }

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public String searchForLists(Principal principal, Model model,
                                 @RequestParam(value = "contains", required = false) String name) throws UnknownList, UnknownUser {

        // find public or non-hidden lists with this in the name
        UserApiContext user = getRegistered(getUserEmail(principal, model), model);
        ArrayList<String> listNames = lists.findList(name);
        ArrayList<ListApiContext> lists = new ArrayList();
        for(String listName: listNames) {
            ListApiContext foundList = listFactory.getList(listName);
            foundList.setViewer(user);
            if (foundList.getViewerCanSubscribe()) {
                lists.add(foundList);
            }
        }

        model.addAttribute("found", lists);
        return "find";
    }

    @RequestMapping(value = "/list/{listName}/subscribers", method = RequestMethod.GET)
    public String viewSubscribers(Principal principal, Model model,
                           @PathVariable("listName") String listName,
                           @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset
    ) throws UnknownUser, UnknownList {

        getRegistered(getUserEmail(principal, model), model);
        ListApiContext list = listFactory.getList(listName);

        model.addAttribute("list", list);

        List<UserApiContext> admins = list.getAdmins();
        model.addAttribute("admins", admins);

        List<UserApiContext> pending = list.getPending();
        model.addAttribute("pendingSubscribers", pending);

        List<UserApiContext> subscribers = list.getSubscribers();
        model.addAttribute("subscribers", subscribers);

        List<UserApiContext> blcoked = list.getBlocked();
        model.addAttribute("blocked", blcoked);

        return "subscribers";
    }

    @RequestMapping(value = "/list/{listName}/unsubscribe", method = RequestMethod.GET)
    public RedirectView requestRemoveSubscription(Principal principal, Model model,
                                            @PathVariable("listName") String listName)
            throws UnknownUser, UnknownList {

        UserApiContext user = getRegistered(getUserEmail(principal, model), model);
        ListApiContext list = listFactory.getList(listName);

        ListSubscription subscriberState = list.getSubscriberState(user);
        if (subscriberState != ListSubscription.BLOCKED) {
            lists.updateListAccess(list.listId, user.userId, ListSubscription.FORGET);
        }

        return new RedirectView("/profile");
    }

    @RequestMapping(value = "/list/{listName}/subscribe", method = RequestMethod.GET)
    public RedirectView requestSubscription(Principal principal, Model model,
                                  @PathVariable("listName") String listName)
            throws UnknownUser, UnknownList {
        UserApiContext user = getRegistered(getUserEmail(principal, model), model);
        ListApiContext list = listFactory.getList(listName);

        if (list.canModify(user)) {
            // we are an admin, subscribe ourself
            lists.updateListAccess(list.listId, user.userId, ListSubscription.SUBSCRIBED);
        } else {
            // if we are not blocked or subscribed, add to pending
            ListSubscription subscriberState = list.getSubscriberState(user);
            if (subscriberState == ListSubscription.UNKNOWN) {
                lists.updateListAccess(list.listId, user.userId, ListSubscription.REQUESTED);
            }
        }

        return new RedirectView("/profile");
    }

    @RequestMapping(value = "/list/{listName}/update", method = RequestMethod.POST)
    public RedirectView updateList(Principal principal, Model model,
                                   @PathVariable("listName") String listName,
                                   @ModelAttribute ListUpdateRequest update
    ) throws UnknownUser, UnknownList, NameExists {
        UserApiContext user = getRegistered(getUserEmail(principal, model), model);
        String newname = Sanitizer.listName(update.getName());

        ListApiContext list = listFactory.getList(listName);

        list.setHidden(user, update.getHidden());
        // hidden lists arent public
        if (update.getHidden()) {
            list.setPublic(user, false);
        } else {
            if (user.admin) { // only let admin users set the public state
                list.setPublic(user, update.getPublic());
            }
        }

        list.setName(user, update.getName());
        return new RedirectView("/list/" + newname);
    }

    @RequestMapping(value = "/list/{listName}/add", method = RequestMethod.POST)
    public RedirectView putCmdrToList(Principal principal, Model model,
                                      @PathVariable("listName") String listName,
                                      @ModelAttribute ListEntryRequest entry
    ) throws UnknownUser, UnknownList, NameExists {
        UserApiContext user = getRegistered(getUserEmail(principal, model), model);
        String cmdr = Sanitizer.cmdrName(entry.getCmdr());
        String hostility = Sanitizer.hostility(entry.getHostility());

        ListApiContext list = listFactory.getList(listName);
        list.addCmdr(user, cmdr, hostility);

        return new RedirectView("/list/" + listName);
    }

    @RequestMapping(value = "/list/{listName}/remove", method = RequestMethod.POST)
    public RedirectView delCmdrToList(Principal principal, Model model,
                                      @PathVariable("listName") String listName,
                                      @ModelAttribute ListEntryRequest entry
    ) throws UnknownUser, UnknownList, NameExists {
        UserApiContext user = getRegistered(getUserEmail(principal, model), model);
        String cmdr = Sanitizer.cmdrName(entry.getCmdr());
        ListApiContext list = listFactory.getList(listName);
        list.delCmdr(user, cmdr);

        return new RedirectView("/list/" + listName);
    }


    @RequestMapping(value = "/confirm-register", method = RequestMethod.GET)
    public RedirectView confirmRegister(Principal principal, Model model) {
        String email = getUserEmail(principal, model);
        users.makeProfile(email);
        return new RedirectView("/");
    }

    @RequestMapping(value = "/webcheck", method = RequestMethod.GET)
    public String webCheck(Principal principal, Model model,
                           @RequestParam(value = "public", required = false, defaultValue = "false") boolean checkPublic,
                           @RequestParam(value = "cmdr", required = false) String cmdr) throws UnknownUser, UnknownList {

        String email = getUserEmail(principal, model);
        UserApiContext user = null;
        if (email != null) {
            user = getRegistered(email, model);
        }

        if (cmdr != null && cmdr.length() > 0) {
            cmdr = Sanitizer.cmdrName(cmdr);

            ArrayList<ListApiContext> checkLists = new ArrayList<>();

            if (user != null) {
                // check against a user's lists
                ArrayList<ListApiContext> ownedLists = user.getOwnedLists();
                checkLists.addAll(ownedLists);
            }

            if (checkPublic) {
                ArrayList<String> publicLists = lists.publicLists();
                for (String listName : publicLists) {
                    long listId = lists.getList(listName);
                    long listOwner = lists.getOwner(listId);
                    ListApiContext listContext = listFactory.getList(listName);
                    checkLists.add(listContext);
                }
            }

            String state = RESPONSE_STATUS_UNKNOWN;

            model.addAttribute("checkedCmdr", cmdr);
            for (ListApiContext checkList : checkLists) {
                String hostileState = checkList.getHostileState(cmdr);
                if (!hostileState.equals(RESPONSE_STATUS_UNKNOWN)) {
                    state = hostileState;
                    break;
                }
            }
            model.addAttribute("alertClass", "alert alert-info");
            if (state.equals(RESPONSE_STATUS_FRIENDLY)) {
                model.addAttribute("alertClass", "alert alert-success");
            } else {
                if (state.equals(RESPONSE_STATUS_HOSTILE)) {
                    model.addAttribute("alertClass", "alert alert-danger");
                }
            }
            model.addAttribute("hostileState", state);
            model.addAttribute("foundCmdr", true);
        }

        return "webcheck";
    }

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String profile(Principal principal, Model model) throws UnknownUser, UnknownList {
        String email = getUserEmail(principal, model);
        if (checkRegistered(email, model)) {
            UserApiContext user = getUserContext(email);
            model.addAttribute("apikey", user.getApiKey());

            ArrayList<ListApiContext> active = listFactory.getLists(users.getActiveSubscriptions(user.userId));
            model.addAttribute("active", active);


            ArrayList<ListApiContext> subscribed = listFactory.getLists(lists.getUserAccessList(user.userId, ListSubscription.SUBSCRIBED));
            model.addAttribute("subscribed", subscribed);

            ArrayList<ListApiContext> pending = listFactory.getLists(lists.getUserAccessList(user.userId, ListSubscription.REQUESTED));
            model.addAttribute("pending", pending);

            return "profile";
        }
        return REGISTRATION_TEMPLATE;
    }
}
