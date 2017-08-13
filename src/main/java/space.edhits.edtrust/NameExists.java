package space.edhits.edtrust;


public class NameExists extends Exception {
    public String getName() {
        return name;
    }

    private final String name;

    public NameExists(String name) {
        super();
        this.name = name;
    }
}
