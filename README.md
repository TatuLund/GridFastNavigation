# GridFastNavigation Add-on for Vaadin 8

Do you like the Grid but would want it to appeal more to old Excel jockeys?
Then this add-on is for you!
GridFastNavigation is a compnent extension for Vaadin Grid, which uses the
unbuffered editing mode and alters its keyboard controls to provide a faster
editing experience.

## Online demo
[Todo](...)

## Download release

Official releases of this add-on are available at Vaadin Directory. For Maven instructions, download and reviews, go to
https://vaadin.com/directory/component/gridfastnavigation-add-on

## Building and running demo

git clone https://github.com/thinwire/GridFastNavigation.git
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

To debug project and make code modifications on the fly in the server-side, right-click the GridFastNavigation-demo project and choose Debug As > Debug on Server. Navigate to http://localhost:8080/ to see the application.

### Debugging client-side

Debugging client side code in the GridFastNavigation-demo project:
  - run "mvn vaadin:run-codeserver" on a separate console while the application is running
  - activate Super Dev Mode in the debug window of the application or by adding ?superdevmode to the URL
  - You can access Java-sources and set breakpoints inside Chrome if you enable source maps from inspector settings.
 
## Release notes

### Version 2.3.9
- Fix: Grid scroll bar width was not taken into account in Editor resizing

### Version 2.3.8
- Further improvement to row validation mode

### Version 2.3.7
- Improvement to row validation mode

### Version 2.3.6
- Added fix to Editor not resized when browser window is being resized bug. See: https://github.com/vaadin/framework/issues/11148

### Version 2.3.5
- Workaround for https://github.com/vaadin/framework/issues/8962
- Workaround for https://github.com/vaadin/framework/issues/7276

### Version 2.3.4
- Fixed bug: Problem with sorting, edit event did not return right item (issue #76).
- Improvement: Home/End key behavior can be disabled with setHomeEndEnabled(false) (issue #75)
- New feature: Move focus programmatically with setFocusedCel(row,col) (issue #68)
- Updated demo

### Version 2.3.3
- Fixed bug: Client side exception occurred when there were hidden columns (issue #74).

### Version 2.3.2
- Added convenience method disableAllColumns() in EditorOpenEvent (issue #27)
- Fixed bug: Editor opening was jammed if there were no editable columns

### Version 2.3.1
- Fixed bug: Tabing out of bounds caused client side exception.

### Version 2.3.0
- Added preliminary support for hidden columns, fixes issue #71
- Minor improvements in setRowValidation(true) mode
- Updated demo

### Version 2.2.4
- Fix bug: Clicking outside Grid when setRowValidation(true) closed editor

### Version 2.2.3
- Improvements to full row validation (see issues #69 and #51), with setRowValidation(..) API

### Version 2.2.2
- Making possible to have full row validation (see issues #69 and #51)
- Updated demo

### Version 2.2.1
- Fixing issue #67: ESC-Click combination froze editor

### Version 2.2.0
- Fixed issue #65: Column indeces where off by one with multiselect model, and column disabling did not work 
- Changed behavior, FastNavigation will no longer force Editor to be enabled

### Version 2.1.20
- Fixed issue #64: RowEditEvent fired after ESC-key 

### Version 2.1.19
- Fixed issue #63: Numpad input was not filttered properly

### Version 2.1.18
- Fixed serialization issue
- Made EditorWidgets.registerHandler(..) public so that it is possible to add your custom editor widget handlers, and added java docs.
- Fix: Selection of the ComboBox textfield was missing
 
### Version 2.1.17
- Fix: Do not fire ClickOutEvent if ComboBox or DateField is open.

### Version 2.1.16
- Returned disableColumns(..) to EditorOpenEvent. It can be used to disable additional columns from editing.

### Version 2.1.15
- Fix, clickOutListener did not work if FastNavigation was not instantiated with dispatchEditEventOnBlur=true parameter (issue: #54)

### Version 2.1.14
- Fix, disabled columns / readonly fields detection logic was flawed (see: issue #55)
- Fixing internal columns handling (see: issue #50)
- Fix, Internal method to get item for events, did NPE with empty DataProvider (see: issue #56) 

### Version 2.1.13
- Bugfix, cursor up/down did not open ComboBox in IE11

### Version 2.1.12
- Fixing issue #52

### Version 2.1.11
- Added possibility to add save shortcuts (see: issue #49)
- setSaveWithCtrlS(true) -> CTRL+S does save and close editor

### Version 2.1.10
- Solving issue #42: Clarifying setOpenEditorWithSingleClick(true) JavaDoc. Using it will prevent Grid's item click event and selection event getting the click.

### Version 2.1.9
- Changed getItem() in selected events to use DataCommunicator, requires Vaadin 8.2+ 

### Version 2.1.8
- Added ClickOutListener 

### Version 2.1.7
- Changed widgetset.xml filename, Fixed issues #40, #41

### Version 2.1.6
- Modified CellFocusEvent and RowFocusEvent to return -1 if focus is in Header/Footer
- Updated JavaDocs

### Version 2.1.5
- Added getItem() to RowFocusEvent and CellFocusEvent
- Minor bugfix, there was regression in fix for issue #35

### Version 2.1.4
- Added getItem() CellEditEvent and RowEditEvent (experimental)
- Fixed issue #37: Enter now saves the value when closing the editor in the last cell
- Fixed issue #35: Listeners should be able to be used automatically now.
- DeleteButtonRenderer deprecated, it is now maintained in Grid RenderersCollection add-on.
- Updated the demo.

### Version 2.1.3
- Added support for closing Editor and dispatching event when clicking outside of Grid.

### Version 2.1.2
- DeleteButtonRenderer, made it boolean type, so that property underneath controls whether the button is enabled or not

### Version 2.1.1
- Fixed DeleteButtonRenderer: Html content mode support in client side was missing
- Demo updated 

### Version 2.1.0
- Added DeleteButtonRenderer
- Demo updated 

### Version 2.0.0
- First version for Vaadin 8
- Features on par with version 0.5.7
- Demo updated 

## Issue tracking

The issues for this add-on are tracked on its github.com page. All bug reports and feature requests are appreciated. 

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

GridFastNavigation is written by Patrik Lindström, Tatu Lund and Johannes Tuikkala and maintained by the Tatu. Vaadin 8 migration initial work was contributed by Brett Sutton.

Major pieces of development of this add-on has been sponsored by multiple Support and Prime customers of Vaadin. See vaadin.com/support and Development on Demand for more details. 


