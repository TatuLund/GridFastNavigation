package org.vaadin.patrik.client;

import java.util.List;
import java.util.logging.Logger;

import org.vaadin.patrik.FastNavigation;
import org.vaadin.patrik.client.EditorStateManager.EditorListener;
import org.vaadin.patrik.client.FocusTracker.FocusListener;
import org.vaadin.patrik.shared.FastNavigationClientRPC;
import org.vaadin.patrik.shared.FastNavigationServerRPC;
import org.vaadin.patrik.shared.FastNavigationState;

import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.annotations.OnStateChange;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.client.widgets.Grid;
import com.vaadin.client.widgets.Grid.Editor;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(FastNavigation.class)
public class GridFastNavigationConnector extends AbstractExtensionConnector {

    private static final Logger logger = Logger.getLogger("GridFastNavigation");

    private Grid<Object> grid;
    private EditorStateManager editorManager;
    private FocusTracker focusTracker;
    private FastNavigationServerRPC rpc;

    @Override
    @SuppressWarnings("unchecked")
    protected void extend(ServerConnector target) {
        grid = (Grid<Object>) ((ComponentConnector) target).getWidget();
        rpc = getRpcProxy(FastNavigationServerRPC.class);
        editorManager = new EditorStateManager(grid);
        focusTracker = new FocusTracker(grid);

        registerRpc(FastNavigationClientRPC.class,
                new FastNavigationClientRPC() {
                    @Override
                    public void setDisabledColumns(List<Integer> indices) {
                        editorManager.setDisabledColumns(indices);
                        // TODO: change to INFO
                        logger.warning("Set disabled columns to " + indices);
                    }

                    @Override
                    public void unfreezeEditor() {
                        editorManager.externalUnlock();
                        // TODO: change to INFO
                        logger.warning("Unlocking editor via RPC");
                    }
                });

        editorManager.addListener(new EditorListener() {
            @Override
            public void editorOpened(Grid<Object> grid, Editor<Object> editor,
                    Widget editorWidget, String keybuf, int row, int col) {
                editorManager.clearDisabledColumns();
                rpc.editorOpened(row, col);
                // TODO: change to info
                logger.warning("Sent editor open RPC");
            }

            @Override
            public void editorMoved(Grid<Object> grid, Editor<Object> editor,
                    int oldRow, int newRow, int oldCol, int newCol) {
                if (newRow != oldRow) {
                    editorManager.clearDisabledColumns();
                    rpc.editorOpened(newRow, newCol);
                    // TODO: change to info
                    logger.warning(
                            "Editor moved to new row, sent editor open event via RPC");
                } else {
                    editorManager.clearDisabledColumns();
                    rpc.editorMoved(newRow, newCol, oldRow, oldCol);
                    // TODO: change to info
                    logger.warning("Notified of editor move via RPC");
                }
            }

            @Override
            public void editorClosed(Grid<Object> grid, Editor<Object> editor,
                    int row, int col, boolean cancel) {
                editorManager.clearDisabledColumns();
                rpc.editorClosed(row, col, cancel);
                // TODO: change to info
                logger.warning("Sent editor closed message via RPC");
            }

            @Override
            public void dataChanged(Grid<Object> grid, Editor<Object> editor,
                    Widget widget, String oldContent, String newContent,
                    int row, int col) {
                // TODO: actually send message via RPC...
                logger.warning("Sent data changed message via RPC");
            }
        });
        
        focusTracker.addListener(new FocusListener() {
            @Override
            public void focusMoved(int currentRow, int currentCol, int lastRow,
                    int lastCol) {
                rpc.focusUpdated(currentRow, currentCol);
            }
        });

        updateFocusTracking();
    }

    void updateEditorTabBehavior() {
        /// TODO: implement
    }

    @OnStateChange("openEditorOnType")
    void updateEditorOpenOnType() {
        editorManager.setOpenEditorByTyping(getState().openEditorOnType);
    }

    @OnStateChange("hasEditorOpenListener")
    void updateEditorOpenLocking() {
        editorManager.setWaitForExternalUnlock(getState().hasFocusListener);
    }

    @OnStateChange("openShortcuts")
    void updateOpenShortcuts() {
        editorManager.clearOpenShortcuts();
        for (int sc : getState().openShortcuts) {
            editorManager.addOpenShortcut(sc);
        }
    }

    @OnStateChange("closeShortcuts")
    void updateCloseShortcuts() {
        editorManager.clearCloseShortcuts();
        for (int sc : getState().closeShortcuts) {
            editorManager.addCloseShortcut(sc);
        }
    }

    @OnStateChange({"hasFocusListener", "hasCellFocusListener", "hasRowFocusListener" })
    void updateFocusTracking() {
        logger.warning("Focus tracking state changed");
        FastNavigationState state = getState();
        if (state.hasFocusListener || state.hasCellFocusListener || state.hasRowFocusListener) {
            logger.warning("Focus tracking is enabled");
            focusTracker.start();
        } else {
            logger.warning("Focus tracking is disabled");
            focusTracker.stop();
        }
    }

    @Override
    public FastNavigationState getState() {
        return ((FastNavigationState) super.getState());
    }
}
