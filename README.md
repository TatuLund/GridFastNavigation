[![Published on Vaadin  Directory](https://img.shields.io/badge/Vaadin%20Directory-published-00b4f0.svg)](https://vaadin.com/directory/component/gridfastnavigation-add-on)
[![Stars on Vaadin Directory](https://img.shields.io/vaadin-directory/star/gridfastnavigation-add-on.svg)](https://vaadin.com/directory/component/gridfastnavigation-add-on)

# GridFastNavigation Add-on for Vaadin 7

Do you like the Grid but would want it to appeal more to old Excel jockeys?
Then this add-on is for you!
GridFastNavigation is a component extension for Vaadin Grid, which uses the
unbuffered editing mode and alters its keyboard controls to provide a faster
editing experience.

List of features:
- Enter key can be configured to change column instead of a row
- PgDown/PgUp support
- Tab changes column, Grid can be browsed with cursor keys.
- Typing text enables edit mode. (If you start typing with Delete key, the cell will be emptied first)
- In ComboBox / DateField cursor down will open popup
- The add-on supports TextField, PopupDateField, ComboBox and CheckBox in the edit fields of the editor.
- RowEditEvent and CellEditEvent logic
- Pressing Esc cancel edit and reset the value
- Home/End key support: Home - first row, End - last row, Shift+Home - first column on first row, Shift+End last column on last row
- Server-side focus tracking events either on per-cell or per-row basis
- Editor open/close events
- DateFields now don't stop working after using up/down arrow navigation
- Selecting text when opening editor can be enabled and disabled
- Tab navigation skips disabled columns
- Move with enter/shift+enter and tab/shift+tab as well as up/down arrow keys while editing
- Allows user to start editing without first opening the editor
- Open editor with single mouse click (configurable)
- Optional mode to close editor and dispatch edit event when clicking outside of Grid
- getItem() method in cell and row edit/focus, editor open events

Vaadin 8 branch is at https://github.com/TatuLund/GridFastNavigation/tree/vaadin8

## Online demo
ToDo

## Download release

Official releases of this add-on are available at Vaadin Directory. For Maven instructions, download and reviews, go to https://vaadin.com/directory/component/gridfastnavigation-add-on

## Building and running demo

git clone https://github.com/tatulund/GridFastNavigation.git
mvn clean install
cd GridFastNavigation-demo
mvn jetty:run

To see the demo, navigate to http://localhost:8080/

## Development with Eclipse IDE

For further development of this add-on, the following tool-chain is recommended:
- Eclipse IDE
- m2e wtp plug-in (install it from Eclipse Marketplace)
- Vaadin Eclipse plug-in (install it from Eclipse Marketplace)
- Chrome browser

### Importing project

Choose File > Import... > Existing Maven Projects

Note that Eclipse may give "Plugin execution not covered by lifecycle configuration" errors for pom.xml. Use "Permanently mark goal resources in pom.xml as ignored in Eclipse build" quick-fix to mark these errors as permanently ignored in your project. Do not worry, the project still works fine. 

### Debugging server-side

If you have not already compiled the widgetset, do it now by running vaadin:install Maven target for GridFastNavigation-root project.

If you have a JRebel license, it makes on the fly code changes faster. Just add JRebel nature to your GridFastNavigation-demo project by clicking project with right mouse button and choosing JRebel > Add JRebel Nature

To debug project and make code modifications on the fly in the server-side, right-click the GridFastNavigation-demo project and choose Debug As > Debug on Server. Navigate to http://localhost:8080/GridFastNavigation-demo/ to see the application.

### Debugging client-side

Debugging client side code in the GridFastNavigation-demo project:
  - run "mvn vaadin:run-codeserver" on a separate console while the application is running
  - activate Super Dev Mode in the debug window of the application or by adding ?superdevmode to the URL
  - You can access Java-sources and set breakpoints inside Chrome if you enable source maps from inspector settings.
 
## Release notes

### Version 1.2.0
- Fixed issue #65: Column indeces where off by one with multiselect model, and column disabling did not work 
- Changed behavior, FastNavigation will no longer force Editor to be enabled
- Fixed issue #63: Numpad input was not filttered properly
- Fixed issue #64: RowEditEvent fired after ESC-key 
- Fixing issue #67: ESC-Click combination froze editor
- Improvements to full row validation (see issues #69 and #51), with setRowValidation(..) API
- Made EditorWidgets.registerHandler(..) public so that it is possible to add your custom editor widget handlers, and added java docs.
- Fix: Selection of the ComboBox textfield was missing

### Version 1.1.10
- Fix, clickOutListener did not work if FastNavigation was not instantiated with dispatchEditEventOnBlur=true parameter (issue: #54)
- Returned disableColumns(..) to EditorOpenEvent. It can be used to disable additional columns from editing.
- Fix: Do not fire ClickOutEvent if ComboBox or DateField is open.
- Fixing serialization issue

### Version 1.1.9
- Fix: Cursor up/down did not open ComboBox in IE11 
- Added possibility to add save shortcuts & saveWithCtrlS(true) -> Ctrl + S for Save (issue #49)
- Fixed JavaDocs for setOpenEditorWithSingleClick(true) (issue: #42)
- Fixed issue #56
- Fixed disabled columns / readonly fields detection (issue: #55)

### Version 1.1.8
- Added ClickOutListener

### Version 1.1.7
- Changed widgetset.xml filename, Fixed issues #40, #41

### Version 1.1.6
- Modified CellFocusEvent and RowFocusEvent to return -1 if focus is in Header/Footer
- Updated JavaDocs

### Version 1.1.5
- Added getItemId() to CellFocusEvent and RowFocusEvent
- Minor bug fix, there was regression due fix to issue #35

### Version 1.1.4
- Added getItemId() to CellEditEvent and RowEditEvent
- Fixed issue #35: Listeners should be able to be used independently now.
- Updated the demo
- Synched the version numbers between 2.x.x / Vaadin 8 and 1.x.x / Vaadin 7 edition

### Version 0.5.9
- Added support for closing Editor and dispatching event when clicking outside of Grid.

### Version 0.5.8
- Added DeleteButtonRenderer to demo

### Version 0.5.7
- Made opening by single click configurable
- Bugfixes 

### Version 0.5.6
- Editor will be enabled also by delete key, and the existing value will be deleted.

### Version 0.5.5
- Fixed minor issue: row edit event not triggered when in bottom right corner
- Fixed issue with mouse navigation producing excess row edit events

### Version 0.5.4
- Added option to change column when enter key is pressed on last row (provided that enter key is in row change mode)
- Added more java docs
- Small bug fix with potential index out of bounds issue

### Version 0.5.3
- Small fix. Editor is now opened with single click also.

### Version 0.5.2
- Added support for pageup/down keys in edit mode.
- Added possibility configure enter key to change column instead of row
- Updated the demo

### Version 0.5.1
- Added support for ComboBox in the edit fields of the editor.
- Updated the demo

### Version 0.5.0
- Added support for CheckBox in the edit fields of the editor.
- Updated the demo

### Version 0.4.0
- Fixed to be compatible with 7.7.7 and newer. With older framework versions (7.6.0 to 7.7.6) you need to use 0.3.0

### Version 0.3.0
- Implemented the RowEditEvent and CellEditEvent logic
- Fixed a bug: Pressing Esc should cancel edit and reset the value
- Added Home/End key support: Home - first row, End - last row, Shift+Home - first column on first row, Shift+End last column on last row

### Version 0.2.1:

Moving editor outside the first or last row will cause the editor to save and close. This fixes a usability issue with single-row Grids. Future versions will have this behavior toggleable, once I introduce explicit save-and-close shortcuts.

### Version 0.2.0

- Complete rewrite and architectural redesign
- Customizable open and close shortcuts
- Server-side focus tracking events either on per-cell or per-row basis
- Editor open/close events
- Opening editor by typing can be enabled and disabled
- Keyboard up/down arrow navigation can be enabled and disabled
- DateFields now don't stop working after using up/down arrow navigation
- Selecting text when opening editor can be enabled and disabled
- Tab navigation now skips disabled columns
- Additional columns can be disabled on a per-row basis using editor open event

NOTE: server-side notification of changed data has been removed (for now), but can be emulated using editor open/close events, which are now sent per row.

### Version 0.1-SNAPSHOT

- Initial release. Expect dragons. :)

## Issue tracking

The issues for this add-on are tracked on its github.com page. All bug reports and feature requests are appreciated. They have been helpful for adding right features and improving the quality of the add-on.

## Contributions

Contributions are welcome, but there are no guarantees that they are accepted as such. Process for contributing is the following:
- Fork this project
- Create an issue to this project about the contribution (bug or feature) if there is no such issue about it already. Try to keep the scope minimal.
- Develop and test the fix or functionality carefully. Only include minimum amount of code needed to fix the issue.
- Refer to the fixed issue in commit
- Send a pull request for the original project
- Comment on the original issue that you have implemented a fix for it

## License & Author

Add-on is distributed under Apache License 2.0. For license terms, see LICENSE.txt.

GridFastNavigation is written by Patrik Lindström, Tatu Lund and Johannes Tuikkala and maintained by the Tatu

Major pieces of development of this add-on has been sponsored by multiple Support and Prime customers of Vaadin. See vaadin.com/support and Development on Demand for more details.

