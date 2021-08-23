
package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.RecordEntity;
import nav.portal.core.repositories.*;

import no.nav.portal.rest.api.EntityDtoMappers;
import no.nav.portal.rest.api.TestUtil;
import no.portal.web.generated.api.*;
import org.actioncontroller.*;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.*;
import java.util.stream.Collectors;

public class AreaController {

   private final AreaRepository areaRepository;
   private final DashboardRepository dashboardRepository;
   private final ServiceRepository serviceRepository;
   private final RecordRepository recordRepository;
   private TestUtil testUtil; //TODO DENNE SKAL IKKE BO HER FINN UT ANNEN LØSNING


   public AreaController(DbContext dbContext) {
      this.areaRepository = new AreaRepository(dbContext);
      this.dashboardRepository = new DashboardRepository(dbContext);
      this.serviceRepository = new ServiceRepository(dbContext);
      this.recordRepository = new RecordRepository(dbContext);
      this.testUtil = new TestUtil();
   }


   @GET("/Tiles")
   @JsonBody
   public List<TileDto> getTestData() {
      //Dashboardtype må settes som parameter
      DashboardDto dashboardDto  =  EntityDtoMappers.toDto(dashboardRepository.retrieve("privatperson"));
      List<AreaDto> areaDtos = areaRepository.retrieve(dashboardDto.getAreasIds())
              .stream()
              .map(EntityDtoMappers::toDto)
              .collect(Collectors.toList());
      List<TileDto> tiles = Collections.EMPTY_LIST;
      for(AreaDto dto: areaDtos){
         List<String> serviseIds = dto.getServisesIds();
         List<ServiceDto> services =  serviceRepository.retrieve(serviseIds)
                 .stream().map(EntityDtoMappers::toDto)
                 .collect(Collectors.toList());
         //TODO gå igjennom dette:


      }

      return setAreaStatus(testUtil.getAllTilesWithRandomStatuses());
   }

   @GET("/Areas")
   @JsonBody
   //TODO denne må hente basert på dashboard:
   public List<AreaDto> getAreas() {
      DashboardDto dashboardDto  =  EntityDtoMappers.toDto(dashboardRepository.retrieve("privatperson"));
      List<String> areaCodes = dashboardDto.getAreasIds();
      return areaRepository.retrieve(areaCodes).stream()
              .map(EntityDtoMappers::toDto)
              .collect(Collectors.toList());

   }

   @POST("/Areas")
   @JsonBody
   public AreaDto newAreas(@JsonBody AreaDto areaDto) {
      try{
         areaRepository.retrieve(areaDto.getId());
         areaDto.setBeskrivelse("Ikke lagt til, id allerede i db!");
         return areaDto;
      }
      catch (IllegalArgumentException e){
         areaRepository.save(EntityDtoMappers.toEntity(areaDto));
         return EntityDtoMappers.toDto(areaRepository.retrieve(areaDto.getId()));
      }
   }

   @DELETE("/Areas")
   @JsonBody
   //TODO denne skal kun fjerne området fra ett dashboard
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
