package space.edhits.edtrust;

public class ListUpdateRequest {
    private boolean isPublic;
    private String name;
    private boolean isHidden;

    public boolean getHidden() { return isHidden; }

    public void setHidden(boolean hidden) { isHidden = hidden; }

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
