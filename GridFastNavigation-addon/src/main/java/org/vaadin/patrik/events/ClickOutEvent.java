package org.vaadin.patrik.events;

import com.vaadin.ui.Component;

/**
 * ClickOutEvent is emitted when user clicks outside the Grid.
 * 
 * @since 2.1.8
 * 
 * @see org.vaadin.patrik.FastNavigation#FastNavigation(com.vaadin.ui.Grid, boolean, boolean)
 * @see org.vaadin.patrik.FastNavigation#addClickOutListener(org.vaadin.patrik.FastNavigation.ClickOutListener)
 *
 */
public class ClickOutEvent extends Component.Event {

    public ClickOutEvent(Component source) {
        super(source);
    }
}
