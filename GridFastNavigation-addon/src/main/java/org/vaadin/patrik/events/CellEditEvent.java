package org.vaadin.patrik.events;

import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class CellEditEvent extends Component.Event {

    private int rowIndex;
    private int colIndex;
    
    private String oldData;
    private String newData;

    private Object itemId;
    
    public CellEditEvent(Component source, Integer rowIndex, Integer colIndex, String newData, Object itemId) {
        super(source);
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.newData = newData;
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
     * Get edited column index
     * 
     * @return Edited column index
     */
    public int getColumnIndex() {
        return colIndex;
    }
    
    /**
     * Get old value as String
     * 
     * @return Old value as String
     */
    public String getOldData() {
        return oldData;
    }
    
    /**
     * Get edited value as String
     * 
     * @return Edited value as String
     */
    public String getNewData() {
        return newData;
    }

    /**
     * Get edited item from the underlying data Container
     * 
     * @return Edited item
     */
	public Object getItemId() {
		return itemId;
	}

}
