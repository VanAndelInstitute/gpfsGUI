package com.zaxxis.gpfs.client;

import java.util.ArrayList;
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
import com.google.gwt.user.client.ui.Image;
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
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
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
	@UiField ContentPanel gridPanel;
	@UiField VerticalLayoutContainer vlc;
	@UiField SimpleContainer table;
	@UiField TextArea log;
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
	    Menu contextMenu= new Menu();
	    grid.setContextMenu(contextMenu);
	    MenuItem viewlog = new MenuItem();
	    contextMenu.add(viewlog);
	    viewlog.setText("View mmfslog");
	    viewlog.addSelectionHandler(new SelectionHandler<Item>(){
			@Override
			public void onSelection(SelectionEvent<Item> event) 
			{					
					NodeState selected = grid.getSelectionModel().getSelectedItem();
					final LoadingPopup loading = new LoadingPopup("Loading log for " + selected.getNodeName() + "...",log);
					gpfsService.runCmd("\"ssh root@" + selected.getNodeName() + " cat /var/mmfs/gen/mmfslog\"", new AsyncCallback<String>(){

						@Override
						public void onFailure(Throwable caught) {
							caught.printStackTrace();
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
	    
	    //gpfs node ops
	    String[] ops = {"ping -c 3","mmshutdown -N","mmstartup -N","mmsdrrestore -N", "mmaddnode -N","mmdelnode -N","mmchlicense client --accept -N"};
	    for(final String op:ops)
	    {
	    	MenuItem action = new MenuItem();
	    	contextMenu.add(action);
	    	action.setText(op);
	    	action.addSelectionHandler(new SelectionHandler<Item>(){
				@Override
				public void onSelection(SelectionEvent<Item> event) 
				{					
						boolean safeToRunOnNSD = true;
						List<NodeState> selected = grid.getSelectionModel().getSelectedItems();
						for(NodeState n : selected)
							if(n.getNodeRole().toLowerCase().contains("quo") && op.toLowerCase().contains("mm"))
								safeToRunOnNSD = false;
						String nodeList = "";
						for(NodeState s : selected)
							nodeList += s.getNodeName() + ",";
						nodeList =  nodeList.substring(0, nodeList.length()-1);
						if(safeToRunOnNSD)
							doGPFSConfirm(op + " " + nodeList);
						else
							{ 
								AlertMessageBox alert = new AlertMessageBox("Error, NSD operations must be done via console", "This tool has disabled modifying NSDs for safety reasons");
								alert.show();
							}
				}
			});
	     }
	}
	
	private void doGPFSConfirm(final String s)
	{
		Dialog d = new Dialog();
		 d.setHeadingText("GPFS system change confirmation!");
		 d.setWidget(new HTML("run: <br/><strong> \"" + s + "\"</strong><br/> ARE YOU SURE?!?!"));
		 d.setBodyStyle("fontWeight:bold;padding:13px;");
		 d.setPixelSize(400, 200);
		 d.setHideOnButtonClick(true);
		 d.setPredefinedButtons(PredefinedButton.YES, PredefinedButton.CANCEL);
		 d.show();
		
		 d.getButton(PredefinedButton.YES).addSelectHandler(new SelectHandler(){

			@Override
			public void onSelect(SelectEvent event)
			{
				final LoadingPopup loading = new LoadingPopup("running cmd: " + s + "  ...",log);
				gpfsService.runCmd("\"" + s + "\"", new AsyncCallback<String>(){

					@Override
					public void onFailure(Throwable caught) {
						caught.printStackTrace();
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
