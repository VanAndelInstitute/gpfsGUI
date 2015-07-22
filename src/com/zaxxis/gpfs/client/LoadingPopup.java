package com.zaxxis.gpfs.client;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Popup;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;

public class LoadingPopup extends Popup
{
	LoadingPopup(String msg,int x, int y)
	{
		super();
		VerticalLayoutContainer v = new VerticalLayoutContainer();
		final Image image = new Image("/images/loading1.gif");
        this.add(v);
		v.add(image);
        v.add(new HTML(msg));
        this.show();
        this.setPosition(x, y);
	}
	LoadingPopup(String msg,Widget w)
	{
		super();
		VerticalLayoutContainer v = new VerticalLayoutContainer();
		final Image image = new Image("/images/loading1.gif");
        this.add(v);
		v.add(image);
        v.add(new HTML(msg));
        this.show();
        this.setPosition(w.getAbsoluteLeft() + 30, w.getAbsoluteTop() + 30);
	}

}
