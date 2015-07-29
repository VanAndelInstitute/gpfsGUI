package com.zaxxis.gpfs.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.Popup;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.zaxxis.gpfs.shared.NodeState;
import com.zaxxis.gpfs.shared.NodeStateModel;

public class NodesList extends Composite
{
	private static NodesListUiBinder uiBinder = GWT.create(NodesListUiBinder.class);
	interface NodesListUiBinder extends UiBinder<Widget, NodesList> {}
	private final GPFSServiceAsync gpfsService = GWT.create(GPFSService.class);
	@UiField VerticalLayoutContainer vlc;
	@UiField SimpleContainer table;
	@UiField TextArea log;
	@UiField ContentPanel logPanel;
	private static final NodeStateModel properties = GWT.create(NodeStateModel.class);
	ListStore<NodeState> store=new ListStore<NodeState>(properties.key());
	Grid<NodeState> grid;
	Popup popupImage = new Popup();
	
	public NodesList() 
	{
		initWidget(uiBinder.createAndBindUi(this));
		drawTable();
		reloadState();
	}
	
	private void reloadState()
	{
		final LoadingPopup loading = new LoadingPopup("Loading mmgetstate..",grid);
		gpfsService.getMMState(new AsyncCallback<List<NodeState>>(){

			@Override
			public void onFailure(Throwable caught) {
				loading.hide();
			}

			@Override
			public void onSuccess(List<NodeState> result) {
				store.replaceAll(result);
				loading.hide();
			}});
	}
	
	void drawTable()
	{
		 List<ColumnConfig<NodeState, ?>> columnDefs = new ArrayList<ColumnConfig<NodeState, ?>>();
		 columnDefs.add(new ColumnConfig<NodeState, String>(properties.nodeNumber(), 70, "Node #"));
		 columnDefs.add(new ColumnConfig<NodeState, String>(properties.nodeName(), 200, "Node Name"));
		 columnDefs.add(new ColumnConfig<NodeState, String>(properties.nodeIP(), 220, "Node IP"));
		 columnDefs.add(new ColumnConfig<NodeState, String>(properties.nodeRole(), 220, "Node Role"));
		 columnDefs.add(new ColumnConfig<NodeState, String>(properties.nodeState(), 180, "Node State"));
		
		 ColumnModel<NodeState> colModel = new ColumnModel<NodeState>(columnDefs);
		 grid = new Grid<NodeState>(store, colModel);
		 grid.setHeight(Window.getClientHeight() - 400);
		 table.add(grid);
		 
		 addContextMenu();
	}
	
	public void addContextMenu() 
	{
	    final Menu contextMenu= new Menu();
	    grid.setContextMenu(contextMenu);
	    MenuItem viewlog = new MenuItem();
	    contextMenu.add(viewlog);
	    viewlog.setText("View mmfslog");
	    viewlog.addSelectionHandler(new SelectionHandler<Item>(){
			@Override
			public void onSelection(SelectionEvent<Item> event) 
			{					
					List<String> nodes = new ArrayList<String>();
					for(NodeState n :grid.getSelectionModel().getSelectedItems())
						nodes.add(n.getNodeName());
					
					
					logPanel.setHeadingText("Command Log: GPFS log for: " + nodes.get(0));
					final LoadingPopup loading = new LoadingPopup("Loading log for " + nodes.get(0) + "...",log);
					gpfsService.getLogForHosts(nodes, new AsyncCallback<String>(){
						@Override
						public void onFailure(Throwable caught) {
							log.setText(caught.getMessage());
							loading.hide();
						}

						@Override
						public void onSuccess(String result) {
							log.setText(result);
							loading.hide();
						}});
					
			}
		 });
	    MenuItem refresh = new MenuItem();
	    contextMenu.add(refresh);
	    refresh.setText("Refresh");
	    refresh.addSelectionHandler(new SelectionHandler<Item>(){
			@Override
			public void onSelection(SelectionEvent<Item> event) 
			{					
					reloadState();					
			}
		});
	 
	    //get gpfs node ops from config file and create actions
		 
	    gpfsService.getConfig(new AsyncCallback<HashMap<String,String>>(){
			@Override
			public void onFailure(Throwable caught) 
			{
				caught.printStackTrace();
			}

			@Override
			public void onSuccess(final HashMap<String, String> result)
			{
				for(int i = 1; result.containsKey("nodeop" + i);i++)
				{
					final int j=i;
					MenuItem action = new MenuItem();
			    	contextMenu.add(action);
			    	action.setText(result.get("nodeop"+i+"title"));
			    	action.addSelectionHandler(new SelectionHandler<Item>(){
						@Override
						public void onSelection(SelectionEvent<Item> event) 
						{					
								String nodelist = "";
								boolean safeToRunOnNSD = true;
								List<String> nodes = new ArrayList<String>();
								for(NodeState n: grid.getSelectionModel().getSelectedItems())
								{
									nodelist += n.getNodeName() + ",";
									nodes.add(n.getNodeName());
									if(n.getNodeRole().toLowerCase().contains("quo") && result.get("nodeop"+j).toLowerCase().contains("mm"))
										safeToRunOnNSD = false;
								}
								nodelist =  nodelist.substring(0, nodelist.length()-1);
									
								if(safeToRunOnNSD)
									doGPFSConfirm("nodeop"+j,result.get("nodeop"+j) + " " + nodelist ,nodes);
								else
								{ 
									AlertMessageBox alert = new AlertMessageBox("Error, NSD operations must be done via console", "This tool has disabled modifying NSDs for safety reasons");
									alert.show();
								}
						}
					});
				}
			}});
	    
	    //add a new node to the pool
	    MenuItem add = new MenuItem();
	    contextMenu.add(add);
	    add.setText("Add new node to cluster");
	    add.addSelectionHandler(new SelectionHandler<Item>(){
			@Override
			public void onSelection(SelectionEvent<Item> event) 
			{					
					addNode();					
			}
		});
	    
	    
	}
	
	private void addNode()
	{
		 Dialog d = new Dialog();
		 d.setHeadingText("Add new node to GPFS Cluster");
		 final TextField nodeName = new TextField();
		 VerticalLayoutContainer nameBox = new VerticalLayoutContainer();
		 nameBox.add(new FieldLabel(nodeName, "Node Host name to add"), new VerticalLayoutData(1, -1));
		 nameBox.add(new HTML("you can enter a single node, ex: node044 <br/> or many nodes like: node001,node002,node003,node004,node005,node006,node007,node008,node009,node010,node011,node012,node013,node014,node015,node016,node017,node018,node019,node020,<br/>node021,node022,node023,node024,node025,node026,node027,node028,node029,node030,node031,node032,node033,node034,node035,node036,node037,node038,node039,node040"));
		 d.setWidget(nameBox);
		 d.setBodyStyle("fontWeight:bold;padding:13px;");
		 d.setPixelSize(600, 300);
		 d.setHideOnButtonClick(true);
		 d.setPredefinedButtons(PredefinedButton.OK, PredefinedButton.CANCEL);
		 d.show();
		
		 d.getButton(PredefinedButton.OK).addSelectHandler(new SelectHandler(){
			@Override
			public void onSelect(SelectEvent event)
			{
				String[] nodes = nodeName.getText().trim().split(",");
				for(String node : nodes)
				{
					NodeState n = new NodeState();
					n.setAlreadyInGPFS(false);
					n.setNodeIP("Pending...");
					n.setNodeNumber("Pending "+node );
					n.setNodeRole("Pending");
					n.setNodeName(node);
					n.setNodeState("Pending (run mmaddnode!)");
					store.add(n);
				}
			}});
	}
	
	private void doGPFSConfirm(final String nodeop, final String cmdString, final List<String> nodes)
	{
		Dialog d = new Dialog();
		 d.setHeadingText("GPFS system change confirmation!");
		 d.setWidget(new HTML("run: <br/><strong> \"" + cmdString + "\"</strong><br/> ARE YOU SURE?!?!"));
		 d.setBodyStyle("fontWeight:bold;padding:13px;");
		 d.setPixelSize(400, 200);
		 d.setHideOnButtonClick(true);
		 d.setPredefinedButtons(PredefinedButton.YES, PredefinedButton.CANCEL);
		 d.show();
		
		 d.getButton(PredefinedButton.YES).addSelectHandler(new SelectHandler(){

			@Override
			public void onSelect(SelectEvent event)
			{
				logPanel.setHeadingText("Command Log: " + cmdString);
				final LoadingPopup loading = new LoadingPopup("running cmd: " + cmdString + "  ...",log);
				gpfsService.runCmd(nodeop,nodes, new AsyncCallback<String>(){

					@Override
					public void onFailure(Throwable caught) {
						log.setText(caught.getMessage());
						loading.hide();
					}

					@Override
					public void onSuccess(String result) {
						log.setText(result);
						loading.hide();
						reloadState();
					}});
				
			}});
	}
}
