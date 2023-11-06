package no.nav.portal.rest.api.Helpers;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.HelpTextEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.enums.ServiceType;
import nav.portal.core.repositories.HelpTextRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.nav.portal.rest.api.v3.controllers.HelpTextController;
import no.portal.web.generated.api.*;
import org.actioncontroller.DELETE;
import org.actioncontroller.HttpNotFoundException;
import org.actioncontroller.HttpRequestException;
import org.actioncontroller.PathParam;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.*;
import java.util.stream.Collectors;

public class HelpTextControllerHelper {
    private final HelpTextRepository helpTextRepository;
    Comparator<HelpTextDto> helpTextNrComparator
            = Comparator.comparing(HelpTextDto::getNumber);

    public HelpTextControllerHelper(DbContext context){
        this.helpTextRepository = new HelpTextRepository(context);
    }

    public HelpTextDto save(HelpTextDto helpTextDto){
        Optional<HelpTextEntity> result = helpTextRepository.save(EntityDtoMappers.toHelpTextEntity(helpTextDto));
        assert result.orElse(null) != null;
        return  EntityDtoMappers.toHelpTextDto(result.orElse(null));
    }

    public void update(HelpTextDto helpTextDto){
        HelpTextEntity helpTextEntity = EntityDtoMappers.toHelpTextEntity(helpTextDto);
        helpTextRepository.update(helpTextEntity);
    }

    public void delete(HelpTextDto helpTextDto){
        HelpTextEntity helpTextEntity = EntityDtoMappers.toHelpTextEntity(helpTextDto);
        helpTextRepository.delete(helpTextEntity);
    }

    public HelpTextDto retrieveOneHelpText(int helptext_number, ServiceTypeDto serviceTypeDto) {
        HelpTextEntity helpTextEntity = EntityDtoMappers.toHelpTextEntity(helptext_number, serviceTypeDto);
        return EntityDtoMappers.toHelpTextDtoOptional(helpTextRepository.retrieve(helpTextEntity.getNumber(),helpTextEntity.getType()));
    }

    public List<HelpTextDto> getAllServices() {
        List<HelpTextDto> result = helpTextRepository.retrieveAllServices()
                .stream().map(EntityDtoMappers::toHelpTextDto)
                .collect(Collectors.toList());
        return result.stream().sorted(helpTextNrComparator)
                .collect(Collectors.toList());
    }

    public List<HelpTextDto> getAllComponents() {
        List<HelpTextDto> result = helpTextRepository.retrieveAllComponents()
                .stream().map(EntityDtoMappers::toHelpTextDto)
                .collect(Collectors.toList());
        return result.stream().sorted(helpTextNrComparator)
                .collect(Collectors.toList());
    }

    public List<HelpTextDto> getAllHelpTexts(){
        List<HelpTextDto> result = helpTextRepository.retrieveAllHelpTexts()
                .stream().map(EntityDtoMappers::toHelpTextDto)
                .collect(Collectors.toList());
        return result.stream().sorted(helpTextNrComparator)
                .collect(Collectors.toList());
    }


}
