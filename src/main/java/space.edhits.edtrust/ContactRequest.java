package space.edhits.edtrust;

/**
 * Check the listed status of a commander against your trusted lists
 */
public class ContactRequest {
    private String cmdr;

    public String getCmdr() {
        return cmdr;
    }

    public void setCmdr(String cmdr) {
        this.cmdr = cmdr;
    }
}
