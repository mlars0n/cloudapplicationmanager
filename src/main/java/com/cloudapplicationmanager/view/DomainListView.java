package com.cloudapplicationmanager.view;

import com.cloudapplicationmanager.model.Domain;
import com.cloudapplicationmanager.repository.DomainRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

//Tooltip import comes from https://vaadin.com/directory/component/tooltips4vaadin/samples
import dev.mett.vaadin.tooltip.Tooltips;

@Route(value = "domains", layout = ParentLayoutView.class)
@PageTitle("Domains")
public class DomainListView extends VerticalLayout {

    private static Logger logger = LoggerFactory.getLogger(DomainListView.class);

    private DomainRepository domainRepository;

    private Grid<Domain> domainGrid = new Grid<>();

    private BeanValidationBinder<Domain> binder = new BeanValidationBinder<>(Domain.class);

    //Domain object to be used for creating new domains or editing them
    private Domain domain;

    //Fields to be managed by the binder framework (note that names much match in order to work
    //in the bean binding framework
    private final TextField name = new TextField("Domain Name");
    private final TextField description = new TextField("Description");

    //Dialog to add or edit a domain
    Dialog addEditEnvironmentDialog = new Dialog();

    //Buttons used in the popup domain dialog--need to be set up here so
    //we can adjust the tooltip message in the event handler
    Button domainPopupSaveButton = new Button("Save");
    Button domainPopupCancelButton = new Button("Cancel");
    Button domainPopupDeleteButton = new Button(new Icon(VaadinIcon.TRASH));

    //How many environments exist for a particular domain
    int totalEnvironmentsForDomain = 0;

    public DomainListView(@Autowired DomainRepository domainRepository) {

        //Set up dependencies
        this.domainRepository = domainRepository;

        //Create the page header
        H3 pageHeader = new H3("Domains");

        //Create the button to add a domain
        Button addDomainButton = new Button("Add Domain", new Icon(VaadinIcon.PLUS));

        //Now set up what happens when that add button is clicked
        addDomainButton.addClickListener(event -> {

            //Bind the form and bean (String instance fields only--other fields have to be manually handled)
            binder.bindInstanceFields(this);
            domain = new Domain();
            binder.setBean(domain);

            //Set the environment ID to 0 for a new environment
            //environmentForm.populateEnvironment(0);
            addEditEnvironmentDialog.open();
        });

        //Create the dialog form that will popup when you want to add or edit a row
        addEditEnvironmentDialog.add(createDomainForm());

        //Add the components to the vertical layout
        add(pageHeader, addDomainButton, createDomainGrid(), addEditEnvironmentDialog);
    }

    private FormLayout createDomainForm() {
        FormLayout domainForm = new FormLayout();
        domainForm.add(this.name, this.description, configureDomainPopupButtons());

        return domainForm;
    }

    private HorizontalLayout configureDomainPopupButtons() {

        //Buttons
        domainPopupSaveButton.addClickListener(event -> {
            try {

                logger.debug("Saving domain [{}]", domain.getName());

                //Write this back to the binder object
                binder.writeBean(domain);

                //Save this to the database
                domainRepository.save(domain);

                //Update the domain items so we see the latest ones in the list
                updateDomainItems();

                //Close the dialog and update the items in the list
                addEditEnvironmentDialog.close();

            } catch (ValidationException e) {

                //TODO make sure the validation errors are handled sensibly for clients
                logger.warn("Validation error saving environment form");
            }
        });

        domainPopupCancelButton.addClickListener(event -> {
            addEditEnvironmentDialog.close();
        });

        //Delete functionality
        domainPopupDeleteButton.addClickListener(event -> {

            if (totalEnvironmentsForDomain == 0) {
                ConfirmDialog
                        .createQuestion()
                        .withCaption("Delete warning")
                        .withMessage("Are you sure you want to delete domain  \"" + domain.getName() + "\"?")
                        .withOkButton(() -> {
                            domainRepository.delete(domain);

                            //Update the domain items so we see the latest ones in the list
                            updateDomainItems();
                        }, ButtonOption.focus(), ButtonOption.caption("YES"))
                        .withCancelButton(() -> {
                            addEditEnvironmentDialog.open();
                        }, ButtonOption.caption("NO"))
                        .open();

                addEditEnvironmentDialog.close();
            } //Else do nothing, if we can't actually process the deletion
        });

        domainPopupSaveButton.getStyle().set("marginRight", "10px");
        domainPopupSaveButton.getStyle().set("marginTop", "15px");
        domainPopupCancelButton.getStyle().set("marginTop", "15px");
        domainPopupDeleteButton.getStyle().set("marginTop", "15px");
        domainPopupDeleteButton.getStyle().set("margin-left", "auto");

        HorizontalLayout actions = new HorizontalLayout();
        actions.add(domainPopupSaveButton, domainPopupCancelButton, domainPopupDeleteButton);


        return actions;
    }

    private void updateDomainItems() {
        domainGrid.setItems(domainRepository.findAll());
    }

    /**
     * Definition for the main grid view
     * @return
     */
    private Grid<Domain> createDomainGrid() {


        domainGrid.addColumn(Domain::getName).setHeader("Domain Name").setSortable(true);
        domainGrid.addColumn(Domain::getDescription).setHeader("Description").setSortable(false);
        domainGrid.addColumn(Domain::getCloudId).setHeader("Cloud ID").setSortable(false);

        //Make these all fit their columns
        domainGrid.getColumns().forEach(col -> {
            col.setAutoWidth(true);
        });

        //Add the edit column and functionality to pop up the domain editor when clicked
        domainGrid.addComponentColumn(localDomain -> {
            Button editRowButton = VaadinConstants.editButton();

            editRowButton.addClickListener(event -> {

                totalEnvironmentsForDomain = 0;

                //Bind the form and bean
                binder.bindInstanceFields(this);
                domain = localDomain; //Set the page's domain to this one that we are editing
                binder.setBean(domain);

                //Get how many environments exist for this domain
                totalEnvironmentsForDomain = domainRepository.getEnvironmentsCount(domain);
                logger.debug("Domain [{}] has [{}] environments", domain.getName(), totalEnvironmentsForDomain);

                //Set the tooltip if there are more environments than can be deleted (deletion will be disallowed)
                if (totalEnvironmentsForDomain > 0) {
                    Tooltips.getCurrent().setTooltip(domainPopupDeleteButton, "Delete not allowed because there are " + totalEnvironmentsForDomain
                            + " environment(s) using this domain. To delete remove all environments that use this domain.");
                    logger.debug("Disallowing domain deletion because there are [{}] environments still using this domain.", totalEnvironmentsForDomain);
                } else { //Remove the tooltip if deletion is allowed
                    Tooltips.getCurrent().removeTooltip(domainPopupDeleteButton);
                }

                addEditEnvironmentDialog.open();
            });

            return editRowButton;
        }).setHeader("Edit");

        //Remove the functionality to click on rows
        domainGrid.setSelectionMode(Grid.SelectionMode.NONE);
        domainGrid.addClassNames("contact-grid");
        domainGrid.setSizeFull();
        setSizeFull();

        updateDomainItems();

        return domainGrid;
    }

}
