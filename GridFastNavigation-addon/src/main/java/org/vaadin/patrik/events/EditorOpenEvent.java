package org.vaadin.patrik.events;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;

/**
 * Event used to notify of editor being opened. Can be used
 * to disable some columns (in other words: make them read-only)  
 * 
 * @see org.vaadin.patrik.FastNavigation#addEditorOpenListener(org.vaadin.patrik.FastNavigation.EditorOpenListener)
 *
 * @param <T> Bean type of the Grid where {@link org.vaadin.patrik.FastNavigation} is being used
 */
@SuppressWarnings("serial")
public class EditorOpenEvent<T> extends Component.Event {

    private int rowIndex;
    private int colIndex;
    private T item;
    private Integer[] disabledCols;
    private Grid<T> grid;
    
    public EditorOpenEvent(Component source, int row, int col, T item) {
        super(source);
        rowIndex = row;
        colIndex = col;
        this.item = item;
        grid = (Grid<T>) source;
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
        return colIndex;
    }
    
    /**
     * Set additional columns that should be disable when Editor opens 
     * 
     * @param columns Columns to be set disabled
     */
    public void disableColumns(Integer... columns) {
        disabledCols = columns;
        for (int col : disabledCols) {
        	if (col < 0 || col > grid.getColumns().size()) {
        		throw new IllegalArgumentException("Column index "+col+" out of bounds");
        	}
        }
    }

    /**
     * Set all columns disabled. Can be used for example if you want to conditionally
     * disable editing of a row.
     * 
     * @since 2.3.2
     */
    public void disableAllColumns() {
    	List<Integer> disabled = new ArrayList<>();
    	Integer i=0;
    	for (Column<T, ?> col : grid.getColumns()) {
    		disabled.add(i);
    		i++;
    	}
    	Integer[] dis = disabled.toArray(new Integer[0]);
    	disableColumns(dis);
    }
    
    /**
     * Returns the additional columns that should be disable when Editor opens (see: disableColumns).
     * This method is used internally. Note, if you have set columns non editable or disabled fields
     * via other API's, they are not counted.
     * 
     * @return returns additional columns to be disabled 
     */
    public Integer[] getDisabledColumns() {
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
