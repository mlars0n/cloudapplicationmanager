package com.cloudapplicationmanager.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

/**
 * Establish the top menu
 */
@Route("app")
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class MainView extends AppLayout {

    public MainView() {
        //Whether the navbar or drawer is primary
        setPrimarySection(AppLayout.Section.NAVBAR);

        //Text
        //Text appName = new Text("Cloud Application Manager");

        /*Image img = new Image("https://i.imgur.com/GPpnszs.png", "Vaadin Logo");
        img.setHeight("44px");*/


        addToNavbar(new DrawerToggle(), new Span("Cloud Application Manager"));

        Tab homeTab = new Tab(new RouterLink("Home", MainView.class));
        Tab servicesTab = new Tab(new RouterLink("Edit Services", ServiceListView.class));
        Tab domainsTab = new Tab(new RouterLink("Domains", DomainsView.class));
        Tabs tabs = new Tabs(homeTab, servicesTab, domainsTab);
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        addToDrawer(tabs);

    }

}
