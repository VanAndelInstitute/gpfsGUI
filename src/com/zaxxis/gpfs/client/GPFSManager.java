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
		//TODO it would be better to not have the gpfs volume names hardcoded (ie home and scrath). they could come from the config or dynamically determined.
		tab.add(new NodesList(), "Node Management");
		tab.add(new GPFSTable("\"mmlsdisk home | tail -n +4 ; mmlsdisk scratch | tail -n +4\"",new String[] {"disk","driver","sectorSize","failure group","hasMeta","hasData","status","availability","pool"}), "Disk Status");
		tab.add(new GPFSTable("\"mmlsfileset  home | tail -n +3  ; mmlsfileset  scratch | tail -n +3\"",new String[] {"Name","Status","Path"}), "FileSets");
		tab.add(new GPFSTable("\"mmlsnsd | tail -n +4\"",new String[] {"FileSystem","Disk","NSD's"}), "NSD's");
		tab.add(new GPFSTable("\"mmlsconfig | tail -n +3 | head -n -5\"",new String[] {"Setting","Value"}), "Cluster Config");
		tab.add(new GPFSTable("\"mmlslicense -L | tail -n +3 | head -n -9\"",new String[] {"Server","Lic req'd","Lic used"}), "Licensing");
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
					tab.add(new GPFSTable(result.get("cmd"+i),result.get("cmd"+i+"cols").split(",")), result.get("cmd"+i+"title"));
			}});
		
	}

}
