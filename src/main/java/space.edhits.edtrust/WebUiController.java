package space.edhits.edtrust;

import com.sun.org.apache.xpath.internal.operations.Mod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import space.edhits.edtrust.data.UserProfileData;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;

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

    String getUserEmail(Principal principal, Model model) {
        if (principal != null) {
            LinkedHashMap details = (LinkedHashMap) ((OAuth2Authentication) principal).getUserAuthentication().getDetails();
            if (details != null) {
                model.addAttribute("userFullName", (String)details.getOrDefault("name", "no name"));
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

    UserApiContext getRegistered(String email, Model model) throws UnknownUser {
        if (email == null) throw new UnknownUser();

        UserApiContext user = getUserContext(email);
        model.addAttribute("registered", true);
        model.addAttribute("ownedLists", user.getOwnedLists());
        return user;
    }

    boolean checkRegistered(String email, Model model) {
        try {
            getRegistered(email, model);
            return true;
        } catch (UnknownUser ignored) {
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
        getUserEmail(principal, model);
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
                              @RequestParam(value = "newListName", required = true) String newName) throws UnknownUser {
        UserApiContext user = getRegistered(getUserEmail(principal, model), model);
        newName = Sanitizer.listName(newName);
        try {
            user.lists.createList(user.userId, newName);
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
        user.lists.deleteList(user.lists.getList(listName));
        return new RedirectView("/lists");
    }

    @RequestMapping(value = "/confirm-register", method = RequestMethod.GET)
    public RedirectView confirmRegister(Principal principal, Model model) {
        String email = getUserEmail(principal, model);
        users.makeProfile(email);
        return new RedirectView("/");
    }

}
