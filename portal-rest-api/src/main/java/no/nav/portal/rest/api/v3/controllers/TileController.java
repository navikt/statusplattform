package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.RecordEntity;
import nav.portal.core.repositories.AreaRepository;
import nav.portal.core.repositories.DashboardRepository;
import nav.portal.core.repositories.RecordRepository;
import nav.portal.core.repositories.ServiceRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.*;
import org.actioncontroller.GET;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TileController {


    private final AreaRepository areaRepository;
    private final DashboardRepository dashboardRepository;
    private final ServiceRepository serviceRepository;
    private final RecordRepository recordRepository;


    public TileController(DbContext dbContext) {
        this.areaRepository = new AreaRepository(dbContext);
        this.dashboardRepository = new DashboardRepository(dbContext);
        this.serviceRepository = new ServiceRepository(dbContext);
        this.recordRepository = new RecordRepository(dbContext);
    }



    @GET("/Tiles")
    @JsonBody
    public List<TileDto> getTiles() {
        //Dashboardtype må settes som parameter
        DashboardDto dashboardDto  =  EntityDtoMappers.toDto(dashboardRepository.retrieve("privatperson"));
        List<AreaDto> areaDtos = areaRepository.retrieve(dashboardDto.getAreasIds())
                .stream()
                .map(EntityDtoMappers::toDto)
                .collect(Collectors.toList());
        ArrayList<TileDto> tiles = new ArrayList<>();
        for(AreaDto areaDto: areaDtos){
            List<String> serviseIds = areaDto.getServisesIds();
            List<ServiceDto> services =  serviceRepository.retrieve(serviseIds)
                    .stream().map(EntityDtoMappers::toDto)
                    .collect(Collectors.toList());
            //TODO gå igjennom dette. Hvordan bør dette gjøres?
            for(ServiceDto serviceDto: services){
                Optional<RecordEntity> optRecord = recordRepository.getLatestRecord(serviceDto.getId());
                if(optRecord.isPresent()){
                    serviceDto.setStatus(StatusDto.fromValue(optRecord.get().getStatus()));
                }
                else{
                    serviceDto.setStatus(StatusDto.DOWN);
                }
            }
            TileDto tile = new TileDto();
            tile.setArea(areaDto);
            tile.setServices(services);
            tile.setStatus(getTileStatus(tile));
            tiles.add(tile);
        }
        return tiles;
    }
    //TODO METODEN UNDER SKAL IKKE BO HER
    private StatusDto getTileStatus(TileDto dto){
        if(dto.getServices()
                .stream()
                .map(s -> s.getStatus())
                .collect(Collectors.toList())
                .contains(StatusDto.DOWN)) return StatusDto.DOWN;
        if(dto.getServices()
                .stream()
                .map(s -> s.getStatus())
                .collect(Collectors.toList())
                .contains(StatusDto.ISSUE)) return StatusDto.ISSUE;
        return StatusDto.OK;
    }

}
