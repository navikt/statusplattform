package nav.portal.core.entities;

import nav.portal.core.enums.RuleType;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class OpeningHoursGroup implements OpeningHoursRule {
    private UUID id;
    private String name;
    private List<OpeningHoursRule> rules;

    public OpeningHoursGroup() {
    }

    public OpeningHoursGroup(UUID id, String name, List<OpeningHoursRule> rules) {
        this.id = id;
        this.name = name;
        this.rules = rules;
    }

    @Override
    public RuleType getRuleType() {
        return RuleType.RULE_GROUP;
    }
    @Override
    public UUID getId() {
        return id;
    }

    public OpeningHoursGroup setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public OpeningHoursGroup setName(String name) {
        this.name = name;
        return this;
    }

    public List<OpeningHoursRule> getRules() {
        return rules;
    }

    public OpeningHoursGroup setRules(List<OpeningHoursRule> rules) {
        this.rules = rules;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpeningHoursGroup that = (OpeningHoursGroup) o;
        return id.equals(that.id) && name.equals(that.name) && rules.equals(that.rules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, rules);
    }
}
