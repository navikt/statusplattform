package no.nav.statusplattform.api;

import nav.statusplattform.core.entities.AreaEntity;
import nav.statusplattform.core.entities.AreaWithServices;
import nav.statusplattform.core.entities.DashboardEntity;
import nav.statusplattform.core.entities.HelpTextEntity;
import nav.statusplattform.core.entities.MaintenanceEntity;
import nav.statusplattform.core.entities.OpeningHoursGroup;
import nav.statusplattform.core.entities.OpeningHoursGroupEntity;
import nav.statusplattform.core.entities.OpeningHoursRuleEntity;
import nav.statusplattform.core.entities.OpsMessageEntity;
import nav.statusplattform.core.entities.RecordDeltaEntity;
import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.entities.ServiceEntity;
import nav.statusplattform.core.entities.SubAreaEntity;
import nav.statusplattform.core.enums.OpsMessageSeverity;
import nav.statusplattform.core.enums.OpsMessageStatus;
import nav.statusplattform.core.enums.RuleType;
import nav.statusplattform.core.enums.ServiceStatus;
import nav.statusplattform.core.enums.ServiceType;
import nav.statusplattform.core.openingHours.TimeSpan;
import nav.statusplattform.core.openingHours.UpTimeTotals;
import no.nav.statusplattform.api.TeamKatalogIntegrasjon.TeamKatalogKlient;


import no.nav.statusplattform.generated.api.AreaDto;
import no.nav.statusplattform.generated.api.DashboardDto;
import no.nav.statusplattform.generated.api.DashboardNameIdDto;
import no.nav.statusplattform.generated.api.HelpTextDto;
import no.nav.statusplattform.generated.api.MaintenanceDto;
import no.nav.statusplattform.generated.api.OHGroupDto;
import no.nav.statusplattform.generated.api.OHGroupThinDto;
import no.nav.statusplattform.generated.api.OHRuleDto;
import no.nav.statusplattform.generated.api.OPSmessageDto;
import no.nav.statusplattform.generated.api.RecordDto;
import no.nav.statusplattform.generated.api.ServiceDto;
import no.nav.statusplattform.generated.api.ServiceTypeDto;
import no.nav.statusplattform.generated.api.StatusDto;
import no.nav.statusplattform.generated.api.SubAreaDto;
import no.nav.statusplattform.generated.api.UpTimeTotalsDto;


import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class EntityDtoMappers {
    private static Map<UUID,String> teamIdTeamKatalog = TeamKatalogKlient.getTeams();

    public static ServiceEntity toServiceEntity(ServiceDto dto){
        ServiceEntity entity = new ServiceEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setType(ServiceType.fromDb(dto.getType().getValue()));
        entity.setTeam(dto.getTeam());
        entity.setMonitorlink(dto.getMonitorlink());
        if(dto.getPollingUrl() != null && dto.getPollingUrl().equals("")){
            entity.setPolling_url(null);
        }
        else {
            entity.setPolling_url(dto.getPollingUrl());
        }
        entity.setPollingOnPrem(dto.getPollingOnPrem());
        entity.setStatusNotFromTeam(dto.getStatusNotFromTeam());
        return entity;
    }

    //TODO kanskje denne valideringen hører hjemme et annet sted?


    public static AreaEntity toAreaEntity(AreaDto dto){
        AreaEntity entity = new AreaEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setContains_components(dto.getContainsComponents());
        return entity;
    }

    public static SubAreaEntity toSubAreaEntity(SubAreaDto dto){
        SubAreaEntity entity = new SubAreaEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        return entity;
    }


    public static ServiceDto toServiceDtoShallow(ServiceEntity entity){
        ServiceDto dto = new ServiceDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(ServiceTypeDto.fromValue(entity.getType().getDbRepresentation()));
        dto.setTeam(mapTeamNavnTeamKatalog(entity.getTeam()));
        dto.setTeamId(getTeamUidIfPresent(entity.getTeam()));
        dto.setPollingOnPrem(entity.getPollingOnPrem());
        dto.setPollingUrl(entity.getPolling_url());
        dto.setMonitorlink(entity.getMonitorlink());
        dto.setStatusNotFromTeam(entity.getStatusNotFromTeam());
        return dto;
    }

    private static UUID getTeamUidIfPresent(String teamId){
        UUID uuid = null;
        try{
            uuid = UUID.fromString(teamId);
        }
        catch (Exception ignored){

        }
        return uuid;

    }
    private static String mapTeamNavnTeamKatalog(String teamId){
        UUID uuid;
        try{
            uuid = UUID.fromString(teamId);
        }
        catch (Exception e){
            return teamId;
        }
        if(teamIdTeamKatalog.get(uuid) == null){
            TeamKatalogKlient.updateTeams();
            teamIdTeamKatalog = TeamKatalogKlient.getTeams();
        }
        return teamIdTeamKatalog.getOrDefault(uuid,teamId);
    }

    public static OPSmessageDto toOpsMessageDtoShallow(OpsMessageEntity entity) {
        OPSmessageDto dto = new OPSmessageDto();
        dto.setId(entity.getId());
        dto.setInternalHeader(entity.getInternalHeader());
        dto.setInternalMessage(entity.getInternalText());
        dto.setExternalHeader(entity.getExternalHeader());
        dto.setExternalMessage(entity.getExternalText());
        dto.setStartTime(entity.getStartTime() != null? entity.getStartTime().toOffsetDateTime():null);
        dto.setEndTime(entity.getEndTime() != null? entity.getEndTime().toOffsetDateTime():null);
        dto.setSeverity(entity.getSeverity() != null?
                OPSmessageDto.SeverityEnum.fromValue(entity.getSeverity().getDbRepresentation())
                :null);
        dto.setStatus(entity.getStatus() != null?
                OPSmessageDto.StatusEnum.fromValue(entity.getStatus().getDbRepresentation())
                :null);
        boolean isActive = entity.getStartTime() != null && entity.getEndTime() != null &&
                entity.getStartTime().isBefore(ZonedDateTime.now()) && entity.getEndTime().isAfter(ZonedDateTime.now());
        dto.setIsActive(isActive);
        dto.setOnlyShowForNavEmployees(entity.getOnlyShowForNavEmployees());
        return dto;
    }

    public static OPSmessageDto toOpsMessageDtoDeep(OpsMessageEntity entity, List<ServiceEntity> services) {
        OPSmessageDto dto = toOpsMessageDtoShallow(entity);
        dto.setAffectedServices(services.stream().map(EntityDtoMappers::toServiceDtoShallow).collect(Collectors.toList()));
        return dto;
    }

    public static OpsMessageEntity toOpsMessageEntity(OPSmessageDto opsMessageDto) {
        OpsMessageEntity opsMessageEntity = new OpsMessageEntity();
        opsMessageEntity.setId(opsMessageDto.getId());
        opsMessageEntity.setExternalHeader(opsMessageDto.getExternalHeader());
        opsMessageEntity.setExternalText(opsMessageDto.getExternalMessage());
        opsMessageEntity.setInternalHeader(opsMessageDto.getInternalHeader());
        opsMessageEntity.setInternalText(opsMessageDto.getInternalMessage());
        opsMessageEntity.setSeverity(opsMessageDto.getSeverity() != null?
                OpsMessageSeverity.valueOf((opsMessageDto.getSeverity().getValue())):
                null);
        opsMessageEntity.setStatus(opsMessageDto.getStatus() != null?
                OpsMessageStatus.valueOf((opsMessageDto.getStatus().getValue())):
                null);
        opsMessageEntity.setOnlyShowForNavEmployees(opsMessageDto.getOnlyShowForNavEmployees());
        opsMessageEntity.setStartTime(opsMessageDto.getStartTime() != null?
                opsMessageDto.getStartTime().toZonedDateTime():
                null);
        opsMessageEntity.setEndTime(opsMessageDto.getEndTime() != null?
                opsMessageDto.getEndTime().toZonedDateTime():
                null);
        return opsMessageEntity;
    }


    public static ServiceDto toServiceDtoDeep(ServiceEntity service ,List<ServiceEntity> dependencies){
        Map<ServiceEntity, List<ServiceEntity>> map = new HashMap<>();
        map.put(service,dependencies);
        return toServiceDtoDeep(map.entrySet().stream().findFirst().orElseThrow());
    }

    public static ServiceDto toServiceDtoDeep(Map.Entry<ServiceEntity,List<ServiceEntity>> entry){
        ServiceEntity service = entry.getKey();
        List<ServiceEntity> serviceDependencies = entry.getValue().stream().filter(s -> s.getType().equals(ServiceType.TJENESTE)).toList();
        List<ServiceEntity> componentDependencies = entry.getValue().stream().filter(s -> s.getType().equals(ServiceType.KOMPONENT)).toList();
        ServiceDto dto = new ServiceDto();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setType(ServiceTypeDto.fromValue(service.getType().getDbRepresentation()));
        dto.setTeam(mapTeamNavnTeamKatalog(service.getTeam()));
        dto.setTeamId(getTeamUidIfPresent(service.getTeam()));
        dto.setMonitorlink(service.getMonitorlink());
        dto.setPollingOnPrem(service.getPollingOnPrem());
        dto.pollingUrl(service.getPolling_url());
        dto.setStatusNotFromTeam(service.getStatusNotFromTeam());
        dto.serviceDependencies(serviceDependencies.stream().map(EntityDtoMappers::toServiceDtoShallow).collect(Collectors.toList()));
        dto.setComponentDependencies(componentDependencies.stream().map(EntityDtoMappers::toServiceDtoShallow).collect(Collectors.toList()));
        return dto;
    }

    public static AreaDto toAreaDtoDeep(AreaEntity area, List<ServiceEntity> services){
        AreaDto dto = new AreaDto();
        dto.setId(area.getId());
        dto.setName(area.getName());
        dto.setDescription(area.getDescription());
        dto.setContainsComponents(area.getContains_components());
        dto.setServices(
                services.stream()
                        .map(EntityDtoMappers::toServiceDtoShallow)
                        .collect(Collectors.toList())
        );
        return dto;
    }

    public static AreaDto toAreaDtoShallow(AreaEntity area){
        AreaDto dto = new AreaDto()
                .id(area.getId())
                .name(area.getName())
                .description(area.getDescription())
                .containsComponents(area.getContains_components());
        return dto;
    }


    public static List<SubAreaDto> toSubAreaDtoDeep(Map<SubAreaEntity, List<ServiceEntity>> subAreaWithServices){
        List<SubAreaDto> dtos = new ArrayList<>();
        subAreaWithServices.forEach((subArea, services) -> dtos.add(toSubAreaDtoDeep(subArea, services)));
        return dtos.stream().sorted(Comparator.comparing(SubAreaDto::getName)).collect(Collectors.toList());
    }

    public static SubAreaDto toSubAreaDtoDeep(SubAreaEntity subArea, List<ServiceEntity> services){
        SubAreaDto dto = new SubAreaDto();
        dto.setId(subArea.getId());
        dto.setName(subArea.getName());
        dto.setServices(
                services.stream()
                        .map(EntityDtoMappers::toServiceDtoShallow)
                        .collect(Collectors.toList())
        );
        return dto;
    }

    public static SubAreaDto toSubAreaDtoShallow(SubAreaEntity subArea){
        SubAreaDto dto = new SubAreaDto();
        dto.setId(subArea.getId());
        dto.setName(subArea.getName());

        return dto;
    }

    public static DashboardDto toDashboardDtoDeep(Map.Entry<DashboardEntity,List<AreaWithServices>> dashboardEntry) {
        DashboardDto dto = new DashboardDto();
        dto.setName(dashboardEntry.getKey().getName());
        dto.setId(dashboardEntry.getKey().getId());
        dto.setAreas(dashboardEntry.getValue()
                .stream()
                .map(area -> toAreaDtoDeep(area.getArea(), area.getServices()))
                .collect(Collectors.toList()));
        return dto;
    }

    // Convert a list of entries into a list of DashboardDto objects
    public static List<DashboardDto> toDashboardDtoDeepList(List<Map.Entry<DashboardEntity, List<AreaWithServices>>> dashboardEntries) {
        return dashboardEntries.stream()
                .map(EntityDtoMappers::toDashboardDtoDeep) // Using the existing conversion method for each entry
                .collect(Collectors.toList());
    }


    public static DashboardNameIdDto toDashboardDtoShallow(DashboardEntity entity){
        DashboardNameIdDto dto = new DashboardNameIdDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }

    public static List<DashboardNameIdDto>toDashboardDtoShallow(List<DashboardEntity> entities){
        return entities.stream().map(EntityDtoMappers::toDashboardDtoShallow).collect(Collectors.toList());
    }

    public static List<RecordDto> toRecordDto(List<RecordEntity> recordHistory) {
        return recordHistory.stream().map(EntityDtoMappers::toRecordDto).collect(Collectors.toList());
    }
    public static RecordDto toRecordDto(RecordEntity recordEntity){
        RecordDto dto = new RecordDto();
        dto.setId(recordEntity.getId());
        dto.serviceId(recordEntity.getServiceId());
        dto.setStatus(StatusDto.fromValue(recordEntity.getStatus().getDbRepresentation()));
        dto.setDescription(recordEntity.getDescription());
        dto.setLogLink(recordEntity.getLogglink());
        ZonedDateTime entityTime = recordEntity.getCreated_at();
        dto.setResponseTime(recordEntity.getResponsetime());
        dto.setTimestamp(OffsetDateTime.of(entityTime.toLocalDateTime(),entityTime.getOffset()));
        return dto;
    }

    public static RecordDto toRecordDtoFromRecordDelta(RecordDeltaEntity recordDeltaEntity){
        RecordDto dto = new RecordDto();
        dto.setId(recordDeltaEntity.getId());
        dto.serviceId(recordDeltaEntity.getServiceId());
        dto.setStatus(StatusDto.fromValue(recordDeltaEntity.getStatus().getDbRepresentation()));
        ZonedDateTime entityTime = recordDeltaEntity.getUpdated_at().truncatedTo(ChronoUnit.SECONDS);
        dto.setTimestamp(OffsetDateTime.of(entityTime.toLocalDateTime(),entityTime.getOffset()));
        return dto;
    }

    public static RecordEntity toRecordEntity(RecordDto recordDto) {
        return new RecordEntity()
                .setId(recordDto.getId())
                .setServiceId(recordDto.getServiceId())
                .setStatus(ServiceStatus.valueOf(recordDto.getStatus().getValue()))
                .setDescription(recordDto.getDescription())
                .setLogglink(recordDto.getLogLink())
                .setResponsetime(recordDto.getResponseTime())
               .setCreated_at(ZonedDateTime.of(recordDto.getTimestamp().toLocalDateTime(),ZonedDateTime.now().getZone()));
    }

    public static MaintenanceEntity toMaintenanceEntity(MaintenanceDto maintenanceDto) {
        return new MaintenanceEntity()
                .setServiceId(maintenanceDto.getServiceId())
                .setDescription(maintenanceDto.getDescription())
                .setStart_time(maintenanceDto.getStartTime().toZonedDateTime())
                .setEnd_time(maintenanceDto.getEndTime().toZonedDateTime());
    }

    public static MaintenanceDto toMaintenanceDto(MaintenanceEntity maintenanceEntity){
        return new MaintenanceDto()
                .serviceId(maintenanceEntity.getServiceId())
                .description(maintenanceEntity.getDescription())
                .startTime(maintenanceEntity.getStart_time().toOffsetDateTime())
                .endTime(maintenanceEntity.getEnd_time().toOffsetDateTime());
    }

    public static OpeningHoursRuleEntity toOpeningHoursRuleEntity(OHRuleDto oHRuleDto) {
        return new OpeningHoursRuleEntity()
                .setId(oHRuleDto.getId())
                .setRule(oHRuleDto.getRule())
                .setName(oHRuleDto.getName());
    }

    public static OHRuleDto toOpeningHoursRuleDto(OpeningHoursRuleEntity rule) {
        OHRuleDto dto = new OHRuleDto();
        dto.setId(rule.getId());
        dto.setRule(rule.getRule());
        dto.setName(rule.getName());
        return dto;
    }

    public static List<OHRuleDto>toOpeningHoursRuleDto(List<OpeningHoursRuleEntity> entities){
        return entities.stream().map(EntityDtoMappers::toOpeningHoursRuleDto).collect(Collectors.toList());
    }

    public static OpeningHoursGroupEntity toOpeningHoursGroup(OHGroupThinDto oHGroupThinDto) {
        return new OpeningHoursGroupEntity()
                .setId(oHGroupThinDto.getId())
                .setName(oHGroupThinDto.getName())
                .setRules(oHGroupThinDto.getRules()!= null? oHGroupThinDto.getRules(): Collections.EMPTY_LIST);
    }

    public static OHGroupDto toOpeningHoursGroupDto(OpeningHoursGroup group) {
        OHGroupDto dto = new OHGroupDto();
        dto.setId(group.getId());
        dto.setName(group.getName());

        dto.setRules(group.getRules().stream().map(rule -> {
            if(rule.getRuleType().equals(RuleType.RULE)){
                return new OHGroupDto()
                        .id(rule.getId())
                        .name(rule.getName())
                        .rule(((OpeningHoursRuleEntity)rule).getRule());
            }
            else {return toOpeningHoursGroupDto((OpeningHoursGroup) rule);
            }
        }).collect(Collectors.toList()));

        return dto;
    }

    public static HelpTextDto toHelpTextDto(HelpTextEntity entity){
        HelpTextDto dto = new HelpTextDto();
        dto.setNumber(entity.getNumber());
        dto.setType(ServiceTypeDto.fromValue(entity.getType().getDbRepresentation()));
        dto.setContent(entity.getContent());
        return dto;
    }

    public static HelpTextEntity toHelpTextEntity(HelpTextDto dto){
        HelpTextEntity entity = new HelpTextEntity();
        entity.setNumber(dto.getNumber());
        entity.setType(ServiceType.fromDb(dto.getType().getValue()));
        entity.setContent(dto.getContent());
        return entity;
    }

    public static UpTimeTotalsDto toUpTimeTotalsDto(final UpTimeTotals upTimeTotals) {
        //Orlene: Using BigDecimal as expected by the DTO.
        var dto = new UpTimeTotalsDto();
        dto.setSumOfActualUptime(BigDecimal.valueOf(upTimeTotals.sumOfActualUptime()));
        dto.setSumOfExpectedUptime(BigDecimal.valueOf(upTimeTotals.sumOfExpectedUptime()));
        return dto;
    }
}
