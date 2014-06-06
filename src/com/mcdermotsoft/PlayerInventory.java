package com.mcdermotsoft;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerInventory
{
	private int ok;
	private Map<String,Slot> contents;
	
	public PlayerInventory(){}
	
	public int getOk() {
		return ok;
	}

	public Map<String,Slot> getContents() {
		return contents;
	}

	public void setOk(int ok) {
		this.ok = ok;
	}

	public void setContents(Map<String,Slot> contents) {
		this.contents = contents;
	}
}