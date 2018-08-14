package org.vaadin.patrik.client;

import java.util.List;

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

    private Grid<Object> grid;
    private EditorStateManager editorManager;
    private FocusTracker focusTracker;
    private FastNavigationServerRPC rpc;
    
    @Override
    @SuppressWarnings("unchecked")
    protected void extend(ServerConnector target) {
        grid = (Grid<Object>) ((ComponentConnector) target).getWidget();
        rpc = getRpcProxy(FastNavigationServerRPC.class);
        editorManager = new EditorStateManager(grid,getState());
        focusTracker = new FocusTracker(grid);
        editorManager.setConnector(this);
        
        registerRpc(FastNavigationClientRPC.class,
                new FastNavigationClientRPC() {
                    @Override
                    public void setDisabledColumns(List<Integer> indices) {
                        editorManager.setDisabledColumns(indices);
                    }

                    @Override
                    public void unlockEditor(int lockId) {
                        editorManager.externalUnlock(lockId);
                    }
                    
                    @Override
                    public void validationHasErrors() {
                    	editorManager.moveEditorToError();
                    }

					@Override
					public void setFocusedCell(int row, int col) {
						GridViolators.setFocusedCell(grid,row,col);		
					}
                    
                });

        editorManager.addListener(new EditorListener() {
            @Override
            public void editorOpened(Grid<Object> grid, Editor<Object> editor,
                    int row, int col, int lockId) {
                editorManager.clearDisabledColumns();
                if(getState().hasEditorOpenListener) {
                    rpc.editorOpened(row, col, lockId);
                }
            }

            @Override
            public void editorClosed(Grid<Object> grid, Editor<Object> editor,
                    int row, int col, boolean cancel) {
                editorManager.clearDisabledColumns();
                if(getState().hasEditorCloseListener) {
                    rpc.editorClosed(row, col, cancel);
                }
            }

            @Override
            public void dataChanged(Grid<Object> grid, Editor<Object> editor,
                    Widget widget, String newContent,
                    int row, int col) {
                if(getState().hasCellEditListener) {
                    rpc.cellUpdated(row, col, newContent);
                }
                if(getState().hasRowEditListener) {
                    rpc.rowUpdated(row);
                }
            }

			@Override
			public void clickOut(Grid<Object> grid) {
                if(getState().hasClickOutListener) {
                    rpc.clickOut();
                }
				
			}
        });
        
        focusTracker.addListener(new FocusListener() {
            @Override
            public void focusMoved(int currentRow, int currentCol, int lastRow,
                    int lastCol) {
            	editorManager.notifyIfDataChanged(lastRow, lastCol);
            	editorManager.saveOldContent(currentCol);
                if(getState().hasFocusListener) {
                    rpc.focusUpdated(currentRow, currentCol);
                }
            }
        });

        updateCloseShortcuts();
        updateSaveShortcuts();
        updateOpenShortcuts();
        updateFocusTracking();
    }

    public FocusTracker getFocusTracker() {
    	return focusTracker;
    }
    
    public void requestValidate(boolean move) {
    	rpc.forceValidate(move);
    }

    @OnStateChange("rowValidation")
    void setRowValidation() {
    	editorManager.setRowValidation(getState().rowValidation);
    }
    
    @OnStateChange("saveWithCtrlS")
    void saveWithCtrlS() {
    	editorManager.setSaveWithCtrlS(getState().saveWithCtrlS);
    }
    
    @OnStateChange("openEditorWithSingleClick")
    void openEditorWithSingleClick() {
    	editorManager.setOpenEditorWithSingleClick(getState().openEditorWithSingleClick);
    }
    
    @OnStateChange("changeColumnAfterLastRow")
    void changeColumnAfterLastRow() {
        editorManager.setChangeColumnAfterLastRow(getState().changeColumnAfterLastRow);
    }
    
    @OnStateChange("allowArrowRowChange")
    void updateArrowKeyBehavior() {
        editorManager.setAllowRowChangeWithArrow(getState().allowArrowRowChange);
    }
    
    @OnStateChange("allowTabRowChange")
    void updateEditorTabBehavior() {
        editorManager.setAllowTabRowChange(getState().allowTabRowChange);
    }

    @OnStateChange("openEditorOnType")
    void updateEditorOpenOnType() {
        editorManager.setOpenEditorByTyping(getState().openEditorOnType);
    }

    @OnStateChange("selectTextOnEditorOpen")
    void updateSelectAll() {
        editorManager.setSelectTextOnFocus(getState().selectTextOnEditorOpen);
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

    @OnStateChange("saveShortcuts")
    void updateSaveShortcuts() {
        editorManager.clearSaveShortcuts();
        for (int sc : getState().saveShortcuts) {
            editorManager.addSaveShortcut(sc);
        }
    }
 
    @OnStateChange("hasClickOutListener") 
    void addClickOutListener() {
    	editorManager.addClickOutListener();
    }

    @OnStateChange("homeEndEnabled")
    void setHomeEndEnabled() {
    	editorManager.setHomeEndEnabled(getState().homeEndEnabled);
    }
    
    @OnStateChange({"hasFocusListener", "hasCellFocusListener", "hasRowFocusListener", "hasRowEditListener", "hasCellEditListener", "hasEditorOpenListener" })
    void updateFocusTracking() {
        FastNavigationState state = getState();
        if (state.hasFocusListener || state.hasCellFocusListener || state.hasRowFocusListener || state.hasCellEditListener || state.hasRowEditListener || state.hasEditorOpenListener) {
            focusTracker.start();
            if (state.hasEditorOpenListener) editorManager.setWaitForExternalUnlock(state.hasFocusListener);
            else editorManager.setWaitForExternalUnlock(false);
        } else {
            focusTracker.stop();
        }
    }

    @Override
    public FastNavigationState getState() {
        return ((FastNavigationState) super.getState());
    }
}
