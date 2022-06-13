package com.cloudapplicationmanager.view;

import com.cloudapplicationmanager.model.Service;
import com.cloudapplicationmanager.repository.EnvironmentRepository;
import com.cloudapplicationmanager.repository.ServiceRepository;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Route(value = "services", layout = ParentLayoutView.class)
@RouteAlias(value = "", layout = ParentLayoutView.class)
@PageTitle("Services")
@CssImport(value = "./styles/vaadin-grid-styles.css", themeFor = "vaadin-grid")
public class ServiceListView extends VerticalLayout {

    private static Logger logger = LoggerFactory.getLogger(ServiceListView.class);

    //This is autowired below in the constructor
    private ServiceRepository serviceRepository;
    private EnvironmentRepository environmentRepository;

    Grid<Service> serviceGrid = new Grid<>();

    public ServiceListView(@Autowired ServiceRepository serviceRepository, @Autowired EnvironmentRepository environmentRepository) {
        this.serviceRepository = serviceRepository;
        this.environmentRepository = environmentRepository;

        addClassName("list-view");
        configureGrid();
        populateGrid();

        Button addServiceButton = new Button("Add Service", new Icon(VaadinIcon.PLUS));

        addServiceButton.addClickListener(click -> getUI().ifPresent(ui -> ui.navigate("service/0")));

        setSizeFull();
        add(addServiceButton, configureGridLayout());
    }

    private void populateGrid() {
        serviceGrid.setItems(serviceRepository.findAll());
    }

    private Component configureGridLayout() {
        HorizontalLayout content = new HorizontalLayout(serviceGrid);
        //content.setFlexGrow(2, grid);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }

    private void configureGrid() {

        //Add the columns to the grid
        serviceGrid.addColumn(Service::getName).setHeader("Name").setSortable(true);
        serviceGrid.addColumn(Service::getDescription).setHeader("Description").setSortable(true);
        serviceGrid.addColumn(service -> environmentRepository.countByService(service)).setHeader("Total Environments").setSortable(true);
        serviceGrid.addColumn(service -> environmentRepository.countByServiceAndHealthCheckActive(service, true)).setHeader("Health Check Active")
                        .setKey("active");
        serviceGrid.addColumn(service ->
                        environmentRepository.countByServiceAndIsHealthyAndHealthCheckActive(service, true, true))
                        .setHeader("Healthy (Health Check Active)")
                                .setKey("healthy");
        serviceGrid.addColumn(service ->
                        environmentRepository.countByServiceAndIsHealthyAndHealthCheckActive(service, false, true))
                        .setHeader("Unhealthy (Health Check Active)")
                                .setKey("unhealthy");

        //Add what happens when you click on a row
        serviceGrid.addSelectionListener(item -> {

            Optional<Service> optionalService = item.getFirstSelectedItem();
            long serviceId = 0;

            if (optionalService.isPresent()) {
                serviceId = optionalService.get().getId();
            } else {
                logger.warn("Did not find optional service in ServiceListView. Check conditions that made this happen if possible.");
            }

            //Navigate to the service view page
            UI.getCurrent().navigate(ServiceView.class, serviceId);
        });

        //Set up the cell style
        styleHealthCheckCells();

        //Additional grid options
        serviceGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        serviceGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        serviceGrid.setSizeFull();
    }

    /**
     * Method to handle all the cell stylings to show what is healthy and what is not
     */
    private void styleHealthCheckCells() {
        serviceGrid.getColumnByKey("healthy").setClassNameGenerator(service -> {
            long count = environmentRepository.countByServiceAndHealthCheckActive(service, true);
            if (count > 0)
                return "allisgood";
            else
                return "";
        });
    }
}
