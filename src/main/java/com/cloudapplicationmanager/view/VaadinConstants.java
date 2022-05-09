package com.cloudapplicationmanager.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Class to help ensure UI elements are the same throughout
 */
public class VaadinConstants {

    public static Button saveButton() {
        return new Button("Save", new Icon(VaadinIcon.DISC));
    }

    public static Button editButton() {
        return new Button(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
    }

    public static Button deleteButton() {
        return new Button(new Icon(VaadinIcon.TRASH));
    }

    public static Button backButton() {
        return new Button("Back", new Icon(VaadinIcon.ENTER_ARROW));
    }
}
