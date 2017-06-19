package org.vaadin.patrik.client;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ui.VCheckBox;
import com.vaadin.client.ui.VPopupCalendar;
import com.vaadin.client.ui.VTextField;

public class EditorWidgets {

    private static final Logger logger = Logger.getLogger("EditorWidgets");

    public interface WidgetHandler<T extends Widget> {

        void selectAll(T widget);

        String getValue(T widget);

        void setValue(T widget, String value);

        void focus(T widget);

        void enable(T widget);

        void disable(T widget);

        boolean isUpDownNavAllowed(T widget);
    }

    private static final Map<Class<?>, WidgetHandler<?>> widgetHandlers;

    //
    // Magic happens here: statically assign handlers for supported widget types
    // This enables support for value revert, append and selectAll
    //

    // Function exists to power syntax assist...
    private static <T extends Widget> void registerHandler(Class<T> cls,
            WidgetHandler<T> handler) {
        widgetHandlers.put(cls, handler);
    }

    static {
        widgetHandlers = new HashMap<Class<?>, WidgetHandler<?>>();

        registerHandler(VTextField.class, new WidgetHandler<VTextField>() {
            @Override
            public void selectAll(VTextField widget) {
                if (widget.isEnabled()) {
                    widget.selectAll();
                }
            }

            @Override
            public String getValue(VTextField widget) {
                return widget.getValue();
            }

            @Override
            public void setValue(VTextField widget, String value) {
                widget.setValue(value);
            }

            public void focus(VTextField widget) {
                if (widget.isReadOnly()) {
                    widget.getElement().blur();
                    widget.getElement().focus();
                }
            }

            @Override
            public void enable(VTextField widget) {
                widget.setEnabled(true);
                widget.setReadOnly(false);
            }

            @Override
            public void disable(VTextField widget) {
                widget.setEnabled(false);
                widget.setReadOnly(true);
            }

            @Override
            public boolean isUpDownNavAllowed(VTextField widget) {
                return true;
            }
        });

        registerHandler(VCheckBox.class, new WidgetHandler<VCheckBox>() {
            @Override
            public String getValue(VCheckBox widget) {
            	String value = "";
            	if (widget.getValue()) value = "true";
            	else value = "false";
                return value; 
            }

            @Override
            public void setValue(VCheckBox widget, String valueText) {
            	Boolean value = false;
            	if ("true".equals(valueText)) value = true; 
                widget.setValue(value);
            }

            public void focus(VCheckBox widget) {
                if (widget.isEnabled()) {
                    widget.getElement().blur();
                    widget.getElement().focus();
                }
            }

            @Override
            public void enable(VCheckBox widget) {
                widget.setEnabled(true);
            }

            @Override
            public void disable(VCheckBox widget) {
                widget.setEnabled(false);
            }

            @Override
            public boolean isUpDownNavAllowed(VCheckBox widget) {
                return true;
            }

			@Override
			public void selectAll(VCheckBox widget) {
				// TODO Auto-generated method stub
				
			}
        });

       registerHandler(VPopupCalendar.class,
                new WidgetHandler<VPopupCalendar>() {
                    @Override
                    public void selectAll(VPopupCalendar widget) {
                        if (widget.isEnabled()) {
                            widget.text.selectAll();
                        }
                    }

                    @Override
                    public String getValue(VPopupCalendar widget) {
                        return widget.text.getValue();
                    }

                    @Override
                    public void setValue(VPopupCalendar widget, String value) {
                        widget.text.setValue(value);
                    }

                    @Override
                    public void focus(VPopupCalendar widget) {
                        // Only perform blur/focus refocusing if calendar popup
                        // is not visible
                        if (widget.isEnabled()
                                && !widget.calendar.isAttached()) {
                            widget.getElement().blur();
                            widget.getElement().focus();
                        }
                    }

                    @Override
                    public void enable(VPopupCalendar widget) {
                        widget.setEnabled(true);
                        widget.setReadonly(false);
                    }

                    @Override
                    public void disable(VPopupCalendar widget) {
                        widget.setEnabled(false);
                        widget.setReadonly(true);
                    }

                    @Override
                    public boolean isUpDownNavAllowed(VPopupCalendar widget) {
                        return false;
                    }

                });

        // TODO: support more widget types!
    }

    private static <T> WidgetHandler<?> getHandler(Class<T> cls) {
        if (widgetHandlers.containsKey(cls)) {
            return widgetHandlers.get(cls);
        } else {
            logger.warning("Unhandled widget type " + cls.getSimpleName());
        }
        return null;
    }

    //
    // Public API
    //

    @SuppressWarnings("unchecked")
    public static <T extends Widget> void selectAll(T widget) {
        if (widget != null) {
            WidgetHandler<?> handler = getHandler(widget.getClass());
            if (handler != null) {
                ((WidgetHandler<T>) handler).selectAll(widget);
            }
        } else {
            logger.warning("EditorWidgets.selectAll: Widget is null");
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Widget> String getValue(T widget) {
        if (widget != null) {
            WidgetHandler<?> handler = getHandler(widget.getClass());
            if (handler != null) {
                return ((WidgetHandler<T>) handler).getValue(widget);
            }
        } else {
            logger.warning("EditorWidgets.getValue: Widget is null");
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    public static <T extends Widget> void setValue(T widget, String value) {
        if (widget != null) {
            WidgetHandler<?> handler = getHandler(widget.getClass());
            if (handler != null) {
                ((WidgetHandler<T>) handler).setValue(widget, value);
            }
        } else {
            logger.warning("EditorWidgets.setValue: Widget is null");
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Widget> void focus(T widget) {
        if (widget != null) {
            WidgetHandler<?> handler = getHandler(widget.getClass());
            if (handler != null) {
                ((WidgetHandler<T>) handler).focus(widget);
            }
        } else {
            logger.warning("EditorWidgets.focus: Widget is null");
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Widget> void enable(T widget) {
        if (widget != null) {
            WidgetHandler<?> handler = getHandler(widget.getClass());
            if (handler != null) {
                ((WidgetHandler<T>) handler).enable(widget);
            }
        } else {
            logger.warning("EditorWidgets.enable: Widget is null");
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Widget> void disable(T widget) {
        if (widget != null) {
            WidgetHandler<?> handler = getHandler(widget.getClass());
            if (handler != null) {
                ((WidgetHandler<T>) handler).disable(widget);
            }
        } else {
            logger.warning("EditorWidgets.disable: Widget is null");
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Widget> boolean isUpDownNavAllowed(T widget) {
        if (widget != null) {
            WidgetHandler<?> handler = getHandler(widget.getClass());
            if (handler != null) {
                return ((WidgetHandler<T>) handler).isUpDownNavAllowed(widget);
            }
        } else {
            logger.warning("EditorWidgets.isUpDownNavAllowed: Widget is null");
        }
        return true;
    }
}
