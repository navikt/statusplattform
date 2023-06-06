package nav.portal.core.entities;

import nav.portal.core.enums.RuleType;

import java.util.Objects;
import java.util.UUID;

public class OpeningHoursRuleEntity implements OpeningHoursRule {

    private UUID id;
    private String name;
    private String rule;

    public OpeningHoursRuleEntity() {
    }

    public OpeningHoursRuleEntity(UUID id, String name, String rule) {
        this.id = id;
        this.name = name;
        this.rule = rule;
    }

    @Override
    public RuleType getRuleType() {
        return RuleType.RULE;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public OpeningHoursRuleEntity setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public OpeningHoursRuleEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getRule() {
        return rule;
    }

    public OpeningHoursRuleEntity setRule(String rule) {
        this.rule = rule;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OpeningHoursRuleEntity)) return false;
        OpeningHoursRuleEntity that = (OpeningHoursRuleEntity) o;
        return getId().equals(that.getId()) && getName().equals(that.getName()) && getRule().equals(that.getRule());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getRule());
    }
}



