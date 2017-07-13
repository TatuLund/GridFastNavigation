package org.vaadin.patrik.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.annotation.WebServlet;

import org.vaadin.patrik.FastNavigation;
import org.vaadin.patrik.FastNavigation.CellFocusListener;
import org.vaadin.patrik.FastNavigation.EditorCloseListener;
import org.vaadin.patrik.FastNavigation.EditorOpenListener;
import org.vaadin.patrik.FastNavigation.RowEditListener;
import org.vaadin.patrik.FastNavigation.RowFocusListener;
import org.vaadin.patrik.events.CellFocusEvent;
import org.vaadin.patrik.events.EditorCloseEvent;
import org.vaadin.patrik.events.EditorOpenEvent;
import org.vaadin.patrik.events.RowEditEvent;
import org.vaadin.patrik.events.RowFocusEvent;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Binder;
import com.vaadin.data.Binder.Binding;
import com.vaadin.data.converter.StringToBooleanConverter;
import com.vaadin.data.converter.StringToDateConverter;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;


import com.vaadin.ui.TextField;
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

	private static final int MAX_MESSAGES = 50;

	List<ServerMessage> messageList;
	Grid<ServerMessage> messageTable;
	ListDataProvider<ServerMessage> messageData;

	final List<DemoColumns> demoList;
	final ListDataProvider<DemoColumns> demoData;
	final Grid<DemoColumns> grid;

	public DemoUI()
	{
		demoList = new ArrayList<>();
		demoData = new ListDataProvider<>(demoList);

		grid = new Grid<DemoColumns>();
		grid.setDataProvider(demoData);
		grid.getEditor().setEnabled(true);
		grid.getEditor().setBuffered(false);

	}

	@Override
	protected void init(VaadinRequest vaadinRequest)
	{
		final VerticalLayout layout = new VerticalLayout();

		initMessageTable();

		initGrid(grid);
		initNavigation(grid);

		layout.setMargin(true);
		layout.setSpacing(true);
		layout.addComponent(grid);
		layout.addComponent(messageTable);
		layout.setSizeFull();
		setContent(layout);
	}

	private void writeOutput(final String msg)
	{
		this.access(new Runnable()
		{
			@Override
			public void run()
			{
				while (messageData.getItems().size() >= MAX_MESSAGES)
				{
					messageList.remove(0);
					messageData.refreshAll();
				}

				ServerMessage message = new ServerMessage(msg);
				messageList.add(message);
				messageData.refreshItem(message);
				messageTable.scrollTo(messageList.size() - 1);
			}
		});
	}

	private void initMessageTable()
	{

		messageList = new ArrayList<>();
		messageData = new ListDataProvider<ServerMessage>(messageList);

		messageTable = new Grid<ServerMessage>("Server messages");
		messageTable.setDataProvider(messageData);
		
		
		messageTable.addColumn(ServerMessage::getMessage).setCaption("Message").setExpandRatio(1);


		messageTable.setSizeFull();
		// messageTable.setImmediate(true);
	}

	private void initNavigation(final Grid<DemoColumns> grid)
	{
		FastNavigation nav = new FastNavigation(grid, false);
		nav.setChangeColumnAfterLastRow(true);

		nav.addRowEditListener(new RowEditListener()
		{
			@Override
			public void onEvent(RowEditEvent event)
			{
				int rowIndex = event.getRowIndex();
				if (rowIndex >= 0)
				{
					printChangedRow(rowIndex);
				}

			}
		});

		// Open with F2
		nav.addEditorOpenShortcut(KeyCode.F2);
		writeOutput("Editor can also be opened with F2");

		// Close with F3
		nav.addEditorCloseShortcut(KeyCode.F3);
		writeOutput("Editor can also be closed with F3");

		// Row focus change
		nav.addRowFocusListener(new RowFocusListener()
		{
			@Override
			public void onEvent(RowFocusEvent event)
			{
				writeOutput("Focus moved to row " + event.getRow());
			}
		});
		writeOutput("Added row focus change listener");

		// Cell focus change
		nav.addCellFocusListener(new CellFocusListener()
		{
			@Override
			public void onEvent(CellFocusEvent event)
			{
				int row = event.getRow();
				int col = event.getColumn();
				writeOutput("Focus moved to cell [" + row + ", " + col + " ]");
			}
		});
		writeOutput("Added cell focus change listener");

		// Listening to opening of editor
		nav.addEditorOpenListener(new EditorOpenListener()
		{
			@Override
			public void onEvent(EditorOpenEvent event)
			{
				int row = event.getRow();
				writeOutput("Editor opened on row " + row + " at column " + event.getColumn());
			}
		});
		writeOutput("Added editor open listener");

		// Listening to closing of editor
		nav.addEditorCloseListener(new EditorCloseListener()
		{
			@Override
			public void onEvent(EditorCloseEvent event)
			{
				writeOutput("Editor closed on row " + event.getRow() + ", column " + event.getColumn() + ", "
						+ (event.wasCancelled() ? "user cancelled change" : "user saved change"));
			}
		});
		writeOutput("Added editor close listener");
	}

	private void initGrid(Grid<DemoColumns> grid)
	{
		TextField col1 = new TextField();
		TextField col2 = new TextField();
		TextField col3 = new TextField();
		TextField col4 = new TextField();
		TextField col5 = new TextField();
		TextField col6 = new TextField();
		TextField col7 = new TextField();
		TextField col8 = new TextField();
		TextField col10 = new TextField();
		ComboBox<String> col11 = new ComboBox<>();
		String[] options =
		{ "Soft", "Medium", "Hard" };

		col11.setDataProvider(new ListDataProvider<String>(Arrays.asList(options)));

		Binder<DemoColumns> binder = grid.getEditor().getBinder();

		Binding<DemoColumns, String> col1Binding = binder.forField(col1).bind(DemoColumns::getCol1,
				DemoColumns::setCol1);
		grid.addColumn(DemoColumns::getCol1).setCaption("Col1").setWidth(100).setEditorBinding(col1Binding);

		binder.forField(col2).bind(DemoColumns::getCol2, DemoColumns::setCol2);
		grid.addColumn(DemoColumns::getCol2).setCaption("Col2").setWidth(100);

		Binding<DemoColumns, Integer> col3Binding = binder.forField(col3)
				.withConverter(new StringToIntegerConverter("Must enter a number"))
				.bind(DemoColumns::getCol3, DemoColumns::setCol3);
		grid.addColumn(DemoColumns::getCol3).setCaption("Col3").setWidth(100).setEditorBinding(col3Binding);

		Binding<DemoColumns, Integer> col4Binding = binder.forField(col4).withConverter(new StringToIntegerConverter("Must enter a number"))
				.bind(DemoColumns::getCol4, DemoColumns::setCol4);
		grid.addColumn(DemoColumns::getCol4).setCaption("Col4").setWidth(100).setEditorBinding(col4Binding);

		Binding<DemoColumns, Integer> col5Binding = binder.forField(col5).withConverter(new StringToIntegerConverter("Must enter a number"))
				.bind(DemoColumns::getCol5, DemoColumns::setCol5);
		grid.addColumn(DemoColumns::getCol5).setCaption("Col5").setWidth(100).setEditorBinding(col5Binding);

		Binding<DemoColumns, Integer> col6Binding = binder.forField(col6).withConverter(new StringToIntegerConverter("Must enter a number"))
				.bind(DemoColumns::getCol6, DemoColumns::setCol6);
		grid.addColumn(DemoColumns::getCol6).setCaption("Col6").setWidth(100).setEditorBinding(col6Binding);

		Binding<DemoColumns, Integer> col7Binding = binder.forField(col7).withConverter(new StringToIntegerConverter("Must enter a number"))
				.bind(DemoColumns::getCol7, DemoColumns::setCol7);
		grid.addColumn(DemoColumns::getCol7).setCaption("Col7").setWidth(100).setEditorBinding(col7Binding);

		Binding<DemoColumns, Date> col8Binding = binder.forField(col8).withConverter(new StringToDateConverter()).bind(DemoColumns::getCol8,
				DemoColumns::setCol8);
		grid.addColumn(DemoColumns::getCol8).setCaption("Col8").setWidth(100).setEditorBinding(col8Binding);

		Binding<DemoColumns, Boolean> col10Binding = binder.forField(col10).withConverter(new StringToBooleanConverter("Must enter true or false"))
				.bind(DemoColumns::getCol10, DemoColumns::setCol10);
		grid.addColumn(DemoColumns::getCol10).setCaption("Col10").setWidth(100).setEditorBinding(col10Binding);

		Binding<DemoColumns, String> col11Binding = binder.forField(col11).bind(DemoColumns::getCol11, DemoColumns::setCol11);
		grid.addColumn(DemoColumns::getCol11).setCaption("Col11").setWidth(150).setEditorBinding(col11Binding);

		for (int i = 0; i < 100; ++i)
		{

			demoList.add(new DemoColumns(i));
		}
		demoData.refreshAll();
		grid.setSelectionMode(SelectionMode.NONE);
		grid.setSizeFull();
	}

	private void printChangedRow(int rowIndex)
	{

		DemoColumns rowData = demoList.get(rowIndex);

		writeOutput("Row " + rowIndex + " changed to: " + rowData);
	}

}
