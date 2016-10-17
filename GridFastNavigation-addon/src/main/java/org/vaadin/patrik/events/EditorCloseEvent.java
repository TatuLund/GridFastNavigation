package org.vaadin.patrik.events;

import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class EditorCloseEvent  extends Component.Event {

    private boolean cancelled;
    private int rowIndex;
    private int colIndex;
    
    public EditorCloseEvent(Component source, int row, int col, boolean cancel) {
        super(source);
        rowIndex = row;
        colIndex = col;
        cancelled = cancel;
    }

    public boolean wasCancelled() {
        return cancelled;
    }
    
    public int getRow() {
        return rowIndex;
    }
    
    public int getColumn() {
        return colIndex;
    }
    
}
