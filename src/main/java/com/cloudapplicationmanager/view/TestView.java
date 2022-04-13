package com.cloudapplicationmanager.view;

import com.cloudapplicationmanager.model.Environment;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route(value = "testview", layout = ParentLayoutView.class)
@PageTitle("TestView")
public class TestView extends FormLayout {

    //Environment grid
    Grid<Environment> environmentGrid = new Grid(Environment.class);

    public TestView() {
        add(createEnvironmentLayout(), new Button("Add Environment"));
    }

    private Grid createEnvironmentLayout() {
        //VerticalLayout environmentVerticalLayout = new VerticalLayout();

        //Button addEnvironmentButton = new Button("Add Environment");
        //environmentVerticalLayout.add(addEnvironmentButton);

        //Create the grid
        environmentGrid.addClassNames("contact-grid");
        environmentGrid.setSizeFull();
        environmentGrid.setColumns("name");
        environmentGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        //setSizeFull();
        environmentGrid.setSelectionMode(Grid.SelectionMode.NONE);

        //Test data
        Environment environment = new Environment();
        environment.setName("Test environment");
        Environment environment2 = new Environment();
        environment.setName("Test environment2");

        List<Environment> environmentList = new ArrayList<>();
        environmentList.add(environment);
        environmentList.add(environment2);
        environmentGrid.setItems(environmentList);
        setSizeFull();
        //environmentGrid.setItems(environmentRepository.findAll());

        this.setColspan(environmentGrid, 2);
        this.setHeightFull();

        //environmentVerticalLayout.add(environmentGrid);

        //This is critical for the grid to work since there is a 3 column layout by default
        //this.setColspan(environmentVerticalLayout, 3);

        return environmentGrid;
    }
}
