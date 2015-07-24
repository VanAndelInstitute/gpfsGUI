package com.zaxxis.gpfs.client;

import java.util.HashMap;
import java.util.List;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.zaxxis.gpfs.shared.NodeState;
import com.zaxxis.gpfs.shared.TableData;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("gpfsservice")
public interface GPFSService extends RemoteService {
	String runCmd(String name);
	List<NodeState> getMMState();
	List<TableData> getTabularData(String cmd);
	HashMap<String,String> getConfig();
	
}
