package org.vaadin.patrik.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.vaadin.patrik.client.EditorTracker.Listener;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.ui.FocusUtil;
import com.vaadin.client.widget.grid.DefaultEditorEventHandler;
import com.vaadin.client.widget.grid.EventCellReference;
import com.vaadin.client.widgets.Grid;
import com.vaadin.client.widgets.Grid.Column;
import com.vaadin.client.widgets.Grid.Editor;
import com.vaadin.client.widgets.Grid.EditorDomEvent;

public class EditorStateManager {

    private static final Logger logger = Logger.getLogger("EditorStateManager");

    //
    // Editor listener callback interface
    // Used for communicating de-noised events back to the Connector/RPC layer
    //

    public interface EditorListener {

        void editorOpened(Grid<Object> grid, Editor<Object> editor,
                Widget editorWidget, String keybuf, int row, int col);

        void editorClosed(Grid<Object> grid, Editor<Object> editor, int row,
                int col, boolean cancel);

        void dataChanged(Grid<Object> grid, Editor<Object> editor,
                Widget widget, String oldContent, String newContent, int row,
                int col);

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

        @Override
        protected boolean handleOpenEvent(EditorDomEvent<Object> event) {
            int key = event.getDomEvent().getKeyCode();
            boolean shift = event.getDomEvent().getShiftKey();
            boolean open = false;
            
            if (isOpenEvent(event)) {
                open = true;
            } else if(isKeyPressEvent(event)) {
                if(openShortcuts.contains(key)) {
                    open = true;
                } else if(openEditorOnType) {
                    if(Keys.isAlphaNumericKey(key)) {
                        open = true;
                        queueKey(key, shift);
                    }
                }
            }
            
            if(open) {
                final EventCellReference<?> cell = event.getCell();
                event.getDomEvent().preventDefault();
                openEditor(cell.getRowIndex(), cell.getColumnIndexDOM()); // TODO: IndexDOM or Index?
            }
            
            return open;
        }
        
        @Override
        protected boolean handleMoveEvent(EditorDomEvent<Object> event) {

            //
            // Adapted from original Grid source
            //
            
            Event e = event.getDomEvent();
            final EventCellReference<Object> cell = event.getCell();
            
            if (e.getTypeInt() == Event.ONCLICK) {
                openEditor(cell.getRowIndex(), cell.getColumnIndexDOM());
                return true;
            }
            
            if (e.getTypeInt() == Event.ONKEYDOWN) {

                boolean move = false;
                
                final int columnCount = event.getGrid().getVisibleColumns().size();
                final int rowCount = event.getGrid().getDataSource().size();
                
                boolean shift = e.getShiftKey();
                int key = e.getKeyCode();
                int currentCol = getFocusedCol();
                int currentRow = getFocusedRow();
                
                int targetCol = currentCol;
                int targetRow = currentRow;
                
                if(Keys.isColumnChangeKey(key)) {
                    int colDelta = shift ? -1 : 1;
                    
                    // Remember to skip disabled columns
                    do {
                        targetCol += colDelta;
                    } while(disabledColumns.contains(targetCol));
                    
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
                    int rowDelta = shift ? -1 : 1;
                    
                    if(Keys.isUpDownArrowKey(key)) {
                        rowDelta = 0;
                        if(allowArrowRowChange) {
                            if(key == KeyCodes.KEY_UP) {
                                rowDelta = -1;
                            } else if(key == KeyCodes.KEY_DOWN) {
                                rowDelta = 1;
                            }
                        }
                    }
                    
                    targetRow += rowDelta;
                    
                    // Clamp row number, assume we landed in a safe, non-disabled column
                    if(targetRow < 0) targetRow = 0;
                    if(targetRow >= rowCount) targetRow = rowCount - 1;
                        
                    move = true;
                }

                if(move) {
                    event.getDomEvent().preventDefault();
                    
                    if(currentCol != targetCol || currentRow != targetRow) {
                        openEditor(targetRow, targetCol);
                    }
                }
                
                return move;
            }

            return false;        
        }

        @Override
        protected boolean handleCloseEvent(EditorDomEvent<Object> event) {
            boolean close = false;
            if (isCloseEvent(event)) {
                close = true;
            } else if(isKeyPressEvent(event)) {
                int key = event.getDomEvent().getKeyCode();
                if(closeShortcuts.contains(key)) {
                    close = true;
                }
            }
            
            if(close) {
                event.getDomEvent().preventDefault();
                closeEditor(false);
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
        // TODO: use it :E
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
    private EditorTracker editorTracker;

    private List<Character> keybuf = new ArrayList<Character>();
    private Set<Integer> openShortcuts = new LinkedHashSet<Integer>();
    private Set<Integer> closeShortcuts = new LinkedHashSet<Integer>();
    private Set<Integer> disabledColumns = new HashSet<Integer>();

    private boolean shouldWaitForExternal = false;
    private boolean waitingForExternal = false;
    private boolean waitingForEditorOpen = false;

    private boolean wasEditorCanceled = false;
    
    private boolean openEditorOnType = true;
    private boolean allowTabRowChange = true;
    private boolean allowArrowRowChange = true;
    private boolean selectTextOnFocus = true;
    private boolean tabWrapping = true;
    
    @SuppressWarnings("unchecked")
    public EditorStateManager(Grid<?> g) {
        
        grid = ((Grid<Object>) g);
        editor = grid.getEditor();
        editor.setEventHandler(new CustomEditorHandler());
        
        // Create modality curtain
        // TODO: make this minimally obtrusive - constant movement is likely to cause flicker
        curtain = Document.get().createDivElement();
        curtain.getStyle().setBackgroundColor("#777");
        curtain.getStyle().setOpacity(0.5);
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
                if (isBusy() && (event.getTypeInt() & Event.ONKEYDOWN) > 0) {
                    queueKey(event.getKeyCode(), event.getShiftKey());
                }
                event.stopPropagation();
                event.preventDefault();
            }
        });
        
        // Add editor state tracker
        editorTracker = new EditorTracker(editor);
        editorTracker.addListener(new Listener() {
            @Override
            public void editorOpened(int row, int col) {
                notifyEditorOpened(getCurrentEditorWidget(), flushKeys(), row, col);
            }

            @Override
            public void editorClosed(int row, int col) {
                notifyEditorClosed(row, col, wasEditorCanceled);
            }

            @Override
            public void editorMoved(int row, int col, int oldrow, int oldcol) {
                // Only notify of row changes
                if(row != oldrow) {
                    notifyEditorClosed(oldrow,oldcol,false);
                    notifyEditorOpened(getCurrentEditorWidget(), flushKeys(), row, col);
                }
            }
        });
        editorTracker.start();
 
        //
        // Actively run the unlock routine
        // This code is supposed to place the event-swallowing curtain of doom
        // on top of the Grid to prevent people from entering data into fields
        // while the server could still be busy figuring out if they should be
        // allowed to do so.
        //
        
        final AnimationCallback locker = new AnimationCallback() {
            @Override
            public void execute(double timestamp) {
                if(grid.isEditorActive()) {
                    if(isBusy()) {
                        lock();
                    } else {
                        unlock();
                    }
                }
                
                if(waitingForEditorOpen) {
                    boolean open = GridViolators.isEditorReallyActive(editor);

                    if(open) {
                        waitingForEditorOpen = false;
                        if(selectTextOnFocus) {
                            EditorWidgets.selectAll(getCurrentEditorWidget());
                        }
                        EditorWidgets.focus(getCurrentEditorWidget());
                    }
                }
                
                AnimationScheduler.get().requestAnimationFrame(this);
            }
        };
        AnimationScheduler.get().requestAnimationFrame(locker);
    }

    private void waitForEditorOpen() {
        waitingForEditorOpen = true;
        if (shouldWaitForExternal) {
            waitingForExternal = true;
        }
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

    //
    // Listeners
    //

    public void addListener(EditorListener listener) {
        editorListeners.add(listener);
    }

    private void notifyEditorOpened(Widget editorWidget, String keybuf, int row,
            int col) {
        for (EditorListener l : editorListeners) {
            l.editorOpened(grid, editor, editorWidget, keybuf, row, col);
        }
    }

    private void notifyEditorClosed(int row, int col, boolean cancel) {
        for (EditorListener l : editorListeners) {
            l.editorClosed(grid, editor, row, col, cancel);
        }
    }

    // TODO: send notifications of changed data!
    private void notifyDataChanged(String oldContent, String newContent,
            int row, int col) {
        for (EditorListener l : editorListeners) {
            l.dataChanged(grid, editor, getCurrentEditorWidget(), oldContent,
                    newContent, row, col);
        }
    }

    //
    // State
    //

    public boolean isEditorOpen() {
        return grid.isEditorActive();
    }

    public void setWaitForExternalUnlock(boolean enable) {
        shouldWaitForExternal = enable;
    }

    // Set internal wait for external state to false. This releases the lock on
    // the grid.
    public void externalUnlock() {
        waitingForExternal = false;
    }

    public void setDisabledColumns(List<Integer> cols) {
        disabledColumns.clear();
        for (int col : cols) {
            disabledColumns.add(col);
            EditorWidgets.disable(getEditorWidgetForColumn(col));
        }
    }

    public void addDisabledColumn(int col) {
        disabledColumns.add(col);
        if(grid.isEditorActive()) {
            EditorWidgets.disable(getEditorWidgetForColumn(col));
        }
    }

    public void clearDisabledColumns() {
        disabledColumns.clear();
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
    
    // If set to true (default), focusing a Grid cell and then pressing an alpha-
    // numeric key will open the editor. If false, the editor must be activated
    // by double clicking or pressing ENTER or a custom editor opening shortcut key
    public void setOpenEditorByTyping(boolean enable) {
        openEditorOnType = enable;
    }

    // Request opening the editor. This function should be used internally instead
    // of the direct editor.editRow() calls.
    public void openEditor(int row, int col) {
        if(grid.isEditorActive()) {
            editor.editRow(row,col);
            if(selectTextOnFocus) {
                EditorWidgets.selectAll(getCurrentEditorWidget());
            }
        } else {
            editor.editRow(row,col);
            waitForEditorOpen();
        }
    }

    // Request closing the editor. This function should be used internally instead
    // of the direct editor.save() or editor.cancel() calls.
    public void closeEditor(boolean cancel) {
        if (cancel) {
            editor.cancel();
        } else {
            editor.save();
        }
        
        notifyEditorClosed(getFocusedRow(), getFocusedCol(), cancel);
        FocusUtil.setFocus(grid, true);
    }

    //
    // Lock/Unlock/Busy
    //

    public boolean isBusy() {
        boolean busy = (shouldWaitForExternal && waitingForExternal)
                || waitingForEditorOpen;
        if(busy) {
            logger.warning(
                    "We're busy: waiting for open=" + 
                    waitingForEditorOpen + 
                    ", should wait for external=" + 
                    shouldWaitForExternal + 
                    ", waiting for external=" +
                    waitingForExternal);
        }
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
    // Keybuffer management
    //

    private void queueKey(int sym, boolean shift) {
        if (Keys.isAlphaNumericKey(sym)) {
            char keychar = (char) sym;
            if (shift)
                keychar = Character.toUpperCase(keychar);
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
