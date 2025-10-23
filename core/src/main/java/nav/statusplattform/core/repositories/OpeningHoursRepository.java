package nav.statusplattform.core.repositories;

import nav.statusplattform.core.entities.*;
import nav.statusplattform.core.enums.RuleType;
import nav.statusplattform.core.exceptionHandling.ExceptionUtil;
import nav.statusplattform.core.openingHours.OpeningHoursParser;
import nav.statusplattform.core.openingHours.TimeSpan;
import org.actioncontroller.HttpRequestException;
import org.fluentjdbc.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class OpeningHoursRepository {

    private final DbContextTable ohRuleTable;
    private final DbContextTable ohGroupTable;
    private final DbContextTable serviceOHgroupTable;

    public OpeningHoursRepository(DbContext dbContext) {
        ohRuleTable = dbContext.table("oh_rule");
        ohGroupTable = dbContext.table("oh_group");
        serviceOHgroupTable = dbContext.table("service_oh_group");
    }

    public UUID save(OpeningHoursRuleEntity entity) {
        //Sjekk på navn
        if (ohRuleTable.where("name", entity.getName()).getCount() > 0) {
            throw new HttpRequestException("Åpningstid med navn: " + entity.getName() + " finnes allerede");
        }
        DatabaseSaveResult<UUID> result = ohRuleTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("name", entity.getName())
                .setField("rule", entity.getRule())
                .execute();
        return result.getId();
    }

    public void update(OpeningHoursRuleEntity entity) {
        ohRuleTable.where("id", entity.getId())
                .update()
                .setField("name", entity.getName())
                .setField("rule", entity.getRule())
                .execute();
    }

    public boolean deleteOpeningHoursRule(UUID openingHoursId) {
        if (ohRuleTable.where("id", openingHoursId).singleObject(OpeningHoursRepository::toOpeningRule).isEmpty()) {
            return false;
        }
        deleteGroupOrRuleFromAllGroups(openingHoursId);
        ohRuleTable.where("id", openingHoursId).executeDelete();
        return true;
    }

    public boolean deleteOpeninghourGroup(UUID groupId) {
        if (ohGroupTable.where("id", groupId).singleObject(OpeningHoursRepository::toOpeningHoursGroupEntity).isEmpty()) {
            return false;
        }
        deleteGroupOrRuleFromAllGroups(groupId);
        serviceOHgroupTable.where("group_id", groupId).executeDelete();
        ohGroupTable.where("id", groupId).executeDelete();
        return true;
    }

    private void deleteGroupOrRuleFromAllGroups(UUID id) {
        List<OpeningHoursGroupEntity> allGroups = ohGroupTable.orderedBy("name")
                .stream(OpeningHoursRepository::toOpeningHoursGroupEntity)
                .collect(Collectors.toList());
        allGroups.forEach(group -> {
                    group.getRules().remove(id);
                    ohGroupTable
                            .newSaveBuilderWithUUID("id", group.getId())
                            .setField("name", group.getName())
                            .setField("rule_group_ids", group.getRules()
                                    .stream().map(UUID::toString)
                                    .collect(Collectors.toList()))
                            .execute();
                }
        );


    }

    public UUID saveGroup(OpeningHoursGroupEntity group) {
        //Sjekk på navn
        if (ohGroupTable.where("name", group.getName()).getCount() > 0) {
            throw new HttpRequestException("Åpningstidsgruppe med navn: " + group.getName() + " finnes allerede");
        }
        if (containsCircularGroupDependency(group)) {
            throw new HttpRequestException("Åpningsgruppe inneholder sirkuler avhengighet");
        }
        List<String> ids = group.getRules().stream().map(String::valueOf).collect(Collectors.toList());
        DatabaseSaveResult<UUID> result = ohGroupTable
                .newSaveBuilderWithUUID("id", group.getId())
                .setField("name", group.getName())
                .setField("rule_group_ids", ids.isEmpty() ? null : ids)
                .execute();


        return result.getId();
    }

    public void updateGroup(OpeningHoursGroupEntity group) {
        if (containsCircularGroupDependency(group)) {
            throw new HttpRequestException("Åpningsgruppe inneholder sirkuler avhengighet");
        }
        ohGroupTable.where("id", group.getId())
                .update()
                .setField("name", group.getName())
                .setField("rule_group_ids", group.getRules().stream().map(String::valueOf).collect(Collectors.toList()))
                .execute();
    }

    private boolean containsCircularGroupDependency(OpeningHoursGroupEntity group) {
        UUID groupId = group.getId();
        List<OpeningHoursGroup> subGroups = getAllSubGroups(group);
        List<UUID> subGroupsIds = subGroups.stream().map(OpeningHoursGroup::getId).collect(Collectors.toList());
        if (subGroupsIds.contains(groupId)) {
            return true;
        }
        for (OpeningHoursGroup subGroup : subGroups) {
            if (containsCircularGroupDependency(subGroup)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsCircularGroupDependency(OpeningHoursGroup group) {
        UUID groupId = group.getId();
        List<OpeningHoursGroup> subGroups = getAllSubGroups(group);
        List<UUID> subGroupsIds = subGroups.stream().map(OpeningHoursGroup::getId).collect(Collectors.toList());
        if (subGroupsIds.contains(groupId)) {
            return true;
        }
        for (OpeningHoursGroup subGroup : subGroups) {
            if (containsCircularGroupDependency(subGroup)) {
                return true;
            }
        }
        return false;
    }


    private ArrayList<OpeningHoursGroup> getAllSubGroups(OpeningHoursGroupEntity group) {
        ArrayList<OpeningHoursGroup> subgroupsDirectlyUnderGroup = (ArrayList<OpeningHoursGroup>) group.getRules()
                .stream()
                .map(this::retriveGroupOrRule)
                .filter(rule -> rule.isPresent() && rule.get().getRuleType().equals(RuleType.GROUP))
                .map(r -> (OpeningHoursGroup) r.get())
                .collect(Collectors.toList());
        ArrayList<OpeningHoursGroup> result = new ArrayList<>(subgroupsDirectlyUnderGroup);

        subgroupsDirectlyUnderGroup.forEach(g -> result.addAll(getAllSubGroups(g)));
        return result;
    }

    private ArrayList<OpeningHoursGroup> getAllSubGroups(OpeningHoursGroup group) {
        ArrayList<OpeningHoursGroup> subgroupsDirectlyUnderGroup = (ArrayList<OpeningHoursGroup>) group.getRules()
                .stream()
                .filter(rule -> rule.getRuleType().equals(RuleType.GROUP))
                .map(r -> (OpeningHoursGroup) r)
                .collect(Collectors.toList());
        ArrayList<OpeningHoursGroup> result = new ArrayList<>(subgroupsDirectlyUnderGroup);

        subgroupsDirectlyUnderGroup.forEach(g -> result.addAll(getAllSubGroups(g)));
        return result;
    }

    public Optional<OpeningHoursGroup> retrieveOneGroup(UUID group_id) {
        Optional<OpeningHoursGroupEntity> optionalOfGroupEntity = ohGroupTable.where("id", group_id).singleObject(OpeningHoursRepository::toOpeningHoursGroupEntity);
        if (optionalOfGroupEntity.isEmpty()) {
            return Optional.empty();
        }
        OpeningHoursGroupEntity openingHoursGroup = optionalOfGroupEntity.get();

        List<OpeningHoursRule> rules = openingHoursGroup.getRules().stream()
                .map(this::retriveGroupOrRule)
                .filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());

        OpeningHoursGroup result = new OpeningHoursGroup()
                .setId(openingHoursGroup.getId())
                .setName(openingHoursGroup.getName())
                .setRules(rules);

        return Optional.of(result);
    }

    public Optional<OpeningHoursRuleEntity> retriveRule(UUID id) {
        return ohRuleTable.where("id", id).singleObject(OpeningHoursRepository::toOpeningRule);
    }


    private Optional<OpeningHoursRule> retriveGroupOrRule(UUID id) {
        Optional<OpeningHoursRuleEntity> ruleEntity = ohRuleTable.where("id", id).singleObject(OpeningHoursRepository::toOpeningRule);
        if (ruleEntity.isPresent()) {
            return Optional.of(ruleEntity.get());
        }
        return Optional.of(retrieveOneGroup(id).get());
    }

    public List<OpeningHoursRuleEntity> getAllOpeningHoursRules() {
        return ohRuleTable.orderedBy("name").stream(OpeningHoursRepository::toOpeningRule).collect(Collectors.toList());
    }

    public List<OpeningHoursGroup> getAllGroups() {
        return ohGroupTable.orderedBy("name")
                .stream(OpeningHoursRepository::toOpeningHoursGroupEntity)
                .map(this::getGroupFromEntity)
                .collect(Collectors.toList());

    }

    private OpeningHoursGroup getGroupFromEntity(OpeningHoursGroupEntity entity) {
        return new OpeningHoursGroup()
                .setId(entity.getId())
                .setName(entity.getName())
                .setRules(entity.getRules()
                        .stream().map(uuid -> retriveGroupOrRule(uuid)
                                .orElseThrow(() -> new IllegalArgumentException("Not found: opening hours rule with id " + uuid)))
                        .collect(Collectors.toList()));
    }

    public void setOpeningHoursToService(UUID groupId, UUID serviceId) {
        serviceOHgroupTable.where("service_id", serviceId)
                .executeDelete();
        serviceOHgroupTable.insert()
                .setField("service_id", serviceId)
                .setField("group_id", groupId)
                .execute();
    }

    //Set a default group contain open 24/7 all year around
    public void setDefaultOpeningHoursToService(UUID serviceId) {
        serviceOHgroupTable.where("service_id", serviceId)
                .executeDelete();
        ohGroupTable.where("name", "default opening hours")
                .singleObject(row -> row.getUUID("id"))
                .ifPresent(groupId -> serviceOHgroupTable.insert()
                        .setField("service_id", serviceId)
                        .setField("group_id", groupId)
                        .execute());
    }

    public void removeOpeningHoursFromService(UUID serviceId) {
        serviceOHgroupTable.where("service_id", serviceId)
                .executeDelete();
    }

    /*
        Returns a group containing business opening hour rules for a given service
    */
    public Optional<OpeningHoursGroup> getOHGroupForService(UUID service_id) {
        DbContextTableAlias g = ohGroupTable.alias("g");
        DbContextTableAlias s2g = serviceOHgroupTable.alias("s2g");

        Optional<OpeningHoursGroupEntity> entity = s2g.where("service_id", service_id)
                .leftJoin(s2g.column("group_id"), g.column("id"))
                .singleObject(r -> toOpeningHoursGroupEntity(r.table(g)));
        return entity.map(this::getGroupFromEntity);
    }

    public Map<UUID, OpeningHoursGroup> getAllOpeningtimeForAllServicesWithOpeningTime() {
        DbContextTableAlias g = ohGroupTable.alias("g");
        DbContextTableAlias s2g = serviceOHgroupTable.alias("s2g");
        Map<UUID, OpeningHoursGroup> result = new HashMap<>();

        s2g.leftJoin(s2g.column("group_id"), g.column("id")).
                forEach(row -> {
                    UUID serviceId = row.getUUID("service_id");
                    OpeningHoursGroupEntity groupEntity = toOpeningHoursGroupEntity(row.table(g));
                    OpeningHoursGroup openingHoursGroup = getGroupFromEntity(groupEntity);
                    result.put(serviceId, openingHoursGroup);
                });

        return result;

    }

    /*
     *The code returns a map containing each date in the timespan and its corresponding opening times.
     *Firstly, get the group of rules for a given service. After that, the rule applicable for each day
     *returns its opening hours in String format to determine the opening hours.
     *The string format containing opening start and end times is converted to local time
     *format and stored in an opening hours object. The end of the result is added alongside its
     *corresponding date in a map, returning a map containing the date as keys and corresponding
     * opening start and end times as values.*/
    public Map<LocalDate, OpeningHours> getMapContainingOpeningHoursForTimeSpan(UUID serviceId, TimeSpan timeSpan) {
        Map<LocalDate, OpeningHours> openingHoursMap = new HashMap<>();

        Optional<OpeningHoursGroup> group = getOHGroupForService(serviceId);
        OpeningHoursGroup oHGroup = group.orElseThrow();
        LocalDate startDate = timeSpan.from().toLocalDate();
        LocalDate endDate = timeSpan.to().toLocalDate();
        while (startDate.isBefore(endDate)) {
            String oHTimeString = OpeningHoursParser.getOpeninghours(startDate, oHGroup); //opening hours in String format
            OpeningHours openingHours = new OpeningHours(
                    OpeningHoursParser.getOpeningTime(oHTimeString),
                    OpeningHoursParser.getClosingTime(oHTimeString));
            openingHoursMap.put(startDate, openingHours);

            startDate = startDate.plusDays(1);
        }
        return openingHoursMap;
    }

    static OpeningHoursRuleEntity toOpeningRule(DatabaseRow row) {
        try {
            return new OpeningHoursRuleEntity(row.getUUID("id"),
                    row.getString("name"),
                    row.getString("rule"));
        } catch (SQLException e) {
            throw ExceptionUtil.soften(e);
        }
    }

    static OpeningHoursGroupEntity toOpeningHoursGroupEntity(DatabaseRow row) {
        try {
            List<UUID> ruleIDs = row.getStringList("rule_group_ids") != null ?
                    row.getStringList("rule_group_ids").stream()
                            .map(UUID::fromString)
                            .collect(Collectors.toList()) : Collections.EMPTY_LIST;
            return new OpeningHoursGroupEntity(row.getUUID("id"),
                    row.getString("name"),
                    ruleIDs);
        } catch (SQLException e) {
            throw ExceptionUtil.soften(e);
        }
    }

}
