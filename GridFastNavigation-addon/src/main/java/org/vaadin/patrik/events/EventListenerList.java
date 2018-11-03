package org.vaadin.patrik.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Used in {@link org.vaadin.patrik.FastNavigation} for listener maintenance
 *
 * @param <LISTENER> Listener type
 * @param <EVENT> Event type
 */
public class EventListenerList<LISTENER extends Listener<EVENT>, EVENT> implements Serializable {
    private final List<LISTENER> listeners;
    
    public EventListenerList() {
        listeners = new ArrayList<LISTENER>();
    }
    
    public void addListener(LISTENER l) {
        listeners.add(l);
    }
    
    public void removeListener(LISTENER l) {
        listeners.remove(l);
    }
    
    public void clearListeners() {
        listeners.clear();
    }
    
    public void dispatch(EVENT event) {
        for(LISTENER l : listeners) {
            l.onEvent(event);
        }
    }
}