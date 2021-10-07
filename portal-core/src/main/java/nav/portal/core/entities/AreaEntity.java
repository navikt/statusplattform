package nav.portal.core.entities;


import java.util.UUID;

public class AreaEntity {

    private UUID id;
    private String name;
    private String beskrivelse;
    private String ikon;


    public AreaEntity() {
    }

    public AreaEntity(UUID id, String name, String beskrivelse, String ikon) {
        this.id = id;
        this.name = name;
        this.beskrivelse = beskrivelse;
        this.ikon = ikon;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public void setBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getIkon() {
        return ikon;
    }

    public void setIkon(String ikon) {
        this.ikon = ikon;
    }

}
