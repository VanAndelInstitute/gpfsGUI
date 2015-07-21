package com.zaxxis.gpfs.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.Popup;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
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
		showImagePopup();
		gpfsService.getMMState(new AsyncCallback<List<NodeState>>(){

			@Override
			public void onFailure(Throwable caught) {
				popupImage.hide();
				
			}

			@Override
			public void onSuccess(List<NodeState> result) {
				store.replaceAll(result);
				popupImage.hide();
				
			}});
		
	}
	void drawTable()
	{
		 List<ColumnConfig<NodeState, ?>> columnDefs = new ArrayList<ColumnConfig<NodeState, ?>>();
		 ColumnConfig<NodeState, String> cc1 = new ColumnConfig<NodeState, String>(properties.nodeNumber(), 250, "Node Number");
		 ColumnConfig<NodeState, String> cc2 = new ColumnConfig<NodeState, String>(properties.nodeName(), 220, "Node Name");
		 ColumnConfig<NodeState, String> cc3 = new ColumnConfig<NodeState, String>(properties.nodeState(), 220, "Node State");
		 columnDefs.add(cc1);
		 columnDefs.add(cc2);
		 columnDefs.add(cc3);
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
					gpfsService.runCmd("\"ssh root@" + selected.getNodeName() + " cat /var/mmfs/gen/mmfslog\"", new AsyncCallback<String>(){

						@Override
						public void onFailure(Throwable caught) {
							caught.printStackTrace();
						}

						@Override
						public void onSuccess(String result) {
							log.setText(result);							
						}});
					
			}
		});
	    
	    //gpfs node ops
	    String[] ops = {"mmshutdown -N","mmstartup -N","mmsdrrestore -N", "mmaddnode -N","mmdelnode -N","mmchlicense client --accept -N"};
	    for(final String op:ops)
	    {
	    	MenuItem action = new MenuItem();
	    	contextMenu.add(action);
	    	action.setText(op);
	    	action.addSelectionHandler(new SelectionHandler<Item>(){
				@Override
				public void onSelection(SelectionEvent<Item> event) 
				{					
						List<NodeState> selected = grid.getSelectionModel().getSelectedItems();
						String nodeList = "";
						for(NodeState s : selected)
							nodeList += s.getNodeName() + ",";
						nodeList =  nodeList.substring(0, nodeList.length()-1);
						doGPFSConfirm(op + " " + nodeList);
				}
			});
	     }
        
	   

	}
	
	private void showImagePopup() 
	{
	        final Image image = new Image("/images/loading1.gif");
	        popupImage.add(image);
	        popupImage.show();
	        popupImage.setPosition(100, 100);
	}
	
	private void doGPFSConfirm(String s)
	{
		Dialog d = new Dialog();
		 d.setHeadingText("GPFS system change confirmation!");
		 d.setWidget(new HTML("run: <br/><strong> \"" + s + "\"</strong><br/> ARE YOU SURE?!?!"));
		 d.setBodyStyle("fontWeight:bold;padding:13px;");
		 d.setPixelSize(500, 300);
		 d.setHideOnButtonClick(true);
		 d.setPredefinedButtons(PredefinedButton.YES, PredefinedButton.CANCEL);
		 d.show();
	}
}
