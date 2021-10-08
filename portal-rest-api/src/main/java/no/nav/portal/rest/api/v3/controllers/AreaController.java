
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



   @GET("/Areas/:Dashboard")
   @JsonBody
   public List<AreaDto> getAreas(@PathParam("Dashboard") String dashboard) {
      return areaRepositoryHelper.getAreasOnDashboard(dashboard);

   }

   @POST("/Areas")
   @JsonBody
   public AreaDto newArea(@JsonBody AreaDto areaDto) {
         return areaRepositoryHelper.newArea(areaDto);
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
