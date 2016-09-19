package org.vaadin.patrik.shared;

import com.vaadin.shared.communication.ServerRpc;

/**
 * Client-to-server RPC methods 
 */
public interface FastNavigationServerRPC extends ServerRpc {
    
    void focusUpdated(int rowIndex, int colIndex);
    
    void rowUpdated(int rowIndex);

    void cellUpdated(int rowIndex, int colIndex, String oldData,
            String newData);
    
    void editorOpened(int rowIndex, int colIndex);
    
    void ping();
    
}
