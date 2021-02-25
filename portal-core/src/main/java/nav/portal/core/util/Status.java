package nav.portal.core.util;

import java.util.HashMap;
import java.util.Random;

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