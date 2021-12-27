package com.cloudapplicationmanager.view;

import com.cloudapplicationmanager.model.Service;
import com.cloudapplicationmanager.repository.ServiceRepository;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

@Route(value = "services", layout = MainView.class)
@PageTitle("Services")
public class ServiceListView extends VerticalLayout {

    private static Logger logger = LoggerFactory.getLogger(ServiceListView.class);

    //This is autowired below in the constructor
    private ServiceRepository serviceRepository;

    Grid<Service> grid = new Grid<>(Service.class);
    TextField filterText = new TextField();

    public ServiceListView(@Autowired ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;

        addClassName("list-view");
        configureGrid();
        populateGrid();

        Button addServiceButton = new Button("Add New Service");
        addServiceButton.getStyle().set("margin-left", "auto");

        addServiceButton.addClickListener(click -> getUI().ifPresent(ui -> ui.navigate("service/0")));

        add(addServiceButton, configureGridLayout());

        //createServiceList();
    }

    private void populateGrid() {
        grid.setItems(serviceRepository.findAll());
    }

    private Component configureGridLayout() {
        HorizontalLayout content = new HorizontalLayout(grid);
        //content.setFlexGrow(2, grid);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }

    private void configureGrid() {

        grid.addClassNames("contact-grid");
        grid.setSizeFull();
        grid.setColumns("name", "description", "healthCheckScheme", "healthCheckPort", "healthCheckPath");
        //grid.addComponentColumn(item -> new Button(VaadinIcon.EDIT.create()));
        grid.addComponentColumn(item -> new RouterLink("Edit", ServiceView.class, item.getId()));

        //Create delete button
        grid.addComponentColumn(item -> {
            Button button = new Button("Delete");
            button.addClickListener(click -> {
                logger.debug("Deleting Service with id [{}]", item.getId());

                ConfirmDialog
                        .createQuestion()
                        .withCaption("Delete warning")
                        .withMessage("Are you sure you want to delete service  \"" + item.getName() + "\"?")
                        .withOkButton(() -> {
                            serviceRepository.deleteById(item.getId());
                            grid.setItems(serviceRepository.findAll());
                        }, ButtonOption.focus(), ButtonOption.caption("YES"))
                        .withCancelButton(ButtonOption.caption("NO"))
                        .open();
            });
            return button;
        });

        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        setSizeFull();

        grid.setSelectionMode(Grid.SelectionMode.NONE);
        /*SingleSelect<Grid<Service>, Service> serviceSelect = grid.asSingleSelect();
        serviceSelect.addValueChangeListener(e -> {
            Service selectedService = e.getValue();

            QueryParameters queryParameters = new

            UI.getCurrent().navigate("services", new QueryParameters("id", selectedService.getId().toString()));
            logger.debug("You selected service [{}]", selectedService.getName());
        });*/
    }

    /*private void createServiceList() {
        List<Service> services = serviceRepository.findAll();

        for (Service service: services) {
            add(new ListItem(service.getName()));
        }

    }
*/

}
