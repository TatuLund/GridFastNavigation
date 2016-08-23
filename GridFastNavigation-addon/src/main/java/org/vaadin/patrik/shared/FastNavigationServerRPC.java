package org.vaadin.patrik.shared;

import com.vaadin.shared.communication.ServerRpc;

public interface FastNavigationServerRPC extends ServerRpc {
    void rowUpdated(int rowIndex);
}
