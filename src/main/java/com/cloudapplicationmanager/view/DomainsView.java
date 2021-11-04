package com.cloudapplicationmanager.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "domains", layout = MainView.class)
@PageTitle("Domains")
public class DomainsView extends VerticalLayout {

    public DomainsView() {
        add(new H1("DOMAINS HERE"));
    }

}
