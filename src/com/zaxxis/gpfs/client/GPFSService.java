package com.zaxxis.gpfs.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.zaxxis.gpfs.shared.NodeState;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("gpfsservice")
public interface GPFSService extends RemoteService {
	String runCmd(String name);
	List<NodeState> getMMState();
	
}
