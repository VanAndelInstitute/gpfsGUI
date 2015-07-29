package com.zaxxis.gpfs.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.TabPanel;

import java.util.HashMap;

public class GPFSManager extends Composite
{

	private static GPFSManagerUiBinder uiBinder = GWT.create(GPFSManagerUiBinder.class);

	interface GPFSManagerUiBinder extends UiBinder<Widget, GPFSManager> 	{}
	private final GPFSServiceAsync gpfsService = GWT.create(GPFSService.class);
	@UiField TabPanel tab;

	public GPFSManager()
	{
		initWidget(uiBinder.createAndBindUi(this));
	
		tab.add(new NodesList(), "Node Management");
		gpfsService.getConfig(new AsyncCallback<HashMap<String,String>>(){
			@Override
			public void onFailure(Throwable caught) 
			{
				caught.printStackTrace();
			}

			@Override
			public void onSuccess(HashMap<String, String> result)
			{
				for(int i = 1; result.containsKey("cmd" + i);i++)
					tab.add(new GPFSTable("cmd"+i,result.get("cmd"+i),result.get("cmd"+i+"cols").split(",")), result.get("cmd"+i+"title"));
			}});
	}
}
