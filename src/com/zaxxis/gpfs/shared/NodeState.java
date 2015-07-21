package com.zaxxis.gpfs.shared;

import java.io.Serializable;

public class NodeState implements Serializable
{
	private static final long serialVersionUID = 1L;
	String nodeNumber;
	String nodeName;
	String nodeState;
	Boolean alreadyInGPFS;
	
	
	public String getKey()
	{
		return nodeNumber;
	}
	public void setKey(String name)
	{
		this.nodeNumber = name;
	}
	
	public String getNodeNumber() {
		return nodeNumber;
	}
	public void setNodeNumber(String nodeNumber) {
		this.nodeNumber = nodeNumber;
	}
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	public String getNodeState() {
		return nodeState;
	}
	public void setNodeState(String nodeState) {
		this.nodeState = nodeState;
	}
	public Boolean getAlreadyInGPFS() {
		return alreadyInGPFS;
	}
	public void setAlreadyInGPFS(Boolean alreadyInGPFS) {
		this.alreadyInGPFS = alreadyInGPFS;
	}

}
