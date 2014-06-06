package com.mcdermotsoft;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Slot
{
	private String label, tsid;
	private int count;
	@JsonProperty("item_def")
	private ItemDef item_def;
	private Map<String,Slot> contents;
	
	public Slot(){}
	
	public String getLabel() {
		return label;
	}

	public ItemDef getItemDef() {
		return item_def;
	}

	public Map<String,Slot> getContents() {
		return contents;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setDef(ItemDef item_def) {
		this.item_def = item_def;
	}

	public void setContents(Map<String,Slot> contents) {
		this.contents = contents;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getTsid() {
		return tsid;
	}

	public void setTsid(String tsid) {
		this.tsid = tsid;
	}
}