package nav.portal.core.entities;

import nav.portal.core.enums.RuleType;

import java.util.UUID;

public interface OpeningHoursRule {

     String getName();
     RuleType getRuleType();
     UUID getId();

}
