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
    private Object itemId;
    
    public EditorOpenEvent(Component source, int row, int col, Object itemId) {
        super(source);
        rowIndex = row;
        colIndex = col;
        this.itemId = itemId;
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
     * Get opened itemId from the underlying data Container
     * 
     * @return Opened itemId
     */
	public Object getItemId() {
		return itemId;
	}
}
