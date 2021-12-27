package com.cloudapplicationmanager.view;

import com.cloudapplicationmanager.model.Domain;
import com.cloudapplicationmanager.model.Environment;
import com.cloudapplicationmanager.model.Service;
import com.cloudapplicationmanager.repository.DomainRepository;
import com.cloudapplicationmanager.repository.EnvironmentRepository;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Note this is not a spring managed component but rather needs to be set up by its parent component. Setting
 * this up as a @SpringComponent causes odd JavaScript and other errors that are related to lifecycle and scope
 * management.
 */
public class EnvironmentForm extends FormLayout {

    private static Logger logger = LoggerFactory.getLogger(EnvironmentForm.class);

    //Environment form fields
    TextField name = new TextField("Name");
    TextField description = new TextField("Description");
    TextField subdomain = new TextField("Subdomain");
    TextField urlPath = new TextField("URL Path");
    Select<Domain> domain = new Select<>();
    Checkbox healthCheckActive = new Checkbox("Health check active");

    //Other elements that I'll need
    H3 formTitle = new H3("New Environment");
    Button save = new Button("Save");
    Button cancel = new Button("Cancel");

    // The parent dialog of this form. Ideally this class wouldn't need that but it has to know how to close itself
    Dialog dialog;

    //Parent ServiceView object
    private ServiceView serviceView;

    //Validation and form binder service
    private BeanValidationBinder<Environment> binder = new BeanValidationBinder<>(Environment.class);

    //Environment object to use for the form and binding
    private Environment environment;

    //Will need these to save and populate the environments
    private EnvironmentRepository environmentRepository;
    private DomainRepository domainRepository;

    //The service this environment will be part of
    private Service service;

    /**
     * Constructor for creating a new environment form
     * @param environmentRepository
     * @param domainRepository
     * @param dialog
     */
    public EnvironmentForm(EnvironmentRepository environmentRepository, DomainRepository domainRepository, Service service, Dialog dialog, ServiceView serviceView) {
        this(environmentRepository, domainRepository, service, serviceView, dialog, 0);
    }

    /**
     * Constructor for populating an environment (or creating a new one if ID 0 is used)
     * @param environmentRepository
     * @param domainRepository
     * @param dialog
     * @param environmentId
     */
    public EnvironmentForm(EnvironmentRepository environmentRepository, DomainRepository domainRepository, Service service,
                           ServiceView serviceView, Dialog dialog, long environmentId) {
        this.environmentRepository = environmentRepository;
        this.domainRepository= domainRepository;
        this.service = service;
        this.serviceView = serviceView;
        this.dialog = dialog;

        //Look up the environment if needed or create a blank one if needed
        if (environmentId == 0) {
            environment = new Environment();
        } else {
            Optional<Environment> environmentOption = environmentRepository.findById(environmentId);

            if (environmentOption.isPresent()) {
                environment = environmentOption.get();
            } else {
                //TODO This error would be unexpected, but should be handled in a user-friendly way
                logger.error("Could not find environment with id [{}]", environmentId);
            }
        }

        configureForm();
    }

    public void configureForm() {

        //Set up the domain selector
        domain.setLabel("Domain");
        domain.setPlaceholder("Choose one");
        domain.setItemLabelGenerator(Domain::getName);
        domain.setItems(domainRepository.findAll());
        domain.setValue(domainRepository.findById(1l).get());

        //Set up bindings
        binder.bindInstanceFields(this);
        binder.setBean(environment);

        //Construct the rest of the form
        createEnvironmentForm();
    }

    private void createEnvironmentForm() {

        //Set up the form title
        //H3 formTitle = new H3("New Environment");
        this.setColspan(formTitle, 2);

        //Make sure this checkbox is on its own line
        //this.setColspan(healthCheckActive, 2);

        //Add all the components
        this.add(formTitle, name, description, subdomain, urlPath, healthCheckActive, domain, createButtons());
    }

    private HorizontalLayout createButtons() {
        //Buttons
        save.addClickListener(event -> {
            try {

                //Write this back to the binder object
                binder.writeBean(environment);

                //Add the service to the environment
                environment.setService(service);

                logger.debug("Saving environment [{}]", environment.getName());

                //Save this to the database
                environmentRepository.save(environment);

                //Update the calling page
                addListener(EnvironmentSaveEvent.class, e -> logger.debug("Environment save event fired IN ENV CLASS"));
                fireEvent(new EnvironmentSaveEvent(this, false));

                serviceView.updateService();

                //Close the dialog and update the items in the list
                dialog.close();

            } catch (ValidationException e) {

                //TODO make sure the validation errors are handled sensibly for clients
                logger.warn("Validation error saving environment form");
            }
        });

        cancel.addClickListener(event -> {
            binder.readBean(null);

            dialog.close();
        });

        HorizontalLayout actions = new HorizontalLayout();
        actions.add(save, cancel);
        save.getStyle().set("marginRight", "10px");
        save.getStyle().set("marginTop", "15px");
        cancel.getStyle().set("marginTop", "15px");

        return actions;
    }

    public static class EnvironmentSaveEvent extends ComponentEvent<EnvironmentForm> {
        public EnvironmentSaveEvent(EnvironmentForm source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}

