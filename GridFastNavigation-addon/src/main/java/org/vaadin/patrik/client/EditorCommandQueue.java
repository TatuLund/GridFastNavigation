package org.vaadin.patrik.client;

import java.util.LinkedList;
import java.util.Queue;

import com.vaadin.client.widgets.Grid;

public class EditorCommandQueue {
    
    private static final class Command {
        public static enum CommandType {
            OPEN,
            CANCEL,
            CLOSE
        }
        
        private CommandType type;
        private int rowIndex;
        private int colIndex;
        
        public Command(CommandType type, int row, int col) {
            this.type = type;
            rowIndex = row;
            colIndex = col;
        }
        
        public CommandType getType() {
            return type;
        }
        
        public void perform(Grid<?> g) {
            switch(type) {
            case OPEN:
                g.getEditor().editRow(rowIndex, colIndex);
                break;
            case CANCEL:
                g.getEditor().cancel();
                break;
            case CLOSE:
                g.getEditor().save();
                break;
            }
        }
    }
    
    private Queue<Command> queue;
    private Grid<?> grid;
    
    public EditorCommandQueue(Grid<?> g) {
        queue = new LinkedList<Command>();
        grid = g;
    }
    
    public void openEditor(int row, int col) {
        
    }
    
    public void saveAndCloseEditor() {
        
    }
    
    public void cancelAndCloseEditor() {
        
    }
    
    public boolean pumpQueue() {
        if(queue.size() > 0) {
            Command c = queue.remove();
            c.perform(grid);
            return queue.size() > 0;
        }
        return false;
    }

}
