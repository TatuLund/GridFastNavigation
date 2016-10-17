package org.vaadin.patrik.events;

public interface Listener<EventType> {
    public void onEvent(EventType event);
}