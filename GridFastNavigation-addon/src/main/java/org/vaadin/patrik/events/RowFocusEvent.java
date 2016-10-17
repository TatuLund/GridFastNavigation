package org.vaadin.patrik.events;

import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class RowFocusEvent extends Component.Event {

    private int row;
    
    public RowFocusEvent(Component source, int rowIndex) {
        super(source);
        this.row = rowIndex;
    }
    
    public int getRow() {
        return row;
    }

}
