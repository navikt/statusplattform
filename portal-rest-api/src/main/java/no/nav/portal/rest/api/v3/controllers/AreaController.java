
package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.ExampleEntity;

import nav.portal.core.repositories.ExampleRepository;

import no.nav.portal.rest.api.TestUtil;
import no.portal.web.generated.api.AreaDto;
import no.portal.web.generated.api.ServiceDto;
import no.portal.web.generated.api.StatusAndIncidentsDto;
import no.portal.web.generated.api.StatusDto;
import org.actioncontroller.*;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.*;
import java.util.stream.Collectors;

public class AreaController {

   private final ExampleRepository exampleRepo;


   public AreaController(DbContext dbContext) {
      this.exampleRepo = new ExampleRepository(dbContext);
   }


   @GET("/testAreas")
   @JsonBody
   public List<AreaDto> getTestData() {
      return setAreaStatus(TestUtil.getAllAreasWithRandomStatuses());
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


   private List<AreaDto> setAreaStatus(List<AreaDto> dtos){
      dtos.forEach(dto -> dto.setStatus(getAreaStatus(dto)));
      return dtos;

   }

   private StatusDto getAreaStatus(AreaDto dto){
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
