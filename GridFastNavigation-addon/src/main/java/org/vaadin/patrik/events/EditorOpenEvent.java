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

    /**
     * Get row where editor was opened
     * 
     * @return Row index
     */
    public int getRow() {
        return rowIndex;
    }
    
    /**
     * Get column where editor was opened
     * 
     * @return Column index
     */
    public int getColumn() {
        return colIndex;
    }
    
    /**
     * Use this method to mark columns as not editable 
     * 
     * @param columns The columns that will be marked not editable
     */
    public void disableColumns(int... columns) {
        disabledCols = columns;
    }

    /** 
     * Get columns that are not editable
     * 
     * @return Indeces of the non editable columns
     */
    public int[] getDisabledColumns() {
        return disabledCols;
    }
    
}
