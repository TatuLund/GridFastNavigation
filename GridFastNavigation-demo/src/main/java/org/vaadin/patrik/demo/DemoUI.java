package org.vaadin.patrik.demo;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@Push
//@PreserveOnRefresh
@Theme("demo")
@Title("GridFastNavigation Add-on Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI {

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = DemoUI.class, heartbeatInterval=5, closeIdleSessions=true, widgetset = "org.vaadin.patrik.demo.DemoWidgetSet")
	public static class Servlet extends VaadinServlet {
	}

	@Override
	protected void init(VaadinRequest vaadinRequest) {
		final VerticalLayout layout = new VerticalLayout();

		layout.setSizeFull();
		
		MessageGrid messageGrid = new MessageGrid();
		DemoFastGrid demoGrid = new DemoFastGrid(messageGrid);
	
		
		Button clearButton = new Button("Clear");
		clearButton.addClickListener(e ->  {
			messageGrid.clear();
		});
		
		
		Button addButton = new Button();
		addButton.setIcon(VaadinIcons.PLUS_CIRCLE); // Add Row
		addButton.addClickListener(e -> {
			demoGrid.addBlankRow();
		});
		addButton.setDescription("Add a new row");

		Button rowValidationButton = new Button();
		rowValidationButton.setIcon(VaadinIcons.CHECK_CIRCLE_O);
		rowValidationButton.setStyleName(ValoTheme.BUTTON_QUIET);
		rowValidationButton.addClickListener(e -> {
			demoGrid.getNavigation().setRowValidation(!demoGrid.getNavigation().getRowValidation());
			if (!demoGrid.getNavigation().getRowValidation()) rowValidationButton.setStyleName(ValoTheme.BUTTON_QUIET);
			else rowValidationButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
		});
		rowValidationButton.setDescription("Toggle rowValidation");

		Button rowOpenClickButton = new Button();
		rowOpenClickButton.setIcon(VaadinIcons.FOLDER_OPEN_O);
		rowOpenClickButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
		rowOpenClickButton.addClickListener(e -> {
			demoGrid.getNavigation().setOpenEditorWithSingleClick(!demoGrid.getNavigation().getOpenEditorWithSingleClick());
			if (!demoGrid.getNavigation().getOpenEditorWithSingleClick()) rowOpenClickButton.setStyleName(ValoTheme.BUTTON_QUIET);
			else rowOpenClickButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
		});
		rowOpenClickButton.setDescription("Toggle openEditorWithSingleClick");

		Button rowOpenByTypingButton = new Button();
		rowOpenByTypingButton.setIcon(VaadinIcons.KEYBOARD_O);
		rowOpenByTypingButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
		rowOpenByTypingButton.addClickListener(e -> {
			demoGrid.getNavigation().setOpenEditorOnTyping(!demoGrid.getNavigation().getOpenEditorOnTyping());
			if (!demoGrid.getNavigation().getOpenEditorOnTyping()) rowOpenByTypingButton.setStyleName(ValoTheme.BUTTON_QUIET);
			else rowOpenByTypingButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
		});
		rowOpenByTypingButton.setDescription("Toggle openEditorOnTyping");

        Button disableGridEditButton = new Button();
        disableGridEditButton.setIcon(VaadinIcons.PENCIL);
		disableGridEditButton.setDescription("Toggle Grid Editing");
		disableGridEditButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
        disableGridEditButton.addClickListener(e->{
        	if (demoGrid.getEditor().isEnabled()) {
        		if (demoGrid.getEditor().isOpen()) {
        			demoGrid.getEditor().cancel();
        		}
        		demoGrid.getEditor().setEnabled(false);
        		disableGridEditButton.setStyleName(ValoTheme.BUTTON_QUIET);
        	} else {
        		demoGrid.getEditor().setEnabled(true);
        		disableGridEditButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
        	}
        });
                
        Button moveSelectionButton = new Button();
        moveSelectionButton.setIcon(VaadinIcons.BULLSEYE);
        moveSelectionButton.setDescription("Toggle select follow");
        moveSelectionButton.setStyleName(ValoTheme.BUTTON_QUIET);
        moveSelectionButton.addClickListener(e->{
        	if (demoGrid.moveSelection) {
        		demoGrid.moveSelection = false;
        		demoGrid.deselectAll();
        		demoGrid.setSelectionMode(SelectionMode.NONE);
        		moveSelectionButton.setStyleName(ValoTheme.BUTTON_QUIET);
        	} else {
        		demoGrid.moveSelection = true;
        		demoGrid.setSelectionMode(SelectionMode.SINGLE);
        		moveSelectionButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
        	}
        });
		
        Button resetFocusButton = new Button();
        resetFocusButton.setIcon(VaadinIcons.CORNER_UPPER_LEFT);
        resetFocusButton.setDescription("Reset focust to 0,1");
        resetFocusButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
        resetFocusButton.addClickListener(e->{
        	demoGrid.resetFocus();
        });

        HorizontalLayout buttons = new HorizontalLayout();
		buttons.addComponents(addButton,rowValidationButton,rowOpenClickButton,rowOpenByTypingButton,disableGridEditButton,moveSelectionButton,resetFocusButton);
		
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.addComponent(demoGrid);
		layout.addComponent(buttons);
		layout.addComponent(messageGrid);
		layout.addComponent(clearButton);
		layout.setSizeFull();
		layout.setExpandRatio(demoGrid, 10);
		layout.setExpandRatio(buttons, 1);
		layout.setExpandRatio(clearButton, 1);
		layout.setExpandRatio(messageGrid, 6);

		setContent(layout);
	}
}