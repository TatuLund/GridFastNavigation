package org.vaadin.patrik.events;

import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class CellFocusEvent extends Component.Event {

    private int row;
    private int col;
    private boolean rowChanged;
    private boolean colChanged;
    private Object itemId;
    
    public CellFocusEvent(Component source, int row, int col, boolean rowChanged, boolean colChanged, Object itemId) {
        super(source);
        this.row = row;
        this.col = col;
        this.itemId = itemId;
    }

    /**
     * Get currently focused row index
     * 
     * @return The row index
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
        return col;
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
     * Get itemId which was edited from underlying datasource
     * 
     * @return itemId which is edited
     */
	public Object getItemId() {
		return itemId;
	}
}
