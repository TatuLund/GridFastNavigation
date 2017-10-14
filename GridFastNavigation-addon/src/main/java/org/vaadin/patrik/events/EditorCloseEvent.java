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

    /**
     * Return true if the editor closed after canceling event
     * 
     * @return True if edit was canceled
     */
    public boolean wasCancelled() {
        return cancelled;
    }

    /**
     * Get the row where editor was before closing
     * 
     * @return The row index
     */
    public int getRow() {
        return rowIndex;
    }
    
    /**
     * Get the column where editor was before closing
     * 
     * @return The column index
     */
    public int getColumn() {
        return colIndex;
    }
    
}
