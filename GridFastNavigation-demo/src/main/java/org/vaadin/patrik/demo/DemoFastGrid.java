package org.vaadin.patrik.demo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.vaadin.grid.cellrenderers.action.DeleteButtonRenderer;
import org.vaadin.patrik.FastNavigation;

import com.vaadin.data.Binder;
import com.vaadin.data.Binder.Binding;
import com.vaadin.data.converter.LocalDateTimeToDateConverter;
import com.vaadin.data.converter.LocalDateToDateConverter;
import com.vaadin.data.converter.StringToFloatConverter;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.renderers.DateRenderer;

public class DemoFastGrid extends Grid<DemoColumns> {
	private static final long serialVersionUID = 1L;

	final List<DemoColumns> demoList;
	final ListDataProvider<DemoColumns> demoData;
	private FastNavigation<DemoColumns> nav;
	public boolean moveSelection = false;
	
	private MessageLog messageLog;
	private int lastEditedRow = 0;

	DemoFastGrid(MessageLog messageLog) {
		super("Fast Navigation Grid");
		this.messageLog = messageLog;
		demoList = new ArrayList<>();
		demoData = new ListDataProvider<>(demoList);

		this.setDataProvider(demoData);
		this.getHeaderRow(0).setStyleName("my-background");
		
		initNavigation();

		this.getEditor().setEnabled(true);
		this.getEditor().setBuffered(false);

		bindColumnsToEditor();
		
	}

	private void initNavigation() {
		
		nav = new FastNavigation<>(this, false, true);
		nav.setChangeColumnAfterLastRow(true);
		nav.setOpenEditorWithSingleClick(true);
		
		nav.addRowEditListener(event -> {
			getDataProvider().refreshAll();
			int rowIndex = event.getRowIndex();
			if (rowIndex >= 0) {
				printChangedRow(rowIndex,(DemoColumns) event.getItem());
			}			
			if (((DemoColumns) event.getItem()).getCol9() == true) {
				this.getColumn("col6").getEditorBinding().getField().setReadOnly(true);
			} else {
				this.getColumn("col6").getEditorBinding().getField().setReadOnly(false);				
			}
		});

		DeleteButtonRenderer<DemoColumns> deleteButton = new DeleteButtonRenderer<DemoColumns>(clickEvent -> {
			if (this.getEditor().isOpen())
				this.getEditor().cancel();

			demoList.remove(clickEvent.getItem());
			this.getDataProvider().refreshAll();
		},VaadinIcons.TRASH.getHtml()+" Delete",VaadinIcons.CHECK.getHtml()+" Confirm");
		deleteButton.setHtmlContentAllowed(true);
		this.addColumn(action -> true,deleteButton).setCaption("Action").setWidth(120).setHidable(true);

		// Open with F2
		nav.addEditorOpenShortcut(KeyCode.F2);
		messageLog.writeOutput("Editor can also be opened with F2");

		// Close with F3
		nav.addEditorCloseShortcut(KeyCode.F3);
		messageLog.writeOutput("Editor can also be closed with F3");

		nav.setSaveWithCtrlS(true);

		Grid<DemoColumns> grid = this; 
		
		// Row focus change
		nav.addRowFocusListener(event -> {
			if (moveSelection) {
				if (event.getRow() >= 0) grid.select((DemoColumns) event.getItem());
				else grid.deselectAll();
			}
			messageLog.writeOutput("Focus moved to row " + event.getRow());
		});
		messageLog.writeOutput("Added row focus change listener");

		// Cell focus change
		nav.addCellFocusListener(event -> {
			int row = event.getRow();
			int col = event.getColumnIndex();
			messageLog.writeOutput("Focus moved to cell [" + row + ", " + col + " ]");
		});
		messageLog.writeOutput("Added cell focus change listener");

		// Listening to opening of editor
		nav.addEditorOpenListener(event ->  {
			// Uncomment to demonstrate conditional row editing			
//			if (((DemoColumns) event.getItem()).getCol9()) event.disableAllColumns();
            int row = event.getRow();
			lastEditedRow = row;
			messageLog.writeOutput("Editor opened on row " + row + " at column " + event.getColumnIndex());
		});
		messageLog.writeOutput("Added editor open listener");

		// Listening to closing of editor
		nav.addEditorCloseListener(event -> {
			messageLog.writeOutput("Editor closed on row " + event.getRow() + ", column " + event.getColumnIndex() + ", "
					+ (event.wasCancelled() ? "user cancelled change" : "user saved change"));
		});
		messageLog.writeOutput("Added editor close listener");
		
		nav.addClickOutListener(event -> {
			messageLog.writeOutput("User clicked outside Grid: "+event.getSource().toString());
		});
	}

	private void addDemoRow() {
		// its an unbuffered editor so canceling doesn't lose data just
		// closes the
		// editor.
		if (this.getEditor().isOpen())
			this.getEditor().cancel();

		DemoColumns part = new DemoColumns();
		demoList.add(part);
		this.getDataProvider().refreshAll();
	}

	/**
	 * We bind each column to a field (shared by all rows) so that we can edit
	 * each cell.
	 */
	private void bindColumnsToEditor() {
		TextField col1 = new TextField();
		TextField col3 = new TextField();
		TextField col4 = new TextField();
		TextField col5 = new TextField();
		TextField col6 = new TextField();
		DateTimeField col7 = new DateTimeField();
		DateField col8 = new DateField();
		CheckBox col9 = new CheckBox();
		col9.setDescription("Selecting this will disable Col6");
		ComboBox<String> col10 = new ComboBox<>();
		String[] options =
		{ "Soft", "Medium", "Hard" };

		col10.setDataProvider(new ListDataProvider<String>(Arrays.asList(options)));

		Binder<DemoColumns> binder = this.getEditor().getBinder();

		// Col1 simple string
		Binding<DemoColumns, String> col1Binding = binder.forField(col1).asRequired("Empty value not accepted").bind(DemoColumns::getCol1,
				DemoColumns::setCol1);
		this.addColumn(DemoColumns::getCol1).setCaption("Col1").setExpandRatio(1).setEditorBinding(col1Binding);

		// Col2 non-editable string
		this.addColumn(DemoColumns::getCol2).setCaption("No Edits").setWidth(150);

		// Col3 Integer
		Binding<DemoColumns, Integer> col3Binding = binder.forField(col3).withNullRepresentation("")
				.withConverter(new StringToIntegerConverter("Must enter a number")).withValidator(new IntegerRangeValidator("Input integer between 0 and 10",0,10))
				.bind(DemoColumns::getCol3, DemoColumns::setCol3);
		this.addColumn(DemoColumns::getCol3).setCaption("Integer").setWidth(150).setEditorBinding(col3Binding);

		// Col4 Float
		Binding<DemoColumns, Float> col4Binding = binder.forField(col4).withNullRepresentation("")
				.withConverter(new StringToFloatConverter("Must enter a number"))
				.bind(DemoColumns::getCol4, DemoColumns::setCol4);
		this.addColumn(DemoColumns::getCol4).setCaption("Float").setWidth(100).setEditorBinding(col4Binding);

		// Col 5 Integer
		Binding<DemoColumns, Integer> col5Binding = binder.forField(col5).withNullRepresentation("")
				.withConverter(new StringToIntegerConverter("Must enter a number")).withValidator(new IntegerRangeValidator("Input integer between 0 and 10",0,10))
				.bind(DemoColumns::getCol5, DemoColumns::setCol5);
		this.addColumn(DemoColumns::getCol5).setCaption("Integer (2)").setWidth(100).setEditorBinding(col5Binding);

		// Col6 Integer(3)
		Binding<DemoColumns, Integer> col6Binding = binder.forField(col6).withNullRepresentation("")
				.withConverter(new StringToIntegerConverter("Must enter a number")).withValidator(new IntegerRangeValidator("Input integer between 0 and 10",0,10))
				.bind(DemoColumns::getCol6, DemoColumns::setCol6);
		this.addColumn(DemoColumns::getCol6).setId("col6").setCaption("Col6").setWidth(100).setEditorBinding(col6Binding);

		// Col 7 DateTime
		// Need a zoneoffset for datetimefield
		OffsetDateTime odt = OffsetDateTime.now(ZoneId.systemDefault());
		ZoneOffset zoneOffset = odt.getOffset();

		SimpleDateFormat dateTimeFormat = (SimpleDateFormat) DateFormat.getDateTimeInstance(DateFormat.SHORT,
				DateFormat.SHORT, UI.getCurrent().getLocale());
		Binding<DemoColumns, Date> col7Binding = binder.forField(col7)
				.withConverter(new LocalDateTimeToDateConverter(zoneOffset))
				.bind(DemoColumns::getCol7, DemoColumns::setCol7);
		this.addColumn(DemoColumns::getCol7).setCaption("Date Time").setWidth(180).setEditorBinding(col7Binding)
				.setRenderer(new DateRenderer(dateTimeFormat)).setHidable(true);

		// col 8 Date
		Binding<DemoColumns, Date> col8Binding = binder.forField(col8).withConverter(new LocalDateToDateConverter())
				.bind(DemoColumns::getCol8, DemoColumns::setCol8);

		SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT,
				UI.getCurrent().getLocale());
		this.addColumn(DemoColumns::getCol8).setCaption("Date").setWidth(120).setEditorBinding(col8Binding)
				.setRenderer(new DateRenderer(dateFormat)).setHidable(true);

		// Col 9 Boolean
		Binding<DemoColumns, Boolean> col10Binding = binder.forField(col9).bind(DemoColumns::getCol9,
				DemoColumns::setCol9);
		this.addColumn(DemoColumns::getCol9).setCaption("Boolean").setWidth(150).setEditorBinding(col10Binding);

		// Col 10 Combobox.
		Binding<DemoColumns, String> col11Binding = binder.forField(col10).bind(DemoColumns::getCol10,
				DemoColumns::setCol10);
		this.addColumn(DemoColumns::getCol10).setCaption("Combobox").setWidth(150).setEditorBinding(col11Binding);
		
		for (int i = 0; i < 5; ++i) {
			demoList.add(new DemoColumns());
		}
		demoData.refreshAll();
		this.setSelectionMode(SelectionMode.NONE);
		this.addItemClickListener(event -> {
			System.out.println("Item click event happens: "+event.getItem().toString());			
		});
		this.setSizeFull();
	}

	// Add a blank row to the grid and tell the grid to refresh itself showing
	// the new row.
	public void addBlankRow() {
		// its an unbuffered editor so canceling doesn't lose data just closes
		// the
		// editor.
		if (getEditor().isOpen()) {
			getEditor().cancel();
		}
		demoList.add(new DemoColumns());
		this.getDataProvider().refreshAll();
	}

	public FastNavigation getNavigation() {
		return nav;
	}
	
	private void printChangedRow(int rowIndex, DemoColumns rowData) {
		if (demoList.size() >= rowIndex) {
			// DemoColumns rowData = demoList.get(rowIndex);
			messageLog.writeOutput("Row " + rowIndex + " changed to: " + rowData);
		}
	}

	public void resetFocus() {
		nav.setFocusedCell(0, 1);		
	}

}
