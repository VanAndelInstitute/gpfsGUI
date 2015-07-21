package com.zaxxis.gpfs.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.zaxxis.gpfs.client.GPFSService;
import com.zaxxis.gpfs.shared.NodeState;
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
		String[] lines = execCmd("mmgetstate -a").split("\n");
		for(int i=3;i < lines.length;i++)
		{
			
			String[] vals = lines[i].trim().split("\\s+");
			NodeState n = new NodeState();
			n.setNodeNumber(vals[0]);
			n.setNodeName(vals[1]);
			n.setNodeState(vals[2]);
			nodes.add(n);
		}
		return nodes;
		
	}
	
	
}
