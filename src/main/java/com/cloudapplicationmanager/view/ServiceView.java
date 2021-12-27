package com.cloudapplicationmanager.view;

import com.cloudapplicationmanager.model.Environment;
import com.cloudapplicationmanager.model.Service;
import com.cloudapplicationmanager.repository.DomainRepository;
import com.cloudapplicationmanager.repository.EnvironmentRepository;
import com.cloudapplicationmanager.repository.ServiceRepository;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.shared.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Route(value = "service", layout = MainView.class)
@PageTitle("Service")
public class ServiceView extends VerticalLayout implements HasUrlParameter<Long> {

    private static Logger logger = LoggerFactory.getLogger(ServiceView.class);

    //Form elements/bean items
    private final TextField name = new TextField("Service Name");
    private final TextField description = new TextField("Description");
    private final TextField healthCheckScheme = new TextField("Health Check Scheme");
    private final TextField healthCheckPath= new TextField("Health Check Path");

    //Validation and form binder service
    private BeanValidationBinder<Service> binder = new BeanValidationBinder<>(Service.class);

    //Main Service class, will either be populated if editing or not if new
    private Service service;
    private final ServiceRepository serviceRepository;
    private final EnvironmentRepository environmentRepository;
    private final DomainRepository domainRepository;
    private boolean isNew = false;

    //Environment grid
    private Grid<Environment> environmentGrid = new Grid(Environment.class);

    //EnvironmentForm to be used in the popup dialogues
    //private EnvironmentForm environmentForm;

    public ServiceView(@Autowired ServiceRepository serviceRepository, @Autowired EnvironmentRepository environmentRepository,
                       @Autowired DomainRepository domainRepository) {
    //,                       @Autowired EnvironmentForm environmentForm) {
        this.serviceRepository = serviceRepository;
        this.environmentRepository = environmentRepository;
        this.domainRepository = domainRepository;
        //this.environmentForm = environmentForm;
    }

    /**
     * Method used to start of the page rendering because it has the ID parameter
     * @param event
     * @param serviceId
     */
    @Override
    public void setParameter(BeforeEvent event, Long serviceId) {

        //If the parameter is 0 then create a new service
        if (serviceId == 0) {
            service = new Service();
            isNew = true;
        } else {
            logger.debug("Getting service with ID [{}] to edit", serviceId);

            Optional<Service> optionalService = serviceRepository.findServiceById(serviceId);
            if (optionalService.isPresent()) {
                this.service = optionalService.get();
                logger.debug("Service name is: [{}]", service.getName());
            } else {
                //TODO handle this error gracefully (although don't expect this, as the links
                //will be calculated properly in the app)
                logger.error("Could not find Service with ID [{}]", serviceId);
            }
        }

        //Now that we have the correct parameter, call the code that will create this page
        createPage();
    }

    public void createPage() {
        FormLayout formLayout = new FormLayout();

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("25em", 1),
                new FormLayout.ResponsiveStep("32em", 2),
                new FormLayout.ResponsiveStep("40em", 3));

        formLayout.setColspan(description, 2);

        //Bind the form and bean (String instance fields only--other fields have to be manually handled)
        binder.bindInstanceFields(this);
        binder.setBean(service);

        TextField healthCheckPort = new TextField("Health Check Port");
        binder.forField(healthCheckPort).withConverter(new StringToIntegerConverter("Please enter numbers only"))
                .withNullRepresentation(0)
                .bind(Service::getHealthCheckPort, Service::setHealthCheckPort);

        //Need to move the buttons to their own line
        HtmlComponent br = new HtmlComponent("br");

        H3 elementsHeader = new H3("Environments");

        formLayout.add(name, description, healthCheckScheme, healthCheckPort, healthCheckPath);
        add(formLayout, br, createButtons(binder), br, elementsHeader, createEnvironmentLayout());

        this.addListener(EnvironmentForm.EnvironmentSaveEvent.class, e -> logger.debug("Environment save event fired"));

        //Registration registration = this.addChangeListener(e -> logger.debug("Environment save event fired"));

        //this.addListener()
    }

    @Transactional
    VerticalLayout createEnvironmentLayout() {
        VerticalLayout environmentVerticalLayout = new VerticalLayout();

        Button addEnvironmentButton = new Button("Add Environment");
        environmentVerticalLayout.add(addEnvironmentButton);

        //Add the horizontal layout
        HorizontalLayout gridAndEnvironmentFormLayout = new HorizontalLayout();

        //Create the grid
        environmentGrid.addClassNames("contact-grid");
        environmentGrid.setSizeFull();
        environmentGrid.setColumns("name");
        environmentGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        setSizeFull();
        //environmentGrid.setSelectionMode(Grid.SelectionMode.NONE);

        //Get the environments
        List<Environment> environments = new ArrayList<>();
        environmentGrid.setItems(service.getEnvironments());
        environmentGrid.setSizeFull();
        //environmentGrid.setItems(environmentRepository.findAll());

        //this.setColspan(environmentGrid, 3);

        //Create the dialog and add the environment form to it
        Dialog dialog = new Dialog();
        EnvironmentForm environmentForm = new EnvironmentForm(environmentRepository, domainRepository, service, this, dialog, 0);
        dialog.add(environmentForm);

        gridAndEnvironmentFormLayout.add(environmentGrid);
        /*gridAndEnvironmentFormLayout.setFlexGrow(2, environmentGrid);
        gridAndEnvironmentFormLayout.setFlexGrow(1, environmentForm);*/
        gridAndEnvironmentFormLayout.setSizeFull();

        //addEnvironmentButton.addClickListener(event -> environmentForm.setVisible(true));
        addEnvironmentButton.addClickListener(event -> dialog.open());

        environmentVerticalLayout.add(gridAndEnvironmentFormLayout, dialog);
        environmentVerticalLayout.setSizeFull();

        return environmentVerticalLayout;
    }

    private HorizontalLayout createButtons(Binder binder) {

        //Buttons
        Button save = new Button("Save");
        Button cancel = new Button("Cancel");
        save.addClickListener(event -> {
            try {

                //Write this back to the binder object
                binder.writeBean(service);

                logger.debug("Saving service [{}]", service.getName());

                //Save this to the database
                serviceRepository.save(service);

                //Send the user back to the services list page
                UI.getCurrent().navigate("services");

            } catch (ValidationException e) {

                //TODO make sure the validation errors are handled sensibly for clients
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
        save.getStyle().set("marginTop", "15px");
        cancel.getStyle().set("marginTop", "15px");

        return actions;
    }

    Registration addChangeListener(ComponentEventListener<EnvironmentForm.EnvironmentSaveEvent> listener) {
        return getEventBus().addListener(EnvironmentForm.EnvironmentSaveEvent.class, listener);
    }

    void updateService() {
        //If the parameter is 0 then create a new service
        if (service.getId() == 0) {
            service = new Service();
            isNew = true;
        } else {
            logger.debug("Getting service with ID [{}] to edit", service.getId());

            Optional<Service> optionalService = serviceRepository.findServiceById(service.getId());
            if (optionalService.isPresent()) {
                this.service = optionalService.get();
                logger.debug("Service name is: [{}]", service.getName());
            } else {
                //TODO handle this error gracefully (although don't expect this, as the links
                //will be calculated properly in the app)
                logger.error("Could not find Service with ID [{}]", service.getId());
            }
        }

        environmentGrid.setItems(service.getEnvironments());
    }
}
