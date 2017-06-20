# GridFastNavigation Add-on for Vaadin 7

Do you like the Grid but would want it to appeal more to old Excel jockeys?
Then this add-on is for you!
GridFastNavigation is a compnent extension for Vaadin Grid, which uses the
unbuffered editing mode and alters its keyboard controls to provide a faster
editing experience.

## Online demo
[Try it here](http://patrik.app.fi/GridFastNavigation-demo-0.2.0/)

## Download release

Official releases of this add-on are available at Vaadin Directory. For Maven instructions, download and reviews, go to https://vaadin.com/directory/-/directory/addon/#!addon/gridfastnavigation-add-on

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

To debug project and make code modifications on the fly in the server-side, right-click the GridFastNavigation-demo project and choose Debug As > Debug on Server. Navigate to http://localhost:8080/GridFastNavigation-demo/ to see the application.

### Debugging client-side

Debugging client side code in the GridFastNavigation-demo project:
  - run "mvn vaadin:run-codeserver" on a separate console while the application is running
  - activate Super Dev Mode in the debug window of the application or by adding ?superdevmode to the URL
  - You can access Java-sources and set breakpoints inside Chrome if you enable source maps from inspector settings.
 
## Release notes

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

GridFastNavigation is written by Patrik Lindström, Tatu Lund and Johannes Tuikkala and maintained by the Tatu

