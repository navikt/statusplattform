package nav.statusplattform.core.entities;

import nav.statusplattform.core.enums.RuleType;

import java.util.UUID;

public interface OpeningHoursRule {

     String getName();
     RuleType getRuleType();
     UUID getId();

}
