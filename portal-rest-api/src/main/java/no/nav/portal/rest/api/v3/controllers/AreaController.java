
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


   @GET("/testAreas")
   @JsonBody
   public List<AreaInstanceDto> getTestData() {
      return setAreaStatus(TestUtil.getAllAreasWithRandomStatuses());
   }

   @GET("/adminAreas")
   @JsonBody
   public List<AreaDto> getAreas() {
      return  testUtil.getAreaDtos();
   }

   @POST("/adminAreas")
   @JsonBody
   public List<AreaDto> newAreas(@RequestParam("AdminAreaDto") AreaDto areaDto) {
      testUtil.addAdminArea(areaDto);
      return testUtil.getAreaDtos();
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
   private List<AreaInstanceDto> setAreaStatus(List<AreaInstanceDto> dtos){
      dtos.forEach(dto -> dto.setStatus(getAreaStatus(dto)));
      return dtos;

   }
   //TODO METODEN UNDER SKAL IKKE BO HER
   private StatusDto getAreaStatus(AreaInstanceDto dto){
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
