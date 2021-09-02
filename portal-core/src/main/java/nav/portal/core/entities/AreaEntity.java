package nav.portal.core.entities;

import java.util.List;

public class AreaEntity {

    private String id;
    private String name;
    private String beskrivelse;
    private String ikon;
    private Integer rangering;
    private List<String> servisesIds;

    public AreaEntity() {
    }

    public AreaEntity(String id, String name, String beskrivelse, String ikon, Integer rangering, List<String> servisesIds) {
        this.id = id;
        this.name = name;
        this.beskrivelse = beskrivelse;
        this.ikon = ikon;
        this.rangering = rangering;
        this.servisesIds = servisesIds;
    }

    public List<String> getServisesIds() {
        return servisesIds;
    }

    public void setServisesIds(List<String> servisesIds) {
        this.servisesIds = servisesIds;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIkon() {
        return ikon;
    }

    public void setIkon(String ikon) {
        this.ikon = ikon;
    }

    public Integer getRangering() {
        return rangering;
    }

    public void setRangering(Integer rangering) {
        this.rangering = rangering;
    }
}
