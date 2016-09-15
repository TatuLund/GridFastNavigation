package org.vaadin.patrik.demo;

import java.util.Collection;
import java.util.Date;
import java.util.Random;

import javax.servlet.annotation.WebServlet;

import org.vaadin.patrik.FastNavigation;
import org.vaadin.patrik.FastNavigation.RowEditListener;
import org.vaadin.patrik.events.RowEditEvent;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Container.Indexed;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
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

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();

        final Grid grid;
        grid = new Grid();

        grid.addColumn("col1", String.class);
        grid.addColumn("col2", String.class);
        for (int i = 0; i < 5; ++i) {
            grid.addColumn("col" + (i + 3), Integer.class);
        }
        grid.addColumn("col9", Date.class);

        Random rand = new Random();
        for (int i = 0; i < 100; ++i) {
            grid.addRow("string 1 " + i, "string 2 " + i, rand.nextInt(i + 10),
                    rand.nextInt(i + 10), rand.nextInt(i + 10),
                    rand.nextInt(i + 10), rand.nextInt(i + 10), new Date());
        }
        grid.setSelectionMode(SelectionMode.NONE);
        FastNavigation nav = new FastNavigation(grid);

        
        
        nav.addRowEditListener(new RowEditListener() {
            @Override
            public void rowEdited(RowEditEvent event) {
                int rowIndex = event.getRowIndex();
                if (rowIndex >= 0) {
                    Indexed ds = grid.getContainerDataSource();
                    Object itemId = ds.getIdByIndex(rowIndex);
                    printChangedRow(rowIndex, ds, itemId);
                }
                
            }
        });
        
        // Open with F2
        nav.addEditorOpenShortcut(KeyCode.F2);
        
        System.out.println("enabled f2 again");

        grid.setSizeFull();

        layout.setMargin(true);
        layout.setSpacing(true);
        layout.addComponent(grid);
        layout.setSizeFull();
        setContent(layout);
    }

    private void printChangedRow(int rowIndex, Indexed ds, Object itemId) {
        @SuppressWarnings("unchecked")
        Collection<Object> prodIds = (Collection<Object>) ds.getItem(itemId)
                .getItemPropertyIds();
        
        System.out.println("Row " + rowIndex + " changed to:");
        for (Object o : prodIds) {
            System.out.print(
                    ds.getItem(itemId).getItemProperty(o).getValue() + ", ");
        }
        System.out.println("\n");
    }

}
