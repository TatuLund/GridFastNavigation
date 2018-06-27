package org.vaadin.patrik.events;

import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.components.grid.MultiSelectionModel;

/**
 * Event used to notify of editor being opened. Can be used
 * to disable some columns (in other words: make them read-only)  
 */
@SuppressWarnings("serial")
public class EditorOpenEvent<T> extends Component.Event {

    private int rowIndex;
    private int colIndex;
    private T item;
    private int[] disabledCols;
    private int offset = 0;
    private Grid<T> grid;
    
    public EditorOpenEvent(Component source, int row, int col, T item) {
        super(source);
        rowIndex = row;
        colIndex = col;
        this.item = item;
        grid = (Grid<T>) source;
        if (grid.getSelectionModel() instanceof MultiSelectionModel) offset = 1;
    }

    /**
     * Get index of the row where editor was opened
     * 
     * @return Index of the row where editor was opened
     */
    public int getRow() {
        return rowIndex;
    }
    
    /**
     * Get index of the column where editor was opened
     * 
     * @return Index of the column where editor was opened
     */
    public int getColumnIndex() {
        return colIndex-offset;
    }
    
    /**
     * Set additional columns that should be disable when Editor opens 
     * 
     * @param columns
     */
    public void disableColumns(int... columns) {
        disabledCols = columns;
    }

    /**
     * Returns the additional columns that should be disable when Editor opens (see: disableColumns).
     * This method is used internally. Note, if you have set columns non editable or disabled fields
     * via other API's, they are not counted.
     * 
     * @return returns additional columns to be disabled 
     */
    public int[] getDisabledColumns() {
        return disabledCols;
    }
    
    /**
     * Get item which was opened from underlying datasource
     * 
     * @return Item which is edited
     */
	public T getItem() {
		return item;
	}
}
