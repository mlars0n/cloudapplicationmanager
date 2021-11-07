package com.cloudapplicationmanager.view;

import com.cloudapplicationmanager.model.Service;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "serviceform", layout = MainView.class)
@PageTitle("Service Form")
public class ServiceForm extends FormLayout {
    private TextField name = new TextField("Name");
    private Service service = new Service();

    private BeanValidationBinder<Service> binder = new BeanValidationBinder<>(Service.class);

    public ServiceForm() {
        binder.bindInstanceFields(this);
        binder.setBean(service);
        add(
                name,
                new Button("Save", event -> save())
        );
    }

    private void save() {
        if (binder.validate().isOk()) {
            Service service = binder.getBean();
        }
    }

}
