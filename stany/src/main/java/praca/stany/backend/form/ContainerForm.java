package praca.stany.backend.form;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;
import org.hibernate.event.spi.DeleteEvent;
import praca.stany.backend.entity.Container;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ContainerForm extends FormLayout {
    private Container container;

    Label operation = new Label();
    TextField name = new TextField("name");
    ComboBox<Container> parent = new ComboBox<>("Parent");

    Button save = new Button("Save");
    Button delete = new Button("Delete");
    Button close = new Button("Cancel");

    Binder<Container> binder = new BeanValidationBinder<>(Container.class);

    public ContainerForm(List<Container> containers) {
        addClassName("container-form");
        binder.bindInstanceFields(this);

        parent.setClearButtonVisible(true);
        parent.setItems(containers);
        parent.setItemLabelGenerator(Container::getHierarchicalName);

        add(operation, name,
                parent,
                createButtonsLayout());
    }

    public void setContainer(Container container) {
        this.container = container;
        binder.readBean(container);
    }

    public void setParentComboBox(List<Container> containers){
        //Sortowanie
        containers.sort(Comparator.comparing(Container::getName));
        parent.setItems(containers);
    }

    private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        close.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, container)));
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));
        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));

        return new HorizontalLayout(save, delete, close);
    }

    public void setOperationName(String operation) {
        this.operation.setText(operation);
    }

    private void validateAndSave() {
        try {
            binder.writeBean(container);
            fireEvent(new SaveEvent(this, container));
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    // Events
    public static abstract class ContactFormEvent extends ComponentEvent<ContainerForm> {
        private Container container;

        protected ContactFormEvent(ContainerForm source, Container container) {
            super(source, false);
            this.container = container;
        }

        public Container getContainer() {
            return container;
        }
    }

    public static class SaveEvent extends ContactFormEvent {
        SaveEvent(ContainerForm source, Container container) {
            super(source, container);
        }
    }

    public static class DeleteEvent extends ContactFormEvent {
        DeleteEvent(ContainerForm source, Container container) {
            super(source, container);
        }

    }

    public static class CloseEvent extends ContactFormEvent {
        CloseEvent(ContainerForm source) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}