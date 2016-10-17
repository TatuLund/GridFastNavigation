package org.vaadin.patrik.events;

import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class EditorMoveEvent  extends Component.Event {

    private int rowIndex;
    private int colIndex;
    private int prevRow;
    private int prevCol;
    
    public EditorMoveEvent(Component source, int currentRow, int currentCol, int prevRow, int prevCol) {
        super(source);
        rowIndex = currentRow;
        colIndex = currentCol;
        this.prevRow = prevRow;
        this.prevCol = prevCol;
    }

    public int getRow() {
        return rowIndex;
    }
    
    public int getColumn() {
        return colIndex;
    }
    
    public int getPreviousRow() {
        return prevRow;
    }
    
    public int getPreviousColumn() {
        return prevCol;
    }

    public boolean wasRowChanged() {
        return rowIndex != prevRow;
    }
    
    public boolean wasColumnChanged() {
        return colIndex != prevCol;
    }
}
