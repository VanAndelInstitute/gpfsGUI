package com.zaxxis.gpfs.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sencha.gxt.widget.core.client.container.Viewport;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GpfsGUI implements EntryPoint {
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() 
	{
		Viewport viewport = new Viewport();
		NodesList nl = new NodesList();
		viewport.setWidget(nl);
		RootLayoutPanel.get().add(viewport);
	}
}
