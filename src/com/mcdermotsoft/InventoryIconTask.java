package com.mcdermotsoft;

import java.util.ArrayList;

import android.graphics.Color;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class InventoryIconTask extends AsyncTask<ArrayList<String>, Void, ArrayList<FrameLayout>> 
{
	GlitchActivity activity;
	PlayerInventory inventory;
	static DrawableManager manager = new DrawableManager();
	ArrayList<String> urlList = new ArrayList<String>();
	ArrayList<ImageView> viewList = new ArrayList<ImageView>();
	
	public InventoryIconTask(GlitchActivity activity, PlayerInventory inventory)
	{
		this.activity = activity;
		this.inventory = inventory;
	}
	protected ArrayList<FrameLayout> doInBackground(ArrayList<String>... iconLists)
	{
		ArrayList<String> urls = iconLists[0];
		ArrayList<String> counts = iconLists[1];
		ArrayList<String> categories = iconLists[2];
		ArrayList<FrameLayout> frames = new ArrayList<FrameLayout>();
		
		for(int i=0; i<urls.size(); i++)
		{
			String url = urls.get(i);
			String count = counts.get(i);
			String category = categories.get(i);
			FrameLayout frame = new FrameLayout(activity.getBaseContext());
			ImageView slotImage = new ImageView(activity.getBaseContext());
			TextView itemCount = new TextView(activity.getBaseContext());
			
			if(url != null)
			{
				urlList.add(url);
				viewList.add(slotImage);
				slotImage.setBackgroundResource(R.drawable.filled_slot_border);
				slotImage.setAdjustViewBounds(true);
				slotImage.setMaxHeight(39);
				slotImage.setMaxWidth(39);
				slotImage.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
				slotImage.setTag("icon");
				frame.addView(slotImage);
				if(Integer.parseInt(count) > 2)
				{
					itemCount.setText(count);
					itemCount.setTextSize(8f);
					itemCount.setTextColor(Color.BLACK);
					itemCount.setBackgroundColor(Color.WHITE);
					FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM|Gravity.RIGHT);
					frame.addView(itemCount,params);
				}
				if(category.equals("bag"))
				{
					ImageView openBag = new ImageView(activity.getBaseContext());
					openBag.setBackgroundResource(R.drawable.open_bag);
					openBag.setTag("openCloseButton");
					frame.addView(openBag);
				}
				
				frame.setTag("full");
			}
			if(url == null)
			{
				slotImage.setImageResource(R.drawable.empty_slot_icon);
				slotImage.setBackgroundResource(R.drawable.empty_slot_border);
				slotImage.setAdjustViewBounds(true);
				slotImage.setMaxHeight(39);
				slotImage.setMaxWidth(39);
				slotImage.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
				frame.addView(slotImage);
				frame.setTag("empty");
			}
			
			frames.add(frame);
		}
		
		return frames;
	}
	
	protected void onPostExecute(ArrayList<FrameLayout> frames)
	{
		for(int i=0; i<urlList.size(); i++)
		{
			manager.loadDrawable(urlList.get(i), viewList.get(i), activity.getResources().getDrawable(R.drawable.empty_slot_icon));
		}
		
		FlowLayout bagLayout = (FlowLayout)activity.findViewById(R.id.bagLayout);
		InventoryClickListener listener = new InventoryClickListener(activity, inventory);
		FrameLayout frame = null;
		for(int i=0; i<frames.size(); i++)
		{
			frame = frames.get(i);
			if(frame.getTag().equals("full"))
			{
				frame.setOnClickListener(listener);
			}
			
			bagLayout.addView(frame);
		}
		
		activity.requestFinished++;
	}
}
