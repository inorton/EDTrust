package space.edhits.edtrust.data;

/**
 * Created by inb on 27/09/2017.
 */
public class CompletionResponse {
    boolean success;

    public boolean isSuccess() {
        return success;
    }

    public CompletionResponse(boolean success) {
        this.success = success;
    }
}
