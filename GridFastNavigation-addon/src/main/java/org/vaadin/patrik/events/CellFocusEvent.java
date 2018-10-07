package org.vaadin.patrik.events;

import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;

@SuppressWarnings("serial")
public class CellFocusEvent<T> extends Component.Event {

    private int row;
    private int col;
    private boolean rowChanged;
    private boolean colChanged;
    private T item;
    
    public CellFocusEvent(Component source, int row, int col, boolean rowChanged, boolean colChanged, T item) {
        super(source);
        this.row = row;
        this.col = col;
        this.item = item;
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
     * @return item where focus is, null if focus in Header/Footer 
     */
	public T getItem() {
		return item;
	}

}
