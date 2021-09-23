
package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.RecordEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.*;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.nav.portal.rest.api.Helpers.AreaRepositoryHelper;
import no.portal.web.generated.api.*;
import org.actioncontroller.*;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Timestamp;

public class AreaController {

   private final AreaRepository areaRepository;
   private final AreaRepositoryHelper areaRepositoryHelper;

   private final DashboardRepository dashboardRepository;
   private final ServiceRepository serviceRepository;
   private final RecordRepository recordRepository;


   public AreaController(DbContext dbContext) {
      this.areaRepositoryHelper = new AreaRepositoryHelper(dbContext);
      this.areaRepository = new AreaRepository(dbContext);
      this.dashboardRepository = new DashboardRepository(dbContext);
      this.serviceRepository = new ServiceRepository(dbContext);
      this.recordRepository = new RecordRepository(dbContext);
   }

   @GET("/Areas/:dashboard")
   @JsonBody
   public List<AreaDto> getAreas(@PathParam("dashboard") String dashboard) {
      return areaRepositoryHelper.getAreasOnDashboard(dashboard);
   }

   @GET("/Dashboard")
   @JsonBody
   public List<DashboardDto> getDashboards() {
      return dashboardRepository.retrieveAll().stream()
              .map(EntityDtoMappers::toDto)
              .collect(Collectors.toList());
   }

   @POST("/Areas/:dashboard")
   @JsonBody
   public AreaDto newAreas(@JsonBody AreaDto areaDto, @PathParam("dashboard") String dashboard) {
      try{
         areaRepository.retrieve(areaDto.getId());
         return areaDto;
      }
      catch (IllegalArgumentException e){
         //TODO denne må legges in basert på dashboard
         dashboardRepository.addAreaToDashboard("privatperson", areaDto.getId());
         areaRepository.save(EntityDtoMappers.toEntity(areaDto));
         return EntityDtoMappers.toDto(areaRepository.retrieve(areaDto.getId()));
      }
   }

   @PUT("/ServiceOnArea")
   @JsonBody
   public void addServiceToArea(@JsonBody ServiceToAreaDto serviceToAreaDto) {
      areaRepository.addServiceToArea(serviceToAreaDto.getAreaId()
              ,serviceToAreaDto.getServiceId());
   }

   @DELETE("/ServiceOnArea")
   @JsonBody
   public void removeServiceFromArea(@JsonBody ServiceToAreaDto serviceToAreaDto) {
      areaRepository.removeServiceFromArea(serviceToAreaDto.getAreaId()
              ,serviceToAreaDto.getServiceId());
   }

   @DELETE("/Areas/:dashboard")
   @JsonBody
   public void deleteArea(@JsonBody AreaDto areaDto, @PathParam("dashboard") String dashboard) {
      dashboardRepository.removeArea(dashboard,areaDto.getId());
   }

   @GET("/testStatus")
   @JsonBody
   public List<StatusAndIncidentsDto> getTestStatus() {
      ArrayList<StatusAndIncidentsDto> statusAndIncidentsDtos = new ArrayList<>();
      int numberOfSandIs = 5;
      for(int i = 0; i <numberOfSandIs; i++){
         StatusAndIncidentsDto dto = new StatusAndIncidentsDto();
         dto.status("Status " + i);
         dto.setName("Name " + i);
         statusAndIncidentsDtos.add(dto);
      }
      return statusAndIncidentsDtos;
   }



}
