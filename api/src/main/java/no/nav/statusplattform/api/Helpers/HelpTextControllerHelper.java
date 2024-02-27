package no.nav.statusplattform.api.Helpers;

import nav.statusplattform.core.entities.HelpTextEntity;
import nav.statusplattform.core.enums.ServiceType;
import nav.statusplattform.core.repositories.HelpTextRepository;
import no.nav.statusplattform.api.EntityDtoMappers;
import no.nav.statusplattform.generated.api.HelpTextDto;
import no.nav.statusplattform.generated.api.ServiceTypeDto;
import org.fluentjdbc.DbContext;

import java.util.Comparator;
import java.util.List;
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
        return EntityDtoMappers.toHelpTextDto(helpTextRepository.retrieve(helptext_number, ServiceType.valueOf(serviceTypeDto.getValue())));
    }

    public List<HelpTextDto> getHelpTextServices() {
        return helpTextRepository.retrieveHelpTextServices()
                .stream().map(EntityDtoMappers::toHelpTextDto)
                .sorted(helpTextComparator)
                .toList();
    }

    public List<HelpTextDto> getHelpTextComponents() {
       return helpTextRepository.retrieveHelpTextComponents()
                .stream().map(EntityDtoMappers::toHelpTextDto)
                .sorted(helpTextComparator)
                .toList();
    }

    public List<HelpTextDto> getAllHelpTexts(){
        return helpTextRepository.retrieveAllHelpTexts()
                .stream().map(EntityDtoMappers::toHelpTextDto)
                .sorted(helpTextComparator)
                .collect(Collectors.toList());
    }


}
