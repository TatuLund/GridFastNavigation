package org.vaadin.patrik.shared;

import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.communication.ServerRpc;

public interface DeleteButtonRendererServerRpc extends ServerRpc {
	
	public void onClick(String rowKey, MouseEventDetails mouseEventDetails);
}
