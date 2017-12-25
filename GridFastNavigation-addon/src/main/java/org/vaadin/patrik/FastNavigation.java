package org.vaadin.patrik;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.vaadin.patrik.events.CellEditEvent;
import org.vaadin.patrik.events.CellFocusEvent;
import org.vaadin.patrik.events.ClickOutEvent;
import org.vaadin.patrik.events.EditorCloseEvent;
import org.vaadin.patrik.events.EditorOpenEvent;
import org.vaadin.patrik.events.EventListenerList;
import org.vaadin.patrik.events.Listener;
import org.vaadin.patrik.events.RowEditEvent;
import org.vaadin.patrik.events.RowFocusEvent;
import org.vaadin.patrik.shared.FastNavigationClientRPC;
import org.vaadin.patrik.shared.FastNavigationServerRPC;
import org.vaadin.patrik.shared.FastNavigationState;

import com.vaadin.data.Container.Indexed;
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

    public interface ClickOutListener extends Listener<ClickOutEvent> {
    }
    
    private final EventListenerList<ClickOutListener, ClickOutEvent> clickOutListeners = new EventListenerList<ClickOutListener, ClickOutEvent>();
    
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

    /**
     * Default constructor. Enter key changes the row.
     * 
     * @param g Grid to extend
     */
    public FastNavigation(final Grid g) {
    	setupFastNavigation(g,false,false);
    }

    /**
     * Alternative constructor to set enter key change column instead of a row.
     * 
     * @param g Grid to extend
     * @param changeColumnOnEnter Set Enter key behavior true = Enter changes the column like tab, false = Enter changes the row
     */
    public FastNavigation(final Grid g, boolean changeColumnOnEnter) {
    	setupFastNavigation(g,changeColumnOnEnter,false);
    }
    
    /**
     * Alternative constructor to set enter key change column instead of a row.
     * 
     * @param g Grid to extend
     * @param changeColumnOnEnter Set Enter key behavior true = Enter changes the column like tab, false = Enter changes the row
     * @param dispatchEditEventOnBlur Set Blur event behavior. If set to true, Editor is closed and possible Edit event is dispatched when user clicks outside Grid
     */
    public FastNavigation(final Grid g, boolean changeColumnOnEnter, boolean dispatchEditEventOnBlur) {
    	setupFastNavigation(g,changeColumnOnEnter,dispatchEditEventOnBlur);
    }
    
    private void setupFastNavigation(final Grid g, boolean changeColumnOnEnter, boolean dispatchEditEventOnBlur) {
    	getState().changeColumnOnEnter = changeColumnOnEnter;
    	getState().dispatchEditEventOnBlur = dispatchEditEventOnBlur;
        g.setEditorBuffered(false);
        g.setEditorEnabled(true);
        
        registerRpc(new FastNavigationServerRPC() {

            @Override
            public void rowUpdated(int rowIndex) {
            	Object itemId = getItemIdByRowIndex(g, rowIndex);
                rowEditListeners.dispatch(new RowEditEvent(g, rowIndex, itemId));
            }

			private Object getItemIdByRowIndex(final Grid g, int rowIndex) {
				Indexed ds = g.getContainerDataSource();
            	Object itemId = null;
            	if (rowIndex >= 0) itemId = ds.getIdByIndex(rowIndex);
				return itemId;
			}

            @Override
            public void cellUpdated(int rowIndex, int colIndex, String newData) {
            	Object itemId = getItemIdByRowIndex(g, rowIndex);
                cellEditListeners.dispatch(new CellEditEvent(g, rowIndex, colIndex, newData, itemId));
            }

            @Override
            public void focusUpdated(int rowIndex, int colIndex) {
            	Object itemId = getItemIdByRowIndex(g, rowIndex);
                if (hasRowFocusListener && rowIndex != lastFocusedRow) {
                    rowFocusListeners.dispatch(new RowFocusEvent(g, rowIndex, itemId));
                }

                if (hasCellFocusListener && (rowIndex != lastFocusedRow || colIndex != lastFocusedCol)) {
                    cellFocusListeners.dispatch(new CellFocusEvent(g, rowIndex, colIndex,
                            lastFocusedRow == rowIndex,
                            lastFocusedCol == colIndex, itemId));
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

			@Override
			public void clickOut() {
				clickOutListeners.dispatch(new ClickOutEvent(g));
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

    /**
     * If set to true (default = true), editor opens with single mouse click.
     * 
     * @param enable true (default = true), editor opens with single mouse click.
     */
    public void setOpenEditorWithSingleClick(boolean enable) {
    	getState().openEditorWithSingleClick = enable;
    }

    public boolean getOpenEditorWithSingleClick() {
        return getState().openEditorWithSingleClick;
    }
    
    /**
     * If set to true (default = false), pressing enter on last row will change
     * focus to first row and change column to next editable column. Not applicable
     * if enter key is set to change column instead of row.
     * 
     * @param enable Boolean value, see description of the method
     */
    public void setChangeColumnAfterLastRow(boolean enable) {
        getState().changeColumnAfterLastRow = enable;
    }

    public boolean getChangeColumnAfterLastRow() {
        return getState().changeColumnAfterLastRow;
    }

    //
    // Tab capture
    //

    /**
     * If set to true, tabbing outside the edge of the current row will wrap the
     * focus around and switch to the next/previous row. If false, tabbing will
     * wrap around the current row.
     * 
     * @param enable Boolean value, see description of the method
     */
    public void setAllowTabToChangeRow(boolean enable) {
        getState().allowTabRowChange = enable;
    }
      
    public boolean getAllowTabToChangeRow() {
        return getState().allowTabRowChange;
    }

    /**
     * If set to true, text is selected when editor is opened
     * 
     * @param enable Boolean value, see description of the method
     */
    public void setSelectTextOnEditorOpen(boolean enable) {
        getState().selectTextOnEditorOpen = enable;
    }

    public boolean getSelectTextOnEditorOpen() {
        return getState().selectTextOnEditorOpen;
    }

    /**
     * If set to true, you can use the arrow keys to move the editor up and down
     * 
     * @param enable Boolean value, see description of the method
     */
    public void setAllowArrowToChangeRow(boolean enable) {
        getState().allowArrowRowChange = enable;
    }
    
    public boolean getAllowArrowToChangeRow() {
        return getState().allowArrowRowChange;
    }
    
    //
    // Editor opening
    //

    /**
     * If set to true (default), focusing a Grid cell and then pressing an alpha-
     * numeric key will open the editor. If false, the editor must be activated
     * by double clicking or pressing ENTER or a custom editor opening shortcut key
     * 
     * @param enable Boolean value, see description of the method
     */
    public void setOpenEditorOnTyping(boolean enable) {
        getState().openEditorOnType = enable;
    }


    public boolean getOpenEditorOnTyping() {
        return getState().openEditorOnType;
    }

    /**
     * Editor opening extra shortcuts
     * 
     * @param code Key code
     */
    public void addEditorOpenShortcut(int code) {
        getState().openShortcuts.add(code);
    }

    public void removeEditorOpenShortcut(int code) {
        getState().openShortcuts.remove(code);
    }

    public void clearEditorOpenShortcuts() {
        getState().openShortcuts.clear();
    }

    /**
     * Editor close/cancel extra shortcuts
     * 
     * @param code Key code
     */
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
    
    /**
     * Register row edit listener, which is triggered when cell value is being 
     * changed. Useful to hook e.g. database commit on edit.
     * 
     * @param listener
     *            an RowEditListener instance
     */
    public void addRowEditListener(RowEditListener listener) {
        rowEditListeners.addListener(listener);
        getState().hasRowEditListener = true;
    }

    public void addCellEditListener(CellEditListener listener) {
        cellEditListeners.addListener(listener);
        getState().hasCellEditListener = true;
    }

    /**
     * Register cell focus listener, which is triggered when focus has 
     * changed. 
     * 
     * @param listener
     *            an CellFocusListener instance
     */
    public void addCellFocusListener(CellFocusListener listener) {
        cellFocusListeners.addListener(listener);
        
        getState().hasFocusListener = true;
        getState().hasCellFocusListener = true;
        hasCellFocusListener = true;
    }

    /**
     * Register row focus listener, which is triggered when row has 
     * changed. 
     * 
     * @param listener
     *            an RowFocusListener instance
     */
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
    

    /**
     * Register editor close listener, which is emitted each time editor is being closed.
     * 
     * @param listener an EditorCloseListener instance
     */
    public void addEditorCloseListener(EditorCloseListener listener) {
        editorCloseListeners.addListener(listener);
        
        getState().hasEditorCloseListener = true;
    }

    /**
     * Register click out listener, which is emitted when user clicks outside the
     * grid. This is not true blur event, since it is triggered by mouse only 
     * 
     * @param listener a ClickOutListener instance
     */
    public void addClickOutListener(ClickOutListener listener) {
        clickOutListeners.addListener(listener);
        
        getState().hasClickOutListener = true;
    }
}
