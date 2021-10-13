package no.nav.portal.rest.api.Helpers;

import nav.portal.core.entities.AreaWithServices;
import nav.portal.core.entities.RecordEntity;
import nav.portal.core.repositories.DashboardRepository;
import nav.portal.core.repositories.RecordRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.AreaDto;
import no.portal.web.generated.api.ServiceDto;
import no.portal.web.generated.api.StatusDto;
import no.portal.web.generated.api.TileDto;
import org.fluentjdbc.DbContext;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class DashboardRepositoryHelper {

    private final DashboardRepository dashboardRepository;
    private final RecordRepository recordRepository;


    public DashboardRepositoryHelper(DbContext dbContext) {
        this.dashboardRepository = new DashboardRepository(dbContext);
        this.recordRepository = new RecordRepository(dbContext);
    }



    public List<TileDto> getTilesOnDashboard(UUID id) {
        List<AreaWithServices> areasWithServices = dashboardRepository.retrieveOne(id).getValue();
        List<AreaDto> areaDtos = areasWithServices.stream()
                .map(aws ->
                        EntityDtoMappers.toAreaDtoDeep(aws.getArea(),aws.getServices()))
                .collect(Collectors.toList());
        List<TileDto> tiles = Collections.EMPTY_LIST;
        for(AreaDto areaDto: areaDtos){
            for(ServiceDto serviceDto: areaDto.getServices()){
                //Henter siste record-innslag for tjenester
                Optional<RecordEntity> optRecord = recordRepository.getLatestRecord(serviceDto.getId());
                if(optRecord.isPresent()){
                    //setter status dersom den finnes.
                    serviceDto.setStatus(StatusDto.fromValue(optRecord.get().getStatus().getDbRepresentation()));
                }
                else{
                    //Hvis ikke er den nede.
                    serviceDto.setStatus(StatusDto.DOWN);
                }
            }
            TileDto tile = new TileDto();
            tile.setArea(areaDto);
            tile.setServices(areaDto.getServices());
            tile.setStatus(getTileStatus(tile));
            tiles.add(tile);
        }
        return tiles;
    }
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
