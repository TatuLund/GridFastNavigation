package org.vaadin.patrik;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.vaadin.patrik.events.CellEditEvent;
import org.vaadin.patrik.events.EditorOpenEvent;
import org.vaadin.patrik.events.FocusMoveEvent;
import org.vaadin.patrik.events.RowEditEvent;
import org.vaadin.patrik.events.RowFocusEvent;
import org.vaadin.patrik.shared.FastNavigationClientRPC;
import org.vaadin.patrik.shared.FastNavigationServerRPC;
import org.vaadin.patrik.shared.FastNavigationState;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.util.ReflectTools;

@SuppressWarnings("serial")
public class FastNavigation extends AbstractExtension {
    
    private static Logger _logger = Logger.getLogger("FastNavigation");
    private static Logger getLogger() {
        return _logger;
    }

    //
    // Event interfaces
    //
    
    public interface RowEditListener {
        public void rowEdited(RowEditEvent event);
    }
    
    public interface CellEditListener {
        public void cellEdited(CellEditEvent event);
    }
    
    public interface FocusMoveListener {
        public void focusMoved(FocusMoveEvent event);
    }
    
    public interface RowFocusListener {
        public void rowFocused(RowFocusEvent event);
    }

    public interface EditorOpenListener {
        public void editorOpened(EditorOpenEvent event);
    }
    
    //
    // Actual class stuff
    //
    
    // Mirror state value here to avoid unnecessary comms
    private boolean hasRowFocusListener = false;
    
    // Mirror state value here to avoid unnecessary comms
    private boolean hasCellFocusListener = false;

    // Information about previously seen focused row
    private int lastFocusedRow = 0;
    private int lastFocusedCol = 0;
    
    public FastNavigation(final Grid g) {
        g.setEditorBuffered(false);
        g.setEditorEnabled(true);

        registerRpc(new FastNavigationServerRPC() {
            @Override
            public void rowUpdated(int rowIndex) {
                _fireEvent(new RowEditEvent(g,rowIndex));
            }
            
            @Override
            public void cellUpdated(int rowIndex, int colIndex, String oldData, String newData) {
                _fireEvent(new CellEditEvent(g, rowIndex, colIndex, oldData, newData));
            }

            @Override
            public void focusUpdated(int rowIndex, int colIndex) {
                if(hasRowFocusListener) {
                    _fireEvent(new RowFocusEvent(g,rowIndex));
                }
                
                if(hasCellFocusListener) {
                    _fireEvent(new FocusMoveEvent(g, rowIndex, colIndex, lastFocusedRow == rowIndex, lastFocusedCol == colIndex));
                    lastFocusedRow = rowIndex;
                    lastFocusedCol = colIndex;
                }
            }

            @Override
            public void editorOpened(int rowIndex, int colIndex) {
                // NOTE: this method should only ever be called if we have editor open listeners
                EditorOpenEvent ev = new EditorOpenEvent(g,rowIndex,colIndex); 
                _fireEvent(ev);
                getRPC().setDisabledColumns(ev.getDisabledColumns());
                getRPC().unfreezeEditor();
                
            }

            @Override
            public void ping() {
                getLogger().info("Received ping");
            }
            
        }, FastNavigationServerRPC.class);

        extend(g);
    }
    
    private FastNavigationClientRPC getRPC() {
        return getRpcProxy(FastNavigationClientRPC.class);
    }
    
    @Override
    public FastNavigationState getState() {
        return (FastNavigationState) super.getState();
    }

    //
    // Tab capture
    //
    
    public void setTabCapture(boolean enable) {
        getState().tabCapture = enable;
        markAsDirty();
    }
    
    public boolean getTabCapture() {
        return getState().tabCapture;
    }
    
    public void setSelectTextOnEditorOpen(boolean enable) {
        getState().selectTextOnEditorOpen = enable;
    }
    
    public boolean getSelectTextOnEditorOpen() {
        return getState().selectTextOnEditorOpen;
    }
    
    //
    // Editor opening
    //
    
    public void setOpenEditorOnTyping(boolean enable) {
        getState().openEditorOnType = enable;
    }
    
    public boolean getOpenEditorOnTyping() {
        return getState().openEditorOnType;
    }
    
    //
    // Editor opening extra shortcuts
    //
    
    public void addEditorOpenShortcut(int code) {
        getState().openShortcuts.add(code);
        markAsDirty();
    }
    
    public void removeEditorOpenShortcut(int code) {
        getState().openShortcuts.remove(code);
        markAsDirty();
    }
    
    public void clearEditorOpenShortcuts() {
        getState().openShortcuts.clear();
        markAsDirty();
    }
    
    //
    // Editor close/cancel extra shortcuts
    //
    
    public void addEditorCloseShortcut(int code) {
        getState().closeShortcuts.add(code);
        markAsDirty();        
    }
    
    public void removeEditorCloseShortcut(int code) {
        getState().closeShortcuts.remove(code);
        markAsDirty();        
    }
    
    public void clearEditorCloseShortcut(int code) {
        getState().closeShortcuts.clear();
        markAsDirty();        
    }
    
    //
    // Event listeners
    //
    
    private final Map<Class<?>, Class<?>> eventToListenerMap = new HashMap<Class<?>,Class<?>>();
    private final Map<Class<?>, Method> listenerToFunctionMap = new HashMap<Class<?>,Method>();
    
    // XXX: who designed this API?
    private <L> void _addListener(Class<? extends Component.Event> eventClass, L listener, String functionName) {
        Method dispatchMethod = ReflectTools.findMethod(listener.getClass(), functionName, eventClass);
        eventToListenerMap.put(eventClass, listener.getClass());
        listenerToFunctionMap.put(listener.getClass(), dispatchMethod);
        addListener(eventClass,listener,dispatchMethod);
    }
    
    // XXX: this is probably the hardest way to fire an event..
    @SuppressWarnings("unchecked")
    private <E> void _fireEvent(E event) {
        Class<?> listenerClass = eventToListenerMap.get(event.getClass());        
        Collection<Object> listeners = (Collection<Object>)getListeners(listenerClass);
        Method dispatchMethod = listenerToFunctionMap.get(listenerClass);
        
        for(Object listener : listeners) {
            try {
                dispatchMethod.invoke(listener, event);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void addRowEditListener(RowEditListener listener) {
        _addListener(RowEditEvent.class, listener, "rowEdited");
    }
    
    public void addCellEditListener(CellEditListener listener) {
        _addListener(CellEditEvent.class, listener, "cellEdited");
    }
    
    public void addFocusMoveListener(FocusMoveListener listener) {
        _addListener(FocusMoveEvent.class, listener, "focusMoved");
        getState().hasFocusListener = true;
        getState().hasCellFocusListener = true;
        hasCellFocusListener = true;
    }
    
    public void addRowFocusListener(RowFocusListener listener) {
        _addListener(RowFocusEvent.class, listener, "rowFocused");
        getState().hasFocusListener = true;
        getState().hasRowFocusListener = true;
        hasRowFocusListener = true;
    }
    
    /**
     * Register editor open listener, which will let you control which columns should be editable
     * on a row-by-row basis as the editor opens. Note, that adding this listener will cause the
     * Grid to become disabled until the server has processed the event.
     * 
     * @param listener an EditorOpenListener instance
     */
    public void addEditorOpenListener(EditorOpenListener listener) {
        _addListener(EditorOpenEvent.class, listener, "editorOpened");
        getState().hasEditorOpenListener = true;
    }
}
