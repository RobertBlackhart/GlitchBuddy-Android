package com.mcdermotsoft;

public class SearchResult implements Comparable<SearchResult>
{
	private String name, classTsid, id, avatarUrl;
	private int created, count;
	private double cost, avgCost;

	public SearchResult(String classTsid, String name, double cost, int created, String id, int count, double avgCost)
	{
		setClassTsid(classTsid);
		setName(name);
		setCost(cost);
		setCreated(created);
		setId(id);
		setCount(count);
		setAvgCost(avgCost);
	}
	
	public SearchResult(String name)
	{
		setName(name);
		avatarUrl = null;
	}
	
	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getClassTsid()
	{
		return classTsid;
	}
	
	public double getCost()
	{
		return cost;
	}
	
	public int getCreated() 
	{
		return created;
	}

	public String getId() 
	{
		return id;
	}

	public String getName()
	{
		return name;
	}
	
	public void setClassTsid(String classTsid)
	{
		this.classTsid = classTsid;
	}
	
	public void setCost(double cost)
	{
		this.cost = cost;
	}

	public void setCreated(int created)
	{
		this.created = created;
	}

	public void setId(String id) 
	{
		this.id = id;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public int getCount() 
	{
		return count;
	}

	public double getAvgCost() 
	{
		return avgCost;
	}

	public void setCount(int count) 
	{
		this.count = count;
	}

	public void setAvgCost(double avgCost) 
	{
		this.avgCost = avgCost;
	}
	
	@Override
	public int compareTo(SearchResult other)
	{
		double unitPriceThis = getCost()/getCount();
		double unitPriceOther = other.getCost()/other.getCount();
		
		return Double.compare(unitPriceThis, unitPriceOther);
	}
}
