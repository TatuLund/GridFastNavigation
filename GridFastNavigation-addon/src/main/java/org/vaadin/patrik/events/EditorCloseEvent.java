package org.vaadin.patrik.events;

import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.components.grid.MultiSelectionModel;

/**
 * EditorCloseEvent is emitted when Editor is closed
 * 
 * @see org.vaadin.patrik.FastNavigation#addEditorCloseListener(org.vaadin.patrik.FastNavigation.EditorCloseListener)
 *
 * @param <T> Bean type of the Grid where {@link org.vaadin.patrik.FastNavigation} is being used
 */
@SuppressWarnings("serial")
public class EditorCloseEvent<T>  extends Component.Event {

    private boolean cancelled;
    private int rowIndex;
    private int colIndex;
    private int offset = 0;
    
    public EditorCloseEvent(Component source, int row, int col, boolean cancel) {
        super(source);
        rowIndex = row;
        colIndex = col;
        cancelled = cancel;
        Grid<T> grid = (Grid<T>) source;
        if (grid.getSelectionModel() instanceof MultiSelectionModel) offset = 1;
    }

    /**
     * Was editor cancelled = true or saved = false
     * 
     * @return true if editor was cancelled
     */
    public boolean wasCancelled() {
        return cancelled;
    }
    
    /**
     * Get index of the column where editor was closed
     * 
     * @return Index of the column where editor was closed
     */
    public int getRow() {
        return rowIndex;
    }
    
    /**
     * Get index of the column where editor was closed
     * 
     * @return Index of the column where editor was closed
     */
    public int getColumnIndex() {
        return colIndex-offset;
    }
    
}
