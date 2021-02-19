package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.ExampleEntity;

import nav.portal.core.repositories.ExampleRepository;

import no.portal.web.generated.api.ExampleDataDto;
import org.actioncontroller.*;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.UUID;

public class ExampleController {

   private final ExampleRepository exampleRepo;


   public ExampleController(DbContext dbContext) {
      this.exampleRepo = new ExampleRepository(dbContext);
   }



   @GET("/example/:exampleUid")
   @JsonBody
   public ExampleDataDto getData(
         @PathParam("roleUid") UUID uid) {
      return toExampleDto(exampleRepo.retrieve(uid));
   }


   public static ExampleDataDto toExampleDto(ExampleEntity entity) {
      return new ExampleDataDto()
            .uid(entity.getUid().toString())
            .code(entity.getCode());
   }

}
