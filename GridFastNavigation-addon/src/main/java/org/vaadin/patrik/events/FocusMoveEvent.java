package org.vaadin.patrik.events;

import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class FocusMoveEvent extends Component.Event {

    private int row;
    private int col;
    private boolean rowChanged;
    private boolean colChanged;
    
    public FocusMoveEvent(Component source, int row, int col, boolean rowChanged, boolean colChanged) {
        super(source);
        this.row = row;
        this.col = col;
    }

    /**
     * Get currently focused row index
     */
    public int getRow() {
        return row;
    }
    
    /**
     * Return true if the row was changed from the previously known value
     */
    public boolean wasRowChanged() {
        return rowChanged;
    }
    
    /**
     * Get currently focused column index
     */
    public int getColumn() {
        return col;
    }
    
    /**
     * Return true if the column was changed from the previously known value
     */
    public boolean wasColumnChanged() {
        return colChanged;
    }
    
}
