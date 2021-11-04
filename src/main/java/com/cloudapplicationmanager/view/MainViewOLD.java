package com.cloudapplicationmanager.view;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.PWA;

/**
 * Establish the top menu
 */
@Route("MainViewOld")
@PWA(name = "Project Base for Vaadin", shortName = "Project Base")
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
@PageTitle("Home")
public class MainViewOLD extends VerticalLayout implements RouterLayout {

    public MainViewOLD() {

        // HEADER
        //Icon drawer = VaadinIcon.MENU.create();

        Tab actionButton1 = new Tab(VaadinIcon.HOME.create(), new RouterLink("Home", MainViewOLD.class));
        Tab actionButton2 = new Tab(VaadinIcon.SERVER.create(), new RouterLink("Services", ServiceListView.class));
        Tab actionButton3 = new Tab(VaadinIcon.PACKAGE.create(), new Span("Domains"));
        Tabs buttonBar = new Tabs(actionButton1, actionButton2, actionButton3);

        //new RouterLink("Home", MainView.class);

        //Icon help = VaadinIcon.QUESTION_CIRCLE.create();
        HorizontalLayout header = new HorizontalLayout(buttonBar);
        //header.expand(title);
        header.setJustifyContentMode(JustifyContentMode.CENTER);
        header.setPadding(true);
        header.setWidth("100%");

        // WORKSPACE
        VerticalLayout workspace = new VerticalLayout();
        workspace.setSizeFull();
        workspace.add(new H1("HELLO"));

        // FOOTER
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidth("100%");
        footer.setJustifyContentMode(JustifyContentMode.CENTER);
        footer.add(VaadinIcon.CLOUD.create());
        footer.add(new Span("Cloud Application Manager"));
        footer.add(VaadinIcon.CLOUD.create());

        // MAIN CONTAINER
        setSizeFull();
        setMargin(false);
        setSpacing(false);
        setPadding(false);
        add(header, workspace, footer);

        /*MenuBar menuBar = new MenuBar();
        Text selected = new Text("");
        Div message = new Div(new Text("Selected: "), selected);

        MenuItem project = menuBar.addItem("Project");
        MenuItem account = menuBar.addItem("Account");
        menuBar.addItem("Sign Out", e -> selected.setText("Sign Out"));

        SubMenu projectSubMenu = project.getSubMenu();
        MenuItem users = projectSubMenu.addItem("Users");
        MenuItem billing = projectSubMenu.addItem("Billing");

        SubMenu usersSubMenu = users.getSubMenu();
        usersSubMenu.addItem("List", e -> selected.setText("List"));
        usersSubMenu.addItem("Add", e -> selected.setText("Add"));

        SubMenu billingSubMenu = billing.getSubMenu();
        billingSubMenu.addItem("Invoices", e -> selected.setText("Invoices"));
        billingSubMenu.addItem("Balance Events",
                e -> selected.setText("Balance Events"));

        account.getSubMenu().addItem("Edit Profile",
                e -> selected.setText("Edit Profile"));
        account.getSubMenu().addItem("Privacy Settings",
                e -> selected.setText("Privacy Settings"));
        add(menuBar, message);*/
    }
}
