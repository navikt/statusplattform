package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.RecordEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.AreaRepository;
import nav.portal.core.repositories.DashboardRepository;
import nav.portal.core.repositories.RecordRepository;
import nav.portal.core.repositories.ServiceRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.ServiceDto;
import org.actioncontroller.DELETE;
import org.actioncontroller.GET;
import org.actioncontroller.POST;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceController {

    private final AreaRepository areaRepository;
    private final DashboardRepository dashboardRepository;
    private final ServiceRepository serviceRepository;
    private final RecordRepository recordRepository;


    public ServiceController(DbContext dbContext) {
        this.areaRepository = new AreaRepository(dbContext);
        this.dashboardRepository = new DashboardRepository(dbContext);
        this.serviceRepository = new ServiceRepository(dbContext);
        this.recordRepository = new RecordRepository(dbContext);
    }






    @POST("/ServiceRecord")
    @JsonBody
    public void uppdateRecord(@JsonBody ServiceDto serviceDto) {
        if(serviceRepository.doesEntryExist(serviceDto.getId())){
            //Servisen er lagret fra før
            //Legger kunn til en ny record. Men skal man sammenligne?
            RecordEntity entity = new RecordEntity(serviceDto.getId(),
                    serviceDto.getStatus().getValue()
                    ,new Timestamp(System.currentTimeMillis()),
                    42);
            recordRepository.save(entity);
        }
        else{
            //Servicen er ikke lagret fra før
            //Dette skal legges inn i ett tjeneste lag
            ServiceEntity entity = new ServiceEntity();
            entity.setId(serviceDto.getId());
            entity.setName(serviceDto.getName());
            entity.setType(serviceDto.getType());
            entity.setTeam(serviceDto.getTeam());
            entity.setDependencies(serviceDto.getDependencies());
            entity.setMonitorlink(serviceDto.getMonitorlink());
            entity.setDescription(serviceDto.getDescription());
            entity.setLogglink(serviceDto.getLogglink());
            serviceRepository.save(entity);
        }
        RecordEntity entity = new RecordEntity(serviceDto.getId(),
                serviceDto.getStatus().getValue()
                ,new Timestamp(System.currentTimeMillis()),
                42);
        recordRepository.save(entity);
    }

    @POST("/Service")
    @JsonBody
    public void newService(@JsonBody ServiceDto serviceDto) {
        //Servicen er ikke lagret fra før
        //Dette skal legges inn i ett tjeneste lag
        ServiceEntity entity = new ServiceEntity();
        entity.setId(serviceDto.getId());
        entity.setName(serviceDto.getName());
        entity.setType(serviceDto.getType());
        entity.setTeam(serviceDto.getTeam());
        entity.setDependencies(serviceDto.getDependencies());
        entity.setMonitorlink(serviceDto.getMonitorlink());
        entity.setDescription(serviceDto.getDescription());
        entity.setLogglink(serviceDto.getLogglink());
        serviceRepository.save(entity);
    }

    @GET("/Services")
    @JsonBody
    public List<ServiceDto> getServices() {
        return serviceRepository.retrieveAll()
                .stream()
                .map(EntityDtoMappers::toDto)
                .collect(Collectors.toList());

    }

    @DELETE("/Service")
    @JsonBody
    //TODO denne skal kun fjerne området fra ett dashboard
    public void deleteService(@JsonBody ServiceDto serviceDto) {
        serviceRepository.delete(serviceDto.getId());
    }


}
