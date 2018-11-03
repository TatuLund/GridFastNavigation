package org.vaadin.patrik.events;

import com.vaadin.ui.Component;

/**
 * RowEditEvent is emitted when value in the Editor has been changed
 * 
 * @see org.vaadin.patrik.FastNavigation#addRowEditListener(org.vaadin.patrik.FastNavigation.RowEditListener)
 *
 * @param <T> Bean type of the Grid where {@link org.vaadin.patrik.FastNavigation} is being used
 */
@SuppressWarnings("serial")
public class RowEditEvent<T> extends Component.Event {

    private int rowIndex;
    private T item;
    
    public RowEditEvent(Component source, Integer rowIndex, T item) {
        super(source);
        this.rowIndex = rowIndex;
        this.item = item;
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

