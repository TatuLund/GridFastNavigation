package org.vaadin.patrik.events;

import com.vaadin.event.SerializableEventListener;

/**
 * Supertype of the Listeners used in {@link org.vaadin.patrik.FastNavigation}
 * 
 * @param <EventType> Type of the Event
 */
public interface Listener<EventType> extends SerializableEventListener {
    public void onEvent(EventType event);
}