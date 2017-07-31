package space.edhits.edtrust.data;

import space.edhits.edtrust.UnknownUser;
import java.io.Closeable;

/**
 * Get basic details about a user/profile and their lists
 */
public interface UserProfileData extends Closeable {

    String getApiKey(long userId);

    long getId(String email) throws UnknownUser;

    long getIdFromKey(String apikey) throws UnknownUser;

    Boolean getAdminStatus(long userId);

    String getEmail(long userId) throws UnknownUser;

    long makeProfile(String username);

    void updateProfileAdmin(long userId, boolean adminStatus);
}