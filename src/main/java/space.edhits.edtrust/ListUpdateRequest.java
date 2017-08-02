package space.edhits.edtrust;

public class ListUpdateRequest {
    private boolean isPublic;
    private String name;

    public boolean getPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
