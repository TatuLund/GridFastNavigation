package org.vaadin.patrik.events;

import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.MultiSelectionModel;

@SuppressWarnings("serial")
public class CellFocusEvent extends Component.Event {

    private int row;
    private int col;
    private boolean rowChanged;
    private boolean colChanged;
    private Object itemId;
    private int offset = 0;

    public CellFocusEvent(Component source, int row, int col, boolean rowChanged, boolean colChanged, Object itemId) {
        super(source);
        this.row = row;
        this.col = col;
        this.itemId = itemId;
        Grid grid = (Grid) source;
        if (grid.getSelectionModel() instanceof MultiSelectionModel) offset = 1;
    }

    /**
     * Get currently focused row index
     * 
     * @return The row index, -1 if the focus is in Header/Footer
     */
    public int getRow() {
        return row;
    }
    
    /**
     * Return true if the row was changed from the previously known value
     * 
     * @return true if the row was changed
     */
    public boolean wasRowChanged() {
        return rowChanged;
    }
    
    /**
     * Get currently focused column index
     * 
     * @return The column index
     */
    public int getColumn() {
        return col-offset;
    }
    
    /**
     * Return true if the column was changed from the previously known value
     * 
     * @return true if the column was changed
     */
    public boolean wasColumnChanged() {
        return colChanged;
    }

    /**
     * Get itemId which where focus is from underlying datasource
     * 
     * @return itemId where focus is, null if focus in Header/Footer 
     */
	public Object getItemId() {
		return itemId;
	}
}
