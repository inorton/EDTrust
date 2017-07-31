package space.edhits.edtrust;

import com.sun.org.apache.xpath.internal.operations.Mod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.view.RedirectView;
import space.edhits.edtrust.data.CmdrList;
import space.edhits.edtrust.data.UserProfileData;

import java.security.Principal;
import java.util.LinkedHashMap;

/**
 * Created by inb on 31/07/2017.
 */
@Controller
public class WebUiController {

    @Autowired
    protected UserApiContextFactory userFactory;

    @Autowired
    protected UserProfileData users;

    String getUser(Principal principal, Model model) {
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

    String ensureRegistered(String email, Model model, String template) {
        if (email != null) {
            // user is authenticated, are they registerd?
            try {
                UserApiContext user = userFactory.getUserByEmail(email);
                model.addAttribute("registered", true);
                model.addAttribute("ownedLists", user.getOwnedLists());

            } catch (UnknownUser unknownUser) {
                // the user is not registered, send them to the registration page
                return "register";
            }
        }
        return template;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Principal principal, Model model) {
        return ensureRegistered(getUser(principal, model), model, "index");
    }

    @RequestMapping(value = "/about", method = RequestMethod.GET)
    public String about(Principal principal, Model model) {
        getUser(principal, model);
        return "about";
    }

    @RequestMapping(value = "/lists", method = RequestMethod.GET)
    public String lists(Principal principal, Model model) {
        return ensureRegistered(getUser(principal, model), model, "lists");
    }

    @RequestMapping(value = "/confirm-register", method = RequestMethod.GET)
    public RedirectView confirmRegister(Principal principal, Model model) {
        String email = getUser(principal, model);
        users.makeProfile(email);
        return new RedirectView("/");
    }

}
