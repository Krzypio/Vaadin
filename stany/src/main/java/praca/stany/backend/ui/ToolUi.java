package praca.stany.backend.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.Route;
import praca.stany.backend.entity.Container;
import praca.stany.backend.entity.Tool;
import praca.stany.backend.form.ContainerForm;
import praca.stany.backend.form.ToolForm;
import praca.stany.backend.service.ContainerService;
import praca.stany.backend.service.ToolService;

import java.util.*;

@Route("tool")
@CssImport("./styles/tool-styles.css")
public class ToolUi extends VerticalLayout {
    private ToolService toolService;
    private TreeGrid<Tool> grid = new TreeGrid<>(Tool.class);
    //private TextField filterText = new TextField();
    ComboBox<Tool> searchBox = new ComboBox<>();
    boolean expanded = false;
    private Button expandButton = new Button("Expand ");

    private ToolForm form;

    public ToolUi(ToolService toolService){
        this.toolService = toolService;
        addClassName("tool-view");
        setSizeFull();
        configureGrid();

        searchBox.setItems(toolService.findAll());
        searchBox.setItemLabelGenerator(Tool::getHierarchicalName);

        form = new ToolForm(toolService.findAll());
        form.addListener(ToolForm.SaveEvent.class, this::saveTool);
        form.addListener(ToolForm.DeleteEvent.class, this::deleteTool);
        form.addListener(ToolForm.CloseEvent.class, e -> closeEditor());
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
        editButton.addClickListener(click -> editTool(grid.asSingleSelect().getValue(), false));
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

    private void copyRoot(Tool parentOriginal) {
        Tool parentCopy = copyOnlyRoot(parentOriginal);
        Notification.show("Copy " + parentCopy.getName() + " created");
        //kopiowanie zawartości
        for (Tool childOriginal : parentOriginal.getChildren()) {
            copyDescendants(childOriginal, parentCopy);
        }
        //Zrob tabele zaleznosci oryginal/kopia dla root, pozniej potomstwa, potomstwa potomstwa itd.
        //tworzenie nazwy nie dziala poprawnie przy kopiowaniu osoby z ojcem
        updateList();
    }

    private void copyDescendants(Tool descendantOriginal, Tool parentCopy){
        Tool descendantCopy = new Tool(descendantOriginal.getName(), parentCopy);
        toolService.save(descendantCopy);
        for (Tool childOriginal : descendantOriginal.getChildren()) {
            copyDescendants(childOriginal, descendantCopy);
        }
    }

    private Tool copyOnlyRoot(Tool chosen){
        chosen = toolService.findOne(chosen);  //dzięki temu odświeżamy listę siblings i błąd nie występuje
        //SIBLINGS
        Set<String> siblingsName1 = new HashSet<>();
        siblingsName1.add(chosen.getName());
        if (chosen.getParent() == null){
            for (Tool rootContainer : toolService.findAll()) {
                if (rootContainer.getParent() == null)
                    siblingsName1.add(rootContainer.getName());
            }//for
        } else {
            for (Tool child: chosen.getParent().getChildren()) {
                siblingsName1.add(child.getName());
            }//for
        }//else
        String newName = chosen.getName() + "_copy";
        int postfix = 0;
        while (siblingsName1.contains(newName+postfix)){
            postfix++;
        }
        //Skopiowanie roota
        Tool newOne = new Tool(newName+postfix);
        if (chosen.getParent() == null)
            newOne.setParent(null);
        else
            newOne.setParent(chosen.getParent());
        toolService.save(newOne);
        return newOne;
    }


    private void expandCollapse() {
        if(expanded == false){
            grid.expand(toolService.findAll());
            expanded = true;
            expandButton.setText("Collapse");
        } else {
            grid.collapse(toolService.findAll());
            expanded = false;
            expandButton.setText("Expand");
        }
    }

    private void updateBox(Tool searchedTool) {
        if (null == searchedTool){
            grid.asSingleSelect().setValue(null);
            grid.collapse(toolService.findAll());
        }
        else {
            grid.collapse(toolService.findAll());
            grid.expandRecursively(searchedTool.getAllAncestors(), searchedTool.getAncestorsNumber());
            grid.asSingleSelect().setValue(searchedTool);
            grid.expand(searchedTool);
        }
    }

    private void addContainer() {
        Tool actual = grid.asSingleSelect().getValue();
        editTool(new Tool(null, actual), true);
    }

    private void configureGrid() {
        grid.addClassName("tool-grid");
        grid.setSizeFull();

        //grid.removeColumnByKey("company");
        //grid.setColumns("firstName", "lastName", "email", "status");
        //grid.addColumn(contact -> {
        //    Company company = contact.getCompany();
        //  return company == null ? "-" : company.getName();
        //}).setHeader("Company");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        //grid.asSingleSelect().addValueChangeListener(event ->
        //        editContainer(event.getValue()));

        //Tree
        grid.setColumns("name");
        grid.setHierarchyColumn("name");

        //Columns
        grid.addColumn(Tool::getSaveDate).setHeader("Data dodania");

        grid.expand(toolService.findAll());
    }

    public void editTool(Tool tool, boolean add) {   //add kontrola usuwania parent
        if (tool == null) {
            closeEditor();
        } else {

            //Usuwanie potomstwa i container z listy
            List<Tool> parentBoxList = toolService.findAll();
            parentBoxList.removeAll(tool.getAllDescendants());

            //Kontrola usuwania parent dla edit i nazwa
            if (!add){
                form.setOperationName("Edit");
                parentBoxList.remove(tool); //blokuje add
            } else {
                form.setOperationName("Add");
            }

            form.setParentComboBox(parentBoxList);

            form.setContainer(tool);
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
        List<Tool> tools = toolService.findAll();
        tools.sort(Comparator.comparing(Tool::getHierarchicalName));
        searchBox.setItems(tools);

        //grid.setItems(containerService.findAll(filterText.getValue()));
        form.setParentComboBox(toolService.findAll());

        //Aktualizuje wygląd drzewa ------------------------------------------------------
        List<Tool> nodes = getSortedTools();
        grid.getTreeData().clear();
        nodes.forEach(n -> grid.getTreeData().addItem(n.getParent(), n));
        //grid.setSortableColumns("name");
        grid.getDataProvider().refreshAll();    //zawsze odświeży, omija bugi
    }

    private List<Tool> getSortedTools(){
        List<Tool> toSortTools = toolService.findAll();
        toSortTools.sort(Comparator.comparing(Tool::getName, String::compareToIgnoreCase).thenComparing(Tool::getName));
        List<Tool> sortedTools = new LinkedList<>();
        while (!toSortTools.isEmpty()){
            for (Tool too: toSortTools) {
                if (too.getParent() == null || sortedTools.contains(too.getParent())){
                    sortedTools.add(too);
                }//if
            }//for
            toSortTools.removeAll(sortedTools);
        }
        return sortedTools;
    }

    private void saveTool(ToolForm.SaveEvent event) {
        //NAME
        String name = event.getTool().getName().trim().replaceAll("\\s{2,}", " ");    //trim
        event.getTool().setName(name);

        boolean isAlphaNumeric = name != null &&
                name.chars().anyMatch(Character::isLetterOrDigit);

        if (!isAlphaNumeric){
            Notification.show("Error: Tool must have a name");
            return;
        }

        //SIBLINGS
        Set<String> siblingsName = new HashSet<>();
        if (event.getTool().getParent() == null){
            for (Tool rootTool : toolService.findAll()) {
                if (rootTool.getParent() == null)
                    siblingsName.add(rootTool.getName());
            }//for
        } else {
            //rozwiązanie błędu z edycją kontenerów z parent
            Set<Tool> siblings = event.getTool().getParent().getChildren();
            if (siblings.contains(event.getTool())) siblings.remove(event.getTool());
            for (Tool child: siblings) {
                siblingsName.add(child.getName());
            }//for
        }//else

        if (siblingsName.contains(event.getTool().getName())) {
            //Jeśli potomstwo ojca zawiera już gościa o takiej nazwie
            Notification.show("Error: Siblings can not have the same name");
            return;
        }
        toolService.save(event.getTool());
        updateList();
        //Jeśli się udało to zaznacz edytowany container
        if (toolService.findAll().contains(event.getTool())) {
            grid.collapse(toolService.findAll());
            grid.expand(event.getTool().getAllAncestors());
            //grid.asSingleSelect().setValue(event.getContainer().getParent());
            Notification.show("Saved: " + event.getTool().getHierarchicalName());
        }
        closeEditor();
    }

    private void deleteTool(ToolForm.DeleteEvent event) {
        if (!event.getTool().getChildren().isEmpty()){
            Notification.show("Error: " + event.getTool().getHierarchicalName() + " have descendants. Remove them first.");
            return;
        }

        toolService.delete(event.getTool());
        updateList();
        if (!toolService.findAll().contains(event.getTool())) {
            Notification.show("Deleted: " + event.getTool().getHierarchicalName());
        }
        closeEditor();
    }
}
