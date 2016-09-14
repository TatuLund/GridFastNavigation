package org.vaadin.patrik.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.vaadin.patrik.FastNavigation;
import org.vaadin.patrik.shared.FastNavigationServerRPC;
import org.vaadin.patrik.shared.FastNavigationState;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.client.ui.VPopupCalendar;
import com.vaadin.client.ui.VTextField;
import com.vaadin.client.widget.grid.DefaultEditorEventHandler;
import com.vaadin.client.widget.grid.selection.SelectionModel;
import com.vaadin.client.widgets.Grid;
import com.vaadin.client.widgets.Grid.Column;
import com.vaadin.client.widgets.Grid.EditorDomEvent;
import com.vaadin.shared.ui.Connect;

import elemental.events.KeyboardEvent;

@Connect(FastNavigation.class)
@SuppressWarnings("serial")
public class FastNavigationConnector extends AbstractExtensionConnector {

    private final Logger logger = Logger.getLogger("FastNavigation");
    private static final Set<Integer> alphaNumSet = new HashSet<Integer>();
    
    private Grid<Object> grid;
    private Grid.Editor<Object> editor;
    private SelectionModel<Object> gridSelectionModel;
    private FastNavigationServerRPC rpc;
    private EditorHandler handler;
    private List<Character> inputBuffer;
    
    private boolean opening = false;

    static {
        
        //
        // Store alphanumeric keycodes
        //
        
        for (int i = KeyCodes.KEY_A; i <= KeyCodes.KEY_Z; i++) {
            alphaNumSet.add(i);
        }
        for (int i = KeyCodes.KEY_ZERO; i <= KeyCodes.KEY_NINE; i++) {
            alphaNumSet.add(i);
        }
        for (int i = KeyCodes.KEY_NUM_ZERO; i <= KeyCodes.KEY_NUM_NINE; i++) {
            alphaNumSet.add(i);
        }
    }
    
    /**
     * Is keyCode alpha numeric key [0-9a-zA-Z]
     * 
     * @param keyCode
     * @return
     */
    private static boolean isAlphaNumericKey(int keyCode) {
        return alphaNumSet.contains(keyCode);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected void extend(ServerConnector target) {
        handler = new EditorHandler();
        inputBuffer = new ArrayList<Character>();

        // Type-pun our Grid to be usable..
        grid = (Grid<Object>) ((ComponentConnector) target).getWidget();
        editor = grid.getEditor();
        editor.setEventHandler(handler);

        // Add up/down navigation logic and immediate edit logic
        grid.addDomHandler(createKeyDownHandler(), KeyDownEvent.getType());

        gridSelectionModel = grid.getSelectionModel();

        rpc = getRpcProxy(FastNavigationServerRPC.class);
        logger.info("Enabled GridFastNavigation for " + grid);
    }

    private KeyDownHandler createKeyDownHandler() {
        return new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                switch (event.getNativeEvent().getKeyCode()) {
                case KeyboardEvent.KeyCode.F2:
                    grid.getEditor().editRow(getFocusedRow());
                    break;
                case KeyboardEvent.KeyCode.UP:
                    moveUp();
                    break;
                case KeyboardEvent.KeyCode.DOWN:
                    moveDown();
                    break;
                }
                editImmediatelyIfAlphaNumKey(
                        event.getNativeEvent().getKeyCode(),
                        event.getNativeEvent().getShiftKey());
            }
        };
    }

    private void editImmediatelyIfAlphaNumKey(final Integer typedKeyCode,
            boolean shift) {
        if (!opening && !grid.isEditorActive()
                && isAlphaNumericKey(typedKeyCode)) {
            opening = true;
            inputBuffer.clear();
            handler.setPosition(getFocusedRow(), getFocusedCol());
            editor.editRow(getFocusedRow(), getFocusedCol());

            AnimationScheduler.get().requestAnimationFrame(
                    new AnimationScheduler.AnimationCallback() {
                        @Override
                        public void execute(double timestamp) {
                            if (!isEditorReallyActive()) {
                                AnimationScheduler.get()
                                        .requestAnimationFrame(this);
                            } else {
                                handler.updateWidgetFromPosition();
                                handler.focusCurrentInput();
                                opening = false;
                            }
                        }
                    });
        }
        if (opening) {
            char character = (char) typedKeyCode.intValue();
            if (!shift) {
                inputBuffer.add(Character.toLowerCase(character));
            } else {
                inputBuffer.add(character);
            }
        }
    }

    /**
     * Overridden EditorHandler class, used to override the Grid's standard
     * editor behavior.
     */
    private class EditorHandler extends DefaultEditorEventHandler<Object> {

        // TODO: Support all standard widget types.
        private Widget currentWidget = null;

        private int currentRowIndex = -1;
        private int currentColIndex = -1;

        private boolean somethingChanged = false;

        private void setPosition(int row, int col) {
            currentRowIndex = row;
            currentColIndex = col;
        }

        private boolean updatePosition(EditorDomEvent<Object> event) {
            int row = event.getCell().getRowIndex();
            int col = event.getCell().getColumnIndex();
            if (row != currentRowIndex || col != currentColIndex) {
                setPosition(row, col);
                updateWidgetFromPosition();
                return true;
            }
            return false;
        }

        private void updateWidgetFromPosition() {
            try {
                Column<?, Object> column = grid.getColumn(currentColIndex);
                currentWidget = getEditorWidgetForColumn(column);
            } catch (Exception ignore) {
                currentWidget = null;
            }
        }

        public void focusCurrentInput() {
            focusInput(currentWidget);
        }

        private void focusInput(Widget widget) {
            if (widget instanceof VTextField) {
                VTextField tf = (VTextField) widget;
                tf.selectAll();
                if (!inputBuffer.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (char i : inputBuffer) {
                        sb.append(i);
                    }
                    inputBuffer.clear();
                    tf.setValue(sb.toString());
                }
                tf.addChangeHandler(new ChangeHandler() {

                    @Override
                    public void onChange(ChangeEvent event) {
                        if (!somethingChanged) {
                            somethingChanged = true;
                        }
                    }
                });
            }

            if (widget instanceof VPopupCalendar) {
                VPopupCalendar df = (VPopupCalendar) widget;
                df.text.selectAll();
                // TODO add change listener
            }
        }

        @Override
        protected boolean handleMoveEvent(EditorDomEvent<Object> event) {
            boolean result = super.handleMoveEvent(event);
            if (updatePosition(event)) {
                focusInput(currentWidget);
            }
            return result;
        }

        @Override
        protected boolean handleCloseEvent(EditorDomEvent<Object> event) {
            boolean result = false;
            observeMouseClickOriginatedRowChange(event);
            if (isThisRowChangeEvent(event)) {
                triggerValueChange(event);
                fireRowChangeEventIfRowIsChanged(currentRowIndex);

                result = super.handleCloseEvent(event);
            } else if (isThisEditorCancelEvent(event)) {
                currentRowIndex = -1;
                currentColIndex = -1;
                result = super.handleCloseEvent(event);
            }
            return result;
        }

        protected boolean isThisRowChangeEvent(EditorDomEvent<Object> event) {
            return rowChangedWithKeyEvent(event);
        }

        private boolean isThisEditorCancelEvent(EditorDomEvent<Object> event) {
            final Event e = event.getDomEvent();
            return (isKeyPressEvent(e) && e.getKeyCode() == KEYCODE_CLOSE);
        }

        /**
         * Changes the value also on the server-side
         * 
         * @param event
         */
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

        private void fireRowChangeEventIfRowIsChanged(int changedRowIndex) {
            if (somethingChanged) {
                somethingChanged = false;
                fireRowChangeEvent(changedRowIndex);
            }
        }

        private boolean rowChangedWithKeyEvent(EditorDomEvent<Object> event) {
            final Event e = event.getDomEvent();
            return isKeyPressEvent(e) && (rowChangedWithTab(event, e)
                    || rowChangedWithEnterOrArrow(e.getKeyCode()));
        }

        private void observeMouseClickOriginatedRowChange(
                final EditorDomEvent<Object> event) {
            final Event e = event.getDomEvent();
            final int previousRowIndex = event.getRowIndex();
            if (isMousePressEvent(e)) {
                
                // Wait until the editor is moved before firing value change
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        if (currentRowIndex != previousRowIndex) {
                            triggerValueChange(event);
                            fireRowChangeEventIfRowIsChanged(previousRowIndex);
                        }
                    }
                });
            }
        }

        private boolean rowChangedWithEnterOrArrow(int keyCode) {
            return keyCode == KeyCodes.KEY_ENTER || keyCode == KeyCodes.KEY_UP
                    || keyCode == KeyCodes.KEY_DOWN
                    || keyCode == KeyCodes.KEY_MAC_ENTER;
        }

        private boolean isKeyPressEvent(final Event e) {
            return e.getTypeInt() == Event.ONKEYDOWN;
        }

        private boolean isMousePressEvent(final Event e) {
            return e.getTypeInt() == Event.ONMOUSEDOWN;
        }
    }

    private boolean rowChangedWithTab(EditorDomEvent<Object> event, Event e) {
        if (e.getKeyCode() == KeyCodes.KEY_TAB) {
            int colDelta = (e.getShiftKey() ? -1 : +1);
            int columnCount = event.getGrid().getVisibleColumns().size();
            int colIndex = event.getFocusedColumnIndex() + colDelta;
            int rowIndex = event.getRowIndex();

            // Handle row change with horizontal move when column goes out of range.
            if (colIndex >= columnCount
                    && rowIndex < event.getGrid().getDataSource().size() - 1) {
                return true;
            } else if (colIndex < 0 && rowIndex > 0) {
                return true;
            }
        }
        return false;
    }

    private void moveUp() {
        if (grid.isEditorActive()) {
            moveEditorUp();
            return;
        }
    }

    private void moveDown() {
        if (grid.isEditorActive()) {
            moveEditorDown();
            return;
        }
    }

    private void moveEditorUp() {
        int row = editor.getRow();
        if (row > 0) {
            editor.editRow(row - 1);
        }
    }

    private void moveEditorDown() {
        int row = editor.getRow();
        if (row < grid.getDataSource().size() - 1) {
            editor.editRow(row + 1);
        }
    }
    
    private void fireRowChangeEvent(int rowIndex) {
        rpc.rowUpdated(rowIndex);
    }

    private Widget getEditorWidgetForColumn(Column<?, Object> column) {
        Map<Column<?, Object>, Widget> editorColumnToWidgetMap = getEditorColumnToWidgetMap();
        Widget widget = editorColumnToWidgetMap.get(column);
        return widget;
    }
    
    @Override
    public FastNavigationState getState() {
        return (FastNavigationState)super.getState();
    }

    // ========================================================================
    // Violators - access Grid internals irrespective of visibility
    // ========================================================================

    private native final int getFocusedRow() /*-{
        var grid = this.@org.vaadin.patrik.client.FastNavigationConnector::grid;
        var cfh = grid.@com.vaadin.client.widgets.Grid::cellFocusHandler;
        var row = cfh.@com.vaadin.client.widgets.Grid.CellFocusHandler::rowWithFocus;
        return row;
    }-*/;

    private native final int getFocusedCol() /*-{
        var grid = this.@org.vaadin.patrik.client.FastNavigationConnector::grid;
        var cfh = grid.@com.vaadin.client.widgets.Grid::cellFocusHandler;
        var cell = cfh.@com.vaadin.client.widgets.Grid.CellFocusHandler::getFocusedCell()();
        var col = cell.@com.vaadin.client.widget.escalator.Cell::getColumn()();
        return col;
    }-*/;

    private native final Map<Column<?, Object>, Widget> getEditorColumnToWidgetMap() /*-{
        var editor = this.@org.vaadin.patrik.client.FastNavigationConnector::editor;
        return editor.@com.vaadin.client.widgets.Grid.Editor::columnToWidget;
    }-*/;

    private native final boolean isEditorReallyActive() /*-{
        var editor = this.@org.vaadin.patrik.client.FastNavigationConnector::editor;
        var state = editor.@com.vaadin.client.widgets.Grid.Editor::state;
        var ordinal = state.@com.vaadin.client.widgets.Grid.Editor.State::ordinal()();
        return ordinal == 3;
    }-*/;
}
