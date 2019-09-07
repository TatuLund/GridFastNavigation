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
import org.vaadin.patrik.helper.OffsetHelper;
import org.vaadin.patrik.shared.FastNavigationClientRPC;
import org.vaadin.patrik.shared.FastNavigationServerRPC;
import org.vaadin.patrik.shared.FastNavigationState;

import com.vaadin.data.BinderValidationStatus;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.Grid;

/**
 * GridFastNavigation is a component extension for Vaadin Grid, which uses the
 * unbuffered editing mode and alters its keyboard controls to provide a faster
 * and advanced editing experience. This extension provides also number of other
 * improvements to Grid's unbuffered editors and features some bug fixes and
 * workarounds.
 * 
 * @author Tatu Lund
 *
 * @param <T> Bean type of the Grid
 */
@SuppressWarnings("serial")
public class FastNavigation<T> extends AbstractExtension {

    private static Logger _logger = Logger.getLogger("FastNavigation");
    
    private OffsetHelper offsetHelper = new OffsetHelper();

    private static Logger getLogger() {
        return _logger;
    }

    private Grid<T> grid = null;
    
    //
    // Event interfaces
    //

    /**
     * RowEditListener is used for observing {@link RowEditEvent}, which is emitted when item has been edited.
     * 
     * @see RowEditEvent
     * @see FastNavigation#addRowEditListener(RowEditListener)
     */
    public interface RowEditListener extends Listener<RowEditEvent<?>> {
    }
    
    private final EventListenerList<RowEditListener, RowEditEvent<?>> rowEditListeners = new EventListenerList<RowEditListener, RowEditEvent<?>>();

    /**
     * CellEditListener is used for observing {@link CellEditEvent}, which is emitted when item has been edited.
     * 
     * @see CellEditEvent
     * @see FastNavigation#addCellEditListener(CellEditListener)
     */
    public interface CellEditListener extends Listener<CellEditEvent<?>> {
    }
    
    private final EventListenerList<CellEditListener, CellEditEvent<?>> cellEditListeners = new EventListenerList<CellEditListener, CellEditEvent<?>>();

    /**
     * CellFocusListener is used for observing {@link CellFocusEvent}, which is emitted when focused cell changes
     * 
     * @see CellFocusEvent
     * @see FastNavigation#addCellFocusListener(CellFocusListener)
     */
    public interface CellFocusListener extends Listener<CellFocusEvent<?>> {
    }
    
    private final EventListenerList<CellFocusListener, CellFocusEvent<?>> cellFocusListeners = new EventListenerList<CellFocusListener, CellFocusEvent<?>>();

    /**
     * RowFocusListener is used for observing {@link RowFocusEvent}, which is emitted when focused row changes
     * 
     * @see RowFocusEvent
     * @see FastNavigation#addRowFocusListener(RowFocusListener)
     * 
     */
    public interface RowFocusListener extends Listener<RowFocusEvent<?>> {
    }
    
    private final EventListenerList<RowFocusListener, RowFocusEvent<?>> rowFocusListeners = new EventListenerList<RowFocusListener, RowFocusEvent<?>>();

    /**
     * EditorOpenListener is used for observing {@link EditorOpenEvent}, which is emitted when Editor has been opened.
     * Note: Editor is closed and opened when it is moved to a new orw
     * 
     * @see EditorOpenEvent 
     * @see FastNavigation#addEditorOpenListener(EditorOpenListener)
     */
    public interface EditorOpenListener extends Listener<EditorOpenEvent<?>> {
    }
    
    private final EventListenerList<EditorOpenListener, EditorOpenEvent<?>> editorOpenListeners = new EventListenerList<EditorOpenListener, EditorOpenEvent<?>>();

    /**
     * EditorOpenListener is used for observing {@link EditorCloseEvent}, which is emitted when Editor has been closed.
     * Note: Editor is closed and opened when it is moved to a new orw
     *
     * @see EditorCloseEvent
     * @see FastNavigation#addEditorCloseListener(EditorCloseListener)
     */
    public interface EditorCloseListener extends Listener<EditorCloseEvent<?>> {
    }
    
    private final EventListenerList<EditorCloseListener, EditorCloseEvent<?>> editorCloseListeners = new EventListenerList<EditorCloseListener, EditorCloseEvent<?>>();

    /**
     * ClickOutListener is used for observing {@link ClickOutEvent} which is simulated blur event, and emitted when user clicks outside of the Grid
     * 
     * @since 2.1.8
     * 
     * @see ClickOutEvent
     * @see FastNavigation#addClickOutListener(ClickOutListener)
     */
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
    private T editedItem = null;
    private int editedRow = -1;
    private T previousEditedItem = null;
    private int previousEditedRow = -1;

    /**
     * Default constructor. Enter key changes the row.
     * 
     * @param g Grid to extend
     */
    public FastNavigation(final Grid<T> g) {
    	setupFastNavigation(g,false,false);
    }

    /**
     * Alternative constructor to set enter key change column instead of a row.
     * 
     * @param g Grid to extend
     * @param changeColumnOnEnter Set Enter key behavior true = Enter changes the column like tab, false = Enter changes the row
     */
    public FastNavigation(final Grid<T> g, boolean changeColumnOnEnter) {
    	setupFastNavigation(g,changeColumnOnEnter,false);
    }
    
    /**
     * Alternative constructor to set enter key change column instead of a row.
     * 
     * @param g Grid to extend
     * @param changeColumnOnEnter Set Enter key behavior true = Enter changes the column like tab, false = Enter changes the row
     * @param dispatchEditEventOnBlur Set Blur event behavior. If set to true, Editor is closed and possible Edit event is dispatched when user clicks outside Grid
     */
    public FastNavigation(final Grid<T> g, boolean changeColumnOnEnter, boolean dispatchEditEventOnBlur) {
    	setupFastNavigation(g,changeColumnOnEnter,dispatchEditEventOnBlur);
    }
    
    // Internal implementation of the constructor
    private void setupFastNavigation(final Grid<T> g, boolean changeColumnOnEnter, boolean dispatchEditEventOnBlur) {
    	getState().changeColumnOnEnter = changeColumnOnEnter;
    	getState().dispatchEditEventOnBlur = dispatchEditEventOnBlur;
    	grid = g;
        grid.getEditor().setBuffered(false);
        
        registerRpc(new FastNavigationServerRPC() {

        	private T getItemAt(int rowIndex) {
        		T myBean = null;
        		if (rowIndex >= 0 && g.getDataCommunicator().getDataProviderSize() > 0) {
        			myBean = g.getDataCommunicator().fetchItemsWithRange(rowIndex, 1).get(0);
        		}
        		return myBean;
        	}
        	
        	@Override
            public void rowUpdated(int rowIndex, boolean moved) {
            	T item = null;
            	int row = rowIndex;
            	if (!moved) {
            		item = getItemAt(rowIndex);
            	} else {
            		item = previousEditedItem;
            		row = previousEditedRow;
            	}
                rowEditListeners.dispatch(new RowEditEvent<T>(grid, row, item));
            }

            @Override
            public void cellUpdated(int rowIndex, int colIndex, String newData, String oldData, boolean moved) {
            	T item = null;
            	int row = rowIndex;
            	if (!moved) {
            		item = getItemAt(rowIndex);
            	} else {
            		item = previousEditedItem;
            		row = previousEditedRow;
            	}
            	int offset = offsetHelper.calculateOffset(g);
                cellEditListeners.dispatch(new CellEditEvent<T>(grid, row, colIndex - offset, newData, oldData, item));
            }

            @Override
            public void focusUpdated(int rowIndex, int colIndex) {
            	T item = getItemAt(rowIndex);
            	int offset = offsetHelper.calculateOffset(g); 
            	colIndex = colIndex - offset; // apply offset based on selection mode
                if (hasRowFocusListener && rowIndex != lastFocusedRow) {
                    rowFocusListeners.dispatch(new RowFocusEvent<T>(grid, rowIndex, item));
                }

                if (hasCellFocusListener && (rowIndex != lastFocusedRow || colIndex != lastFocusedCol)) {
                    cellFocusListeners.dispatch(new CellFocusEvent<T>(grid, rowIndex, colIndex,
                            lastFocusedRow != rowIndex,
                            lastFocusedCol != colIndex, item));
                }
                
                lastFocusedRow = rowIndex;
                lastFocusedCol = colIndex;
            }

            @Override
            public void editorOpened(int rowIndex, int colIndex, int lockId) {
            	T item = getItemAt(rowIndex);
            	int offset = offsetHelper.calculateOffset(grid);
            	previousEditedItem = editedItem;
            	previousEditedRow = editedRow;
            	editedItem = item;
            	editedRow = rowIndex;
                EditorOpenEvent<T> ev = new EditorOpenEvent<>(grid, rowIndex, colIndex - offset, item);
                editorOpenListeners.dispatch(ev);
                // Update disabled columns or readonly fields status if changed dynamically
                ArrayList<Integer> disabledColumns = new ArrayList<Integer>();
                for (int i=0;i<g.getColumns().size();i++) {
                	if (!grid.getColumns().get(i).isEditable()) {
                		if (!disabledColumns.contains(i)) disabledColumns.add(i+offset);
                	} else if ((grid.getColumns().get(i).getEditorBinding() != null) && 
                			g.getColumns().get(i).getEditorBinding().getField().isReadOnly()) {
                		if (!disabledColumns.contains(i)) disabledColumns.add(i+offset);
                    }
                }
                Integer[] disabled = ev.getDisabledColumns();
                if (disabled != null) {
                    for (int i : disabled) {
                    	if (!disabledColumns.contains(i)) disabledColumns.add(i+offset);
                    }
                }
                getRPC().setDisabledColumns(disabledColumns);
                getRPC().unlockEditor(lockId);
            }

            @Override
            public void ping() {
                getLogger().info("Received ping");
            }

            @Override
            public void editorClosed(int rowIndex, int colIndex,
                    boolean wasCancelled) {
                editorCloseListeners.dispatch(new EditorCloseEvent<T>(grid, rowIndex, colIndex, wasCancelled));
            }

			@Override
			public void clickOut() {
				clickOutListeners.dispatch(new ClickOutEvent(grid));
			}
			
			@Override
			public void forceValidate(boolean move) {
				BinderValidationStatus<T> status = grid.getEditor().getBinder().validate();
				if (status.hasErrors() && move) getRPC().validationHasErrors();
			}

        }, FastNavigationServerRPC.class);

        extend(grid);
    }

    private FastNavigationClientRPC getRPC() {
        return getRpcProxy(FastNavigationClientRPC.class);
    }

    @Override
    public FastNavigationState getState() {
        return (FastNavigationState) super.getState();
    }

    
    /**
     * Set focused cell programmatically and scrolls Grid to target if focus was changed
     * 
     * Note: This method works correctly only after Grid has been fully rendered, since
     * otherwise target cell might not be in the DOM yet.
     * 
     * @since 2.3.4
     * 
     * @param row Target row
     * @param col Target column
     * @throws IllegalArgumentException if row or column is not in acceptable range
     */
    public void setFocusedCell(int row, int col) {
    	setFocusedCell(row,col,false);
    }
    
    
    /**
     * If enabled Editor on the row that has been selected will be augmented with
     * v-grid-editor-selected class name for additional styling.
     *
     * Note: This feature has been added to Vaadin 8.9 and no need to use with it
     *
     * @since 2.4.6
     *
     * @param enabled If true, then editor on selected row will have -selected style
     */
    public void enableEditorSelectedStyle(boolean enabled) {
    	getState().enableSelectedStyle = enabled;
    }
    
    /**
     * Set focused cell programmatically and scrolls Grid to target if focus was changed.
     * The method attempts to wait until Gird is rendered if wait is set to true.
     * 
     * @since 2.4.0
     * 
     * @param row Target row
     * @param col Target column
     * @param wait Wait until Grid is ready before setting the focused cell
     * @throws IllegalArgumentException if row or column is not in acceptable range
     */
    public void setFocusedCell(int row, int col, boolean wait) {
    	if (col < 0 || col > grid.getColumns().size() || row < 0 || row > grid.getDataCommunicator().getDataProviderSize()) throw new IllegalArgumentException("Target row or column out of boundaries");
    	getRPC().setFocusedCell(row, col, wait);
    }
    
    /**
     * If set to true (default = true), editor opens with single mouse click.
     *
     * Note, if this is set to true, Grid's selection listener and item click 
     * listeners will not get the click. Selection event will work with 
     * shift + space.
     * 
     * @param enable Boolean value
     */
    public void setOpenEditorWithSingleClick(boolean enable) {
      	getState().openEditorWithSingleClick = enable;
    }
    
    /**
     * 
     * @return true if Editor is set to open with single click
     */
    public boolean getOpenEditorWithSingleClick() {
        return getState().openEditorWithSingleClick;
    }

    /**
     * If set to true (default = false), pressing enter on last row will change
     * focus to first row and change column to next editable column. Not applicable
     * if enter key is set to change column instead of row.
     * 
     * @param enable Boolean value
     */
    public void setChangeColumnAfterLastRow(boolean enable) {
    	if (getState().changeColumnOnEnter) throw new IllegalStateException("Cannot set change column after last row if enter is set to change column");
        getState().changeColumnAfterLastRow = enable;
    }

    /**
     * 
     * @return true if column is set to change after last row
     */
    public boolean getChangeColumnAfterLastRow() {
        return getState().changeColumnAfterLastRow;
    }

    //
    // Tab capture
    //

    /**
     * If set to true (default = false), FastNavigation will attempt to trigger validation of the 
     * whole row, and closing of editor is not possible if the validation error indicator is on.
     * Also FastNavigation will not jump to first error column.
     * 
     * @since 2.2.2
     * 
     * @param enable Boolean value
     */
    public void setRowValidation(boolean enable) {
        getState().rowValidation = enable;
    }

    /**
     * Get status of row validation
     * 
     * @since 2.2.2
     * 
     * @return true if row validation mode is set on
     */
    public boolean getRowValidation() {
        return getState().rowValidation;
    }

    /**
     * If set to true, tabbing outside the edge of the current row will wrap the
     * focus around and switch to the next/previous row. If false, tabbing will
     * wrap around the current row.
     * 
     * @param enable Boolean value
     */
    public void setAllowTabToChangeRow(boolean enable) {
        getState().allowTabRowChange = enable;
    }
      
    /**
     * 
     * @return true if tabbing on last column changes the row
     */
    public boolean getAllowTabToChangeRow() {
        return getState().allowTabRowChange;
    }

    /**
     * If set to true, text is selected when editor is opened
     * 
     * @param enable Boolean value
     */
    public void setSelectTextOnEditorOpen(boolean enable) {
        getState().selectTextOnEditorOpen = enable;
    }

    /**
     * 
     * @return true if text in field is set to be selected when the Editor is being opened
     */
    public boolean getSelectTextOnEditorOpen() {
        return getState().selectTextOnEditorOpen;
    }

    /**
     * If set to true, you can use the arrow keys to move the editor up and down
     * 
     * @param enable Boolean value
     */
    public void setAllowArrowToChangeRow(boolean enable) {
        getState().allowArrowRowChange = enable;
    }
    
    /**
     * 
     * @return true if the arrow keys up and down are set to change the row
     */
    public boolean getAllowArrowToChangeRow() {
        return getState().allowArrowRowChange;
    }

    
    /**
     * If set to true (=default), home and end keys are used
     * to move to first and last row, and shifted home and end
     * to corners of the Grid.
     * 
     * @since 2.3.4
     * 
     * @param enable Boolean value
     */
    public void setHomeEndEnabled(boolean enable) {
        getState().homeEndEnabled = enable;
    }
    
    /**
     * 
     * @return true if home and end keys are enabled
     */
    public boolean getHomeEndEnabled() {
        return getState().homeEndEnabled;
    }

    //
    // Editor opening
    //

    /**
     * If set to true (default), focusing a Grid cell and then pressing an alpha-
     * numeric key will open the editor. If false, the editor must be activated
     * by double clicking or pressing ENTER or a custom editor opening shortcut key
     * 
     * @param enable Boolean value
     */
    public void setOpenEditorOnTyping(boolean enable) {
        getState().openEditorOnType = enable;
    }


    /**
     * 
     * @return true if Editor is set to be opened by pressing any key
     */
    public boolean getOpenEditorOnTyping() {
        return getState().openEditorOnType;
    }

    /**
     * Add extra Editor opening shortcut
     * 
     * @param code The keycode
     */
    public void addEditorOpenShortcut(int code) {
        getState().openShortcuts.add(code);
    }

    /**
     * Remove Editor opening shortcut 
     * 
     * @param code The keycode
     */
    public void removeEditorOpenShortcut(int code) {
        getState().openShortcuts.remove(code);
    }

    /**
     * Remove all extra Editor opening shortcuts
     */
    public void clearEditorOpenShortcuts() {
        getState().openShortcuts.clear();
    }

    /**
     * Add Editor close/cancel extra shortcut
     * 
     * @param code The keycode
     */
    public void addEditorCloseShortcut(int code) {
        getState().closeShortcuts.add(code);
    }

    /**
     * Remove Editor close/cancel extra shortcut 
     * 
     * @param code The keycode
     */
    public void removeEditorCloseShortcut(int code) {
        getState().closeShortcuts.remove(code);
    }

    /**
     * Remove all extra Editor close/cancel shortcuts
     */
    public void clearEditorCloseShortcuts() {
        getState().closeShortcuts.clear();
    }

    /**
     * Add Editor save extra shortcut
     * 
     * @param code The keycode
     */
    public void addEditorSaveShortcut(int code) {
        getState().saveShortcuts.add(code);
    }

    /**
     * Remove Editor save extra shortcut
     * 
     * @param code The keycode
     */
    public void removeSaveCloseShortcut(int code) {
        getState().saveShortcuts.remove(code);
    }

    /**
     * Remove all extra Editor save shortcuts
     */
    public void clearSaveCloseShortcuts() {
        getState().saveShortcuts.clear();
    }

    /**
     * Turn on saving by CTRL+S key combination
     * 
     * @since 2.2.11
     * 
     * @param enable Boolean value, true = CTRL+S saving enabled
     */
    public void setSaveWithCtrlS(boolean enable) {
    	getState().saveWithCtrlS = enable;
    }
    
    //
    // Event listeners
    //
    
    /**
     * Register row edit listener, which is triggered when cell value is being 
     * changed. Useful to hook e.g. database commit on edit.
     * 
     * @see RowEditEvent
     * 
     * @param listener
     *            an RowEditListener instance
     */
    public void addRowEditListener(RowEditListener listener) {
        rowEditListeners.addListener(listener);
        getState().hasRowEditListener = true;
    }

    /**
     * Register cell edit listener, which is triggered when cell value is being 
     * changed. Useful to hook e.g. database commit on edit.
     * 
     * @see CellEditEvent
     * 
     * @param listener
     *            an CellEditListener instance
     */
    public void addCellEditListener(CellEditListener listener) {
        cellEditListeners.addListener(listener);
        getState().hasCellEditListener = true;
    }

    /**
     * Register cell focus listener, which is triggered when focus has 
     * changed.
     * 
     * @see CellFocusEvent
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
     * @see RowFocusEvent
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
     * @see EditorOpenEvent
     * 
     * @param listener
     *            an EditorOpenListener instance
     */
    public void addEditorOpenListener(EditorOpenListener listener) {
        editorOpenListeners.addListener(listener);
        
        getState().hasEditorOpenListener = true;
    }

    /**
     * Register editor close listener, which is emitted each time editor is being closed.
     * 
     * @see EditorCloseEvent
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
     * @since 2.1.8
     * 
     * @see ClickOutEvent
     * 
     * @param listener a ClickOutListener instance
     */
    public void addClickOutListener(ClickOutListener listener) {
        clickOutListeners.addListener(listener);
        
        getState().hasClickOutListener = true;
    }

    /**
     * Get the current OffsetHelper
     * 
     * @since 2.3.10
     * 
     * @see FastNavigation#setOffsetHelper(OffsetHelper)
     * 
     * @return OffsetHelper 
     */
	public OffsetHelper getOffsetHelper() {
		return this.offsetHelper;
	}

	/**
	 * Use {@link OffsetHelper} to overwrite the calculation for internal offset of columns. 
	 * 
	 * @since 2.3.10
	 * 
	 * @param offsetHelper OffsetHelper instance to be used instead of the default implementation
	 */
	public void setOffsetHelper(OffsetHelper offsetHelper) {
		this.offsetHelper = offsetHelper;
	}

}
