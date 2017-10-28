package org.vaadin.patrik.events;

import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class RowFocusEvent extends Component.Event {

    private int row;
    private Object itemId;
    
    public RowFocusEvent(Component source, int rowIndex, Object itemId) {
        super(source);
        this.row = rowIndex;
        this.itemId = itemId;
    }
    
    /**
     * Get index of the row which was edited
     * 
     * @return Index of the row which is edited, -1 if focus in Header/Footer
     */
    public int getRow() {
        return row;
    }

    /**
     * Get itemId which wherew focus is from underlying datasource
     * 
     * @return itemId where focus is, null if focus in Header/Footer 
     */
	public Object getItemId() {
		return itemId;
	}

}
