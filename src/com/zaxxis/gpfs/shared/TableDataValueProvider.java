package com.zaxxis.gpfs.shared;

import com.sencha.gxt.core.client.ValueProvider;

public class TableDataValueProvider implements ValueProvider<TableData,String>
{
	private Integer index;
	public TableDataValueProvider(int index)
	{
		this.index = index;
	}
	
	@Override
	public String getValue(TableData object) {
		if(index < object.size())
			return object.get(index);
		else
			return "";
	}

	@Override
	public void setValue(TableData object, String value) {
		object.set(index, value);
	}

	@Override
	public String getPath() {
		return index.toString();
	}
}
