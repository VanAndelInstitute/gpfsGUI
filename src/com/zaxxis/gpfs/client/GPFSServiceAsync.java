package com.zaxxis.gpfs.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.zaxxis.gpfs.shared.NodeState;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GPFSServiceAsync {
	void runCmd(String input, AsyncCallback<String> callback)
			throws IllegalArgumentException;

	void getMMState(AsyncCallback<List<NodeState>> callback);
}
