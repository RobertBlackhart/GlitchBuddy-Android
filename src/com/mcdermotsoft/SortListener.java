package com.mcdermotsoft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class SortListener implements View.OnClickListener
{	
	ArrayList<SearchResult> resultsList;
	MyItemArrayAdapter resultsListAdapter;
	
	public SortListener(ArrayList<SearchResult> resultsList, MyItemArrayAdapter resultsListAdapter)
	{
		this.resultsList = resultsList;
		this.resultsListAdapter = resultsListAdapter;
	}
	
	public void onClick(View v)
	{
		ViewGroup parent = (ViewGroup)v.getParent();
		for(int i=0; i<parent.getChildCount(); i++)
		{
			if(!parent.getChildAt(i).equals(v))
			{
				if(parent.getChildAt(i) instanceof SortTextView)
				{
					SortTextView other = (SortTextView)parent.getChildAt(i);
					other.setSortOrder(SortTextView.NONE);
				}
			}
		}
		if(v instanceof SortTextView)
		{
			SortTextView sortText = (SortTextView)v;
			final String sortString = sortText.getTextOnly();
			Log.d("Glitch",sortString);
			
			if(sortText.getSortOrder() == SortTextView.ASCENDING)
			{
				sortText.setSortOrder(SortTextView.DESCENDING);
				Collections.sort(resultsList, new Comparator<SearchResult>()
				{
					public int compare(SearchResult obj1, SearchResult obj2)
					{
						if(sortString.equals("Item Name"))
							return -1*(obj1.getName().compareTo(obj2.getName()));
						if(sortString.equals("Unit Cost"))
							return -1*(new Double(obj1.getCost()/obj1.getCount()).compareTo(new Double(obj2.getCost()/obj2.getCount())));
						if(sortString.equals("% of Average"))
							return -1*(new Double(obj1.getCost()/obj1.getCount()/obj1.getAvgCost()).compareTo(new Double(obj2.getCost()/obj2.getCount()/obj2.getAvgCost())));
						if(sortString.equals("Total Cost"))
							return -1*(new Double(obj1.getCost()).compareTo(obj2.getCost()));
						else
							return 0;
					}
				});
			}
			else
			{
				sortText.setSortOrder(SortTextView.ASCENDING);
				Collections.sort(resultsList, new Comparator<SearchResult>()
				{
					public int compare(SearchResult obj1, SearchResult obj2)
					{
						if(sortString.equals("Item Name"))
							return (obj1.getName().compareTo(obj2.getName()));
						if(sortString.equals("Unit Cost"))
							return (new Double(obj1.getCost()/obj1.getCount()).compareTo(new Double(obj2.getCost()/obj2.getCount())));
						if(sortString.equals("% of Average"))
							return (new Double(obj1.getCost()/obj1.getCount()/obj1.getAvgCost()).compareTo(new Double(obj2.getCost()/obj2.getCount()/obj2.getAvgCost())));
						if(sortString.equals("Total Cost"))
							return (new Double(obj1.getCost()).compareTo(obj2.getCost()));
						else
							return 0;
					}
				});
			}
			
			resultsListAdapter.notifyDataSetChanged();
		}
	}
}
