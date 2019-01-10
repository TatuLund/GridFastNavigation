package org.vaadin.patrik.helper;

import com.vaadin.ui.Grid;
import com.vaadin.ui.components.grid.GridSelectionModel;
import com.vaadin.ui.components.grid.MultiSelectionModel;

import java.io.Serializable;

/**
 * Helps in calculation the offset for the columns in an EditorOpenEvent.
 */
public class OffsetHelper implements Serializable {

	/**
	 * Calculates offset needed e.g. in multiselect mode it needs to be 1. Override this
	 * method if you want to implement custom OffsetHelper by extending this class and
	 * set it with {@link org.vaadin.patrik.FastNavigation#setOffsetHelper(OffsetHelper)}
	 * 
	 * @param g Grid 
	 * @return Offset
	 */
	public int calculateOffset(final Grid<?> g) {
		int offset = 0;
		GridSelectionModel<?> selectionModel = g.getSelectionModel();
		if (selectionModel instanceof MultiSelectionModel
				&& !this.isGridExtensionPackTableSelectionModel(selectionModel)) {
			offset = 1;
		}
		return offset;
	}

	/*
	 * Offset for GridExpansionPack needs to be zero in MultiselectionMode.
	 */
	private boolean isGridExtensionPackTableSelectionModel(GridSelectionModel<?> selectionModel) {
		return selectionModel.getClass().getName()
				.equals("org.vaadin.teemusa.gridextensions.tableselection.TableSelectionModel");
	}
}
