package com.zaxxis.gpfs.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.zaxxis.gpfs.client.GPFSService;
import com.zaxxis.gpfs.shared.NodeState;
import com.zaxxis.gpfs.shared.TableData;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GPFSServiceImpl extends RemoteServiceServlet implements GPFSService 
{

	public String runCmd(String input) 
	{
		return execCmd(input);
	}

	
	private String execCmd(String cmd)
	{
		try
		{
			String ret = "";
			Properties prop = new Properties();
			prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties"));
			String script = prop.getProperty("execScript");
			String host = prop.getProperty("execHost");
			
			String[] aCmdArgs = {script,"ssh root@"+host,cmd};
			Runtime oRuntime = Runtime.getRuntime();
			Process oProcess = null;
			
			oProcess = oRuntime.exec(aCmdArgs);			
			BufferedReader is = new BufferedReader(new InputStreamReader(oProcess.getInputStream()));
			String line;
			while((line = is.readLine()) != null)
				ret += line + "\n";
			
			oProcess.waitFor();
			
			return ret;
			
			
		} catch (Exception e)
		{
			e.printStackTrace();
			return "exec failure\n";
			
		}
		
	}

	@Override
	public List<NodeState> getMMState() 
	{
		List<NodeState> nodes = new ArrayList<>();
		HashMap<String,NodeState> nodeMap = new HashMap<String,NodeState>();
		String[] lines = execCmd("mmgetstate -aL").split("\n");
		for(int i=3;i < lines.length;i++)
		{
			
			String[] vals = lines[i].trim().split("\\s+");
			if(vals.length < 4)
				continue;
			NodeState n = new NodeState();
			n.setNodeNumber(vals[0]);
			n.setNodeName(vals[1]);
			n.setNodeState(vals[5]);
			n.setAlreadyInGPFS(true);
			nodes.add(n);
			nodeMap.put(n.getNodeNumber(),n);
		}
		
		
		String lines2[] = execCmd("mmlscluster").split("\n");
		for(int i=17;i < lines2.length;i++)
		{
			
			String[] vals = lines2[i].trim().split("\\s+");
			if(vals.length < 3)
				continue;
			nodeMap.get(vals[0]).setNodeIP(vals[2]);
			if(vals.length > 4)
				nodeMap.get(vals[0]).setNodeRole(vals[4]);
			else
				nodeMap.get(vals[0]).setNodeRole("");
		}
		
		
		return nodes;
		
	}


	@Override
	public List<TableData> getTabularData(String cmd) {
		List<TableData> table = new ArrayList<TableData>();
		String[] lines = execCmd(cmd).split("\n");
		for(int i=0;i < lines.length;i++)
		{
			String[] vals = lines[i].trim().split("\\s+");
			
			TableData n = new TableData();
			for(String v:vals)
				n.add(v);
			table.add(n);
		}
		return table;
	}
	
	
}
