package praca.stany.backend.service;

import com.vaadin.flow.component.notification.Notification;
import org.springframework.stereotype.Service;
import praca.stany.backend.entity.Container;
import praca.stany.backend.entity.Tool;
import praca.stany.backend.repository.ContainerRepository;
import praca.stany.backend.repository.ToolRepository;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ToolService {
    private static final Logger LOGGER = Logger.getLogger(ToolService.class.getName());
    private ToolRepository toolRepository;

    public ToolService(ToolRepository toolRepository){
        this.toolRepository = toolRepository;
    }

    public List<Tool> findAll(){
        return toolRepository.findAll();
    }

    public Tool findOne(Tool choosen) {
        for (Tool tool: this.findAll()) {
            if (choosen.getId() == tool.getId())
                return tool;
        }//for
        return null;
    }

    public List<Tool> findAll(String stringFilter){
        if (stringFilter == null || stringFilter.isEmpty()){
            return toolRepository.findAll();
        } else {
            return toolRepository.search(stringFilter);
        }
    }

    public long count(){
        return toolRepository.count();
    }

    public void delete(Tool tool){
        toolRepository.delete(tool);
    }

    public void save(Tool tool){
        //Cyklicznosc grafu wykluczona
        if (tool.getAllDescendants().contains(tool.getParent())){
            LOGGER.log(Level.SEVERE, "Tool parent is between his descendants. Cycles in graph are forbidden.");
            System.out.println();
            Notification.show("Tool parent is between his descendants. Cycles in graph are forbidden.");
            return;
        }
        if (tool==tool.getParent()){
            LOGGER.log(Level.SEVERE, "Tool itself and its parent are the same. Cycles in graph are forbidden.");
            System.out.println("Tool itself and its parent are the same. Cycles in graph are forbidden.");
            Notification.show("Tool parent is between his descendants. Cycles in graph are forbidden.");
            return;
        }

        if(tool == null){
            LOGGER.log(Level.SEVERE, "Tool is null. Are you sure you have connected your form to the application?");
            return;
        }
        toolRepository.save(tool);
    }

    @PostConstruct
    public void populateTestData() {

        if (toolRepository.count() == 0) {

            Tool np460 = new Tool("NP-460R");

            Tool noz = new Tool("noz", np460);
            Tool tuleja = new Tool("tuleja", np460);
            Tool stempel = new Tool("stempel", np460);
            Tool gniazdo = new Tool("gniazdo", np460);
            Tool lapki = new Tool("lapki", np460);
            Tool wkladka = new Tool("wkladka", np460);
            Tool wypychacz = new Tool("wypychacz", np460);

            Tool noz_1 = new Tool("LKn-6A-5-1", noz);
            Tool tuleja_1 = new Tool("LKt-6-5A-32", tuleja);
            Tool stempel_1 = new Tool("LKs-6-1-1", stempel);
            Tool gniazdo_1 = new Tool("LKm-6-1-2-20", gniazdo);
            Tool lapki_1 = new Tool("LKy-6-1-1", lapki);
            Tool wkladka_1 = new Tool("LKm-6-1-1-15", wkladka);
            Tool wypychacz_1 = new Tool("LKw-6-1-4", wypychacz);

            List<Tool> maszyna = Arrays.asList(np460);
            List<Tool> kategoria = Arrays.asList(noz, tuleja, stempel, gniazdo, lapki, wkladka, wypychacz);
            List<Tool> narzedzia = Arrays.asList(noz_1, tuleja_1, stempel_1, gniazdo_1, lapki_1, wkladka_1, wypychacz_1);

            toolRepository.saveAll(maszyna);
            toolRepository.saveAll(kategoria);
            toolRepository.saveAll(narzedzia);
        }//if
    }//populateTestData()
}
