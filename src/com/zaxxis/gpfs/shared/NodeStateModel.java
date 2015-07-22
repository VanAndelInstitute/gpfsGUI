package com.zaxxis.gpfs.shared;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface NodeStateModel  extends PropertyAccess<NodeState>
{
	 ModelKeyProvider<NodeState> key();
	 ValueProvider<NodeState, String> nodeNumber();
	 ValueProvider<NodeState, String> nodeName();
	 ValueProvider<NodeState, String> nodeState();
	 ValueProvider<NodeState, Boolean> alreadyInGPFS();
	 ValueProvider<NodeState, String> nodeIP();
	 ValueProvider<NodeState, String> nodeRole();
}
