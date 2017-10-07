package org.vaadin.patrik.events;

import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class CellEditEvent<T> extends Component.Event {

    private int rowIndex;
    private int colIndex;
    
    private String oldData;
    private String newData;

    private T item;
    
    public CellEditEvent(Component source, Integer rowIndex, Integer colIndex, String newData, T item) {
        super(source);
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.newData = newData;
        this.item = item;
    }
    
    /**
     * Get index of the column which was edited
     * 
     * @return Index of the column which is edited
     */
    public int getColumnIndex() {
        return colIndex;
    }
    
    /**
     * Get old value as string from the Editor
     * 
     * @return Old value as string from the Editor
     */
    public String getOldData() {
        return oldData;
    }
    
    /**
     * Get edited value as string from the Editor
     * 
     * @return Edited value as string from the Editor
     */
    public String getNewData() {
        return newData;
    }

    /**
     * Get index of the row which was edited
     * 
     * @return Index of the row which is edited
     */
    public int getRowIndex() {
        return rowIndex;
    }

    /**
     * Get item which was edited from underlying datasource
     * 
     * @return Item which is edited
     */
	public T getItem() {
		return item;
	}
}
