package org.vaadin.patrik.client;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;

public final class Keys {
    private static final Set<Integer> alphaNumSet;
    
    private static final Set<Integer> rowChangeKeys;
    
    private static final Set<Integer> colChangeKeys;
    
    static {
        alphaNumSet = new HashSet<Integer>();
        
        for (int i = KeyCodes.KEY_A; i <= KeyCodes.KEY_Z; i++) {
            alphaNumSet.add(i);
        }
        for (int i = KeyCodes.KEY_ZERO; i <= KeyCodes.KEY_NINE; i++) {
            alphaNumSet.add(i);
        }
        for (int i = KeyCodes.KEY_NUM_ZERO; i <= KeyCodes.KEY_NUM_NINE; i++) {
            alphaNumSet.add(i);
        }
        
        rowChangeKeys = new HashSet<Integer>();
        rowChangeKeys.add(KeyCodes.KEY_UP);
        rowChangeKeys.add(KeyCodes.KEY_DOWN);
        rowChangeKeys.add(KeyCodes.KEY_PAGEUP);
        rowChangeKeys.add(KeyCodes.KEY_PAGEDOWN);
        
        colChangeKeys = new HashSet<Integer>();
        colChangeKeys.add(KeyCodes.KEY_TAB);
        
    }
    
    public static void setEnterBehavior(boolean changeColumnByEnter) {
    	if (changeColumnByEnter) {
            colChangeKeys.add(KeyCodes.KEY_ENTER);
            colChangeKeys.add(KeyCodes.KEY_MAC_ENTER);  		
    	} else {
            rowChangeKeys.add(KeyCodes.KEY_ENTER);
            rowChangeKeys.add(KeyCodes.KEY_MAC_ENTER);    		
    	}
    }
    
    public static int translateNumKey(int keycode) {
    	if (keycode >= 96 && keycode <= 105) return keycode - 48;
    	else return keycode;    	
    }
    
    public static boolean isUpDownArrowKey(int keycode) {
        if(keycode == KeyCodes.KEY_UP || keycode == KeyCodes.KEY_DOWN || keycode == KeyCodes.KEY_PAGEUP || keycode == KeyCodes.KEY_PAGEDOWN) {
            return true;
        }
        return false;
    }
    
    public static boolean isDelKey(int keycode) {
    	if(keycode == KeyCodes.KEY_DELETE) {
    		return true;
    	}
    	return false;
    }

    public static boolean isHomeKey(int keycode) {
    	if(keycode == KeyCodes.KEY_HOME) {
    		return true;
    	}
    	return false;
    }

    public static boolean isEndKey(int keycode) {
    	if(keycode == KeyCodes.KEY_END) {
    		return true;
    	}
    	return false;
    }
    
    
    public static boolean isSpaceKey(int keycode) {
    	if (keycode == KeyCodes.KEY_SPACE) {
    		return true;
    	}
    	return false;
    }

    /**
     * Test if keycode is one of the alpha numeric keys [0-9a-zA-Z]
     * 
     * @param keyCode Key code to be tested
     * @return true if key is alphanumeric
     */
    public static boolean isAlphaNumericKey(int keyCode) {
        return alphaNumSet.contains(keyCode);
    }

    /**
     * Test if keycode is defined as a row change key
     * 
     * @param keyCode Key code to be tested
     * @return true if key is one of the row change keys
     */
    public static boolean isRowChangeKey(int keyCode) {
        return rowChangeKeys.contains(keyCode);
    }
    
    /**
     * Test if keycode is defined as a column change key
     * 
     * @param keyCode Key code to be tested
     * @return true if key is one of the column change keys
     */
    public static boolean isColumnChangeKey(int keyCode) {
        return colChangeKeys.contains(keyCode);
    }


    /**
     * Test if key event contained a mofifier key (i.e. Ctrl, Shift or Alt)
     * 
     * @param event DOM Event
     * @return true if key event contained modifier key
     */
	public static boolean isModifierKey(Event event) {
		return event.getShiftKey() || event.getCtrlKey() || event.getAltKey();
	}    
}
