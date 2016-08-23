package org.vaadin.patrik;

import java.util.Collection;

import org.vaadin.patrik.shared.FastNavigationServerRPC;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.util.ReflectTools;

@SuppressWarnings("serial")
public class FastNavigation extends AbstractExtension {

    public interface FastNavigationListener {
        public void rowEdited(RowEditEvent event);
    }

    public static class RowEditEvent extends Component.Event {

        private int rowIndex;

        public RowEditEvent(Component source, Integer rowIndex) {
            super(source);
            this.rowIndex = rowIndex;
        }

        public int getRowIndex() {
            return rowIndex;
        }

        public void setRowIndex(int rowIndex) {
            this.rowIndex = rowIndex;
        }

    }

    public void addFastNaviagtionListener(FastNavigationListener listener) {
        addListener(RowEditEvent.class, listener, ReflectTools.findMethod(
                listener.getClass(), "rowEdited", RowEditEvent.class));
    }

    public FastNavigation(final Grid g) {
        g.setEditorBuffered(false);
        g.setEditorEnabled(true);

        registerRpc(new FastNavigationServerRPC() {
            @Override
            public void rowUpdated(int rowIndex) {
                fireRowEditedEvent(g, rowIndex);
            }
        }, FastNavigationServerRPC.class);

        extend(g);
    }

    @SuppressWarnings("unchecked")
    private void fireRowEditedEvent(Grid grid, Integer rowIndex) {
        Collection<FastNavigationListener> listeners = (Collection<FastNavigationListener>) getListeners(
                RowEditEvent.class);
        for (FastNavigationListener listener : listeners) {
            listener.rowEdited(new RowEditEvent(grid, rowIndex));
        }
    }
}
