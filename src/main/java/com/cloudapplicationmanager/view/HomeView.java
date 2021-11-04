package com.cloudapplicationmanager.view;

import com.cloudapplicationmanager.model.Service;
import com.cloudapplicationmanager.repository.ServiceRepository;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "", layout = MainView.class)
@PageTitle("Home")
public class HomeView extends VerticalLayout {

    public HomeView() {
        add(new H1("HOME VIEW"));
    }
}
