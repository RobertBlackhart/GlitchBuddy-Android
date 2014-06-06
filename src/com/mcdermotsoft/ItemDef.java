package com.mcdermotsoft;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemDef
{
	String iconic_url;
	String category;
	String desc;
	String base_cost;
	String class_tsid;

	public ItemDef(){}
	
	public String getClass_tsid() {
		return class_tsid;
	}

	public void setClass_tsid(String class_tsid) {
		this.class_tsid = class_tsid;
	}

	public String getBase_cost() {
		return base_cost;
	}

	public void setBase_cost(String base_cost) {
		this.base_cost = base_cost;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getIconic_url() {
		return iconic_url;
	}

	public void setIconic_url(String iconic_url) {
		this.iconic_url = iconic_url;
	}
}
