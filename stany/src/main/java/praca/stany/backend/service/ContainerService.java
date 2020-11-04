package praca.stany.backend.service;

import com.vaadin.flow.component.notification.Notification;
import org.springframework.stereotype.Service;
import praca.stany.backend.entity.Container;
import praca.stany.backend.repository.ContainerRepository;

import javax.annotation.PostConstruct;
import java.util.*;
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

    public Container findOne(Container choosen) {
        for (Container container: this.findAll()) {
            if (choosen.getId() == container.getId())
                return container;
        }//for
        return null;
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
        //Cyklicznosc grafu wykluczona
        if (container.getAllDescendants().contains(container.getParent())){
            LOGGER.log(Level.SEVERE, "Container parent is between his descendants. Cycles in graph are forbidden.");
            System.out.println();
            Notification.show("Container parent is between his descendants. Cycles in graph are forbidden.");
            return;
        }
        if (container==container.getParent()){
            LOGGER.log(Level.SEVERE, "Container itself and its parent are the same. Cycles in graph are forbidden.");
            System.out.println("Container itself and its parent are the same. Cycles in graph are forbidden.");
            Notification.show("Container parent is between his descendants. Cycles in graph are forbidden.");
            return;
        }

        if(container == null){
            LOGGER.log(Level.SEVERE, "Container is null. Are you sure you have connected your form to the application?");
            return;
        }
        containerRepository.save(container);
    }

    @PostConstruct
    public void populateTestData() {

        if (containerRepository.count() == 0) {

            Container wzorNp460 = new Container("wzor_460R");
            save(wzorNp460);

            Container st0 = new Container("st 0", wzorNp460);
            Container st1 = new Container("st I", wzorNp460);
            Container st2 = new Container("st II", wzorNp460);
            Container st3 = new Container("st III", wzorNp460);
            Container st4 = new Container("st IV", wzorNp460);
            Container st5 = new Container("st IV", wzorNp460);

            Container st0_noz = new Container("nóż ucinający", st0);
            Container st0_tuleja = new Container("tuleja tnąca", st0);

            List<Container> stanowiska = Arrays.asList(st0, st1, st2, st3, st4, st5);
            List<Container> narzedzia = Arrays.asList(st0_tuleja, st0_noz);
            containerRepository.saveAll(stanowiska);
            containerRepository.saveAll(narzedzia);
        }//if
    }//populateTestData()
}
