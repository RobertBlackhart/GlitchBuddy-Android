package com.mcdermotsoft;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyItemArrayAdapter extends ArrayAdapter<SearchResult> 
{
	ArrayList<SearchResult> list;
	static DrawableManager drawableManager;
	Context context;
	static DecimalFormat formatter = new DecimalFormat("#0.#");
	
	public MyItemArrayAdapter(Context context, int textViewResourceId, ArrayList<SearchResult> objects) 
	{
		super(context, textViewResourceId, objects);
		this.context = context;
		list = objects;
		drawableManager = new DrawableManager();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View v = convertView;
		
		if (v == null) 
		{
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.item_search_result_row, null);
        }
        final SearchResult result = list.get(position);
        if (result != null) 
        {
	        TextView nameText = (TextView) v.findViewById(R.id.resultName);
	        TextView unitCostText = (TextView) v.findViewById(R.id.resultUnitCost);
	        TextView percentText = (TextView) v.findViewById(R.id.resultPercentAvg);
	        TextView costText = (TextView) v.findViewById(R.id.resultCost);
	        nameText.setText(result.getName()+"("+result.getCount()+")");
	        double unitCost = result.getCost()/result.getCount();
	        unitCostText.setText(String.valueOf(formatter.format(unitCost)));
	        double percent = ((unitCost/result.getAvgCost())-1)*100;
	        String percentS = String.valueOf(formatter.format(percent))+"%";
	        if(percent>0)
	        {
	        	percentS = "+" + percentS;
	        	percentText.setTextColor(Color.RED);
	        }
	        if(percent<=0)
	        {
	        	percentText.setTextColor(Color.GREEN);
	        }
	        percentText.setText(percentS);
	        costText.setText(String.valueOf(formatter.format(result.getCost())));
	        ImageView icon = (ImageView) v.findViewById(R.id.resultIcon);
	        String url = "http://mcdermottrial2008.appspot.com/geticon?filename="+result.getClassTsid()+".png";
	        drawableManager.loadDrawable(url, icon, context.getResources().getDrawable(R.drawable.empty_slot_icon));
        }
		
		return v;
	}
}
