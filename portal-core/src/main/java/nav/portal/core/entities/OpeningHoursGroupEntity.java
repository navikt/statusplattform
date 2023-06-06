package nav.portal.core.entities;

import java.util.List;
import java.util.UUID;

public class OpeningHoursGroupEntity {


    private UUID id;
    private String name;
    private List<UUID> rules;

    public OpeningHoursGroupEntity(UUID id, String name, List<UUID> rules) {
        this.id = id;
        this.name = name;
        this.rules = rules;
    }

    public UUID getId() {
        return id;
    }

    public OpeningHoursGroupEntity setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public OpeningHoursGroupEntity setName(String name) {
        this.name = name;
        return this;
    }

    public List<UUID> getRules() {
        return rules;
    }

    public OpeningHoursGroupEntity setRules(List<UUID> rules) {
        this.rules = rules;
        return this;
    }


}
