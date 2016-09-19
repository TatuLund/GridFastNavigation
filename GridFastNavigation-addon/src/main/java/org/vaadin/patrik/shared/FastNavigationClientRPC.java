package org.vaadin.patrik.shared;

import com.vaadin.shared.communication.ClientRpc;

/**
 * Server-to-client RPC methods 
 */
public interface FastNavigationClientRPC extends ClientRpc {
    
    void setDisabledColumns(int[] indices);
 
    void unfreezeEditor();
    
}
