package space.edhits.edtrust;

/**
 * Given an API Key, identify the user and provide a means to check/append their lists
 */
public class UserApiContext {

    private long defaultList;
    private String cmdr;

    public void load(String cmdr) {
        this.setCmdr(cmdr);
    }

    public ContactResponse check(String cmdr) {
        ContactResponse contactResponse = new ContactResponse();
        contactResponse.setCmdr(cmdr);

        return contactResponse;
    }

    public long getDefaultList() {
        return defaultList;
    }

    public void setDefaultList(long defaultList) {
        this.defaultList = defaultList;
    }

    public String getCmdr() {
        return cmdr;
    }

    public void setCmdr(String cmdr) {
        this.cmdr = cmdr;
    }
}
