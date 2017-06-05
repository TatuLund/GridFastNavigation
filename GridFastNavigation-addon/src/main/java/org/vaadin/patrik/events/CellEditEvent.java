package org.vaadin.patrik.events;

import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class CellEditEvent extends Component.Event {

    private int rowIndex;
    private int colIndex;
    
    private String oldData;
    private String newData;

    public CellEditEvent(Component source, Integer rowIndex, Integer colIndex, String newData) {
        super(source);
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.newData = newData;
    }
    
    public int getRowIndex() {
        return rowIndex;
    }
    
    public int getColumnIndex() {
        return colIndex;
    }
    
    public String getOldData() {
        return oldData;
    }
    
    public String getNewData() {
        return newData;
    }

}
