package org.vaadin.patrik.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;
import com.vaadin.client.widgets.Grid.Editor;

public class EditorTracker {

    interface Listener {

        void editorMoved(int row, int col, int oldrow, int oldcol);

    }

    private List<Listener> listeners;
    private Editor<?> editor;

    private boolean wasOpen;
    private boolean wasClosed;

    private int currentRow;
    private int currentCol;
    private int lastRow;
    private int lastCol;

    private boolean rowChanged;
    private boolean colChanged;

    private boolean run;

    public EditorTracker(Editor<?> e) {
        listeners = new ArrayList<Listener>();
        editor = e;
        wasOpen = GridViolators.isEditorReallyActive(e);
        wasClosed = GridViolators.isEditorReallyClosed(e);
        currentRow = e.getRow();
        currentCol = GridViolators.getEditorColumn(e);
        lastRow = currentRow;
        lastCol = currentCol;
        rowChanged = false;
        colChanged = false;
    }

    public void addListener(Listener l) {
        listeners.add(l);
    }
    
    public void removeListener(Listener l) {
        listeners.remove(l);
    }
    
    public void clearListeners() {
        listeners.clear();
    }
    
    private void notifyEditorMoved() {
        for (Listener l : listeners) {
            l.editorMoved(currentRow, currentCol, lastRow, lastCol);
        }
    }

    public boolean isEditorOpen() {
        return GridViolators.isEditorReallyActive(editor);
    }

    public boolean isEditorClosed() {
        return GridViolators.isEditorReallyClosed(editor);
    }

    public boolean isEditorTransitioning() {
        return !isEditorOpen() && !isEditorClosed();
    }
    
    public boolean wasRowPositionChanged() {
        return rowChanged;
    }
    
    public boolean wasColPositionChanged() {
        return colChanged;
    }

    public void start() {
        run = true;
        updateLoop.execute(0);
    }

    public void stop() {
        run = false;
    }

    public boolean isRunning() {
        return run;
    }

    private boolean updatePosition(int row, int col) {
        if (currentRow != row || currentCol != col) {
            rowChanged = currentRow != row;
            colChanged = currentCol != col;
            lastRow = currentRow;
            lastCol = currentCol;
            currentRow = row;
            currentCol = col;
            return true;
        }
        return false;
    }

    private AnimationCallback updateLoop = new AnimationCallback() {
        @Override
        public void execute(double timestamp) {

            boolean open = GridViolators.isEditorReallyActive(editor);
            boolean closed = GridViolators.isEditorReallyClosed(editor);

            int row = editor.getRow();
            int col = GridViolators.getEditorColumn(editor);

            if (!wasOpen && open) {
                updatePosition(row, col);
                wasOpen = true;
                wasClosed = false;
            } else if (!wasClosed && closed) {
                wasClosed = true;
                wasOpen = false;
            } else if (wasOpen && open) {
                if (updatePosition(row, col)) {
                    notifyEditorMoved();
                }
            }

            if (run) {
                AnimationScheduler.get().requestAnimationFrame(updateLoop);
            }
        }
    };
}
