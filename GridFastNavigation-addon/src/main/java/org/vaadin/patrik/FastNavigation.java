package org.vaadin.patrik;

import java.util.Collection;

import org.vaadin.patrik.events.CellEditEvent;
import org.vaadin.patrik.events.RowEditEvent;
import org.vaadin.patrik.shared.FastNavigationServerRPC;
import org.vaadin.patrik.shared.FastNavigationState;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.Grid;
import com.vaadin.util.ReflectTools;

@SuppressWarnings("serial")
public class FastNavigation extends AbstractExtension {

    public interface RowEditListener {
        public void rowEdited(RowEditEvent event);
    }
    
    public interface CellEditListener {
        public void cellEdited(CellEditEvent event);
    }
    
    public FastNavigation(final Grid g) {
        g.setEditorBuffered(false);
        g.setEditorEnabled(true);

        registerRpc(new FastNavigationServerRPC() {
            @Override
            public void rowUpdated(int rowIndex) {
                fireRowEditedEvent(g, rowIndex);
            }
            
            @Override
            public void cellUpdated(int rowIndex, int colIndex) {
                fireCellEditedEvent(g, rowIndex, colIndex);
            }
        }, FastNavigationServerRPC.class);

        extend(g);
    }
    
    @Override
    public FastNavigationState getState() {
        return (FastNavigationState) super.getState();
    }

    //
    // Tab capture
    //
    
    public void setTabCapture(boolean enable) {
        getState().tabCapture = enable;
    }
    
    public boolean isTabCaptureEnabled() {
        return getState().tabCapture;
    }
    
    //
    // Editor opening extra shortcuts
    //
    
    public void addEditorOpenShortcut(int code) {
        getState().openShortcuts.add(code);
    }
    
    public void removeEditorOpenShortcut(int code) {
        getState().openShortcuts.remove(code);
    }
    
    public void clearEditorOpenShortcuts() {
        getState().openShortcuts.clear();
    }
    
    //
    // Selection events
    //
    
    // TODO 
    
    //
    // Event listeners
    //   
    
    public void addRowEditListener(RowEditListener listener) {
        addListener(RowEditEvent.class, listener, ReflectTools.findMethod(
                listener.getClass(), "rowEdited", RowEditEvent.class));
    }
    
    public void addCellEditListener(CellEditListener listener) {
        addListener(CellEditEvent.class, listener, ReflectTools.findMethod(
                listener.getClass(), "cellEdited", CellEditEvent.class));        
    }
    
    private void fireCellEditedEvent(Grid grid, Integer rowIndex, Integer cellIndex) {
        
    }
    
    @SuppressWarnings("unchecked")
    private void fireRowEditedEvent(Grid grid, Integer rowIndex) {
        Collection<RowEditListener> listeners = (Collection<RowEditListener>) getListeners(
                RowEditEvent.class);
        for (RowEditListener listener : listeners) {
            listener.rowEdited(new RowEditEvent(grid, rowIndex));
        }
    }
}
