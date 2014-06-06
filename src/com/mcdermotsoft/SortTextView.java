package com.mcdermotsoft;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class SortTextView extends TextView 
{
	static int ASCENDING = 1;
	static int DESCENDING = 2;
	static int NONE = 3;
	int sortOrder;
	Context context;
	String text;
	
	public SortTextView(Context context, AttributeSet atrSet)
	{
		super(context, atrSet);
		
		this.context = context;
		text = (String)getText();
		setSortOrder(SortTextView.NONE);
	}
	
	public int getSortOrder()
	{
		return sortOrder;
	}
	
	public String getTextOnly()
	{
		return text;
	}
	
	public void setSortOrder(int sort)
	{
		sortOrder = sort;
		if(sort == SortTextView.ASCENDING)
			setCompoundDrawablesWithIntrinsicBounds(R.drawable.sort_arrow_up, 0, 0, 0);
		if(sort == SortTextView.DESCENDING)
			setCompoundDrawablesWithIntrinsicBounds(R.drawable.sort_arrow_down, 0, 0, 0);
		if(sort == SortTextView.NONE)
			setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
	}
}
