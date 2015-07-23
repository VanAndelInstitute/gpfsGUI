package com.zaxxis.gpfs.shared;
import java.io.Serializable;
import java.util.ArrayList;

public class TableData extends ArrayList<String> implements Serializable
{

	private static final long serialVersionUID = 1L;
	
	public String getKey()
	{
		String key = "";
		for(String s:this)
			key += s;
			
		return key;
	}
	public void setKey(String name)
	{
		this.set(0, name);
	}	
}
