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
     * Get item which was opened from underlying datasource
     * 
     * @return Item which is edited
     */
	public T getItem() {
		return item;
	}
}
