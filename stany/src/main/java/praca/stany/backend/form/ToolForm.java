package praca.stany.backend.form;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import praca.stany.backend.entity.Tool;

import java.util.Comparator;
import java.util.List;

public class ToolForm extends FormLayout {
    private Tool tool;

    Label operation = new Label();
    TextField name = new TextField("name");
    ComboBox<Tool> parent = new ComboBox<>("Parent");

    Button save = new Button("Save");
    Button delete = new Button("Delete");
    Button close = new Button("Cancel");

    Binder<Tool> binder = new BeanValidationBinder<>(Tool.class);

    public ToolForm(List<Tool> tools) {
        addClassName("container-form");
        binder.bindInstanceFields(this);

        parent.setClearButtonVisible(true);
        parent.setItems(tools);
        parent.setItemLabelGenerator(Tool::getHierarchicalName);

        add(operation, name,
                parent,
                createButtonsLayout());
    }

    public void setContainer(Tool tool) {
        this.tool = tool;
        binder.readBean(tool);
    }

    public void setParentComboBox(List<Tool> tools){
        //Sortowanie
        tools.sort(Comparator.comparing(Tool::getHierarchicalName));
        parent.setItems(tools);
    }

    private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        close.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, tool)));
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));
        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));

        return new HorizontalLayout(save, delete, close);
    }

    public void setOperationName(String operation) {
        this.operation.setText(operation);
    }

    private void validateAndSave() {
        try {
            binder.writeBean(tool);
            fireEvent(new SaveEvent(this, tool));
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    // Events
    public static abstract class ContactFormEvent extends ComponentEvent<ToolForm> {
        private Tool tool;

        protected ContactFormEvent(ToolForm source, Tool tool) {
            super(source, false);
            this.tool = tool;
        }

        public Tool getTool() {
            return tool;
        }
    }

    public static class SaveEvent extends ContactFormEvent {
        SaveEvent(ToolForm source, Tool tool) {
            super(source, tool);
        }
    }

    public static class DeleteEvent extends ContactFormEvent {
        DeleteEvent(ToolForm source, Tool tool) {
            super(source, tool);
        }

    }

    public static class CloseEvent extends ContactFormEvent {
        CloseEvent(ToolForm source) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}