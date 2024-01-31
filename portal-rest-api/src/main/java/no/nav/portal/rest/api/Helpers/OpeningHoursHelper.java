package no.nav.portal.rest.api.Helpers;

import nav.portal.core.entities.*;
import nav.portal.core.openingHours.OpeningHoursDailyMap;
import nav.portal.core.openingHours.OpeningHoursParser;
import nav.portal.core.openingHours.OpeningHoursValidator;
import nav.portal.core.repositories.OpeningHoursRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.*;
import org.fluentjdbc.DbContext;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class OpeningHoursHelper {
    private OpeningHoursRepository openingHoursRepository;

    public OpeningHoursHelper(DbContext dbContext) {
        this.openingHoursRepository = new OpeningHoursRepository(dbContext);
    }


    public boolean isValidRule(OHRuleDto oHRuleDto) {
        return OpeningHoursValidator.isAValidRule(oHRuleDto.getRule());
    }

    public OHRuleDto saveNewRule(OHRuleDto oHRuleDto) {
       UUID id = openingHoursRepository.save(EntityDtoMappers.toOpeningHoursRuleEntity(oHRuleDto));
       oHRuleDto.setId(id);
       return oHRuleDto;
    }

    public void updateRule(OHRuleDto oHRuleDto) {
        openingHoursRepository.update(EntityDtoMappers.toOpeningHoursRuleEntity(oHRuleDto));
        OpeningHoursDailyMap.populateMap(openingHoursRepository);
    }

    public void deleteRule(UUID rule_id) {
        openingHoursRepository.deleteOpeningHoursRule(rule_id);
    }

    public OHRuleDto getRule(UUID rule_id) {
        OpeningHoursRuleEntity oHRule = (openingHoursRepository.retriveRule(rule_id)).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Not found: Rule with id "+ rule_id));
        return EntityDtoMappers.toOpeningHoursRuleDto(oHRule);
    }

    public OHGroupThinDto saveGroup(OHGroupThinDto oHGroupThinDto) {
        UUID id = openingHoursRepository.saveGroup(EntityDtoMappers.toOpeningHoursGroup(oHGroupThinDto));
        oHGroupThinDto.setId(id);
        OpeningHoursDailyMap.populateMap(openingHoursRepository);
        return oHGroupThinDto;
    }

    public void deleteGroup(UUID group_id) {
        openingHoursRepository.deleteOpeninghourGroup(group_id);
    }

    public OHGroupDto getGroup(UUID group_id) {
        return EntityDtoMappers.toOpeningHoursGroupDto(openingHoursRepository.retrieveOneGroup(group_id)
                .orElseThrow(() -> new IllegalArgumentException("Not found: Group with id "+ group_id)));
    }

    public void updateGroup(OHGroupThinDto oHGroupThinDto) {
        openingHoursRepository.updateGroup(EntityDtoMappers.toOpeningHoursGroup(oHGroupThinDto));
        OpeningHoursDailyMap.populateMap(openingHoursRepository);
    }

//    public static OpeningHoursGroup getFullGroup(OHGroupThinDto oHGroupThinDto) {
//        List<OpeningHoursRule>rules = new ArrayList<>();
//        oHGroupThinDto.getRules().forEach(rule->{
//            openingHoursRepository.re
//            rules.add(openingHoursRepository.retriveRule(rule).get());
//        });
//        return new OpeningHoursGroup(oHGroupThinDto.getId(),oHGroupThinDto.getName(), rules);
//    }


    public OHGroupDto getOHGroupForService(UUID service_id){
        return openingHoursRepository.getOHGroupForService(service_id).map(EntityDtoMappers::toOpeningHoursGroupDto)
                .orElseThrow(() -> new IllegalArgumentException("Not found: The Group's Service with id "+ service_id));
    }

    public List<OHGroupDto> getAllGroups() {
        return openingHoursRepository.getAllGroups()
                .stream()
                .map(EntityDtoMappers::toOpeningHoursGroupDto)
                .collect(Collectors.toList());
    }

    public String getOpeningHoursForServiceOnDate(UUID service_id, String date) {
        Optional<OpeningHoursGroup> group = openingHoursRepository.getOHGroupForService(service_id);
        return getOpeningHoursForGroupOnDate(group.get(),date);
    }

    public String getOpeningHoursForGroupOnDate(UUID groupId, String date) {
        Optional<OpeningHoursGroup> group = openingHoursRepository.retrieveOneGroup(groupId);
        return getOpeningHoursForGroupOnDate(group.get(),date);
    }

    private String getOpeningHoursForGroupOnDate(OpeningHoursGroup group, String date){
        List<Integer> dateParts = Arrays.stream(date.split("\\."))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        LocalDate ld =  LocalDate.of(dateParts.get(2),dateParts.get(1),dateParts.get(0));
        return OpeningHoursParser.getOpeninghours(ld,group);

    }

    public void setOpeningHoursToService(UUID groupId, UUID serviceId) {
        openingHoursRepository.setOpeningHoursToService(groupId,serviceId);
        OpeningHoursDailyMap.populateMap(openingHoursRepository);

    }
}
