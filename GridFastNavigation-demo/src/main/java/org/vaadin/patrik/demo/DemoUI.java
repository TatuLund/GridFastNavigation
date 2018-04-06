package org.vaadin.patrik.demo;

import java.util.Collection;
import java.util.Date;
import java.util.Random;

import javax.servlet.annotation.WebServlet;

import org.vaadin.grid.cellrenderers.action.DeleteButtonRenderer;
import org.vaadin.grid.cellrenderers.action.DeleteButtonRenderer.DeleteRendererClickEvent;
import org.vaadin.grid.cellrenderers.action.DeleteButtonRenderer.DeleteRendererClickListener;
import org.vaadin.patrik.FastNavigation;
import org.vaadin.patrik.FastNavigation.CellFocusListener;
import org.vaadin.patrik.FastNavigation.ClickOutListener;
import org.vaadin.patrik.FastNavigation.EditorCloseListener;
import org.vaadin.patrik.FastNavigation.EditorOpenListener;
import org.vaadin.patrik.FastNavigation.RowEditListener;
import org.vaadin.patrik.FastNavigation.RowFocusListener;
import org.vaadin.patrik.events.CellFocusEvent;
import org.vaadin.patrik.events.ClickOutEvent;
import org.vaadin.patrik.events.EditorCloseEvent;
import org.vaadin.patrik.events.EditorOpenEvent;
import org.vaadin.patrik.events.RowEditEvent;
import org.vaadin.patrik.events.RowFocusEvent;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Push
@Theme("demo")
@Title("GridFastNavigation Add-on Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI {

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class, widgetset = "org.vaadin.patrik.demo.DemoWidgetSet")
    public static class Servlet extends VaadinServlet {
    }
    
    private static final int MAX_MESSAGES = 50;

    Table messageTable;
    IndexedContainer messageData;
    
    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();
        
        initMessageTable();
        
        final Grid grid = new Grid();
        initGrid(grid);
        initNavigation(grid);
        
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.addComponent(grid);
        layout.addComponent(messageTable);
        layout.setSizeFull();
        setContent(layout);
    }
    
    @SuppressWarnings("unchecked")
    private void writeOutput(final String msg) {
        this.access(new Runnable() {
            @Override
            public void run() {
                while(messageData.size() >= MAX_MESSAGES) {
                    messageData.removeItem(messageData.getIdByIndex(0));
                }
                
                Object item = messageData.addItem();
                messageData.getItem(item).getItemProperty("Message").setValue(msg);
                messageTable.setCurrentPageFirstItemIndex(messageData.indexOfId(item));
            }
        });
    }
    
    private void initMessageTable() {
        messageData = new IndexedContainer();
        messageData.addContainerProperty("Message", String.class, "");
        
        messageTable = new Table("Server messages");
        messageTable.setSizeFull();
        messageTable.setContainerDataSource(messageData);
        messageTable.setImmediate(true);
    }
    
    private void initNavigation(final Grid grid) {
        FastNavigation nav = new FastNavigation(grid,false,true);
        nav.setChangeColumnAfterLastRow(true);

        nav.addRowEditListener(new RowEditListener() {
            @Override
            public void onEvent(RowEditEvent event) {
                int rowIndex = event.getRowIndex();
                if (rowIndex >= 0) {
                    Indexed ds = grid.getContainerDataSource();
                    Object itemId = event.getItemId();
                    printChangedRow(rowIndex, ds, itemId);
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
        nav.addRowFocusListener(new RowFocusListener() {
            @Override
            public void onEvent(RowFocusEvent event) {
            	int row = event.getRow();
                writeOutput("Focus moved to row " + event.getRow());
                grid.select(event.getItemId());
            }
        });
        writeOutput("Added row focus change listener");
        
        // Cell focus change
        nav.addCellFocusListener(new CellFocusListener() {
            @Override
            public void onEvent(CellFocusEvent event) {
                int row = event.getRow();
                int col = event.getColumn();
                writeOutput("Focus moved to cell [" + row + ", " + col + " ]");
            }
        });
        writeOutput("Added cell focus change listener");
        
        // Listening to opening of editor
        nav.addEditorOpenListener(new EditorOpenListener() {
            @Override
            public void onEvent(EditorOpenEvent event) {
                int row = event.getRow();
                writeOutput("Editor opened on row " + row + " at column " + event.getColumn());
            }
        });
        writeOutput("Added editor open listener");
        
        // Listening to closing of editor
        nav.addEditorCloseListener(new EditorCloseListener() {
            @Override
            public void onEvent(EditorCloseEvent event) {
                writeOutput("Editor closed on row " + event.getRow() + ", column " + event.getColumn() + ", " + (event.wasCancelled() ? "user cancelled change" : "user saved change"));
            }
        });
        writeOutput("Added editor close listener");
        
        nav.addClickOutListener(new ClickOutListener() {
        	@Override
        	public void onEvent(ClickOutEvent event) {
        		writeOutput("User click outside Grid: "+event.getSource().toString());
        	}
        });
        
    }
    
    private void initGrid(final Grid grid) {
        
        // Add some columns
		DeleteButtonRenderer deleteButton = new DeleteButtonRenderer(new DeleteRendererClickListener() {
			@Override
			public void click(DeleteRendererClickEvent event) {
				grid.getContainerDataSource().removeItem(event.getItem());
			}
			
		},FontAwesome.TRASH.getHtml()+" Delete",FontAwesome.CHECK.getHtml()+" Confirm");
		deleteButton.setHtmlContentAllowed(true);
    	grid.addColumn("action", Boolean.class);
    	grid.getColumn("action").setEditable(false);
    	grid.getColumn("action").setRenderer(deleteButton);
    	
        grid.addColumn("col1", String.class);
        grid.addColumn("col2", String.class);
        for (int i = 0; i < 5; ++i) {
            grid.addColumn("col" + (i + 3), Integer.class);
        }	
        grid.addColumn("col8", Date.class);
        grid.addColumn("col10", Boolean.class);
        grid.addColumn("col11", String.class);
        ComboBox comboBox = new ComboBox();
        comboBox.addItems("Soft","Medium","Hard");
        grid.getColumn("col11").setEditorField(comboBox);
        
        // Make column 2 read only to test statically read only columns
        grid.getColumn("col2").setEditable(false);
        
        Random rand = new Random();
        for (int i = 0; i < 100; ++i) {
            grid.addRow(true,"string 1 " + i, "string 2 " + i, rand.nextInt(i + 10),
                    rand.nextInt(i + 10), rand.nextInt(i + 10),
                    rand.nextInt(i + 10), rand.nextInt(i + 10), new Date(), false, "Medium");
        }
        grid.setSelectionMode(SelectionMode.SINGLE);
        grid.setSizeFull();
    }

    private void printChangedRow(int rowIndex, Indexed ds, Object itemId) {
        @SuppressWarnings("unchecked")
        Collection<Object> prodIds = (Collection<Object>) ds.getItem(itemId)
                .getItemPropertyIds();
        
        StringBuffer sb = new StringBuffer();
        for (Object o : prodIds) {
            sb.append(ds.getItem(itemId).getItemProperty(o).getValue() + "");
        }
        
        writeOutput("Row " + rowIndex + " changed to: " + sb.toString());
    }

}
