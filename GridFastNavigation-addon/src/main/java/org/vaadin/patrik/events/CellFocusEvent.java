package org.vaadin.patrik.events;

import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;

/**
 * CellFocusEvent is emitted when focused cell is changed in the Grid
 *  
 * @see org.vaadin.patrik.FastNavigation#addCellFocusListener(org.vaadin.patrik.FastNavigation.CellFocusListener)
 * @see org.vaadin.patrik.FastNavigation#setFocusedCell(int, int)
 * @see org.vaadin.patrik.FastNavigation#setFocusedCell(int, int, boolean)
 *
 * @param <T> Bean type of the Grid where {@link org.vaadin.patrik.FastNavigation} is being used
 */
@SuppressWarnings("serial")
public class CellFocusEvent<T> extends Component.Event {

    private int row;
    private int col;
    private boolean rowChanged;
    private boolean colChanged;
    private T item;
    private boolean isUserOriginated;
    
    public CellFocusEvent(Component source, int row, int col, boolean rowChanged, boolean colChanged, T item, boolean isUserOriginated) {
        super(source);
        this.row = row;
        this.col = col;
        this.item = item;
        this.rowChanged = rowChanged;
        this.colChanged = colChanged;
        this.isUserOriginated = isUserOriginated;
        Grid<T> grid = (Grid<T>) source;
    }

    /**
     * Get index of the row which was edited
     * 
     * @return Index of the row which is edited, -1 if focus in Header/Footer
     */
    public int getRow() {
        return row;
    }
    
    /**
     * Return true if the row was changed from the previously known value
     * 
     * @return boolean value
     */
    public boolean wasRowChanged() {
        return rowChanged;
    }
    
    /**
     * Get currently focused column index
	 *
     * @return integer value
     */
    public int getColumnIndex() {
        return col;
    }
      
    /**
     * Return true if the column was changed from the previously known value
	 *
     * @return integer value
     */
    public boolean wasColumnChanged() {
        return colChanged;
    }
    
    /**
     * Get item which where focus is from underlying datasource
     * 
     * @since 2.1.5
     * 
     * @return item where focus is, null if focus in Header/Footer 
     */
	public T getItem() {
		return item;
	}

    /**
     * Returns true if focus changed via UI, false if opened via setFocusedCell method
     * 
     * @see org.vaadin.patrik.FastNavigation#setFocusedCell(int, int)
     * 
     * @return boolean value
     * 
     * @since 2.6.0
     */
	public boolean isUserOriginated() {
		return isUserOriginated;
	}
}
