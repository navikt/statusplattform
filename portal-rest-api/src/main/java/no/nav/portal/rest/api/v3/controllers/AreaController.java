
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


   public AreaController(DbContext dbContext) {
      this.areaRepositoryHelper = new AreaRepositoryHelper(dbContext);
      this.areaRepository = new AreaRepository(dbContext);
   }


   @GET("/Areas")
   @JsonBody
   public List<AreaDto> getAllAreas() {
      return EntityDtoMappers.toAreaDtoDeep(areaRepository.retrieveAll());
   }
   @POST("/Areas")
   @JsonBody
   public AreaDto newArea(@JsonBody AreaDto areaDto) {
         return areaRepositoryHelper.newArea(areaDto);
   }

   @DELETE("/Area/:Area_id")
   @JsonBody
   public void deleteArea(@PathParam("Area_id") UUID area_id ) {
      areaRepository.deleteArea(area_id);
   }


   @GET("/Areas/:Dashboard_id")
   @JsonBody
   public List<AreaDto> getAreas(@PathParam("Dashboard_id") UUID dashboard_id) {
      return areaRepositoryHelper.getAreasOnDashboard(dashboard_id);
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




}
