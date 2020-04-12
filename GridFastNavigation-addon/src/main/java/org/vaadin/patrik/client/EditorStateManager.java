package org.vaadin.patrik.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vaadin.patrik.client.SpinLock.Callback;
import org.vaadin.patrik.client.SpinLock.LockFunction;
import org.vaadin.patrik.shared.FastNavigationState;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.ui.FocusUtil;
import com.vaadin.client.ui.VOverlay;
import com.vaadin.client.widget.grid.DefaultEditorEventHandler;
import com.vaadin.client.widget.grid.EventCellReference;
import com.vaadin.client.widgets.Grid;
import com.vaadin.client.widgets.Grid.Column;
import com.vaadin.client.widgets.Grid.Editor;
import com.vaadin.client.widgets.Grid.EditorDomEvent;
import com.vaadin.event.ShortcutAction.KeyCode;

public class EditorStateManager {

	private String oldContent;
	private String newContent;
	private boolean deletePressed = false;
	
	//
    // Editor listener callback interface
    // Used for communicating de-noised events back to the Connector/RPC layer
    //

    public interface EditorListener {

        void editorOpened(Grid<Object> grid, Editor<Object> editor,
                int row, int col, int lockId, int keyCode, boolean isUserOriginated);

        void editorClosed(Grid<Object> grid, Editor<Object> editor, int row,
                int col, boolean cancel);

        void dataChanged(Grid<Object> grid, Editor<Object> editor,
                Widget widget, String newContent, String oldContent, int row,
                int col, boolean moved);

        void clickOut(Grid<Object> grid);
    }

    //
    // Custom editor open/close/move behavior
    //
    
    private class CustomEditorEventHandler extends DefaultEditorEventHandler<Object> {
        
		//
        // Editor event handler - this receives an unfiltered event stream from the browser and
        // needs to identify the type of incoming event - handleOpenEvent gets _all_ events, for
        // example, and needs to return 'true' when it's actually been offered a valid open event.
        // It then also needs to handle that event.
        //

    	private boolean isClickEvent(EditorDomEvent<Object> event) {
    		final Event e = event.getDomEvent();
    		return e.getTypeInt() == Event.ONCLICK;
    	}
        		
        @Override
        protected boolean handleOpenEvent(EditorDomEvent<Object> event) {
            
            if(isBusy()) return false;

            int key = event.getDomEvent().getKeyCode();
            boolean shift = event.getDomEvent().getShiftKey();
            boolean open = false;
            deletePressed = false;
            
            if (isOpenEvent(event) || (openEditorWithSingleClick && isClickEvent(event))) {
                open = true;
            } else if (isKeyPressEvent(event)) {
                if (openShortcuts.contains(key)) {
                    open = true;
                } else if (openEditorOnType) {
                    if (Keys.isAlphaNumericKey(key)) {
                        open = true;
                        queueKey(Keys.translateNumKey(key), shift);
                    } else if (Keys.isDelKey(key)) {
                    	open = true;
                    	deletePressed = true;
                    }
                }
            }
            
            if (open) {
                final EventCellReference<?> cell = event.getCell();
                event.getDomEvent().preventDefault();
                openEditor(cell.getRowIndex(), cell.getColumnIndexDOM(), key, true);
            }
            
            return open;
        }
        
        @Override
        protected boolean handleMoveEvent(EditorDomEvent<Object> event) {

            if(isBusy()) return false;
            
            //
            // Adapted from original Grid source
            //
            
            Event e = event.getDomEvent();
            final EventCellReference<Object> cell = event.getCell();
            
            if (e.getTypeInt() == Event.ONCLICK) {
            	saveContent();
            	int errorCol = hasValidationError();
            	if (errorCol > -1) {
            		openEditor(cell.getRowIndex(), errorCol, -1, true);
            	} else {
            		openEditor(cell.getRowIndex(), cell.getColumnIndexDOM(), -1, true);
            	}
                return true;
            }
            
            if (e.getTypeInt() == Event.ONKEYDOWN) {

                boolean move = false;
                
                final int columnCount = event.getGrid().getVisibleColumns().size();
                final int rowCount = event.getGrid().getDataSource().size();
                final int visibleRows = (int) event.getGrid().getHeightByRows();
                
                boolean shift = e.getShiftKey();
                int key = e.getKeyCode();
                int currentCol = getFocusedCol();
                int currentRow = getFocusedRow();
                
                // These indices are according to visible columns
                int targetCol = currentCol;
                int targetRow = currentRow;
            	int validationError = hasValidationError();

            	
               	if (Keys.isSpaceKey(key) && shift) {
               		Object item = grid.getDataSource().getRow(currentRow);
               		if (grid.isSelected(item)) {
               			grid.deselect(item);
               		} else {
               			grid.select(item);               			
               		}
                   	event.getDomEvent().preventDefault();                    
            		return false;
               	}               	
            	
                if (Keys.isColumnChangeKey(key)) {
                	saveContent();

                	// If there is validation error, move to column where the error is
                	if ((validationError > -1) && (validationError != currentCol)) {
                		targetCol = validationError;
                	} else {                 
                	
                		int colDelta = shift ? -1 : 1;
                		// Remember to skip disabled columns, take into account hidden columns
                		do {
                			targetCol += colDelta;
                		} while (isDisabledColumn(targetCol) && targetCol < columnCount && targetCol > -1);
                    
                		// Test if we need to move up
                		if (targetCol < 0) {
                			if (allowTabRowChange) {
                				targetCol = columnCount - 1;
                				targetRow--;
                				if (targetRow < 0) targetRow = 0;
                			} else {
                				targetCol = tabWrapping ? columnCount - 1 : 0;
                			}
                		}
                    
                		// Test if we need to move down
                		if(targetCol >= columnCount) {
                			if(allowTabRowChange) {
                				targetCol = 0;
                				targetRow++;
                				if (targetRow >= rowCount) targetRow = 0;
                			} else {
                				targetCol = tabWrapping ? 0 : columnCount - 1;
                			}
                		}
                	}
                    move = true;
                }
                
                if (validationError == -1 && Keys.isRowChangeKey(key) && !Keys.isModifierKey(e)) {
                	saveContent();
                	int rowDelta = shift ? -1 : 1;
                    
                    if(Keys.isUpDownArrowKey(key)) {
                        rowDelta = 0;
                        if(allowArrowRowChange && EditorWidgets.isUpDownNavAllowed(getCurrentEditorWidget())) {
                            if (key == KeyCodes.KEY_UP) {
                                rowDelta = -1;
                            } else if (key == KeyCodes.KEY_DOWN) {
                                rowDelta = 1;
                            } else if (key == KeyCodes.KEY_PAGEUP) {
                            	rowDelta = -visibleRows;
                            } else if (key == KeyCodes.KEY_PAGEDOWN) {
                            	rowDelta = visibleRows;
                            }
                        }
                    }
                    
                    targetRow += rowDelta;
                    
                    // Close editor if we're moving outside bounds - fixes Dan Golob's issue
                    // regarding single-column Grids, where close shortcuts will cancel changes.
                    if (targetRow < 0) {
                    	saveEditor(event);
                        targetRow = 0;
                    } else if (targetRow >= rowCount) {
                    	if (changeColumnAfterLastRow) {
                    		targetRow = 0;                    			
                            // Remember to skip disabled columns
                            do {
                                targetCol++;
                            } while (isDisabledColumn(targetCol) && targetCol < columnCount);
                            move = true;
                            // If we were on last column do nothing
                            if (targetCol >= columnCount) {
                            	targetCol = columnCount-1;
                            	targetRow = rowCount-1;
                            	saveEditor(event);
                            	move = false;
                            }
                    	} else {
                    		saveEditor(event);
                    		targetRow = rowCount - 1;
                   		}
                   	} else {
                       	move = true;
                   	}
               	}
                
               	if (homeEndEnabled && validationError == -1 && Keys.isHomeKey(key)) {
               		saveContent();
               		targetRow = 0;
               		if (shift) targetCol = 0;
                   	move = true;
               	}
                
               	if (homeEndEnabled && validationError == -1 && Keys.isEndKey(key)) {
               		saveContent();
               		targetRow = rowCount-1;
               		if (shift) targetCol = columnCount-1;
                   	move = true;
               	}
                
               	if (move) {
                   	event.getDomEvent().preventDefault();                    
                  
                   	if (currentCol != targetCol || currentRow != targetRow) {
                       	triggerValueChange(event);
                       	openEditor(targetRow, targetCol, e.getKeyCode(), true);
                   	}
               	} else if (rowValidation) {
               		// Grid's Editors editor request callback has a bug, that it clears all error indications, not 
               		// just the one being edited, this is ugly and not perfect workaround to that.  
               		List<Column<?, Object>> errorColumns = getErrorColumns();
               		if (errorColumns.size() > 0) {
               			Scheduler.get().scheduleFixedDelay(() -> {
             				DivElement message = GridViolators.getEditorErrorMessage(grid);
               				grid.getEditor().setEditorError(message.getInnerText(), errorColumns);
               				setErrorColumns(errorColumns);
               				return false;
               			}, 500); // 500ms is experimentally found value, to ensure we put errors back after callback
               		}
               	}
               
               	return move;
            }

            return false;
            
        }

        // This is a hack to work around the issue that unbuffered editor cannot
        // be closed by calling save() since validation isn't executed and so
        // neither success() nor failure() is called, resulting a timeout
        private void saveEditor(EditorDomEvent<Object> event) {
    	    triggerValueChange(event);
    	    closeEditor(false);
        }

        @Override
        protected boolean handleCloseEvent(EditorDomEvent<Object> event) {
            
            if(isBusy()) return false;

            //
            // This is actually the explicit _CANCEL_ or _SAVE_ of the editor. 
            //
            
            boolean close = false;
            boolean save = rowValidation;
            // Allow closing if there is now validation error
            if (hasValidationError() == -1) if (isCloseEvent(event)) {
                close = true;
            } else if (isKeyPressEvent(event)) {
            	boolean ctrl = event.getDomEvent().getCtrlKey();
                int key = event.getDomEvent().getKeyCode();
                if (closeShortcuts.contains(key)) {
                    close = true;
                }
                if (saveShortcuts.contains(key) || (saveWithCtrlS && ctrl && key == KeyCode.S)) {
                	close = true;
                    save = true;
                }
            }
            
            if (close) {
                event.getDomEvent().preventDefault();
                if (save) {
                	saveEditor(event);
                } else {
                	closeEditor(true);
                }
            }
            
            return close;
        }
        
        //
        // Helper(s) for identifying different types of events
        //

        private boolean isKeyPressEvent(EditorDomEvent<Object> event) {
            return event.getDomEvent().getTypeInt() == Event.ONKEYDOWN;
        }

        //
        // Value change trigger required for situations where the standard field
        // validation routines would not necessarily kick in until we try to save
        // the row. This is a hack, and should be considered as such.
        //
        private void triggerValueChange(EditorDomEvent<Object> event) {
            Widget editorWidget = event.getEditorWidget();
            if (editorWidget != null) {
                Element focusedElement = WidgetUtil.getFocusedElement();
                if (editorWidget.getElement().isOrHasChild(focusedElement)) {
                    focusedElement.blur();
                    focusedElement.focus();
                }
            }
        }
    }


    //
    // EditorStateManager
    //

    private Set<EditorListener> editorListeners = new LinkedHashSet<EditorListener>();
    
    private Element curtain;
    private Grid<Object> grid;
    private Grid.Editor<Object> editor;
    
    private List<Character> keybuf = new ArrayList<Character>();
    private Set<Integer> openShortcuts = new LinkedHashSet<Integer>();
    private Set<Integer> closeShortcuts = new LinkedHashSet<Integer>();
    private Set<Integer> saveShortcuts = new LinkedHashSet<Integer>();
    private Set<Integer> disabledColumns = new HashSet<Integer>();

    private RPCLock externalLocks;
    private boolean waitingForEditorOpen = false;
    private boolean useExternalLocking = false;
    
    private boolean openEditorOnType = true;
    private boolean openEditorWithSingleClick = true;
    private boolean allowTabRowChange = true;
    private boolean allowArrowRowChange = true;
    private boolean selectTextOnFocus = true;
    private boolean tabWrapping = true;
    private boolean changeColumnAfterLastRow = false;
    private boolean saveWithCtrlS = false;
	private boolean clickOutListenerAdded = false;
	private boolean rowValidation = false;
	private boolean homeEndEnabled = true;
	private FastNavigationState state;
	private GridFastNavigationConnector gridFastNavigationConnector;
	private boolean enableSelectedStyle;
	
    @SuppressWarnings("unchecked")
    public EditorStateManager(Grid<?> g, FastNavigationState state) {
    	this.state = state;
    	
    	Keys.setEnterBehavior(state.changeColumnOnEnter);
        grid = ((Grid<Object>) g);
        editor = grid.getEditor();
        editor.setEventHandler(new CustomEditorEventHandler());

        externalLocks = new RPCLock();
        
        createModalityCurtain();

        grid.addDomHandler(event -> {
        	if (isBusy()) {
        		NativeEvent e = event.getNativeEvent();
                queueKey(e.getKeyCode(), e.getShiftKey());
                    
                event.stopPropagation();
                event.preventDefault();
            }
        }, KeyDownEvent.getType());

        if (state.dispatchEditEventOnBlur) addClickOutListener();
                    
    }

	private void createModalityCurtain() {
		// Create modality curtain
        curtain = Document.get().createDivElement();
        curtain.setClassName("v-grid-editor-curtain");
        curtain.getStyle().setBackgroundColor("#777");
        curtain.getStyle().setOpacity(0);
        curtain.getStyle().setWidth(100, Unit.PCT);
        curtain.getStyle().setHeight(100, Unit.PCT);
        curtain.getStyle().setPosition(Position.ABSOLUTE);
        curtain.getStyle().setLeft(0, Unit.PX);
        curtain.getStyle().setRight(0, Unit.PX);
        curtain.getStyle().setZIndex(90000);
        
        DOM.sinkEvents(curtain, Event.MOUSEEVENTS | Event.KEYEVENTS | Event.FOCUSEVENTS);
        DOM.setEventListener(curtain, event -> {
        	if ((event.getTypeInt() & Event.ONKEYDOWN) > 0) {
        		queueKey(event.getKeyCode(), event.getShiftKey());
                event.stopPropagation();
                event.preventDefault();
        	}
        });
	}

	public void addClickOutListener() {
		if (!clickOutListenerAdded) {
			Event.addNativePreviewHandler(event -> {
				EventTarget eventTarget = event.getNativeEvent().getEventTarget();
				if (Element.is(eventTarget) && VOverlay.getOverlayContainer(gridFastNavigationConnector.getConnection()).isOrHasChild(eventTarget.cast())) {
					// If the click happened e.g. in ComboBox popup extending outside Grid we need to skip
					return;
				}
				if ((event.getTypeInt() == Event.ONMOUSEDOWN)) {
					Event nativeEvent = Event.as(event.getNativeEvent());
					int ex = nativeEvent.getClientX();
					int ey = nativeEvent.getClientY();
					if (clickHappenedOutsideGrid(ex, ey)) {
						if (state.dispatchEditEventOnBlur && isEditorOpen() && (hasValidationError() == -1)) {
							saveContent();
							Element focusedElement = WidgetUtil.getFocusedElement();
							Widget editorWidget = getCurrentEditorWidget();
							if (editorWidget.getElement().isOrHasChild(focusedElement)) {
								focusedElement.blur();
								focusedElement.focus();
							}
							closeEditor(false);
						}
						notifyClickOut();
					}
				}
			});
			clickOutListenerAdded = true;
		}
	}

	private boolean clickHappenedOutsideGrid(int ex, int ey) {
		int x1 = grid.getAbsoluteLeft();
		int y1 = grid.getAbsoluteTop();
		int y2 = y1 + grid.getOffsetHeight();
		int x2 = x1 + grid.getOffsetWidth();
		boolean clickHappenedOutsideGrid = false;
		if (!((x1 < ex && ex < x2) && (y1 < ey && ey < y2))) {
			clickHappenedOutsideGrid = true;
		}
		return clickHappenedOutsideGrid;
	}
    
    private boolean isBusy() {
        boolean busy = (useExternalLocking && externalLocks.isLocked())
                || waitingForEditorOpen;
        return busy;
    }

    // Apply the input-blocking curtain
    private void lock() {
        grid.getElement().appendChild(curtain);
    }

    // Remove the input-blocking curtain
    void unlock() {
        try {
            grid.getElement().removeChild(curtain);
        } catch (Exception ignore) {
            /* ignored */
        }
    }

    // Check if validation errors are in any of the columns
    private int hasValidationError() {
    	if (!rowValidation) return -1;
        int validationError = -1;
        int hidden = 0;
        for (int i = 0, l = grid.getColumns().size(); i < l; ++i) {
        	Column<?,Object> column = grid.getColumn(i);
        	if (grid.getColumn(i).isHidden()) hidden++;
        	if (grid.getEditor().isEditorColumnError(column)) {
        		validationError = i;
        		break;
        	}
        }
        if (validationError == -1 ) return -1;
        return validationError-hidden;
    }
            
    private List<Column<?,Object>> getErrorColumns() {
    	boolean first = true;
    	List<Column<?,Object>> columns = new ArrayList<>();
        for (int i = 0, l = grid.getColumns().size(); i < l; ++i) {
        	Column<?,Object> column = grid.getColumn(i);
        	if (grid.getEditor().isEditorColumnError(column)) {
        		if (!first) {
        			columns.add(column);        			
        		} else {
        			first = false;
        		}
        	}
        }        
        return columns;
    }

    private void setErrorColumns(List<Column<?,Object>> columns) {
    	for (Column<?,Object> col : columns) {
    		grid.getEditor().setEditorColumnError(col, true);
    	}
    }
    
    //
    // Spinlocks
    //
    private void waitForEditorOpen() {
        SpinLock.lock(new LockFunction() {
            @Override
            public boolean execute() {
                return (useExternalLocking && externalLocks.isLocked()) || !GridViolators.isEditorReallyActive(editor);
            }
        }, new Callback() {
            
            @Override
            public void complete() {
            
                unlock();
                
                // Reset all editor widgets to enabled
                for (int i = 0, l = grid.getVisibleColumns().size(); i < l; ++i) {
                    EditorWidgets.enable(getEditorWidgetForColumn(i,false));
                }
                
                // Then disable the ones that should be disabled
                for (int column : disabledColumns) {
                    EditorWidgets.disable(getEditorWidgetForColumn(column,false));
                    Element el = null;
                    int frozenColumns = grid.getFrozenColumnCount();
                    if (column < frozenColumns) {
                    	el = (Element) GridViolators.getFrozenCellWrapper(grid).getChild(column);
            		} else {
                    	el = (Element) GridViolators.getEditorCellWrapper(grid).getChild(column);            			
            		}
            		if (el != null) {
            			el.addClassName("not-editable");
            		}
                }
                
                Widget editorWidget = getCurrentEditorWidget();

                if (!hasEditableColumns()) {
                    EditorWidgets.focus(editorWidget);                
                	return;
                }
                
                // Check required to avoid overwriting disabled editors
                int currentCol = getFocusedCol();
                if (!isDisabledColumn(currentCol)) {
                
                    // Handle possible value reset of editor widget
                	saveOldContent();
                    String buf = flushKeys();
                    if(!buf.trim().isEmpty() && !deletePressed) {
                        if (selectTextOnFocus) {
                            EditorWidgets.setValue(editorWidget, buf);
                        } else {
                        	String value = EditorWidgets.getValue(editorWidget) + buf;
                            EditorWidgets.setValue(editorWidget, value);
                        }
                        
                    } else {
                        // Select text if desired
                        if (selectTextOnFocus) {
                            EditorWidgets.selectAll(editorWidget);
                        }
                    }
                    if (deletePressed) {
                        EditorWidgets.setValue(editorWidget,"");
                        deletePressed = false;
                    }
                    
                } else {

                    // Try to shunt focus to another widget 
                    int origCol = currentCol;
                    {
                        // Try going right first
                        while (isDisabledColumn(currentCol) && currentCol < grid.getVisibleColumns().size()) {currentCol++;}
                        if(currentCol < grid.getVisibleColumns().size()) {
                            // Move editor focus here
                            editorWidget = getEditorWidgetForColumn(currentCol);
                            openEditor(getFocusedRow(), currentCol, -1, true);
                            return;
                        }
                        
                        // Reset currentCol
                        currentCol = origCol;
                        
                        // Try going left instead
                        while (isDisabledColumn(currentCol) && currentCol > -1) {currentCol--;}
                        if(currentCol >= 0) {
                            // Move editor focus here
                            editorWidget = getEditorWidgetForColumn(currentCol);
                            openEditor(getFocusedRow(), currentCol, -1, true);
                            return;
                        }
                        
                        editorWidget = null;
                    }
                    
                    if (selectTextOnFocus && !deletePressed) {
                        EditorWidgets.selectAll(editorWidget);
                    }
                    if (deletePressed) {
                        EditorWidgets.setValue(editorWidget,"");
                        deletePressed = false;
                    }
                    
                }
                
                // Make sure editor widget is in focus
                EditorWidgets.focus(editorWidget);
                
            }
        });
        lock();
    }

    private void waitForEditorReady() {
        SpinLock.lock(new LockFunction() {
            @Override
            public boolean execute() {
                return !GridViolators.isEditorReallyActive(editor);
            }
        }, new Callback() {
            @Override
            public void complete() {
                
                unlock();
                
                Widget editorWidget = getCurrentEditorWidget();
                if(selectTextOnFocus) {
                    EditorWidgets.selectAll(editorWidget);
                }
                EditorWidgets.focus(editorWidget);
            }
        });
        lock();
    }
    

    //
    // Shortcuts
    //

    public void addOpenShortcut(int key) {
        openShortcuts.add(key);
    }

    public void clearOpenShortcuts() {
        openShortcuts.clear();
    }

    public void addCloseShortcut(int key) {
        closeShortcuts.add(key);
    }

    public void clearCloseShortcuts() {
        closeShortcuts.clear();
    }

    public void addSaveShortcut(int key) {
        saveShortcuts.add(key);
    }

    public void clearSaveShortcuts() {
        saveShortcuts.clear();
    }

    //
    // Listeners
    //

    public void addListener(EditorListener listener) {
        editorListeners.add(listener);
    }

    private void notifyEditorOpened(int row, int col, int keyCode, boolean isUserOriginated) {
        int lock = 0;
        if(useExternalLocking) {
            lock = externalLocks.requestLock();
        }
        for (EditorListener l : editorListeners) {
            l.editorOpened(grid, editor, row, col, lock, keyCode, isUserOriginated);
        }
    }

    private void notifyEditorClosed(int row, int col, boolean cancel) {
        for (EditorListener l : editorListeners) {
            l.editorClosed(grid, editor, row, col, cancel);
        }
    }

    private void notifyDataChanged(String newContent, String oldContent,
            int row, int col, boolean moved) {
        for (EditorListener l : editorListeners) {
            l.dataChanged(grid, editor, getCurrentEditorWidget(),
                    newContent, oldContent, row, col, moved);
        }
    }

    private void notifyClickOut() {
        for (EditorListener l : editorListeners) {
            l.clickOut(grid);
        }
    }

    //
    // State
    //

    public boolean isEditorOpen() {
        return grid.isEditorActive();
    }

    public void setWaitForExternalUnlock(boolean enable) {
        useExternalLocking = enable;
        if(!enable) {
            externalLocks.clearLocks();
        }
    }

    // Set internal wait for external state to false. This releases the lock on
    // the grid.
    public void externalUnlock(int lockId) {
        externalLocks.releaseLock(lockId);
    }

    public void setDisabledColumns(List<Integer> cols) {
        
        // Reset disabled column state
        clearDisabledColumns();
        
        // Add specified extra disabled columns
        for (int col : cols) {
            disabledColumns.add(col);
        }
        
        if(!GridViolators.isEditorReallyClosed(editor)) {
            SpinLock.lock(new LockFunction() {
                @Override
                public boolean execute() {
                    return !GridViolators.isEditorReallyActive(editor);
                }
            }, new Callback() {
                @Override
                public void complete() {
                    // Reset all editor widgets to enabled
                    for(int i = 0, l = grid.getVisibleColumns().size(); i < l; ++i) {
                        EditorWidgets.enable(getEditorWidgetForColumn(i,false));
                    }
                    
                    // Then disable the ones that should be disabled
                    for (int column : disabledColumns) {
                        EditorWidgets.disable(getEditorWidgetForColumn(column,false));
                    }
                }
            });
        }
    }
    
    public void clearDisabledColumns() {
     // Clear all disabled columns
        disabledColumns.clear();
        
        // Add currently non-editable columns
        int i = 0;
        for(Column<?, Object> c : grid.getColumns()) {
            if(!c.isEditable()) {
                disabledColumns.add(i);
            }
            ++i;
        }
    }

    public void setOpenEditorWithSingleClick(boolean enable) {
    	openEditorWithSingleClick = enable;
    }
    
    // If set to true, text is selected when editor is opened
    public void setSelectTextOnFocus(boolean enable) {
        selectTextOnFocus = enable;
    }
    
    // If set to true, you can use the arrow keys to move the editor up and down
    public void setAllowRowChangeWithArrow(boolean enable) {
        allowArrowRowChange = enable;
    }
    
    // If set to true, tabbing outside the edge of the current row will wrap the
    // focus around and switch to the next/previous row. If false, tabbing will
    // wrap around the current row.
    public void setAllowTabRowChange(boolean enable) {
        allowTabRowChange = enable;
    }
    
    // If set to true (default), tabbing past the end of the row will wrap back
    // to the first item in the row. If false, tabbing forward when at the last
    // cell or back when at the first cell has no effect.
    // This only has an effect if allowTabRowChange is false.
    public void setTabWrapping(boolean enable) {
        tabWrapping = enable;
    }

    // If set to true (default = false), pressing enter on last row will change
    // focus to first row and change column to next editable column. Not applicable
    // if enter key is set to change column instead of row.
    public void setChangeColumnAfterLastRow(boolean enable) {
    	changeColumnAfterLastRow = enable;
    }
    
    // If set to true (default), focusing a Grid cell and then pressing an alpha-
    // numeric key will open the editor. If false, the editor must be activated
    // by double clicking or pressing ENTER or a custom editor opening shortcut key
    public void setOpenEditorByTyping(boolean enable) {
        openEditorOnType = enable;
    }
    
    public void setSaveWithCtrlS (boolean enable) {
    	saveWithCtrlS = enable;
    }
     
    public void saveOldContent() {
    	if (isEditorOpen()) {
    		String value = EditorWidgets.getValue(getCurrentEditorWidget());
    		oldContent = value;
    	}
    }

    public void saveOldContent(int col) {
    	if (isEditorOpen()) {
    		String value = EditorWidgets.getValue(getEditorWidgetForColumn(col));
    		oldContent = value;
    	}
    }

    public String getContent() {
        return EditorWidgets.getValue(getCurrentEditorWidget());
    }

    public void saveContent() {
    	if (isEditorOpen()) {
    		String value = EditorWidgets.getValue(getCurrentEditorWidget());
    		newContent = value;
    	}
    }
    
    public String getOldContent() {
        return oldContent;
    }

    public void resetContent() {
    	newContent = oldContent;
    }
    
    public void notifyIfDataChanged(int row, int col, boolean moved) {
    	if (isEditorOpen()) {
    		if ((oldContent != null) && !oldContent.equals(newContent)) {
    			notifyDataChanged(newContent,oldContent,row,col, moved);
    		}
    	}
    }

    
    // Request opening the editor. This function should be used internally instead
    // of the direct editor.editRow() calls.
    public void openEditor(int row, int col, int keyCode, boolean isUserOriginated) {
    	openEditor(row, col, true, keyCode, isUserOriginated);
    }
    
    // Request opening the editor. This function should be used internally instead
    // of the direct editor.editRow() calls.
    public void openEditor(int row, int col, boolean validate, int keyCode, boolean isUserOriginated) {
        AnimationCallback validateCallback = new AnimationCallback() {
            @Override
            public void execute(double timestamp) {
                gridFastNavigationConnector.requestValidate(true);
            }
        };
        
    	if (!editor.isEnabled()) return;
        if (GridViolators.isEditorReallyClosed(editor)) {
            editor.editRow(row,col);
            waitForEditorOpen();
            notifyEditorOpened(row, col, keyCode, isUserOriginated);
        } else {
            int oldRow = getFocusedRow();

            if  (oldRow != row) {
                notifyEditorClosed(oldRow, col, false);
                editor.editRow(row,col);
                waitForEditorOpen();
                // Trigger event after the editor has opened
                notifyEditorOpened(row, col, keyCode, isUserOriginated);
            } else {
                editor.editRow(row,col);
                waitForEditorReady();
                // No need to trigger event if editor just moved to other column
            }
        }
        if (enableSelectedStyle) {
        	if (grid.isSelected(grid.getDataSource().getRow(row))) {
        		GridViolators.getEditorCellWrapper(grid).addClassName("v-grid-editor-selected");
        	} else {
        		GridViolators.getEditorCellWrapper(grid).removeClassName("v-grid-editor-selected");        	
        	}
        }
        // Trigger deferred row validation due timing issue 
        if (validate && rowValidation) AnimationScheduler.get().requestAnimationFrame(validateCallback);
    }

    // Request closing the editor. This function should be used internally instead
    // of the direct editor.save() or editor.cancel() calls.
    public void closeEditor(boolean cancel) {
        int row = getFocusedRow();
        int col = getFocusedCol();
        
        if (cancel) {
       		EditorWidgets.setValue(getCurrentEditorWidget(), oldContent);
       		editor.cancel();
       		resetContent();
        } else {
            if ((oldContent != null) && !oldContent.equals(newContent)) {
            	notifyDataChanged(newContent,oldContent,row,col,false);
            }
            editor.cancel();
        }

        notifyEditorClosed(row, col, cancel);
        FocusUtil.setFocus(grid, true);
        // Ensure modality curtain is removed. If ESC was hold when Editor opened, it was probably closed too fast
        unlock();
    }

    //
    // Keybuffer management
    //

    private void queueKey(int sym, boolean shift) {
        if (Keys.isAlphaNumericKey(sym)) {
            char keychar = (char) sym;
            if (shift) {
                keychar = Character.toUpperCase(keychar);
            } else { 
                keychar = Character.toLowerCase(keychar);
            }
            keybuf.add(keychar);
        }
    }

    private String flushKeys() {
        StringBuilder sb = new StringBuilder();
        for (char c : keybuf) {
            sb.append(c);
        }
        keybuf.clear();
        return sb.toString();
    }

    //
    // Editor state info
    //

    private int getFocusedRow() {
        return editor.getRow();
    }

    private int getFocusedCol() {
        return GridViolators.getEditorColumn(editor);
    }

    // Calculate the internal index of the column including the hidden columns
    private int determineInternalColumnIndex(int index) {
    	int columnCount = grid.getColumns().size();
    	int j=0;
    	int i=0;
    	if (index == 0) {
    		while (grid.getColumn(i).isHidden()) i++;
    		return i;
    	} 
    	while (i<index && j<(columnCount-1)) {
    		if (!grid.getColumn(j).isHidden()) {
    			i++;
    		}
            j++;
    	}
    	return j;
    }

    private Widget getEditorWidgetForColumn(int index) {
    	return getEditorWidgetForColumn(index,true);
    }
    
    private Widget getEditorWidgetForColumn(int index, boolean compensate) {
    	int i = 0;
    	if (compensate) i = determineInternalColumnIndex(index);
    	else i = index;
        Column<?, Object> column = grid.getColumn(i);
        Map<Column<?, ?>, Widget> editorColumnToWidgetMap = GridViolators
                .getEditorColumnToWidgetMap(editor);
        Widget widget = editorColumnToWidgetMap.get(column);
        return widget;
    }

    private Widget getCurrentEditorWidget() {
        return getEditorWidgetForColumn(GridViolators.getFocusedCol(grid));
    }

    // Connector sets gridFastNavigationConnector using this method, hence public
	public void setConnector(
			GridFastNavigationConnector gridFastNavigationConnector) {
				this.gridFastNavigationConnector = gridFastNavigationConnector;
	}

	public void setRowValidation(boolean rowValidation) {
		this.rowValidation = rowValidation;		
	}
	
	public void moveEditorToError() {
		int errorCol = hasValidationError();
		openEditor(getFocusedRow(), errorCol, false, -1, true);		
	}
	
	private boolean hasEditableColumns() {
		boolean hasEditableColumns = false;
		if (disabledColumns.size() < grid.getVisibleColumns().size()) hasEditableColumns = true;
		return hasEditableColumns;
	}

	public void setHomeEndEnabled(boolean homeEndEnabled) {
		this.homeEndEnabled = homeEndEnabled;		
	}

	public void enableSelectedStyle(boolean enableSelectedStyle) {
		this.enableSelectedStyle = enableSelectedStyle;		
	}

    private boolean isDisabledColumn(int targetCol) {
    	int internalColumn = determineInternalColumnIndex(targetCol);        	
    	if (!grid.getColumns().get(internalColumn).isHidden()) {
    		return disabledColumns.contains(internalColumn);
    	} else {
    		// if column is hidden check is skipped
    		return false;
    	}
    }
    
}
