
package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.RecordEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.*;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.nav.portal.rest.api.TestUtil;
import no.portal.web.generated.api.*;
import org.actioncontroller.*;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Timestamp;

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
      ArrayList<TileDto> tiles = new ArrayList<>();
      for(AreaDto areaDto: areaDtos){
         List<String> serviseIds = areaDto.getServisesIds();
         List<ServiceDto> services =  serviceRepository.retrieve(serviseIds)
                 .stream().map(EntityDtoMappers::toDto)
                 .collect(Collectors.toList());
         //TODO gå igjennom dette. Hvordan bør dette gjøres?
         for(ServiceDto serviceDto: services){
             Optional<RecordEntity> optRecord = recordRepository.getLatestRecord(serviceDto.getId());
             if(optRecord.isPresent()){
               serviceDto.setStatus(StatusDto.fromValue(optRecord.get().getStatus()));

             }
         }
         TileDto tile = new TileDto();
         tile.setArea(areaDto);
         tile.setServices(services);
         tile.setStatus(getTileStatus(tile));
         tiles.add(tile);
      }

      return tiles;
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

   @POST("/ServiceRecord")
   @JsonBody
   public void uppdateRecord(@JsonBody ServiceDto serviceDto) {
      if(serviceRepository.doesEntryExist(serviceDto.getId())){
         //Servisen er lagret fra før
         //Legger kunn til en ny record. Men skal man sammenligne?
         RecordEntity entity = new RecordEntity(serviceDto.getId(),
                 serviceDto.getStatus().getValue()
                 ,new Timestamp(System.currentTimeMillis()),
                 42);
         recordRepository.save(entity);
      }
      else{
         //Servicen er ikke lagret fra før
         //Dette skal legges inn i ett tjeneste lag
         ServiceEntity entity = new ServiceEntity();
         entity.setId(serviceDto.getId());
         entity.setName(serviceDto.getName());
         entity.setType(serviceDto.getType());
         entity.setTeam(serviceDto.getTeam());
         entity.setDependencies(serviceDto.getDependencies());
         entity.setMonitorlink(serviceDto.getMonitorlink());
         entity.setDescription(serviceDto.getDescription());
         entity.setLogglink(serviceDto.getLogglink());
         serviceRepository.save(entity);
      }
      RecordEntity entity = new RecordEntity(serviceDto.getId(),
              serviceDto.getStatus().getValue()
              ,new Timestamp(System.currentTimeMillis()),
              42);
      recordRepository.save(entity);
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
