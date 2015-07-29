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
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.container.MarginData;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.zaxxis.gpfs.shared.NodeState;
import com.zaxxis.gpfs.shared.TableData;
import com.zaxxis.gpfs.shared.TableDataValueProvider;

public class GPFSTable extends Composite
{

	private static GPFSTableUiBinder uiBinder = GWT.create(GPFSTableUiBinder.class);
	interface GPFSTableUiBinder extends UiBinder<Widget, GPFSTable> { }
	private final GPFSServiceAsync gpfsService = GWT.create(GPFSService.class);
	@UiField VerticalLayoutContainer tablePanel;
	Grid<TableData> grid;
	ListStore<TableData> store = new ListStore<TableData>(new ModelKeyProvider<TableData>() {
	    @Override
	    public String getKey(TableData item) {
	      return item.getKey();
	    }
	  });
	String nodeop = "nodeop1";
	String ExecCmd = "uname -a";
	String[] columns = {"none"};
	
	
	
	public GPFSTable(String nodeop,String cmd,String[] columns)
	{
		initWidget(uiBinder.createAndBindUi(this));
		tablePanel.setHeight(Window.getClientHeight());
		tablePanel.forceLayout();
		this.nodeop = nodeop;
		this.ExecCmd = cmd;
		this.columns=columns;
		reloadState();
	}
	

	private void reloadState()
	{
		tablePanel.add(LoadingPopup.getLoadingPanel("loading \'" + ExecCmd + "\'"));
		//final LoadingPopup loading = new LoadingPopup("Loading..." + ExecCmd,grid);
		gpfsService.getTabularData(nodeop, new AsyncCallback<List<TableData>>(){

			@Override
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
				tablePanel.add(new HTML("Error running " + ExecCmd));
			
			}

			@Override
			public void onSuccess(List<TableData> result) {
				store.replaceAll(result);
				 List<ColumnConfig<TableData, ?>> columnDefs = new ArrayList<ColumnConfig<TableData, ?>>();
				 for(int i = 0; i < store.get(0).size();i++)
					 columnDefs.add(new ColumnConfig<TableData, String>(new TableDataValueProvider(i), 1000 / store.get(0).size(), i < columns.length ? columns[i] : "Column " + i));
				 ColumnModel<TableData> colModel = new ColumnModel<TableData>(columnDefs); 
				 grid = new Grid<TableData>(store, colModel);
				 grid.setHeight(Window.getClientHeight() - 30);
				 addContextMenu(); 
				 
				 
				 
				 
				//tablePanel.add(grid);
				tablePanel.clear();
				tablePanel.add(grid,new VerticalLayoutData(1, -1));
				tablePanel.forceLayout();
				tablePanel.getScrollSupport().setScrollMode(ScrollMode.ALWAYS);;
							
			}});
	}
	
	public void addContextMenu() 
	{
	    final Menu contextMenu= new Menu();
	    grid.setContextMenu(contextMenu);
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
	}
	
	
}
