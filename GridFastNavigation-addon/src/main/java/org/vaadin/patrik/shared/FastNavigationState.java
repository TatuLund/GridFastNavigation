package org.vaadin.patrik.shared;

import java.util.HashSet;
import java.util.Set;

import com.vaadin.shared.communication.SharedState;

@SuppressWarnings("serial")
public class FastNavigationState extends SharedState {
    
    public boolean openEditorOnType = true;
    
    public boolean selectTextOnEditorOpen = true;

    public boolean allowTabRowChange = true;
    
    public boolean allowArrowRowChange = true;
    
    public boolean hasFocusListener = false;
    
    public boolean hasRowFocusListener = false;
    
    public boolean hasCellFocusListener = false;
    
    public boolean hasRowEditListener = false;
    
    public boolean hasCellEditListener = false;
    
    public boolean hasEditorOpenListener = false;
    
    public boolean hasEditorMoveListener = false;
    
    public boolean hasEditorCloseListener = false;
    
    public Set<Integer> openShortcuts = new HashSet<Integer>();
    
    public Set<Integer> closeShortcuts = new HashSet<Integer>();

}
