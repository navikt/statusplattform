package nav.statusplattform.core.util;

public enum Status {
    OK ("OK"),
    ISSUE ("ISSUE"),
    DOWN ("DOWN");


    private final String name;

    private Status(String s) {
        name = s;
    }

    public String toString() {
        return this.name;
    }


}
