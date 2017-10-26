package org.vaadin.patrik.events;

import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class RowFocusEvent<T> extends Component.Event {

    private int row;
    private T item;
    
    public RowFocusEvent(Component source, int rowIndex, T item) {
        super(source);
        this.row = rowIndex;
        this.item = item;
    }
    
    /**
     * Get index of the row which was edited
     * 
     * @return Index of the row which is edited
     */
    public int getRow() {
        return row;
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
