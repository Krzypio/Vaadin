package praca.stany.backend.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import praca.stany.backend.entity.Container;
import praca.stany.backend.form.ContainerForm;
import praca.stany.backend.service.ContainerService;

import java.util.List;

@Route("")
@CssImport("./styles/container-styles.css")
public class ContainerUi extends VerticalLayout {
    private ContainerService containerService;
    private TreeGrid<Container> grid = new TreeGrid<>(Container.class);
    private TextField filterText = new TextField();
    private ContainerForm form;

    public ContainerUi(ContainerService containerService){
        this.containerService = containerService;
        addClassName("container-view");
        setSizeFull();
        configureGrid();

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
        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        Button addButton = new Button("Add");
        addButton.addClickListener(click -> addContainer());
        Button editButton = new Button ("Edit");
        editButton.addClickListener(click -> editContainer(grid.asSingleSelect().getValue(), false));

        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addButton, editButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void addContainer() {
        Container actual = grid.asSingleSelect().getValue();
        grid.asSingleSelect().clear();
        editContainer(new Container(null, actual), true);
    }

    private void configureGrid() {
        grid.addClassName("container-grid");
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
        grid.expand(containerService.findAll());
    }

    public void editContainer(Container container, boolean add) {   //add kontrola usuwania parent
        if (container == null) {
            closeEditor();
        } else {
            //Usuwanie potomstwa i container z listy
            List<Container> parentBoxList = containerService.findAll();
            parentBoxList.removeAll(container.getAllDescendants());

            //Kontrola usuwania parent dla edit
            if (!add) parentBoxList.remove(container); //blokuje add

            form.setParentComboBox(parentBoxList);

            form.setContainer(container);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void closeEditor() {
        form.setContainer(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void updateList() {
        //grid.setItems(containerService.findAll(filterText.getValue()));
        form.setParentComboBox(containerService.findAll());
        //Tree
        List<Container> nodes = containerService.findAll();
        grid.getTreeData().clear();
        nodes.forEach(n -> grid.getTreeData().addItem(n.getParent(), n));
        grid.expand(nodes);
        grid.getDataProvider().refreshAll();    //zawsze odświeży, omija bugi
    }

    private void saveContainer(ContainerForm.SaveEvent event) {
        containerService.save(event.getContainer());
        updateList();
        closeEditor();
    }

    private void deleteContainer(ContainerForm.DeleteEvent event) {
        containerService.delete(event.getContainer());
        updateList();
        closeEditor();
    }
}
