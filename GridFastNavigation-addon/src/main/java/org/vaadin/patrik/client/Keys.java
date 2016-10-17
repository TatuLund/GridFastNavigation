package org.vaadin.patrik.client;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.event.dom.client.KeyCodes;

public final class Keys {
    private static final Set<Integer> alphaNumSet;
    
    private static final Set<Integer> rowChangeKeys;
    
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
        rowChangeKeys.add(KeyCodes.KEY_ENTER);
        rowChangeKeys.add(KeyCodes.KEY_MAC_ENTER);
        rowChangeKeys.add(KeyCodes.KEY_UP);
        rowChangeKeys.add(KeyCodes.KEY_DOWN);
    }
    
    /**
     * Test if keycode is one of the alpha numeric keys [0-9a-zA-Z]
     */
    public static boolean isAlphaNumericKey(int keyCode) {
        return alphaNumSet.contains(keyCode);
    }

    /**
     * Test if keycode is defined as a row change key
     */
    public static boolean isRowChangeKey(int keyCode) {
        return rowChangeKeys.contains(keyCode);
    }
}
