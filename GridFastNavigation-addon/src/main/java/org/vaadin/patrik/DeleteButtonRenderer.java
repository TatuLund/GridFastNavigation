package org.vaadin.patrik;

import java.lang.reflect.Method;

import org.vaadin.patrik.shared.DeleteButtonRendererServerRpc;
import org.vaadin.patrik.shared.DeleteButtonRendererState;

import com.vaadin.event.ConnectorEventListener;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickEvent;
import com.vaadin.util.ReflectTools;

/**
 * This is not maintained anymore. DeleteButtonRenderer has moved to Grid RenderersCollection add-on
 * 
 * @author Tatu Lund
 *
 * @param <T> Type of the bean
 */
@Deprecated
public class DeleteButtonRenderer<T> extends ClickableRenderer<T, Boolean> {

    /**
     * An interface for listening to {@link DeleteRendererClickEvent renderer click
     * events}.
     *
     */
    @FunctionalInterface
    public interface DeleteRendererClickListener<T> extends ConnectorEventListener {

        static final Method CLICK_METHOD = ReflectTools.findMethod(
                DeleteRendererClickListener.class, "click", DeleteRendererClickEvent.class);

        /**
         * Called when a rendered button is clicked.
         *
         * @param event
         *            the event representing the click
         */
        void click(DeleteRendererClickEvent<T> event);
    }

    /**
     * An event fired when a clickable widget rendered by a DeleteButtonRenderer is
     * clicked.
     *
     * @param <T>
     *            the item type associated with this click event
     */
    public static class DeleteRendererClickEvent<T> extends ClickEvent {

        private final T item;
        private final Column<T, ?> column;

        protected DeleteRendererClickEvent(Grid<T> source, T item,
                Column<T, ?> column, MouseEventDetails mouseEventDetails) {
            super(source, mouseEventDetails);
            this.item = item;
            this.column = column;
        }

        /**
         * Returns the item of the row where the click event originated.
         *
         * @return the item of the clicked row
         */
        public T getItem() {
            return item;
        }

        /**
         * Returns the {@link Column} where the click event originated.
         *
         * @return the column of the click event
         */
        public Column<T, ?> getColumn() {
            return column;
        }
    }


    /**
     * Creates a new button renderer
     * and e.g. localized Strings for meaning delete and confirm
     *
     * @param delete
     *            text meaning delete
     * @param confirm
     *            text meaning confirm
     */
    public DeleteButtonRenderer(String delete, String confirm) {
        super(Boolean.class, "");
        getState().delete = delete;
        getState().confirm = confirm;
        setupRpc();
    }

    /**
     * Creates a new delete button renderer and adds the given click listener to it
     * and e.g. localized Strings for meaning delete and confirm
     *
     * @param listener
     *            the click listener to register
     * @param delete
     *            text meaning delete
     * @param confirm
     *            text meaning confirm
     */
    public DeleteButtonRenderer(DeleteRendererClickListener<T> listener,
            String delete, String confirm) {
        this(delete, confirm);
        addClickListener(listener);
    }

    /**
     * Creates a new delete button renderer.
     * 
     * Delete button renderer creates two stage Delete - Confirm button
     * When in confirm state "delete-confirm" stylename is set.
     */
    public DeleteButtonRenderer() {
        this("Delete","Confirm");
    }

    /**
     * Creates a new button renderer and adds the given click listener to it.
     *
     * @param listener
     *            the click listener to register
     */
    public DeleteButtonRenderer(DeleteRendererClickListener<T> listener) {
        this(listener, "Delete", "Confirm");
    }

    private void setupRpc() {
    	registerRpc(new DeleteButtonRendererServerRpc() {
    		public void onClick(String rowKey, MouseEventDetails mouseEventDetails) {
    	    	Grid<T> grid = getParentGrid();
    	        Object item = grid.getDataCommunicator().getKeyMapper().get(rowKey);
    	        Column<T, Boolean> column = getParent();
    	        fireEvent(new DeleteRendererClickEvent(grid,item,column,mouseEventDetails));
    		}
    	});
    }
    
    @Override
    public String getNullRepresentation() {
        return super.getNullRepresentation();
    }

    @Override
    protected DeleteButtonRendererState getState() {
        return (DeleteButtonRendererState) super.getState();
    }

    @Override
    protected DeleteButtonRendererState getState(boolean markAsDirty) {
        return (DeleteButtonRendererState) super.getState(markAsDirty);
    }

    /**
     * Sets whether the data should be rendered as HTML (instead of text).
     * <p>
     * By default everything is rendered as text.
     *
     * @param htmlContentAllowed
     *            <code>true</code> to render as HTML, <code>false</code> to
     *            render as text
     */
    public void setHtmlContentAllowed(boolean htmlContentAllowed) {
        getState().htmlContentAllowed = htmlContentAllowed;
    }

    /**
     * Gets whether the data should be rendered as HTML (instead of text).
     * <p>
     * By default everything is rendered as text.
     *
     * @return <code>true</code> if the renderer renders a HTML,
     *         <code>false</code> if the content is rendered as text
     */
    public boolean isHtmlContentAllowed() {
        return getState(false).htmlContentAllowed;
    }

    public Registration addClickListener(DeleteRendererClickListener<T> listener) {
        return addListener(DeleteRendererClickEvent.class, listener,
                DeleteRendererClickListener.CLICK_METHOD);
    }


}
