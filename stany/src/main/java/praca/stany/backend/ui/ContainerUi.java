package praca.stany.backend.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import praca.stany.backend.entity.Container;
import praca.stany.backend.form.ContainerForm;
import praca.stany.backend.service.ContainerService;

import java.util.*;

@Route("")
@CssImport("./styles/container-styles.css")
public class ContainerUi extends VerticalLayout {
    private ContainerService containerService;
    private TreeGrid<Container> grid = new TreeGrid<>(Container.class);
    //private TextField filterText = new TextField();
    ComboBox<Container> searchBox = new ComboBox<>();
    boolean expanded = false;
    private Button expandButton = new Button("Expand ");

    private ContainerForm form;

    public ContainerUi(ContainerService containerService){
        this.containerService = containerService;
        addClassName("container-view");
        setSizeFull();
        configureGrid();

        searchBox.setItems(containerService.findAll());
        searchBox.setItemLabelGenerator(Container::getHierarchicalName);

        form = new ContainerForm(containerService.findAll());
        form.addListener(ContainerForm.SaveEvent.class, this::saveContainer);
        form.addListener(ContainerForm.DeleteEvent.class, this::deleteContainer);
        form.addListener(ContainerForm.CloseEvent.class, e -> closeEditor());
        closeEditor();

        Div content = new Div(grid, form);
        content.addClassName("content");
        content.setSizeFull();

        add(getToolbar(), content);
        updateList();
    }

    private HorizontalLayout getToolbar() {
        /*filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());*/
        searchBox.setPlaceholder("Search by name...");
        searchBox.setClearButtonVisible(true);
        searchBox.addValueChangeListener(e -> updateBox(searchBox.getValue()));

        //expandButton defined in class
        expandButton.addClickListener(click -> expandCollapse());
        Button addButton = new Button("Add");
        addButton.addClickListener(click -> addContainer());
        Button editButton = new Button ("Edit");
        editButton.addClickListener(click -> editContainer(grid.asSingleSelect().getValue(), false));
        Button copyButton = new Button ("Copy");
        copyButton.addClickListener(click -> copyRoot(grid.asSingleSelect().getValue()));

        expandButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        copyButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        HorizontalLayout toolbar = new HorizontalLayout(/*filterText,*/ searchBox ,expandButton, addButton, editButton, copyButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void copyRoot(Container parentOriginal) {
        Container parentCopy = copyOnlyRoot(parentOriginal);
        Notification.show("Copy " + parentCopy.getName() + " created");
        //kopiowanie zawartości
        for (Container childOriginal : parentOriginal.getChildren()) {
            copyDescendants(childOriginal, parentCopy);
        }
        //Zrob tabele zaleznosci oryginal/kopia dla root, pozniej potomstwa, potomstwa potomstwa itd.
        //tworzenie nazwy nie dziala poprawnie przy kopiowaniu osoby z ojcem
        updateList();
    }

    private void copyDescendants(Container descendantOriginal, Container parentCopy){
        Container descendantCopy = new Container(descendantOriginal.getName(), parentCopy);
        containerService.save(descendantCopy);
        for (Container childOriginal : descendantOriginal.getChildren()) {
            copyDescendants(childOriginal, descendantCopy);
        }
    }

    private Container copyOnlyRoot(Container chosen){
        chosen = containerService.findOne(chosen);  //dzięki temu odświeżamy listę siblings i błąd nie występuje
        //SIBLINGS
        Set<String> siblingsName1 = new HashSet<>();
        siblingsName1.add(chosen.getName());
        if (chosen.getParent() == null){
            for (Container rootContainer : containerService.findAll()) {
                if (rootContainer.getParent() == null)
                    siblingsName1.add(rootContainer.getName());
            }//for
        } else {
            for (Container child: chosen.getParent().getChildren()) {
                siblingsName1.add(child.getName());
            }//for
        }//else
        String newName = chosen.getName() + "_copy";
        int postfix = 0;
        while (siblingsName1.contains(newName+postfix)){
            postfix++;
        }
        //Skopiowanie roota
        Container newOne = new Container(newName+postfix);
        if (chosen.getParent() == null)
            newOne.setParent(null);
        else
            newOne.setParent(chosen.getParent());
        containerService.save(newOne);
        return newOne;
    }


    private void expandCollapse() {
        if(expanded == false){
            grid.expand(containerService.findAll());
            expanded = true;
            expandButton.setText("Collapse");
        } else {
            grid.collapse(containerService.findAll());
            expanded = false;
            expandButton.setText("Expand");
        }
    }

    private void updateBox(Container searchedContainer) {
        if (null == searchedContainer){
            grid.asSingleSelect().setValue(null);
            grid.collapse(containerService.findAll());
        }
        else {
            grid.collapse(containerService.findAll());
            grid.expandRecursively(searchedContainer.getAllAncestors(), searchedContainer.getAncestorsNumber());
            grid.asSingleSelect().setValue(searchedContainer);
            grid.expand(searchedContainer);
        }
    }

    private void addContainer() {
        Container actual = grid.asSingleSelect().getValue();
        editContainer(new Container(null, actual), true);
    }

    private void configureGrid() {
        grid.addClassName("container-grid");
        grid.setSizeFull();

        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        //Tree
        grid.setColumns("name");
        grid.setHierarchyColumn("name");

        //Columns
        grid.addColumn(Container::getSaveDate).setHeader("Data dodania");

        grid.expand(containerService.findAll());
    }

    public void editContainer(Container container, boolean add) {   //add kontrola usuwania parent
        if (container == null) {
            closeEditor();
        } else {

            //Usuwanie potomstwa i container z listy
            List<Container> parentBoxList = containerService.findAll();
            parentBoxList.removeAll(container.getAllDescendants());

            //Kontrola usuwania parent dla edit i nazwa
            if (!add){
                form.setOperationName("Edit");
                parentBoxList.remove(container); //blokuje add
            } else {
                form.setOperationName("Add");
            }

            form.setParentComboBox(parentBoxList);

            form.setContainer(container);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void closeEditor() {
        form.setOperationName("");
        form.setContainer(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void updateList() {
        List<Container> containers = containerService.findAll();
        containers.sort(Comparator.comparing(Container::getHierarchicalName));
        searchBox.setItems(containers);

        //grid.setItems(containerService.findAll(filterText.getValue()));
        form.setParentComboBox(containerService.findAll());

        //Aktualizuje wygląd drzewa ------------------------------------------------------
        List<Container> nodes = getSortedContainers();
        grid.getTreeData().clear();
        nodes.forEach(n -> grid.getTreeData().addItem(n.getParent(), n));
        //grid.setSortableColumns("name");
        grid.getDataProvider().refreshAll();    //zawsze odświeży, omija bugi
    }

    private List<Container> getSortedContainers(){
        List<Container> toSortContainers = containerService.findAll();
        toSortContainers.sort(Comparator.comparing(Container::getName, String::compareToIgnoreCase).thenComparing(Container::getName));
        List<Container> sortedContainers = new LinkedList<>();
        while (!toSortContainers.isEmpty()){
            for (Container cont: toSortContainers) {
                if (cont.getParent() == null || sortedContainers.contains(cont.getParent())){
                    sortedContainers.add(cont);
                }//if
            }//for
            toSortContainers.removeAll(sortedContainers);
        }
        return sortedContainers;
    }

    private void saveContainer(ContainerForm.SaveEvent event) {
        //NAME
        String name = event.getContainer().getName().trim().replaceAll("\\s{2,}", " ");    //trim
        event.getContainer().setName(name);

        boolean isAlphaNumeric = name != null &&
                name.chars().anyMatch(Character::isLetterOrDigit);

        if (!isAlphaNumeric){
            Notification.show("Error: Container must have a name");
            return;
        }

        //SIBLINGS
        Set<String> siblingsName = new HashSet<>();
        if (event.getContainer().getParent() == null){
            for (Container rootContainer : containerService.findAll()) {
                if (rootContainer.getParent() == null)
                    siblingsName.add(rootContainer.getName());
            }//for
        } else {
            //rozwiązanie błędu z edycją kontenerów z parent
            Set<Container> siblings = event.getContainer().getParent().getChildren();
            if (siblings.contains(event.getContainer())) siblings.remove(event.getContainer());
            for (Container child: siblings) {
                siblingsName.add(child.getName());
            }//for
        }//else

        if (siblingsName.contains(event.getContainer().getName())) {
            //Jeśli potomstwo ojca zawiera już gościa o takiej nazwie
            Notification.show("Error: Siblings can not have the same name");
            return;
        }
        containerService.save(event.getContainer());
        updateList();
        //Jeśli się udało to zaznacz edytowany container
        if (containerService.findAll().contains(event.getContainer())) {
            grid.collapse(containerService.findAll());
            grid.expand(event.getContainer().getAllAncestors());
            //grid.asSingleSelect().setValue(event.getContainer().getParent());
            Notification.show("Saved: " + event.getContainer().getHierarchicalName());
        }
        closeEditor();
    }

    private void deleteContainer(ContainerForm.DeleteEvent event) {
        if (!event.getContainer().getChildren().isEmpty()){
            Notification.show("Error: " + event.getContainer().getHierarchicalName() + " have descendants. Remove them first.");
            return;
        }

        containerService.delete(event.getContainer());
        updateList();
        if (!containerService.findAll().contains(event.getContainer())) {
            Notification.show("Deleted: " + event.getContainer().getHierarchicalName());
        }
        closeEditor();
    }
}
