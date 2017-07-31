package space.edhits.edtrust;

import java.security.SecureRandom;
import java.math.BigInteger;

public class TestHelpers {
    private static final SecureRandom random = new SecureRandom();

    public static final String TEST_USER_EMAIL = "test@edhits.space";
    public static final String TEST_USER_LIST = "space-baddies";
    public static final String TEST_BAD_CMDR = "Bad Dude";

    public static String randomString() {
        return new BigInteger(130, random).toString(32);
    }

    public static String randomEmail() {
        return String.format("u%s@d%s.com", randomString(), randomString());
    }
}
