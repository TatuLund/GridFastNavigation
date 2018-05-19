package org.vaadin.patrik.shared;

import java.util.List;

import com.vaadin.shared.communication.ClientRpc;

/**
 * Server-to-client RPC methods 
 */
public interface FastNavigationClientRPC extends ClientRpc {
    
    void setDisabledColumns(List<Integer> indices);
 
    void unlockEditor(int lockId);
    
    void validationHasErrors();
    
}
