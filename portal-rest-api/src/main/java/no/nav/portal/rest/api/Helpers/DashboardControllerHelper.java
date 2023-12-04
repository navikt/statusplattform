package no.nav.portal.rest.api.Helpers;

import nav.portal.core.openingHours.OpeningHoursDailyMap;
import nav.portal.core.openingHours.OpeningHoursDisplayData;
import nav.portal.core.openingHours.OpeningHoursParser;
import nav.portal.core.repositories.*;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.*;
import org.fluentjdbc.DbContext;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class DashboardControllerHelper {

    private final DashboardRepository dashboardRepository;
    private final RecordRepository recordRepository;
    private final AreaRepository areaRepository;
    private final SubAreaRepository subAreaRepository;
    private final OpeningHoursRepository openingHoursRepository;
    private final OpsControllerHelper opsControllerHelper;
    private final OpeningHoursDailyMap openingHoursDailyMap;


    public DashboardControllerHelper(DbContext dbContext) {
        this.dashboardRepository = new DashboardRepository(dbContext);
        this.recordRepository = new RecordRepository(dbContext);
        this.areaRepository = new AreaRepository(dbContext);
        this.subAreaRepository = new SubAreaRepository(dbContext);
        this.opsControllerHelper = new OpsControllerHelper(dbContext);
        this.openingHoursRepository = new OpeningHoursRepository(dbContext);
        this.openingHoursDailyMap = new OpeningHoursDailyMap(openingHoursRepository);
    }

    public DashboardDto getDashboard(UUID dashboard_id){
        DashboardDto dashboardDto = EntityDtoMappers.toDashboardDtoDeep(dashboardRepository.retrieveOne(dashboard_id));

        setSubAreas(dashboardDto);

        setStatusOnAllServisesDirectlyUnderArea(dashboardDto);

        setStatusOnAllServisesInSubAreas(dashboardDto);

        setStatusOnSubAreas(dashboardDto);

        setStatusOnAreas(dashboardDto);

        dashboardDto.setOpsMessages(opsControllerHelper.getOpsMessagesForDashboard(dashboardDto));

        setOpeningHoursOnServices(dashboardDto);

        return dashboardDto;
    }

    private void setOpeningHoursOnServices(DashboardDto dashboardDto){
        Map<UUID, OpeningHoursDisplayData>todaysDisplayData  = openingHoursDailyMap.getMap();
        dashboardDto.getAreas().forEach(a -> {
            a.getServices().forEach(s -> {
                    OpeningHoursDisplayData ohdd = todaysDisplayData.getOrDefault(s.getId(), null);
                    s.setOhDisplay(ohdd == null? new OHdisplayDto():
                            new OHdisplayDto()
                                    .openingHours(ohdd.getOpeningHours())
                                    .rule(ohdd.getRule())
                                    .displayText(ohdd.getDisplayText())
                                    .isOpen(OpeningHoursParser.isOpen(LocalDateTime.now(), ohdd.getRule()))
                    );
                    }
            );
        });
    }

    private void setSubAreas(DashboardDto dashboardDto) {
        dashboardDto.getAreas().forEach(areaDto -> {
            areaDto.setSubAreas(areaRepository.getSubAreasOnArea(areaDto.getId())
                            .stream()
                            .map(subAreaEntity -> EntityDtoMappers.toSubAreaDtoDeep(subAreaEntity, subAreaRepository.getServicesOnSubArea(subAreaEntity.getId())))
                            .collect(Collectors.toList()));
                });
    }

    private void setStatusOnSubAreas(DashboardDto dashboardDto) {
        dashboardDto.getAreas().forEach(areaDto ->
                areaDto.getSubAreas().forEach(
                        subArea -> subArea.setStatus(getWorstStatusAmongst(subArea.getServices()))

                        )
                );
    }

    private void setStatusOnAreas(DashboardDto dashboardDto) {
        dashboardDto.getAreas()
                .forEach(areaDto -> {
                    List<ServiceDto> allServicesInAreaAndSubAreas = new ArrayList<>(areaDto.getServices());
                    areaDto.getSubAreas().forEach(subArea->
                            allServicesInAreaAndSubAreas.addAll(subArea.getServices())
                    );
                    areaDto.setStatus(getWorstStatusAmongst(allServicesInAreaAndSubAreas));
                });
    }

    private void setStatusOnAllServisesInSubAreas(DashboardDto dashboardDto) {
        dashboardDto.getAreas()
                .forEach(area -> area.getSubAreas().forEach(subArea -> subArea.getServices()
                        .forEach(this::settStatusOnService)));
    }

    private void setStatusOnAllServisesDirectlyUnderArea(DashboardDto dashboardDto) {
        dashboardDto.getAreas()
                .forEach(area -> area.getServices()
                        .forEach(this::settStatusOnService));
    }

    private void settStatusOnService(ServiceDto service){
        if(recordRepository.getLatestRecord(service.getId()).isPresent()){
            service.setRecord(
                    EntityDtoMappers.toRecordDto(recordRepository.getLatestRecord(service.getId()).get())
            );
        }
        else if(recordRepository.getActiveRecordDelta(service.getId()).isPresent()){
            service.setRecord(
                    EntityDtoMappers.toRecordDtoFromRecordDelta(recordRepository.getActiveRecordDelta(service.getId()).get())
                    );
        }
        else{
            service.setRecord(new RecordDto());
        }
    }

    private StatusDto getWorstStatusAmongst(List<ServiceDto> services){
        if(services.stream()
                .map(s -> s.getRecord() != null? s.getRecord().getStatus(): null)
                .collect(Collectors.toList())
                .contains(StatusDto.DOWN)) return StatusDto.DOWN;
        if(services.stream()
                .map(s -> s.getRecord() != null? s.getRecord().getStatus(): null)
                .collect(Collectors.toList())
                .contains(StatusDto.ISSUE)) return StatusDto.ISSUE;
        if(services.stream()
                .map(s -> s.getRecord() != null? s.getRecord().getStatus(): null)
                .collect(Collectors.toList())
                .contains(StatusDto.OK)) return StatusDto.OK;
        return null;
    }


    public void deleteDashboard(UUID dashboard_id) {
        dashboardRepository.deleteAreasFromDashboard(dashboard_id);
        dashboardRepository.deleteDashboard(dashboard_id);

    }

    public void updateDashboard(UUID dashboard_id, DashboardUpdateDto dashboardUpdateDto) {
        dashboardRepository.updateNameOfDashboard(dashboard_id, dashboardUpdateDto.getName());
        List<UUID> areasOnDashboard = dashboardRepository.retrieveOne(dashboard_id).getValue().stream().map(a -> a.getArea().getId()).collect(Collectors.toList());
        List<UUID> updatedAreasOnDashboard = dashboardUpdateDto.getAreas();
        if (areasOnDashboard.containsAll(updatedAreasOnDashboard)
                && updatedAreasOnDashboard.containsAll(areasOnDashboard)) {
            return;
        }
        dashboardRepository.settAreasOnDashboard(dashboard_id, dashboardUpdateDto.getAreas());
    }
}
