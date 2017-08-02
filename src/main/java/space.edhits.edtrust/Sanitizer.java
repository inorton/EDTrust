package space.edhits.edtrust;

/**
 * Sanitize Names
 */
public class Sanitizer {

    public static final int MAX_CMDRNAME = 16;
    public static final int MAX_LISTNAME = 32;

    public static String listName(String input) {
        String name = input.replaceAll("[^A-Za-z0-9.-]", "");
        if (name.length() > MAX_LISTNAME) {
            throw new RuntimeException("name too long");
        }
        return name;
    }

    public static String cmdrName(String cmdr) {
        if (cmdr.contains("'")) {
            throw  new RuntimeException("cmdr names should not contain single quotes!");
        }
        cmdr = cmdr.replaceAll("^(?i)CMDR ", "");
        if (cmdr.length() > MAX_CMDRNAME) {
            throw new RuntimeException("name too long");
        }

        return cmdr;
    }

    public static String hostility(String state) {
        if (state.toLowerCase().equals(Constants.RESPONSE_STATUS_FRIENDLY)) {
            return Constants.RESPONSE_STATUS_FRIENDLY;
        }

        return Constants.RESPONSE_STATUS_HOSTILE;
    }
}
