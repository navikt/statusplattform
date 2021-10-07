
package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.repositories.*;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.nav.portal.rest.api.Helpers.AreaRepositoryHelper;
import no.portal.web.generated.api.*;
import org.actioncontroller.*;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.*;

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

   @GET("/Areas/:Dashboard")
   @JsonBody
   public List<AreaDto> getAreas(@PathParam("Dashboard") String dashboard) {
      //TODO endre til å hente basert på uid
      return areaRepositoryHelper.getAreasOnDashboard(dashboard);
   }

   @POST("/Areas")
   @JsonBody
   public AreaDto newArea(@JsonBody AreaDto areaDto) {
         return areaRepositoryHelper.newArea(areaDto);
   }



   @POST("/Areas/:Dashboard")
   @JsonBody
   public AreaDto addAreaToDashboard(@JsonBody AreaDto areaDto, @PathParam("Dashboard") String dashboard) {
         dashboardRepository.(dashboard, areaDto.getId());
         areaRepository.save(EntityDtoMappers.toEntity(areaDto));
         return EntityDtoMappers.toDto(areaRepository.retrieve(areaDto.getId()));
      
   }

   @DELETE("/Areas/:Dashboard")
   @JsonBody
   public void deleteArea(@JsonBody AreaDto areaDto, @PathParam("Dashboard") String dashboard) {
         boolean removed = dashboardRepository.removeArea(dashboard,areaDto.getId());
         if(!removed){
            throw new HttpNotFoundException();
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





}
