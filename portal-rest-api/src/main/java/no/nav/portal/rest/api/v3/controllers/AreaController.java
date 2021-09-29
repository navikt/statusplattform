
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

   @GET("/Areas/:Dashboard")
   @JsonBody
   public List<AreaDto> getAreas(@PathParam("Dashboard") String dashboard) {
      return areaRepositoryHelper.getAreasOnDashboard(dashboard);
   }

   @POST("/Areas")
   @JsonBody
   public AreaDto newArea(@JsonBody AreaDto areaDto) {
      try{
         areaRepository.retrieve(areaDto.getId());
         throw new HttpForbiddenException("Område med id: "+ areaDto.getId() +" finnes fra før.");
      }
      catch (IllegalArgumentException e){
         areaRepository.saveNew(EntityDtoMappers.toEntity(areaDto));
         return EntityDtoMappers.toDto(areaRepository.retrieve(areaDto.getId()));
      }
   }



   @POST("/Areas/:Dashboard")
   @JsonBody
   public AreaDto newAreas(@JsonBody AreaDto areaDto, @PathParam("Dashboard") String dashboard) {
      try{
         areaRepository.retrieve(areaDto.getId());
         return areaDto;
      }
      catch (IllegalArgumentException e){
         dashboardRepository.addAreaToDashboard(dashboard, areaDto.getId());
         areaRepository.saveNew(EntityDtoMappers.toEntity(areaDto));
         return EntityDtoMappers.toDto(areaRepository.retrieve(areaDto.getId()));
      }
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
