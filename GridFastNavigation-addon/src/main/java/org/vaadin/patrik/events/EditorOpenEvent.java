package org.vaadin.patrik.events;

import com.vaadin.ui.Component;

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
    
    public EditorOpenEvent(Component source, int row, int col, T item) {
        super(source);
        rowIndex = row;
        colIndex = col;
        this.item = item;
    }

    public int getRow() {
        return rowIndex;
    }
    
    public int getColumn() {
        return colIndex;
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
