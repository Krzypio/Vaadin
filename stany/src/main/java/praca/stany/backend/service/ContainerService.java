package praca.stany.backend.service;

import org.springframework.stereotype.Service;
import praca.stany.backend.entity.Container;
import praca.stany.backend.repository.ContainerRepository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ContainerService {
    private static final Logger LOGGER = Logger.getLogger(ContainerService.class.getName());
    private ContainerRepository containerRepository;

    public ContainerService(ContainerRepository containerRepository){
        this.containerRepository = containerRepository;
    }

    public List<Container> findAll(){
        return containerRepository.findAll();
    }

    public List<Container> findAll(String stringFilter){
        if (stringFilter == null || stringFilter.isEmpty()){
            return containerRepository.findAll();
        } else {
            return containerRepository.search(stringFilter);
        }
    }

    public long count(){
        return containerRepository.count();
    }

    public void delete(Container container){
        containerRepository.delete(container);
    }

    public void save(Container container){
        if(container == null){
            LOGGER.log(Level.SEVERE, "Container is null. Are you sure you have connected your form to the application?");
            return;
        }
        containerRepository.save(container);
    }

    @PostConstruct
    public void populateTestData() {
        Container tata = new Container("Tata");
        save(tata);

        Container syn = new Container("syn", tata);
        save(syn);
    }//populateTestData()
}
