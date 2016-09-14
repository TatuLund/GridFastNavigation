package org.vaadin.patrik.shared;

import java.util.HashSet;

import com.vaadin.shared.AbstractComponentState;

@SuppressWarnings("serial")
public class FastNavigationState extends AbstractComponentState {
    
    public boolean tabCapture = false;
    
    public boolean selectRowOnEdit = false;
    
    public HashSet<Integer> openShortcuts = new HashSet<Integer>();

}
