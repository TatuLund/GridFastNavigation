package org.vaadin.patrik;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.vaadin.patrik.events.CellEditEvent;
import org.vaadin.patrik.events.CellFocusEvent;
import org.vaadin.patrik.events.EditorCloseEvent;
import org.vaadin.patrik.events.EditorOpenEvent;
import org.vaadin.patrik.events.EventListenerList;
import org.vaadin.patrik.events.Listener;
import org.vaadin.patrik.events.RowEditEvent;
import org.vaadin.patrik.events.RowFocusEvent;
import org.vaadin.patrik.shared.FastNavigationClientRPC;
import org.vaadin.patrik.shared.FastNavigationServerRPC;
import org.vaadin.patrik.shared.FastNavigationState;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.Grid;


@SuppressWarnings("serial")
public class FastNavigation extends AbstractExtension {

    private static Logger _logger = Logger.getLogger("FastNavigation");

    private static Logger getLogger() {
        return _logger;
    }

    //
    // Event interfaces
    //

    public interface RowEditListener extends Listener<RowEditEvent> {
    }
    
    private final EventListenerList<RowEditListener, RowEditEvent> rowEditListeners = new EventListenerList<RowEditListener, RowEditEvent>();

    public interface CellEditListener extends Listener<CellEditEvent> {
    }
    
    private final EventListenerList<CellEditListener, CellEditEvent> cellEditListeners = new EventListenerList<CellEditListener, CellEditEvent>();

    public interface CellFocusListener extends Listener<CellFocusEvent> {
    }
    
    private final EventListenerList<CellFocusListener, CellFocusEvent> cellFocusListeners = new EventListenerList<CellFocusListener, CellFocusEvent>();

    public interface RowFocusListener extends Listener<RowFocusEvent> {
    }
    
    private final EventListenerList<RowFocusListener, RowFocusEvent> rowFocusListeners = new EventListenerList<RowFocusListener, RowFocusEvent>();

    public interface EditorOpenListener extends Listener<EditorOpenEvent> {
    }
    
    private final EventListenerList<EditorOpenListener, EditorOpenEvent> editorOpenListeners = new EventListenerList<EditorOpenListener, EditorOpenEvent>();

    public interface EditorCloseListener extends Listener<EditorCloseEvent> {
    }
    
    private final EventListenerList<EditorCloseListener, EditorCloseEvent> editorCloseListeners = new EventListenerList<EditorCloseListener, EditorCloseEvent>();


    //
    // Actual class stuff
    //

    // Mirror state value here to avoid unnecessary comms
    private boolean hasRowFocusListener = false;

    // Mirror state value here to avoid unnecessary comms
    private boolean hasCellFocusListener = false;

    // Information about previously seen focused row
    private int lastFocusedRow = 0;
    private int lastFocusedCol = 0;

    public FastNavigation(final Grid g) {
    	setupFastNavigation(g,false);
    }

    public FastNavigation(final Grid g, boolean changeColumnOnEnter) {
    	setupFastNavigation(g,changeColumnOnEnter);
    }
    
    public void setupFastNavigation(final Grid g, boolean changeColumnOnEnter) {
    	getState().changeColumnOnEnter = changeColumnOnEnter;
        g.setEditorBuffered(false);
        g.setEditorEnabled(true);
        
        registerRpc(new FastNavigationServerRPC() {

            @Override
            public void rowUpdated(int rowIndex) {
                rowEditListeners.dispatch(new RowEditEvent(g, rowIndex));
            }

            @Override
            public void cellUpdated(int rowIndex, int colIndex, String newData) {
                cellEditListeners.dispatch(new CellEditEvent(g, rowIndex, colIndex, newData));
            }

            @Override
            public void focusUpdated(int rowIndex, int colIndex) {
                if (hasRowFocusListener && rowIndex != lastFocusedRow) {
                    rowFocusListeners.dispatch(new RowFocusEvent(g, rowIndex));
                }

                if (hasCellFocusListener && (rowIndex != lastFocusedRow || colIndex != lastFocusedCol)) {
                    cellFocusListeners.dispatch(new CellFocusEvent(g, rowIndex, colIndex,
                            lastFocusedRow == rowIndex,
                            lastFocusedCol == colIndex));
                }
                
                lastFocusedRow = rowIndex;
                lastFocusedCol = colIndex;
            }

            @Override
            public void editorOpened(int rowIndex, int colIndex, int lockId) {
                EditorOpenEvent ev = new EditorOpenEvent(g, rowIndex, colIndex);
                editorOpenListeners.dispatch(ev);
                int[] disabled = ev.getDisabledColumns();
                if (disabled != null) {
                    ArrayList<Integer> disabledColumns = new ArrayList<Integer>();
                    for (int i : disabled) {
                        disabledColumns.add(i);
                    }
                    getRPC().setDisabledColumns(disabledColumns);
                }
                getRPC().unlockEditor(lockId);
            }

            @Override
            public void ping() {
                getLogger().info("Received ping");
            }

            @Override
            public void editorClosed(int rowIndex, int colIndex,
                    boolean wasCancelled) {
                editorCloseListeners.dispatch(new EditorCloseEvent(g, rowIndex, colIndex, wasCancelled));
            }

        }, FastNavigationServerRPC.class);

        extend(g);
    }

    private FastNavigationClientRPC getRPC() {
        return getRpcProxy(FastNavigationClientRPC.class);
    }

    @Override
    public FastNavigationState getState() {
        return (FastNavigationState) super.getState();
    }

    //
    // Tab capture
    //

    public void setAllowTabToChangeRow(boolean enable) {
        getState().allowTabRowChange = enable;
    }
      
    public boolean getAllowTabToChangeRow() {
        return getState().allowTabRowChange;
    }

    public void setSelectTextOnEditorOpen(boolean enable) {
        getState().selectTextOnEditorOpen = enable;
    }

    public boolean getSelectTextOnEditorOpen() {
        return getState().selectTextOnEditorOpen;
    }
    
    public void setAllowArrowToChangeRow(boolean enable) {
        getState().allowArrowRowChange = enable;
    }
    
    public boolean getAllowArrowToChangeRow() {
        return getState().allowArrowRowChange;
    }
    
    //
    // Editor opening
    //

    public void setOpenEditorOnTyping(boolean enable) {
        getState().openEditorOnType = enable;
    }

    public boolean getOpenEditorOnTyping() {
        return getState().openEditorOnType;
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
    // Editor close/cancel extra shortcuts
    //

    public void addEditorCloseShortcut(int code) {
        getState().closeShortcuts.add(code);
    }

    public void removeEditorCloseShortcut(int code) {
        getState().closeShortcuts.remove(code);
    }

    public void clearEditorCloseShortcut(int code) {
        getState().closeShortcuts.clear();
    }

    //
    // Event listeners
    //
    
    public void addRowEditListener(RowEditListener listener) {
        rowEditListeners.addListener(listener);
        getState().hasRowEditListener = true;
    }

    public void addCellEditListener(CellEditListener listener) {
        cellEditListeners.addListener(listener);
        getState().hasCellEditListener = true;
    }

    public void addCellFocusListener(CellFocusListener listener) {
        cellFocusListeners.addListener(listener);
        
        getState().hasFocusListener = true;
        getState().hasCellFocusListener = true;
        hasCellFocusListener = true;
    }

    public void addRowFocusListener(RowFocusListener listener) {
        rowFocusListeners.addListener(listener);
        
        getState().hasFocusListener = true;
        getState().hasRowFocusListener = true;
        hasRowFocusListener = true;
    }

    /**
     * Register editor open listener, which will let you control which columns
     * should be editable on a row-by-row basis as the editor opens. Note, that
     * adding this listener will cause the Grid to become disabled until the
     * server has processed the event.
     * 
     * @param listener
     *            an EditorListener instance
     */
    public void addEditorOpenListener(EditorOpenListener listener) {
        editorOpenListeners.addListener(listener);
        
        getState().hasEditorOpenListener = true;
    }
    
    public void addEditorCloseListener(EditorCloseListener listener) {
        editorCloseListeners.addListener(listener);
        
        getState().hasEditorCloseListener = true;
    }
}
