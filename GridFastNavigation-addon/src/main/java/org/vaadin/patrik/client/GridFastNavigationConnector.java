package org.vaadin.patrik.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.vaadin.patrik.FastNavigation;
import org.vaadin.patrik.client.EditorStateManager.EditorListener;
import org.vaadin.patrik.client.FocusTracker.FocusListener;
import org.vaadin.patrik.shared.FastNavigationClientRPC;
import org.vaadin.patrik.shared.FastNavigationServerRPC;
import org.vaadin.patrik.shared.FastNavigationState;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.BrowserInfo;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorMap;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.annotations.OnStateChange;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.client.widget.grid.EditorHandler;
import com.vaadin.client.widget.grid.EditorHandler.EditorRequest;
import com.vaadin.client.widgets.Grid;
import com.vaadin.client.widgets.Grid.Column;
import com.vaadin.client.widgets.Grid.Editor;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.grid.editor.EditorClientRpc;
import com.vaadin.shared.ui.grid.editor.EditorServerRpc;

import elemental.json.JsonObject;

@SuppressWarnings("serial")
@Connect(FastNavigation.class)
public class GridFastNavigationConnector extends AbstractExtensionConnector {

    private Grid<Object> grid;
    private EditorStateManager editorManager;
    private FocusTracker focusTracker;
    private FastNavigationServerRPC rpc;
  
	// Return -1.0 if Grid has no vertical scroll bar otherwise its width
	private double getVerticalScrollBarWidth() {
		for (Element e : getGridParts("div")) {
			if (e.getClassName().contains("v-grid-scroller-vertical")) {
				if (BrowserInfo.get().isIE11() || BrowserInfo.get().isEdge()) { 
					return e.getClientWidth();
				} else {
					return e.getOffsetWidth();					
				}
			}
		}
		return -1.0;
	}
	
	// Get elements in Grid by tag name
	private Element[] getGridParts(String elem) {
		NodeList<Element> elems = grid.getElement().getElementsByTagName(elem);
		Element[] ary = new Element[elems.getLength()];
		for (int i = 0; i < ary.length; ++i) {
			ary[i] = elems.getItem(i);
		}
		return ary;
	}
    
    @Override
    @SuppressWarnings("unchecked")
    protected void extend(ServerConnector target) {
        grid = (Grid<Object>) ((ComponentConnector) target).getWidget();
        rpc = getRpcProxy(FastNavigationServerRPC.class);
        editorManager = new EditorStateManager(grid,getState());
        focusTracker = new FocusTracker(grid);
        editorManager.setConnector(this);
        AnimationCallback editorColumnAndWidthFix = new AnimationCallback() {
            @Override
            public void execute(double timestamp) {
        		int cols = grid.getVisibleColumns().size();
        		DivElement editorOverlay = GridViolators.getEditorOverlay(grid);
        		Double scrollerWidth = getVerticalScrollBarWidth();
        		Double gridWidth = (double) grid.getOffsetWidth();
        		if (scrollerWidth > 0.0) gridWidth = gridWidth - scrollerWidth; 
        		editorOverlay.getStyle().setWidth(gridWidth, Style.Unit.PX);
        		DivElement cellWrapper = GridViolators.getEditorCellWrapper(grid);
            	for (int i=0;i<cols;i++) {
            		Element element = (Element) cellWrapper.getChild(i);
            		double width = grid.getVisibleColumns().get(i).getWidthActual();
            		element.getStyle().setWidth(width, Style.Unit.PX);
            	}
            }
        };
        AnimationCallback editorColumnWidthFix = new AnimationCallback() {
            @Override
            public void execute(double timestamp) {
        		int cols = grid.getVisibleColumns().size();
        		DivElement cellWrapper = GridViolators.getEditorCellWrapper(grid);
            	for (int i=0;i<cols;i++) {
            		Element element = (Element) cellWrapper.getChild(i);
            		double width = grid.getVisibleColumns().get(i).getWidthActual();
            		element.getStyle().setWidth(width, Style.Unit.PX);
            	}
            }
        };
        grid.addColumnVisibilityChangeHandler(event -> {
        	if (grid.isEditorActive()) {
        		GridViolators.redrawEditor(grid);
        		AnimationScheduler.get().requestAnimationFrame(editorColumnWidthFix);
        	}
        });
        grid.addColumnResizeHandler(event -> {
        	if (grid.isEditorActive()) {
        		AnimationScheduler.get().requestAnimationFrame(editorColumnWidthFix);
        	}        	
        });
		Window.addResizeHandler(event -> {
        	if (grid.isEditorActive()) {
        		AnimationScheduler.get().requestAnimationFrame(editorColumnAndWidthFix);
        	}
		});
        
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
					public void setFocusedCell(int row, int col, boolean wait) {
						Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {							
							@Override
							public void execute() {
								if (wait) {
									Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
										@Override
										public boolean execute() {
											if (grid.isWorkPending()) return true;
											else return false;
										}
									}, 100);							
								}
								GridViolators.setFocusedCell(grid,row,col);		
							}
						});
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
                    int row, int col, boolean moved) {
                if(getState().hasCellEditListener) {
                    rpc.cellUpdated(row, col, newContent, moved);
                }
                if(getState().hasRowEditListener) {
                    rpc.rowUpdated(row, moved);
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
            	editorManager.notifyIfDataChanged(lastRow, lastCol, lastRow != currentRow);
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
