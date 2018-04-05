package org.vaadin.patrik.events;

import java.io.Serializable;

public interface Listener<EventType> extends Serializable {
    public void onEvent(EventType event);
}