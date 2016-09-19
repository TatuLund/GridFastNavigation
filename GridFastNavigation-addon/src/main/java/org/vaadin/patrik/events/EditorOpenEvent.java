package org.vaadin.patrik.events;

import com.vaadin.ui.Component;

/**
 * Event used to notify of editor being opened. Can be used
 * to disable some columns (in other words: make them read-only)  
 */
@SuppressWarnings("serial")
public class EditorOpenEvent extends Component.Event {

    private int rowIndex;
    private int colIndex;
    private int[] disabledCols;
    
    public EditorOpenEvent(Component source, int row, int col) {
        super(source);
        rowIndex = row;
        colIndex = col;
    }

    public int getRow() {
        return rowIndex;
    }
    
    public int getColumn() {
        return colIndex;
    }
    
    /**
     * Use this method to mark columns as not editable 
     * @param columns
     */
    public void disableColumns(int... columns) {
        disabledCols = columns;
    }
    
    public int[] getDisabledColumns() {
        return disabledCols;
    }
    
}
