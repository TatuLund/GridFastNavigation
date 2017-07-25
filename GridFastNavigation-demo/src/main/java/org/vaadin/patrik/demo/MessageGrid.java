package org.vaadin.patrik.demo;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.Grid;
import com.vaadin.ui.UI;

public class MessageGrid extends Grid<ServerMessage> implements MessageLog
{
	private static final long serialVersionUID = 1L;


	private static final int MAX_MESSAGES = 50;


	List<ServerMessage> messageList;
	ListDataProvider<ServerMessage> messageData;

	MessageGrid()
	{
		super("Message Log");
		
		messageList = new ArrayList<>();
		messageData = new ListDataProvider<ServerMessage>(messageList);

		this.setDataProvider(messageData);
		this.addColumn(ServerMessage::getMessage).setCaption("Message").setExpandRatio(1);
		this.setSizeFull();
	}
	
	public void writeOutput(final String msg)
	{
		UI.getCurrent().access(new Runnable()
		{
			@Override
			public void run()
			{
				while (messageData.getItems().size() >= MAX_MESSAGES)
				{
					messageList.remove(0);
					messageData.refreshAll();
				}

				ServerMessage message = new ServerMessage(msg);
				messageList.add(message);
				messageData.refreshItem(message);
				MessageGrid.this.scrollTo(messageList.size() - 1);
			}
		});
	}

	public void clear()
	{
		messageList.clear();
		messageData.refreshAll();
	}


}
