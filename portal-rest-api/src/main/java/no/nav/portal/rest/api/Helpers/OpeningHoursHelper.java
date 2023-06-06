package no.nav.portal.rest.api.Helpers;

import nav.portal.core.entities.OpeningHoursGroup;
import nav.portal.core.entities.OpeningHoursRuleEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.openingHours.OpeningHoursValidator;
import nav.portal.core.repositories.OpeningHoursRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.*;
import org.actioncontroller.HttpNotFoundException;
import org.fluentjdbc.DbContext;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class OpeningHoursHelper {
    private OpeningHoursRepository openingHoursRepository;
   // private OpeningHoursLogic openingHoursLogic;

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
    }

    public void deleteRule(UUID rule_id) {
        openingHoursRepository.deleteOpeninghours(rule_id);
    }

    public OHRuleDto getRule(UUID rule_id) {
        OpeningHoursRuleEntity oHRule = (openingHoursRepository.retriveRule(rule_id)).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Not found: Rule with id "+ rule_id));
        return EntityDtoMappers.toOpeningHoursRuleDto(oHRule);
    }

    public OHGroupThinDto saveGroup(OHGroupThinDto oHGroupThinDto) {
        UUID id = openingHoursRepository.saveGroup(EntityDtoMappers.toOpeningHoursGroup(oHGroupThinDto));
        oHGroupThinDto.setId(id);
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
    }

    public OHGroupDto getOHGroupForService(UUID service_id){
        return EntityDtoMappers.toOpeningHoursGroupDto(openingHoursRepository.getOHGroupForService(service_id)
                .orElseThrow(() -> new IllegalArgumentException("Not found: The Group's Service with id "+ service_id)));
    }

    /*public OHGroupDto getOHGroupForService(UUID service_id){
        return openingHoursRepository.getOHGroupForService(service_id).stream().
                map(EntityDtoMappers::toAreaDtoShallow).collect(Collectors.toList());
    }*/
}
