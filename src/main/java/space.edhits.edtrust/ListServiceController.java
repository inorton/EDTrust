package space.edhits.edtrust;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for list check/submit API
 */
@RestController
@RequestMapping(value = Constants.TRUST_SERVICE_BASE)
public class ListServiceController {

    final UserApiContextFactory contextFactory;

    public ListServiceController(@Autowired UserApiContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public ResponseEntity<ContactResponse> check(@RequestHeader(value = "apikey", required = true) String apikey,
                                                 @RequestBody ContactRequest contactRequest) throws UnknownUser, UnknownList {
        UserApiContext user = contextFactory.getUser(apikey);
        ContactResponse check = user.check(contactRequest.getCmdr());
        return ResponseEntity.ok(check);
    }


}
