package org.vaadin.patrik.helper;

import com.vaadin.ui.Grid;
import com.vaadin.ui.components.grid.GridSelectionModel;
import com.vaadin.ui.components.grid.MultiSelectionModel;

/**
 * Helps in calculation the offset for the columns in an EditorOpenEvent.
 */
public class OffsetHelper {

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
