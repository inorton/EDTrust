package space.edhits.edtrust;

/**
 * Sanitize Names
 */
public class Sanitizer {

    public static String listName(String input) {
        return input.replaceAll("[^A-Za-z0-9.-]", "");
    }
}
