package org.vaadin.patrik.events;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

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