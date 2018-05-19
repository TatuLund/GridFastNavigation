package org.vaadin.patrik.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;
import com.google.gwt.animation.client.AnimationScheduler.AnimationHandle;
import com.vaadin.client.widgets.Grid;

/**
 * Actively track position of focus in Grid using RequestAnimationFrame
 */
public class FocusTracker {

    public interface FocusListener {
        public void focusMoved(int currentRow, int currentCol, int lastRow,
                int lastCol);
    }

    private List<FocusListener> listeners;
    private Grid<?> grid;
    private int currentRow;
    private int currentCol;
    private int lastRow;
    private int lastCol;
    private boolean run;
	protected AnimationHandle handle;

    public FocusTracker(Grid<?> g) {
        this.grid = g;
        currentRow = GridViolators.getFocusedRow(g);
        currentCol = GridViolators.getFocusedCol(g);
        lastRow = currentRow;
        lastCol = currentCol;
        listeners = new ArrayList<FocusListener>();
        run = false;
    }

    public void start() {
        if(!run) {
            run = true;
            updateLoop.execute(0);
        }
    }

    public void stop() {
        run = false;
    }

    public boolean isRunning() {
        return run;
    }
    
    public void reset() {
    	currentRow = -1;
    	currentCol = -1;
        notifyFocusMoved();
    }
    
    private void notifyFocusMoved() {
        for (FocusListener l : listeners) {
            l.focusMoved(currentRow, currentCol, lastRow, lastCol);
        }
    }
    
    public void addListener(FocusListener l) {
        listeners.add(l);
    }
    
    public void removeListener(FocusListener l) {
        listeners.remove(l);
    }
    
    public void clearListeners() {
        listeners.clear();
    }

    private AnimationCallback updateLoop = new AnimationCallback() {
        @Override
        public void execute(double timestamp) {
            
            int row = GridViolators.getFocusedRow(grid);
            int col = GridViolators.getFocusedCol(grid);

            if (row != currentRow || col != currentCol) {
                lastRow = currentRow;
                currentRow = row;
                lastCol = currentCol;
                currentCol = col;
                notifyFocusMoved();
            }

            if (run) {
                handle = AnimationScheduler.get().requestAnimationFrame(updateLoop);
            }
        }
    };

}
