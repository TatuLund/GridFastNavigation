package org.vaadin.patrik.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vaadin.patrik.client.SpinLock.Callback;
import org.vaadin.patrik.client.SpinLock.LockFunction;
import org.vaadin.patrik.shared.FastNavigationState;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.VConsole;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.ui.FocusUtil;
import com.vaadin.client.widget.grid.DefaultEditorEventHandler;
import com.vaadin.client.widget.grid.EventCellReference;
import com.vaadin.client.widgets.Grid;
import com.vaadin.client.widgets.Grid.Column;
import com.vaadin.client.widgets.Grid.Editor;
import com.vaadin.client.widgets.Grid.EditorDomEvent;
import com.vaadin.event.ShortcutAction.KeyCode;

public class EditorStateManager {

	private String oldContent;
	private String newContent;
	private boolean deletePressed = false;
	
	//
    // Editor listener callback interface
    // Used for communicating de-noised events back to the Connector/RPC layer
    //

    public interface EditorListener {

        void editorOpened(Grid<Object> grid, Editor<Object> editor,
                int row, int col, int lockId);

        void editorClosed(Grid<Object> grid, Editor<Object> editor, int row,
                int col, boolean cancel);

        void dataChanged(Grid<Object> grid, Editor<Object> editor,
                Widget widget, String newContent, int row,
                int col);
        
        void clickOut(Grid<Object> grid); 

    }

    //
    // Custom editor open/close/move behavior
    //
    
    private class CustomEditorHandler extends DefaultEditorEventHandler<Object> {
        
		//
        // Editor event handler - this receives an unfiltered event stream from the browser and
        // needs to identify the type of incoming event - handleOpenEvent gets _all_ events, for
        // example, and needs to return 'true' when it's actually been offered a valid open event.
        // It then also needs to handle that event.
        //

    	
    	private boolean isClickEvent(EditorDomEvent<Object> event) {
    		final Event e = event.getDomEvent();
    		return e.getTypeInt() == Event.ONCLICK;
    	}
        		
        @Override
        protected boolean handleOpenEvent(EditorDomEvent<Object> event) {
            
            if(isBusy()) return false;
            
            int key = event.getDomEvent().getKeyCode();
            boolean shift = event.getDomEvent().getShiftKey();
            boolean open = false;
            deletePressed = false;
            
            if (isOpenEvent(event) || (openEditorWithSingleClick && isClickEvent(event))) {
                open = true;
            } else if (isKeyPressEvent(event)) {
                if (openShortcuts.contains(key)) {
                    open = true;
                } else if (openEditorOnType) {
                    if (Keys.isAlphaNumericKey(key)) {
                        open = true;
                        queueKey(key, shift);
                    } else if (Keys.isDelKey(key)) {
                    	open = true;
                    	deletePressed = true;
                    }
                }
            }
            
            if (open) {
                final EventCellReference<?> cell = event.getCell();
                event.getDomEvent().preventDefault();
                openEditor(cell.getRowIndex(), cell.getColumnIndexDOM()); // TODO: IndexDOM or Index?
            }
            
            return open;
        }
        
        @Override
        protected boolean handleMoveEvent(EditorDomEvent<Object> event) {

            if(isBusy()) return false;
            
            //
            // Adapted from original Grid source
            //
            
            Event e = event.getDomEvent();
            final EventCellReference<Object> cell = event.getCell();
            
            if (e.getTypeInt() == Event.ONCLICK) {
            	saveContent();
                openEditor(cell.getRowIndex(), cell.getColumnIndexDOM());
                return true;
            }
            
            if (e.getTypeInt() == Event.ONKEYDOWN) {

                boolean move = false;
                
                final int columnCount = event.getGrid().getVisibleColumns().size();
                final int rowCount = event.getGrid().getDataSource().size();
                final int visibleRows = (int) event.getGrid().getHeightByRows();
                
                boolean shift = e.getShiftKey();
                int key = e.getKeyCode();
                int currentCol = getFocusedCol();
                int currentRow = getFocusedRow();
                
                int targetCol = currentCol;
                int targetRow = currentRow;
                
                if(Keys.isColumnChangeKey(key)) {
                	saveContent();
                    int colDelta = shift ? -1 : 1;
                    
                    // Remember to skip disabled columns
                    do {
                        targetCol += colDelta;
                    } while (disabledColumns.contains(targetCol) && targetCol < columnCount);
                    
                    // Test if we need to move up
                    if(targetCol < 0) {
                        if(allowTabRowChange) {
                            targetCol = columnCount - 1;
                            targetRow--;
                        } else {
                            targetCol = tabWrapping ? columnCount - 1 : 0;
                        }
                    }
                    
                    // Test if we need to move down
                    if(targetCol >= columnCount) {
                        if(allowTabRowChange) {
                            targetCol = 0;
                            targetRow++;
                        } else {
                            targetCol = tabWrapping ? 0 : columnCount - 1;
                        }
                    }
                    
                    move = true;
                }
                
                if(Keys.isRowChangeKey(key)) {
                	saveContent();
                	int rowDelta = shift ? -1 : 1;
                    
                    if(Keys.isUpDownArrowKey(key)) {
                        rowDelta = 0;
                        if(allowArrowRowChange && EditorWidgets.isUpDownNavAllowed(getCurrentEditorWidget())) {
                            if (key == KeyCodes.KEY_UP) {
                                rowDelta = -1;
                            } else if (key == KeyCodes.KEY_DOWN) {
                                rowDelta = 1;
                            } else if (key == KeyCodes.KEY_PAGEUP) {
                            	rowDelta = -visibleRows;
                            } else if (key == KeyCodes.KEY_PAGEDOWN) {
                            	rowDelta = visibleRows;
                            }
                        }
                    }
                    
                    targetRow += rowDelta;
                    
                    // Close editor if we're moving outside bounds - fixes Dan Golob's issue
                    // regarding single-column Grids, where close shortcuts will cancel changes.
                    // TODO: re-think this functionality when save-and-close shortcuts are available.
                    if (targetRow < 0) {
                        closeEditor(false);
                        targetRow = 0;
                    } else if (targetRow >= rowCount) {
                    	if (changeColumnAfterLastRow) {
                    		targetRow = 0;                    			
                            // Remember to skip disabled columns
                            do {
                                targetCol++;
                            } while (disabledColumns.contains(targetCol) && targetCol < columnCount);
                            move = true;
                            // If we were on last column do nothing
                            if (targetCol >= columnCount) {
                            	targetCol = columnCount-1;
                            	targetRow = rowCount-1;
                        		closeEditor(false);
                        		move = false;
                            }
                    	} else {
                    		closeEditor(false);
                    		targetRow = rowCount - 1;
                    	}
                    } else {
                        move = true;
                    }
                }

                if(Keys.isHomeKey(key)) {
                	saveContent();
                	targetRow = 0;
                	if (shift) targetCol = 0;
                    move = true;
                }
                
                if(Keys.isEndKey(key)) {
                	saveContent();
                	targetRow = rowCount-1;
                	if (shift) targetCol = columnCount-1;
                    move = true;
                }
                
                if (move) {
                    event.getDomEvent().preventDefault();
                    
                    if (currentCol != targetCol || currentRow != targetRow) {
                        triggerValueChange(event);
                        openEditor(targetRow, targetCol);
                    }
                }
                
                return move;
            }

            return false;        
        }

        @Override
        protected boolean handleCloseEvent(EditorDomEvent<Object> event) {
            
            if(isBusy()) return false;
            
            boolean hasValidationError = false;
            for (Column<?, Object> column : grid.getColumns()) {
            	if (grid.getEditor().isEditorColumnError(column)) hasValidationError = true;
            }
            
            //
            // This is actually the explicit _CANCEL_ or _SAVE_ of the editor. 
            //
            
            boolean close = false;
            boolean save = false;
            if (isCloseEvent(event)) {
                close = true;
            } else if (isKeyPressEvent(event)) {
                boolean ctrl = event.getDomEvent().getCtrlKey();
                int key = event.getDomEvent().getKeyCode();
                if (closeShortcuts.contains(key)) {
                    close = true;
                }
                if (!hasValidationError && (saveShortcuts.contains(key) || (saveWithCtrlS && ctrl && key == KeyCode.S))) {
                    close = true;
                    save = true;
                }
            }
            
            if (close) {
                event.getDomEvent().preventDefault();
                if (save) {
                	closeEditor(false);
                } else {
                	closeEditor(true);
                }
            }
            
            return close;
        }
        
        //
        // Helper(s) for identifying different types of events
        //

        private boolean isKeyPressEvent(EditorDomEvent<Object> event) {
            return event.getDomEvent().getTypeInt() == Event.ONKEYDOWN;
        }

        //
        // Value change trigger required for situations where the standard field
        // validation routines would not necessarily kick in until we try to save
        // the row. This is a hack, and should be considered as such.
        //
        private void triggerValueChange(EditorDomEvent<Object> event) {
            Widget editorWidget = event.getEditorWidget();
            if (editorWidget != null) {
                Element focusedElement = WidgetUtil.getFocusedElement();
                if (editorWidget.getElement().isOrHasChild(focusedElement)) {
                    focusedElement.blur();
                    focusedElement.focus();
                }
            }
        }
    }

    //
    // EditorStateManager
    //

    private Set<EditorListener> editorListeners = new LinkedHashSet<EditorListener>();
    
    private Element curtain;
    private Grid<Object> grid;
    private Grid.Editor<Object> editor;
    
    private List<Character> keybuf = new ArrayList<Character>();
    private Set<Integer> openShortcuts = new LinkedHashSet<Integer>();
    private Set<Integer> closeShortcuts = new LinkedHashSet<Integer>();
    private Set<Integer> saveShortcuts = new LinkedHashSet<Integer>();
    private Set<Integer> disabledColumns = new HashSet<Integer>();

    private RPCLock externalLocks;
    private boolean waitingForEditorOpen = false;
    private boolean useExternalLocking = false;
    
    private boolean openEditorOnType = true;
    private boolean openEditorWithSingleClick = true;
    private boolean allowTabRowChange = true;
    private boolean allowArrowRowChange = true;
    private boolean selectTextOnFocus = true;
    private boolean tabWrapping = true;
    private boolean changeColumnAfterLastRow = false;
    private boolean saveWithCtrlS = false;
    
    @SuppressWarnings("unchecked")
    public EditorStateManager(Grid<?> g, FastNavigationState state) {
        
    	Keys.setEnterBehavior(state.changeColumnOnEnter);
        grid = ((Grid<Object>) g);
        editor = grid.getEditor();
        editor.setEventHandler(new CustomEditorHandler());
        
        externalLocks = new RPCLock();
        
        // Create modality curtain
        // TODO: make this minimally obtrusive - constant movement is likely to cause flicker
        curtain = Document.get().createDivElement();
        curtain.setClassName("v-grid-editor-curtain");
        curtain.getStyle().setBackgroundColor("#777");
        curtain.getStyle().setOpacity(0);
        curtain.getStyle().setWidth(100, Unit.PCT);
        curtain.getStyle().setHeight(100, Unit.PCT);
        curtain.getStyle().setPosition(Position.ABSOLUTE);
        curtain.getStyle().setLeft(0, Unit.PX);
        curtain.getStyle().setRight(0, Unit.PX);
        curtain.getStyle().setZIndex(90000);

        DOM.sinkEvents(curtain, Event.MOUSEEVENTS | Event.KEYEVENTS | Event.FOCUSEVENTS);
        DOM.setEventListener(curtain, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if ((event.getTypeInt() & Event.ONKEYDOWN) > 0) {
                    queueKey(event.getKeyCode(), event.getShiftKey());
                    event.stopPropagation();
                	event.preventDefault();
                }
            }
        });

        grid.addDomHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if(isBusy()) {
                    NativeEvent e = event.getNativeEvent();
                    queueKey(e.getKeyCode(), e.getShiftKey());
                    
                    event.stopPropagation();
                    event.preventDefault();
                }
            }
        }, KeyDownEvent.getType());

        if (state.dispatchEditEventOnBlur) Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
           	@Override
          	public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
           		if ((event.getTypeInt() == Event.ONMOUSEDOWN)) {
                    int x1 = grid.getAbsoluteLeft();
                    int y1 = grid.getAbsoluteTop();
                    int y2 = y1 + grid.getOffsetHeight();
                    int x2 = x1 + grid.getOffsetWidth();                    
         			Event nativeEvent = Event.as(event.getNativeEvent());
         			int ex = nativeEvent.getClientX();
         			int ey = nativeEvent.getClientY();
           			if (!((x1 < ex && ex < x2) && (y1 < ey && ey < y2))) {
           				if (isEditorOpen()) {
           					saveContent();
           					Element focusedElement = WidgetUtil.getFocusedElement();
           					Widget editorWidget = getCurrentEditorWidget();
           					if (editorWidget.getElement().isOrHasChild(focusedElement)) {
           						focusedElement.blur();
           						focusedElement.focus();
           					}
           					closeEditor(false);
           				}
           				notifyClickOut();
           			}            			
           		}
           	}
         });
            
        
        // TODO: fix listening for keyboard while locked
    }
    
    private boolean isBusy() {
        boolean busy = (useExternalLocking && externalLocks.isLocked())
                || waitingForEditorOpen;
        return busy;
    }

    // Apply the input-blocking curtain
    private void lock() {
        grid.getElement().appendChild(curtain);
    }

    // Remove the input-blocking curtain
    private void unlock() {
        try {
            grid.getElement().removeChild(curtain);
        } catch (Exception ignore) {
            /* ignored */
        }
    }
    
    //
    // Spinlocks
    //
    
    private void waitForEditorOpen() {
        SpinLock.lock(new LockFunction() {
            @Override
            public boolean execute() {
                return (useExternalLocking && externalLocks.isLocked()) || !GridViolators.isEditorReallyActive(editor);
            }
        }, new Callback() {
            
            @Override
            public void complete() {
            
                unlock();
                
                // Reset all editor widgets to enabled
                for (int i = 0, l = grid.getVisibleColumns().size(); i < l; ++i) {
                    EditorWidgets.enable(getEditorWidgetForColumn(i));
                }
                
                // Then disable the ones that should be disabled
                for (int column : disabledColumns) {
                    EditorWidgets.disable(getEditorWidgetForColumn(column));
                }
                
                Widget editorWidget = getCurrentEditorWidget();

                // Check required to avoid overwriting disabled editors
                int currentCol = getFocusedCol();
                if (!disabledColumns.contains(currentCol)) {
                	saveOldContent();
                    // Handle possible value reset of editor widget
                    String buf = flushKeys();
                    if(!buf.trim().isEmpty() && !deletePressed) {
                        if (selectTextOnFocus) {
                            EditorWidgets.setValue(editorWidget, buf);
                        } else {
                            EditorWidgets.setValue(editorWidget, EditorWidgets.getValue(editorWidget) + buf);
                        }
                        
                    } else {
                        // Select text if desired
                        if (selectTextOnFocus) {
                            EditorWidgets.selectAll(editorWidget);
                        }
                    }
                    if (deletePressed) {
                        EditorWidgets.setValue(editorWidget,"");
                        deletePressed = false;
                    }
                    
                } else {

                    // Try to shunt focus to another widget 
                    int origCol = currentCol;
                    {
                        
                        // Try going right first
                        while(disabledColumns.contains(++currentCol)) {}
                        if(currentCol < grid.getVisibleColumns().size()) {
                            // Move editor focus here
                            editorWidget = getEditorWidgetForColumn(currentCol);
                            openEditor(getFocusedRow(), currentCol);
                            return;
                        }
                        
                        // Reset currentCol
                        currentCol = origCol;
                        
                        // Try going left instead
                        while(disabledColumns.contains(--currentCol)) {}
                        if(currentCol >= 0) {
                            // Move editor focus here
                            editorWidget = getEditorWidgetForColumn(currentCol);
                            openEditor(getFocusedRow(), currentCol);
                            return;
                        }
                        
                        editorWidget = null;
                    }
                    
                    if (selectTextOnFocus && !deletePressed) {
                        EditorWidgets.selectAll(editorWidget);
                    }
                    if (deletePressed) {
                        EditorWidgets.setValue(editorWidget,"");
                        deletePressed = false;
                    }
                    
                }
                
                // Make sure editor widget is in focus
                EditorWidgets.focus(editorWidget);
                
                // XXX: this function can _conceivably_ get stuck if there are _no_ editable columns 
            }
        });
        lock();
    }

    private void waitForEditorReady() {
        SpinLock.lock(new LockFunction() {
            @Override
            public boolean execute() {
                return !GridViolators.isEditorReallyActive(editor);
            }
        }, new Callback() {
            @Override
            public void complete() {
                
                unlock();
                
                Widget editorWidget = getCurrentEditorWidget();
                if(selectTextOnFocus) {
                    EditorWidgets.selectAll(editorWidget);
                }
                EditorWidgets.focus(editorWidget);
            }
        });
        lock();
    }
    

    //
    // Shortcuts
    //

    public void addOpenShortcut(int key) {
        openShortcuts.add(key);
    }

    public void clearOpenShortcuts() {
        openShortcuts.clear();
    }

    public void addCloseShortcut(int key) {
        closeShortcuts.add(key);
    }

    public void clearCloseShortcuts() {
        closeShortcuts.clear();
    }

    public void addSaveShortcut(int key) {
    	saveShortcuts.add(key);
    }
    
    public void clearSaveShortcuts() {
    	saveShortcuts.clear();
    }
    
    //
    // Listeners
    //

    public void addListener(EditorListener listener) {
        editorListeners.add(listener);
    }

    private void notifyEditorOpened(int row, int col) {
        int lock = 0;
        if(useExternalLocking) {
            lock = externalLocks.requestLock();
        }
        for (EditorListener l : editorListeners) {
            l.editorOpened(grid, editor, row, col, lock);
        }
    }

    private void notifyEditorClosed(int row, int col, boolean cancel) {
        for (EditorListener l : editorListeners) {
            l.editorClosed(grid, editor, row, col, cancel);
        }
    }

    private void notifyDataChanged(String newContent,
            int row, int col) {
        for (EditorListener l : editorListeners) {
            l.dataChanged(grid, editor, getCurrentEditorWidget(),
                    newContent, row, col);
        }
    }

    private void notifyClickOut() {
        for (EditorListener l : editorListeners) {
            l.clickOut(grid);
        }
    }    

    //
    // State
    //

    public boolean isEditorOpen() {
        return grid.isEditorActive();
    }

    public void setWaitForExternalUnlock(boolean enable) {
        useExternalLocking = enable;
        if(!enable) {
            externalLocks.clearLocks();
        }
    }

    // Set internal wait for external state to false. This releases the lock on
    // the grid.
    public void externalUnlock(int lockId) {
        externalLocks.releaseLock(lockId);
    }

    public void setDisabledColumns(List<Integer> cols) {
        
        // Reset disabled column state
        clearDisabledColumns();
        
        // Add specified extra disabled columns
        for (int col : cols) {
            disabledColumns.add(col);
        }
        
        if(!GridViolators.isEditorReallyClosed(editor)) {
            SpinLock.lock(new LockFunction() {
                @Override
                public boolean execute() {
                    return !GridViolators.isEditorReallyActive(editor);
                }
            }, new Callback() {
                @Override
                public void complete() {
                    // Reset all editor widgets to enabled
                    for(int i = 0, l = grid.getVisibleColumns().size(); i < l; ++i) {
                        EditorWidgets.enable(getEditorWidgetForColumn(i));
                    }
                    
                    // Then disable the ones that should be disabled
                    for (int column : disabledColumns) {
                        EditorWidgets.disable(getEditorWidgetForColumn(column));
                    }
                }
            });
        }
    }
    
    public void clearDisabledColumns() {
     // Clear all disabled columns
        disabledColumns.clear();
        
        // Add currently non-editable columns
        int i = 0;
        for(Column<?, Object> c : grid.getVisibleColumns()) {
            if(!c.isEditable()) {
                disabledColumns.add(i);
            }
            ++i;
        }
    }

    public void setOpenEditorWithSingleClick(boolean enable) {
    	openEditorWithSingleClick = enable;
    }

    // If set to true, text is selected when editor is opened
    public void setSelectTextOnFocus(boolean enable) {
        selectTextOnFocus = enable;
    }
    
    // If set to true, you can use the arrow keys to move the editor up and down
    public void setAllowRowChangeWithArrow(boolean enable) {
        allowArrowRowChange = enable;
    }
    
    // If set to true, tabbing outside the edge of the current row will wrap the
    // focus around and switch to the next/previous row. If false, tabbing will
    // wrap around the current row.
    public void setAllowTabRowChange(boolean enable) {
        allowTabRowChange = enable;
    }
    
    // If set to true (default), tabbing past the end of the row will wrap back
    // to the first item in the row. If false, tabbing forward when at the last
    // cell or back when at the first cell has no effect.
    // This only has an effect if allowTabRowChange is false.
    public void setTabWrapping(boolean enable) {
        tabWrapping = enable;
    }

    // If set to true (default = false), pressing enter on last row will change
    // focus to first row and change column to next editable column. Not applicable
    // if enter key is set to change column instead of row.
    public void setChangeColumnAfterLastRow(boolean enable) {
    	changeColumnAfterLastRow = enable;
    }
    
    // If set to true (default), focusing a Grid cell and then pressing an alpha-
    // numeric key will open the editor. If false, the editor must be activated
    // by double clicking or pressing ENTER or a custom editor opening shortcut key
    public void setOpenEditorByTyping(boolean enable) {
        openEditorOnType = enable;
    }
    
    public void setSaveWithCtrlS (boolean enable) {
        saveWithCtrlS = enable;
    }
    
    public void saveOldContent() {
    	if (isEditorOpen()) {
    		oldContent = EditorWidgets.getValue(getCurrentEditorWidget());
    	}
    }

    public void saveOldContent(int col) {
    	if (isEditorOpen()) {
    		oldContent = EditorWidgets.getValue(getEditorWidgetForColumn(col));
    	}
    }

    public String getContent() {
        return EditorWidgets.getValue(getCurrentEditorWidget());
    }

    public void saveContent() {
    	if (isEditorOpen()) {
    		newContent = EditorWidgets.getValue(getCurrentEditorWidget());
    	}
    }
    
    public String getOldContent() {
        return oldContent;
    }

    public void resetContent() {
    	newContent = oldContent;
    }
    
    public void notifyIfDataChanged(int row, int col) {
    	if (isEditorOpen()) {
    		if ((oldContent != null) && !oldContent.equals(newContent)) {
    			notifyDataChanged(newContent,row,col);
    		}
    	}
    }

    // Request opening the editor. This function should be used internally instead
    // of the direct editor.editRow() calls.
    public void openEditor(int row, int col) {
        if(GridViolators.isEditorReallyClosed(editor)) {
            editor.editRow(row,col);
            notifyEditorOpened(row,col);
            waitForEditorOpen();
        } else {
            int oldRow = getFocusedRow();

            if(oldRow != row) {
                notifyEditorClosed(oldRow, col, false);
                editor.editRow(row,col);
                notifyEditorOpened(row,col);
                waitForEditorOpen();
            } else {
                editor.editRow(row,col);
                waitForEditorReady();
            }
        }
    }

    // Request closing the editor. This function should be used internally instead
    // of the direct editor.save() or editor.cancel() calls.
    public void closeEditor(boolean cancel) {
        int row = getFocusedRow();
        int col = getFocusedCol();
        
        if (cancel) {
        	EditorWidgets.setValue(getEditorWidgetForColumn(col), oldContent);
            editor.cancel();
        } else {
            editor.save();
            if ((oldContent != null) && !oldContent.equals(newContent)) {
            	notifyDataChanged(newContent,row,col);
            }
        }

        notifyEditorClosed(row, col, cancel);
        FocusUtil.setFocus(grid, true);
    }

    //
    // Keybuffer management
    //

    private void queueKey(int sym, boolean shift) {
        if (Keys.isAlphaNumericKey(sym)) {
            char keychar = (char) sym;
            if (shift) {
                keychar = Character.toUpperCase(keychar);
            } else { 
                keychar = Character.toLowerCase(keychar);
            }
            keybuf.add(keychar);
        }
    }

    private String flushKeys() {
        StringBuilder sb = new StringBuilder();
        for (char c : keybuf) {
            sb.append(c);
        }
        keybuf.clear();
        return sb.toString();
    }

    //
    // Editor state info
    //

    private int getFocusedRow() {
        return editor.getRow();
    }

    private int getFocusedCol() {
        return GridViolators.getEditorColumn(editor);
    }

    private Widget getEditorWidgetForColumn(int index) {
        Column<?, Object> column = grid.getColumn(index);
        Map<Column<?, ?>, Widget> editorColumnToWidgetMap = GridViolators
                .getEditorColumnToWidgetMap(editor);
        Widget widget = editorColumnToWidgetMap.get(column);
        return widget;
    }

    private Widget getCurrentEditorWidget() {
        return getEditorWidgetForColumn(GridViolators.getFocusedCol(grid));
    }

}
