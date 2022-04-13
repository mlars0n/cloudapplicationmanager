package com.cloudapplicationmanager.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Class to help ensure UI elements are the same throughout
 */
public class VaadinConstants {

    public static Button editButton() {
        return new Button(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
    }
}
