package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.ExampleEntity;

import nav.portal.core.repositories.ExampleRepository;

import no.nav.portal.rest.api.TestUtil;
import no.portal.web.generated.api.AreaDto;
import org.actioncontroller.*;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.List;
import java.util.UUID;

public class AreaController {

   private final ExampleRepository exampleRepo;


   public AreaController(DbContext dbContext) {
      this.exampleRepo = new ExampleRepository(dbContext);
   }


   @GET("/testAreas")
   @JsonBody
   public List<AreaDto> getTestData() {
      return TestUtil.getAllAreasWithRandomStatuses();
   }

}
