package com.cloudapplicationmanager.view;

import com.cloudapplicationmanager.model.Service;
import com.cloudapplicationmanager.repository.ServiceRepository;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

@Route(value = "service", layout = MainView.class)
@PageTitle("Service")
public class ServiceView extends VerticalLayout implements HasUrlParameter<Long> {

    private static Logger logger = LoggerFactory.getLogger(ServiceView.class);

    private Service service;
    private ServiceRepository serviceRepository;

    public ServiceView(@Autowired ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    /**
     * Method used to start of the page rendering because it has the ID parameter
     * @param event
     * @param parameter
     */
    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        logger.debug("Getting service with ID [{}] to edit", parameter);

        Optional<Service> optionalService = serviceRepository.findById(parameter);
        if (optionalService.isPresent()) {
            this.service = optionalService.get();
        } else {
            //TODO handle this error gracefully
            logger.error("Could not find Service with ID [{}]", parameter);
        }

        logger.debug("Service name is: [{}]", service.getName());

        //Now that we have the correct parameter, call the code that will create this page
        createFormLayout();
    }

   /* public void createPage() {
        add(formLayout(), buttonLayout());
    }*/

    public void createFormLayout() {
        FormLayout layoutWithBinder = new FormLayout();

        //This should use the same validation as the JPA model object
        Binder<Service> binder = new BeanValidationBinder<>(Service.class);

        //Name field
        TextField nameField = new TextField();
        nameField.setRequiredIndicatorVisible(true);
        binder.bind(nameField, Service::getName, Service::setName);
        layoutWithBinder.addFormItem(nameField, "Service Name");

        //Description field
        TextField descriptionField = new TextField();
        binder.bind(descriptionField, Service::getDescription, Service::setDescription);
        layoutWithBinder.addFormItem(descriptionField, "Description");         

        //Health check scheme
        TextField healthCheckSchemeField = new TextField();
        binder.bind(healthCheckSchemeField, Service::getHealthCheckScheme, Service::setHealthCheckScheme);
        layoutWithBinder.addFormItem(healthCheckSchemeField, "Health Check Scheme");

        //Health check port
        //TODO handle validation for the integer/string conversion with a sensible error display
        TextField healthCheckPortField = new TextField();
        binder.forField(healthCheckPortField).withConverter(new StringToIntegerConverter("Please enter a number"))
                .withNullRepresentation(0)
                .bind(Service::getHealthCheckPort, Service::setHealthCheckPort);
        layoutWithBinder.addFormItem(healthCheckPortField, "Health Check Port");

        //Health check path
        TextField healthCheckPathField= new TextField();
        binder.bind(healthCheckPathField, Service::getHealthCheckPath, Service::setHealthCheckPath);
        layoutWithBinder.addFormItem(healthCheckPathField, "Health Check Path");

        //Read the bean into the bound fields
        binder.readBean(service);

        //Add everything to this layout
        add(layoutWithBinder, createButtons(binder));
    }

    private HorizontalLayout createButtons(Binder binder) {
        //Buttons
        Button save = new Button("Save");
        Button cancel = new Button("Cancel");
        save.addClickListener(event -> {
            try {
                logger.debug("Saving service [{}]", service.getName());

                //Write this back to the binder object
                binder.writeBean(service);

                //Save this to the database
                serviceRepository.save(service);

                //Send the user back to the services list page
                UI.getCurrent().navigate("services");

            } catch (ValidationException e) {

                //TODO make sure the validation errors are handled
                logger.error("Validation error: ", e);
            }
        });

        cancel.addClickListener(event -> {
            binder.readBean(null);

            //Send the user back to the services list page
            UI.getCurrent().navigate("services");

        });

        HorizontalLayout actions = new HorizontalLayout();
        actions.add(save, cancel);
        save.getStyle().set("marginRight", "10px");

        return actions;
    }
}
