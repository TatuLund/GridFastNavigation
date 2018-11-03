package org.vaadin.patrik.events;

import com.vaadin.ui.Component;

/**
 * RowFocusEvent is emitted when focused row is changed in the Grid
 * 
 * @see org.vaadin.patrik.FastNavigation#addRowFocusListener(org.vaadin.patrik.FastNavigation.RowFocusListener)
 *
 * @param <T> Bean type of the Grid where {@link org.vaadin.patrik.FastNavigation} is being used
 */
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
     * @return Index of the row which is edited, -1 if focus in Header/Footer
     */
    public int getRow() {
        return row;
    }

    /**
     * Get item which wherew focus is from underlying datasource
     * 
     * @return item where focus is, null if focus in Header/Footer 
     */
	public T getItem() {
		return item;
	}


}
