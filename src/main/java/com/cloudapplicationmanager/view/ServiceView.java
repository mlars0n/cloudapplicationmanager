package com.cloudapplicationmanager.view;

import com.cloudapplicationmanager.model.Environment;
import com.cloudapplicationmanager.model.Service;
import com.cloudapplicationmanager.repository.DomainRepository;
import com.cloudapplicationmanager.repository.EnvironmentRepository;
import com.cloudapplicationmanager.repository.ServiceRepository;
import com.cloudapplicationmanager.service.EnvironmentHealthCheckService;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.router.*;
import com.vaadin.flow.shared.Registration;
import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Route(value = "service", layout = ParentLayoutView.class)
@PageTitle("Service")
public class ServiceView extends VerticalLayout implements HasUrlParameter<Long> {

    private static Logger logger = LoggerFactory.getLogger(ServiceView.class);

    //Form elements/bean items
    private final TextField name = new TextField("Service Name");
    private final TextField description = new TextField("Description");
    private final TextField healthCheckScheme = new TextField("Health Check Scheme");
    private final TextField healthCheckPath= new TextField("Health Check Path");

    //Create service save/back/delete buttons
    private Button saveButton = VaadinConstants.saveButton();
    private Button backButton = VaadinConstants.backButton();
    private Button deleteButton = VaadinConstants.deleteButton();

    //Validation and form binder service
    private BeanValidationBinder<Service> binder = new BeanValidationBinder<>(Service.class);

    //Main Service class, will either be populated if editing or not if new
    private Service service;
    private long serviceId;
    private final ServiceRepository serviceRepository;
    private final EnvironmentRepository environmentRepository;
    private final DomainRepository domainRepository;
    private final EnvironmentHealthCheckService environmentHealthCheckService;
    private boolean isNew = false;

    //Environment grid
    private Grid<Environment> environmentGrid = new Grid();

    //EnvironmentForm to be used in the popup dialogues
    //private EnvironmentForm environmentForm;

    public ServiceView(@Autowired ServiceRepository serviceRepository, @Autowired EnvironmentRepository environmentRepository,
                       @Autowired DomainRepository domainRepository, @Autowired EnvironmentHealthCheckService environmentHealthCheckService) {
    //,                       @Autowired EnvironmentForm environmentForm) {
        this.serviceRepository = serviceRepository;
        this.environmentRepository = environmentRepository;
        this.domainRepository = domainRepository;
        this.environmentHealthCheckService = environmentHealthCheckService;
    }

    /**
     * Method used to start of the page rendering because it has the ID parameter
     * @param event
     * @param serviceId
     */
    @Override
    public void setParameter(BeforeEvent event, Long serviceId) {

        this.serviceId = serviceId;

        //Populate the service object
        populateServiceData();

        //Hide the delete button if we are creating a service
        if (serviceId != null && serviceId == 0) {
            deleteButton.setVisible(false);
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

        H3 serviceHeader= new H3("Service");
        H3 environmentsHeader = new H3("Environments");

        /*HorizontalLayout buttonLayout = createButtons(binder);
        formLayout.setColspan(buttonLayout, 3);*/

        formLayout.add(name, description, healthCheckScheme, healthCheckPort, healthCheckPath);
        add(serviceHeader, formLayout, createButtons(binder), environmentsHeader, createEnvironmentLayout());

        this.addListener(EnvironmentForm.EnvironmentUpdateEvent.class, e -> logger.debug("Environment save event fired"));
    }

    private void populateServiceData() {
        //If the parameter is 0 then this is for adding a new service
        if (serviceId == 0) {
            service = new Service();
            isNew = true;
        } else {
            logger.debug("Getting service with ID [{}]", serviceId);

            Optional<Service> optionalService = serviceRepository.findServiceById(serviceId);
            if (optionalService.isPresent()) {
                this.service = optionalService.get();
                logger.debug("Service name is: [{}]", service.getName());
            } else {
                //TODO handle this error gracefully with a proper "404" type error (although don't expect this, as the links
                //will be calculated properly in the app)
                logger.error("Could not find Service with ID [{}]", serviceId);
            }
        }

        //Populate or refresh the environment grid
        environmentGrid.setItems(service.getEnvironments());
    }

    private void refreshEnvironmentList(EnvironmentForm.EnvironmentUpdateEvent event) {
        logger.debug("Environment save event fired IN SERVICE VIEW class, refreshing list");

        //TODO could refresh this in a more direct way but this will be a quick query, i.e. lookup by PK with eager fetch of environments
        this.populateServiceData();
    }

    VerticalLayout createEnvironmentLayout() {

        //Create the dialog and add the environment form to it
        Dialog dialog = new Dialog();
        EnvironmentForm environmentForm = new EnvironmentForm(environmentRepository, domainRepository, service, this, dialog);
        dialog.add(environmentForm);

        //Set up the basic layout
        VerticalLayout environmentVerticalLayout = new VerticalLayout();

        //Add the horizontal layout
        HorizontalLayout gridAndEnvironmentFormLayout = new HorizontalLayout();

        //Create the grid columns
        environmentGrid.addColumn(Environment::getName).setHeader("Name").setSortable(true);
        environmentGrid.addColumn(environment -> environmentHealthCheckService.getHealthCheckUrl(environment)).setHeader("Fully qualified health check URL (generated)");
        environmentGrid.addComponentColumn(environment -> getHealthCheckActiveIcon(environment)).setHeader("Health check active?");

        //Add more health check info
        environmentGrid.addComponentColumn(environment -> getIsHealthyIcon(environment)).setHeader("Healthy?").setSortable(true);

        environmentGrid.addColumn(Environment::getLastHealthCheck).setHeader("Last checked").setSortable(true);

        //Add the edit button
        environmentGrid.addComponentColumn(environment -> {
            Button editRowButton = VaadinConstants.editButton();
            editRowButton.addClickListener(event -> {
                //This has to be checked for null
                if (event != null) {
                    environmentForm.populateEnvironment(environment.getId());
                }

                dialog.open();
            });

            return editRowButton;
        }).setHeader("Edit");

        //environmentGrid.setColumns("name", "description", "subDomain", "urlPath", "healthCheckActive");
        environmentGrid.getColumns().forEach(col -> col.setAutoWidth(true));

        //environmentGrid.setSelectionMode(Grid.SelectionMode.NONE);

        //Populate  the environments
        environmentGrid.setItems(service.getEnvironments());

        //Set the size to take up the page
        environmentGrid.setSizeFull();
        gridAndEnvironmentFormLayout.setSizeFull();

        //Make sure you can't click on a row
        environmentGrid.setSelectionMode(Grid.SelectionMode.NONE);

        gridAndEnvironmentFormLayout.add(environmentGrid);

        //Add the event listener that fires when an environment is saved
        environmentForm.addListener(EnvironmentForm.EnvironmentUpdateEvent.class, this::refreshEnvironmentList);

        //Create the "add environment" button and what to do when it is clicked
        Button addEnvironmentButton = new Button("Add Environment", new Icon(VaadinIcon.PLUS));
        addEnvironmentButton.addClickListener(event -> {

            //Set the environment ID to 0 for a new environment
            environmentForm.populateEnvironment(0);
            dialog.open();
        });

        //Create the "add environment" button and what to do when it is clicked

        //Set the high level grid properties
        environmentGrid.addClassNames("contact-grid");
        environmentGrid.setSizeFull();
        setSizeFull();

        //Add the components to the environment layout
        environmentVerticalLayout.add(addEnvironmentButton, gridAndEnvironmentFormLayout, dialog);
        environmentVerticalLayout.setSizeFull();

        return environmentVerticalLayout;
    }

    /**
     * Manual health check button
     * @param environment
     * @return
     */
    private Button healthCheckButton(Environment environment) {
        Button healthCheckButton = new Button("Check Health Now", VaadinIcon.CHECK_SQUARE_O.create());
        healthCheckButton.addClickListener(event -> {
            boolean environmentHealthy = environmentHealthCheckService.checkHealth(environment);

            //Configure the button notification
            int duration = 3000;
            Notification.Position position = Notification.Position.BOTTOM_CENTER;

            if (environmentHealthy) {
                Notification notification = Notification.show("Environment is healthy", duration, position);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification notification = Notification.show("Environment is not healthy", duration, position);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        return healthCheckButton;
    }

    private HorizontalLayout createButtons(Binder binder) {

        //Add the save event
        saveButton.addClickListener(event -> {
            try {

                //Write this back to the binder object
                binder.writeBean(service);

                logger.debug("Saving service [{}]", service.getName());

                //Save this to the database
                serviceRepository.save(service);

                //Send the user back to the services list page
                //UI.getCurrent().navigate("services");

                //Stay on this page but notify the user that the save operation worked
                Notification notification = Notification.show("Saved", 3000, Notification.Position.TOP_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } catch (ValidationException e) {

                //TODO make sure the validation errors are handled sensibly for clients
                logger.error("Validation error: ", e);
            }
        });

        backButton.addClickListener(event -> {
            binder.readBean(null);

            //Send the user back to the services list page
            UI.getCurrent().navigate("services");

        });

        deleteButton.addClickListener(click -> {
            logger.debug("Deleting Service with id [{}]", service.getId());

            ConfirmDialog
                    .createQuestion()
                    .withCaption("Delete warning")
                    .withMessage("Are you sure you want to delete service  \"" + service.getName() + "\"?")
                    .withOkButton(() -> {
                        //Delete the service
                        serviceRepository.deleteById(service.getId());

                        //Return to the Service List page
                        UI.getCurrent().navigate(ServiceListView.class);
                    }, ButtonOption.focus(), ButtonOption.caption("YES"))
                    .withCancelButton(ButtonOption.caption("NO"))
                    .open();
        });

        //Layout the save/back/delete buttons
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();

        //Push the delete button over to the right--couldn't make this work without CSS
        deleteButton.getStyle().set("margin-left", "auto");

        //Add in the buttons but don't include the delete button if this is a new service
        actions.add(saveButton, backButton, deleteButton);

        return actions;
    }

    Registration addChangeListener(ComponentEventListener<EnvironmentForm.EnvironmentUpdateEvent> listener) {
        return getEventBus().addListener(EnvironmentForm.EnvironmentUpdateEvent.class, listener);
    }

    private HorizontalLayout getHealthCheckActiveIcon(Environment environment) {
        HorizontalLayout healthCheckActiveLayout = new HorizontalLayout();
        healthCheckActiveLayout.setAlignItems(Alignment.CENTER);
        Icon icon;
        //Paragraph text;
        if (environment.isHealthCheckActive()) {
            icon = new Icon(VaadinIcon.CHECK_CIRCLE);
            icon.setColor("black");
        } else {
            icon = new Icon(VaadinIcon.CLOSE_CIRCLE);
        }

        healthCheckActiveLayout.add(icon);

        return healthCheckActiveLayout;
    }

    private HorizontalLayout getIsHealthyIcon(Environment environment) {

        HorizontalLayout isHealthyLayout = new HorizontalLayout();
        isHealthyLayout.setAlignItems(Alignment.CENTER);
        Icon icon;
        //Paragraph text;
        if (environment.getIsHealthy()) {
            icon = new Icon(VaadinIcon.CHECK_CIRCLE);
            icon.setColor("green");
        } else {
            icon = new Icon(VaadinIcon.CLOSE_CIRCLE);
            icon.setColor("red");
        }

        isHealthyLayout.add(icon);

        //If this isn't being checked don't display any icon
        if (!environment.isHealthCheckActive()) {
            isHealthyLayout.setVisible(false);
        }

        return isHealthyLayout;
    }
}

