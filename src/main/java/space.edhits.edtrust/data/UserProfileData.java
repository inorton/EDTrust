package space.edhits.edtrust.data;

import java.util.ArrayList;

/**
 * Get basic details about a user/profile and their lists
 */
public interface UserProfileData {

    String getApiKey(long userId);

    Boolean getAdminStatus(long userId);

    long makeProfile(String username);

    void updateProfileAdmin(long userId, boolean adminStatus);
}
