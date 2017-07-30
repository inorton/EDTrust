package space.edhits.edtrust;

/**
 * REST API response for checking a commander name
 */
public class ContactResponse {

    public ContactResponse() {
        this.status = Constants.RESPONSE_STATUS_UNKNOWN;
    }

    private String cmdr;
    private String status;

    public String getCmdr() {
        return cmdr;
    }

    public void setCmdr(String cmdr) {
        this.cmdr = cmdr;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
