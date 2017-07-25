package org.vaadin.patrik.demo;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Push
@Theme("demo")
@Title("GridFastNavigation Add-on Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI
{

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = DemoUI.class, widgetset = "org.vaadin.patrik.demo.DemoWidgetSet")
	public static class Servlet extends VaadinServlet
	{
	}

	public DemoUI()
	{
			
	}

	@Override
	protected void init(VaadinRequest vaadinRequest)
	{
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

		layout.setMargin(true);
		layout.setSpacing(true);
		layout.addComponent(demoGrid);
		layout.addComponent(addButton);
		layout.addComponent(messageGrid);
		layout.addComponent(clearButton);
		layout.setSizeFull();
		layout.setExpandRatio(demoGrid, 10);
		layout.setExpandRatio(addButton, 1);
		layout.setExpandRatio(clearButton, 1);
		layout.setExpandRatio(messageGrid, 6);

		setContent(layout);
	}
}
