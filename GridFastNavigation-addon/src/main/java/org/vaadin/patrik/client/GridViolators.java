package org.vaadin.patrik.client;

import java.util.Map;


import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.widgets.Grid;
import com.vaadin.client.widgets.Grid.Column;
import com.vaadin.client.widgets.Grid.Editor;

public class GridViolators {
    
     // ========================================================================
     // Violators - access Grid internals irrespective of visibility
     // ========================================================================
    
    public static native final boolean isEditorReallyActive(Editor<?> editor) /*-{
        var state = editor.@com.vaadin.client.widgets.Grid.Editor::state;
        var ordinal = state.@com.vaadin.client.widgets.Grid.Editor.State::ordinal()();
        return ordinal == 3;
    }-*/;
    
    public static native final boolean isEditorReallyClosed(Editor<?> editor) /*-{
        var state = editor.@com.vaadin.client.widgets.Grid.Editor::state;
        var ordinal = state.@com.vaadin.client.widgets.Grid.Editor.State::ordinal()();
        return ordinal == 0;
    }-*/;
    
    public static native final Map<Column<?, ?>, Widget> getEditorColumnToWidgetMap(Editor<?> editor) /*-{
        return editor.@com.vaadin.client.widgets.Grid.Editor::columnToWidget;
    }-*/;
    
    public static native final int getFocusedRow(Grid<?> grid) /*-{
        var cfh = grid.@com.vaadin.client.widgets.Grid::cellFocusHandler;
        var row = cfh.@com.vaadin.client.widgets.Grid.CellFocusHandler::rowWithFocus;
        return row;
    }-*/;
    
    public static native final int getFocusedCol(Grid<?> grid) /*-{
        var cfh = grid.@com.vaadin.client.widgets.Grid::cellFocusHandler;
        var cell = cfh.@com.vaadin.client.widgets.Grid.CellFocusHandler::getFocusedCell()();
        var col = cell.@com.vaadin.client.widget.escalator.Cell::getColumn()();
        return col;
    }-*/;        
    
    public static native final int getEditorColumn(Editor<?> editor) /*-{
	return editor.@com.vaadin.client.widgets.Grid.Editor::focusedColumnIndexDOM;
}-*/;
   /**
   	return a minus integer to ignore focused footer/header rows
   */
    public static native final int getGridBodyFocusedRow(Grid<?> g)/*-{
        var h = g.@com.vaadin.client.widgets.Grid::cellFocusHandler;
        var r=h.@com.vaadin.client.widgets.Grid.CellFocusHandler::rowWithFocus;
        var co=h.@com.vaadin.client.widgets.Grid.CellFocusHandler::containerWithFocus;
        var cl = g.@com.vaadin.client.widgets.Grid::getEscalator()();
        var conBody = cl.@com.vaadin.client.widgets.Escalator::getBody()();
        if(co == conBody)
            return r;
        else
            return -1000;
    }-*/;

}
