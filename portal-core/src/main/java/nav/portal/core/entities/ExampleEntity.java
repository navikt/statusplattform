package nav.portal.core.entities;

import java.util.UUID;

public class ExampleEntity {



    private UUID uid;
    private Integer code;


    public UUID getUid() {
        return uid;
    }

    public ExampleEntity setUid(UUID uuid) {
        this.uid = uid;
        return this;
    }
    public Integer getCode() {
        return code;
    }

    public ExampleEntity setCode(Integer code) {
        this.code = code;
        return this;
    }

}
