package org.vaadin.patrik.events;

import java.io.Serializable;

/**
 * Supertype of the Listeners used in {@link org.vaadin.patrik.FastNavigation}
 * 
 * @param <EventType> Type of the Event
 */
public interface Listener<EventType> extends Serializable {
    public void onEvent(EventType event);
}