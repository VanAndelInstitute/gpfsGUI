package com.zaxxis.gpfs.client;

import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.zaxxis.gpfs.shared.NodeState;
import com.zaxxis.gpfs.shared.TableData;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GPFSServiceAsync {
	void getMMState(AsyncCallback<List<NodeState>> callback);
	void getTabularData(String cmd, AsyncCallback<List<TableData>> callback);
	void getConfig(AsyncCallback<HashMap<String, String>> callback);
	void getLogForHosts(List<String> hosts, AsyncCallback<String> callback);
	void runCmd(String nodeop, List<String> hosts, AsyncCallback<String> callback);
}
