package org.vaadin.patrik.events;

import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class RowEditEvent extends Component.Event {

    private int rowIndex;
    private Object itemId;
    
    public RowEditEvent(Component source, Integer rowIndex, Object itemId) {
        super(source);
        this.rowIndex = rowIndex;
        this.itemId = itemId;
    }

    /**
     * Get edited row index
     * 
     * @return Edited row index
     */
    public int getRowIndex() {
        return rowIndex;
    }

    /**
     * Get edited itemId from the underlying data Container
     * 
     * @return Edited itemId
     */
	public Object getItemId() {
		return itemId;
	}

}

