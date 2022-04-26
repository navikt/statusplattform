
package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.repositories.*;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.nav.portal.rest.api.Helpers.AreaControllerHelper;
import no.nav.portal.rest.api.Helpers.SubAreaControllerHelper;
import no.portal.web.generated.api.*;
import org.actioncontroller.*;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.*;
import java.util.stream.Collectors;


public class AreaController {

   private final AreaRepository areaRepository;
   private final AreaControllerHelper areaControllerHelper;
   private final SubAreaRepository subAreaRepository;
   private final SubAreaControllerHelper subAreaControllerHelper;

   public AreaController(DbContext dbContext) {
      this.areaControllerHelper = new AreaControllerHelper(dbContext);
      this.areaRepository = new AreaRepository(dbContext);
      this.subAreaRepository = new SubAreaRepository(dbContext);
      this.subAreaControllerHelper = new SubAreaControllerHelper(dbContext);
   }


   @GET("/Areas")
   @JsonBody
   public List<AreaDto> getAllAreas() {
      return areaControllerHelper.getAllAreas();

   }

   @POST("/Areas")
   @JsonBody
   public UUID newArea(@JsonBody AreaDto areaDto) {
      UUID uuid = areaControllerHelper.newArea(areaDto).getId();
      areaRepository.setServicesOnArea(uuid,
              areaDto.getServices().stream().map(
                      ServiceDto::getId).collect(Collectors.toList()));
      return uuid;
   }

   @PUT("/Area/:Area_id")
   @JsonBody
   public void updateArea(@PathParam("Area_id") UUID area_id, @JsonBody AreaDto areaDto ) {
      areaControllerHelper.updateArea(area_id, areaDto);
   }

   @DELETE("/Area/:Area_id")
   @JsonBody
   public void deleteArea(@PathParam("Area_id") UUID area_id ) {
      if(!areaRepository.deleteArea(area_id)){
         throw new HttpNotFoundException("Fant ikke område med id: " + area_id);
      }
   }


   @GET("/Areas/:Dashboard_id")
   @JsonBody
   public List<AreaDto> getAreas(@PathParam("Dashboard_id") UUID dashboard_id) {
      return areaControllerHelper.getAreasOnDashboard(dashboard_id);
   }

   @PUT("/Area/:Area_id/:Service_id")
   @JsonBody
   public void addServiceToArea(@PathParam("Area_id") UUID area_id, @PathParam("Service_id") UUID service_id ) {
      areaRepository.addServiceToArea(area_id,service_id);
   }

   @DELETE("/Area/:Area_id/:Service_id")
   @JsonBody
   public void removeServiceFromArea(@PathParam("Area_id") UUID area_id, @PathParam("Service_id") UUID service_id ) {
      areaRepository.removeServiceFromArea(area_id,service_id);
   }

   /*Delen av AreaController for SubArea*/

   @GET("/SubAreas")
   @JsonBody
   public List<SubAreaDto> getAllSubAreas() {
      return EntityDtoMappers.toSubAreaDtoDeep(subAreaRepository.retrieveAll());
   }

   @POST("/SubArea")
   @JsonBody
   public UUID newSubArea(@JsonBody SubAreaDto subAreaDto) {
      UUID uuid = subAreaControllerHelper.newSubArea(subAreaDto).getId();
      subAreaRepository.setServicesOnSubArea(uuid,
              subAreaDto.getServices().stream().map(
                      ServiceDto::getId).collect(Collectors.toList()));
      return uuid;
   }




}
