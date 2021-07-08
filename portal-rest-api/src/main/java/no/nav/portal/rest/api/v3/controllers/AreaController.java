
package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.repositories.ExampleRepository;

import no.nav.portal.rest.api.TestUtil;
import no.portal.web.generated.api.*;
import org.actioncontroller.*;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.*;
import java.util.stream.Collectors;

public class AreaController {

   private final ExampleRepository exampleRepo;
   private TestUtil testUtil; //TODO DENNE SKAL IKKE BO HER FINN UT ANNEN LØSNING


   public AreaController(DbContext dbContext) {
      this.testUtil = new TestUtil();
      this.exampleRepo = new ExampleRepository(dbContext);
   }


   @GET("/Tiles")
   @JsonBody
   public List<TileDto> getTestData() {
      return setAreaStatus(testUtil.getAllTilesWithRandomStatuses());
   }

   @GET("/Areas")
   @JsonBody
   public List<AreaDto> getAreas() {
      return testUtil.getAreaDtos();
   }

   @POST("/Areas")
   @JsonBody
   public List<AreaDto> newAreas(@JsonBody AreaDto areaDto) {
      testUtil.addAdminArea(areaDto);
      return testUtil.getAreaDtos();
   }

   @DELETE("/Areas")
   @JsonBody
   public List<AreaDto> deleteArea(@JsonBody AreaDto areaDto) {
         testUtil.deleteAdminArea(areaDto);
      return testUtil.getAreaDtos();
   }

   @GET("/dashboards")
   @JsonBody
   public List<DashboardDto> getDashBoards() {
      ArrayList<DashboardDto> statusAndIncidentsDtos = new ArrayList<>();
      return statusAndIncidentsDtos;
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

   //TODO METODEN UNDER SKAL IKKE BO HER
   private List<TileDto> setAreaStatus(List<TileDto> dtos){
      dtos.forEach(dto -> dto.setStatus(getTileStatus(dto)));
      return dtos;

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
