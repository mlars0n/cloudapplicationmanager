package com.cloudapplicationmanager.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;

/**
 * Establish the layout menu
 */
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class ParentLayoutView extends AppLayout implements PageConfigurator {

    public ParentLayoutView() {
        //Whether the navbar or drawer is primary
        setPrimarySection(AppLayout.Section.NAVBAR);

        addToNavbar(new DrawerToggle(), new Span("Cloud Application Manager"));
        Tab servicesTab = new Tab(new RouterLink("Services", ServiceListView.class));
        Tab domainsTab = new Tab(new RouterLink("Domains", DomainListView.class));
        Tabs tabs = new Tabs(servicesTab, domainsTab);
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        addToDrawer(tabs);

    }

    @Override
    public void configurePage(InitialPageSettings settings) {
        settings.addFavIcon("icon", "favicon.png", "192x192");
        settings.addLink("shortcut icon", "favicon.png");
    }

}
