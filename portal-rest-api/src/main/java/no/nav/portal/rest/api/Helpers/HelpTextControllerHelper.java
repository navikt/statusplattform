package no.nav.portal.rest.api.Helpers;
import nav.portal.core.entities.HelpTextEntity;
import nav.portal.core.repositories.HelpTextRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.*;
import org.fluentjdbc.DbContext;

import java.util.*;
import java.util.stream.Collectors;

public class HelpTextControllerHelper {
    private final HelpTextRepository helpTextRepository;
    Comparator<HelpTextDto> helpTextComparator
            = Comparator.comparing(HelpTextDto::getType)
            .thenComparing(HelpTextDto::getNumber);

    public HelpTextControllerHelper(DbContext context){
        this.helpTextRepository = new HelpTextRepository(context);
    }

    public HelpTextDto save(HelpTextDto helpTextDto){
        HelpTextEntity result = helpTextRepository.save(EntityDtoMappers.toHelpTextEntity(helpTextDto));
        return  EntityDtoMappers.toHelpTextDto(result);
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
        return EntityDtoMappers.toHelpTextDto(helpTextRepository.retrieve(helpTextEntity.getNumber(),helpTextEntity.getType()));
    }

    public List<HelpTextDto> getHelpTextServices() {
        List<HelpTextDto> result = helpTextRepository.retrieveHelpTextServices()
                .stream().map(EntityDtoMappers::toHelpTextDto)
                .collect(Collectors.toList());
        return result.stream().sorted(helpTextComparator)
                .collect(Collectors.toList());
    }

    public List<HelpTextDto> getHelpTextComponents() {
        List<HelpTextDto> result = helpTextRepository.retrieveHelpTextComponents()
                .stream().map(EntityDtoMappers::toHelpTextDto)
                .collect(Collectors.toList());
        return result.stream().sorted(helpTextComparator)
                .collect(Collectors.toList());
    }

    public List<HelpTextDto> getAllHelpTexts(){
        List<HelpTextDto> result = helpTextRepository.retrieveAllHelpTexts()
                .stream().map(EntityDtoMappers::toHelpTextDto)
                .collect(Collectors.toList());
        return result.stream().sorted(helpTextComparator)
                .collect(Collectors.toList());
    }


}
