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
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
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

import elemental.events.KeyboardEvent;

public class EditorStateManager {

    private static final Logger logger = Logger.getLogger("EditorStateManager");

    //
    //
    //

    public interface EditorListener {

        void editorOpened(Grid<Object> grid, Editor<Object> editor,
                Widget editorWidget, String keybuf, int row, int col);

        void editorMoved(Grid<Object> grid, Editor<Object> editor, int oldRow,
                int newRow, int oldCol, int newCol);

        void editorClosed(Grid<Object> grid, Editor<Object> editor, int row,
                int col, boolean cancel);

        void dataChanged(Grid<Object> grid, Editor<Object> editor,
                Widget widget, String oldContent, String newContent, int row,
                int col);

    }

    //
    //
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

    private boolean waitingForEditorClosed = false;
    private boolean wasEditorCanceled = false;
    
    private boolean openEditorOnType = true;
    private boolean tabCapture = false;
    
    @SuppressWarnings("unchecked")
    public EditorStateManager(Grid<?> g) {
        
        grid = ((Grid<Object>) g);
        editor = grid.getEditor();
        
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
                notifyEditorMoved(row, col, oldrow, oldcol);
            }
        });
        
        // Add Grid navigation handling hook
        grid.addDomHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                int keycode = event.getNativeEvent().getKeyCode();
                switch (keycode) {
                case KeyboardEvent.KeyCode.UP:
                    moveEditorUp();
                    break;
                case KeyboardEvent.KeyCode.DOWN:
                    moveEditorDown();
                    break;
                }
            }
        }, KeyDownEvent.getType());

        // Create super modality curtain
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
        
        // Actively run the unlock routine
        ///////////////////////////////////////////////////////////////
        final AnimationCallback locker = new AnimationCallback() {
            @Override
            public void execute(double timestamp) {
                
                /*
                if(isBusy()) {
                    lock();
                } else {
                    unlock();
                }
                
                if(waitingForEditorOpen) {
                    boolean open = GridViolators.isEditorReallyActive(editor);

                    if(open) {
                        waitingForEditorOpen = false;
                    }
                }
                
                if(waitingForEditorClosed) {
                    boolean closed = GridViolators.isEditorReallyClosed(editor);
                    
                    if(closed) {
                        waitingForEditorClosed = false;
                    }
                }
                */
                
                AnimationScheduler.get().requestAnimationFrame(this);
            }
        };
        AnimationScheduler.get().requestAnimationFrame(locker);
        
        editorTracker.start();
    }

    private void waitForEditorOpen() {
        waitingForEditorOpen = true;
        if (shouldWaitForExternal) {
            waitingForExternal = true;
        }
    }

    private void waitForEditorClosed() {
        waitingForEditorClosed = true;
    }

    @SuppressWarnings("unused")
    private class EditorHandler extends DefaultEditorEventHandler<Object> {

        @Override
        protected boolean handleOpenEvent(EditorDomEvent<Object> event) {
            int key = event.getDomEvent().getKeyCode();
            boolean shift = event.getDomEvent().getShiftKey();
            if (isOpenEvent(event) || isKeyPressEvent(event)) {
                
                if(!openShortcuts.contains(key) || !openEditorOnType && Keys.isAlphaNumericKey(key)) {
                    return false;
                }
                
                if(openEditorOnType && Keys.isAlphaNumericKey(key)) {
                    queueKey(key, shift);
                }
                
                final EventCellReference<?> cell = event.getCell();
                event.getDomEvent().preventDefault();

                editRow(event, cell.getRowIndex(), cell.getColumnIndexDOM());

                return true;
            }
            return false;
        }
        
        @Override
        protected boolean handleMoveEvent(EditorDomEvent<Object> event) {
            
            //
            // NOTE: copied verbatim from DefaultEditorEventHandler
            // We take full responsibility of moving or not moving the editor
            // based on disabled columns.
            //
            
            Event e = event.getDomEvent();
            final EventCellReference<Object> cell = event.getCell();

            // TODO: Move on touch events
            if (e.getTypeInt() == Event.ONCLICK) {

                editRow(event, cell.getRowIndex(), cell.getColumnIndexDOM());

                return true;
            }

            else if (e.getTypeInt() == Event.ONKEYDOWN) {

                int rowDelta = 0;
                int colDelta = 0;

                if (e.getKeyCode() == KEYCODE_MOVE_VERTICAL) {
                    rowDelta = (e.getShiftKey() ? -1 : +1);
                } else if (e.getKeyCode() == KEYCODE_MOVE_HORIZONTAL) {
                    colDelta = (e.getShiftKey() ? -1 : +1);
                    // Prevent tab out of Grid Editor
                    event.getDomEvent().preventDefault();
                }

                final boolean changed = rowDelta != 0 || colDelta != 0;

                if (changed) {

                    int columnCount = event.getGrid().getVisibleColumns().size();

                    int colIndex = event.getFocusedColumnIndex() + colDelta;
                    int rowIndex = event.getRowIndex() + rowDelta;

                    // Skip disabled columns
                    while(disabledColumns.contains(colIndex)) {
                        colIndex += colDelta;
                    }
                    
                    // Handle tab movement logic
                    // TODO: have tab wrap around on the same row
                    if(colIndex >= columnCount) {
                        rowIndex++;
                        colIndex = 0;
                    } else if(colIndex < 0) {
                        rowIndex--;
                        colIndex = columnCount - 1;
                    }
                    
                    int sz = event.getGrid().getDataSource().size();
                    if(rowIndex < 0) rowIndex = 0;
                    if(rowIndex >= sz) rowIndex = sz - 1;

                    editRow(event, rowIndex, colIndex);
                }

                return changed;
            }

            return false;
        }

        @Override
        protected boolean handleCloseEvent(EditorDomEvent<Object> event) {
            if (isCloseEvent(event)) {
                event.getEditor().cancel();
                FocusUtil.setFocus(event.getGrid(), true);
                return true;
            }
            return false;
        }

        private boolean isCancelEvent(EditorDomEvent<Object> event) {
            if (isKeyPressEvent(event)) {
                for (int k : closeShortcuts) {
                    if (event.getDomEvent().getKeyCode() == k) {
                        wasEditorCanceled = true;
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean isRowChangeEvent(EditorDomEvent<Object> event) {
            boolean result = false;

            // Check if row was changed with keyboard
            if (true) {
                final Event e = event.getDomEvent();
                result = isKeyPressEvent(event) && 
                         (wasRowChangedWithTab(event) || 
                         Keys.isRowChangeKey(event.getDomEvent().getKeyCode()));
            }

            // Check if row was changed with mouse
            if (result == false) {

            }

            return result;
        }

        private boolean wasRowChangedWithTab(EditorDomEvent<Object> event) {
            if (event.getDomEvent().getKeyCode() == KeyCodes.KEY_TAB) {

                boolean shift = event.getDomEvent().getShiftKey();
                int colDelta = (shift ? -1 : +1);
                int columnCount = event.getGrid().getVisibleColumns().size();
                int colIndex = event.getFocusedColumnIndex() + colDelta;
                int rowIndex = event.getRowIndex();

                // Skip disabled columns
                while (disabledColumns.contains(colIndex)) {
                    colIndex += colDelta;
                }

                // Handle row change with horizontal move when column goes out
                // of range.
                if ((colIndex >= columnCount
                        && rowIndex < event.getGrid().getDataSource().size()
                                - 1)
                        || (colIndex < 0 && rowIndex > 0)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isKeyPressEvent(EditorDomEvent<Object> event) {
            return event.getDomEvent().getTypeInt() == Event.ONKEYDOWN;
        }

        private boolean isMousePressEvent(EditorDomEvent<Object> event) {
            return event.getDomEvent().getTypeInt() == Event.ONMOUSEDOWN;
        }

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

    private void notifyEditorMoved(int row, int col, int oldRow,
            int oldCol) {
        for (EditorListener l : editorListeners) {
            l.editorMoved(grid, editor, oldRow, row, oldCol, col);
        }
    }

    private void notifyEditorClosed(int row, int col, boolean cancel) {
        for (EditorListener l : editorListeners) {
            l.editorClosed(grid, editor, row, col, cancel);
        }
    }

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
        if(editorTracker.isEditorOpen()) {
            EditorWidgets.disable(getEditorWidgetForColumn(col));
        }
    }

    public void clearDisabledColumns() {
        disabledColumns.clear();
    }

    //
    // Request editor open/close
    //

    public void setOpenEditorByTyping(boolean enable) {
        openEditorOnType = enable;
    }

    public void openEditor() {
        openEditor(getFocusedRow(), getFocusedCol());
    }

    public void openEditor(int row, int col) {
        if (isBusy())
            return;

        boolean moved = false;
        int oldRow = getFocusedRow();
        int oldCol = getFocusedCol();

        // Try to open editor again
        if (grid.isEditorActive()) {
            editor.save();
            moved = true;
        }

        // Request editor to open itself
        editor.editRow(row, col);

        if (moved) {
            notifyEditorMoved(oldRow, row, oldCol, col);
        }

        // Lock editor UI and wait for the timer to release it
        waitForEditorOpen();
    }

    public void closeEditor(boolean cancel) {
        if (cancel) {
            editor.cancel();
        } else {
            editor.save();
        }
        waitForEditorClosed();
    }

    public void moveEditorUp() {
        if (grid.isEditorActive()) {
            int row = editor.getRow();
            if (row > 0) {
                openEditor(row - 1, getFocusedCol());
            }
        }
    }

    public void moveEditorDown() {
        if (grid.isEditorActive()) {
            int row = editor.getRow();
            if (row < grid.getDataSource().size() - 1) {
                openEditor(row + 1, getFocusedCol());
            }
        }
    }

    //
    // Lock/Unlock/Busy
    //

    public boolean isBusy() {
        boolean busy = (shouldWaitForExternal && waitingForExternal)
                || waitingForEditorOpen || waitingForEditorClosed;
        if(busy) {
            logger.warning(
                    "We're busy: waiting for open=" + 
                    waitingForEditorOpen + 
                    ", should wait for external=" + 
                    shouldWaitForExternal + 
                    ", waiting for external=" +
                    waitingForExternal +
                    ", waiting for closed=" +
                    waitingForEditorClosed);
        }
        return busy;
    }

    private void lock() {
        logger.warning("Locking grid");
        grid.getElement().appendChild(curtain);
    }

    private void unlock() {
        logger.warning("Unlocking grid");
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
