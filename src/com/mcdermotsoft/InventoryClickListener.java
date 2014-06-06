package com.mcdermotsoft;

import java.util.ArrayList;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class InventoryClickListener implements OnClickListener 
{
	GlitchActivity activity;
	PlayerInventory inventory;
	static DrawableManager manager = new DrawableManager();
	ArrayList<String> urlList = new ArrayList<String>();
	ArrayList<ImageView> viewList = new ArrayList<ImageView>();
	
	public InventoryClickListener(GlitchActivity activity, PlayerInventory inventory)
	{
		this.activity = activity;
		this.inventory = inventory;
	}
	
	@Override
	public void onClick(View v) 
	{
		FlowLayout parent = (FlowLayout)v.getParent();
		FlowLayout slotLayout = (FlowLayout)activity.findViewById(R.id.slotLayout);
		
		int index = parent.indexOfChild(v);
		Map<String,Slot> subItems = inventory.getContents().get("slot_" + index).getContents();
		if(subItems != null)
		{
			View openBag = parent.findViewWithTag("open");
			if(openBag != null)
			{
				if(v.equals(openBag))
				{
					v.setTag("closed");
					slotLayout.removeAllViews();
					ImageView button = (ImageView)v.findViewWithTag("openCloseButton");
					button.setBackgroundResource(R.drawable.open_bag);
					return;
				}
				else
				{
					openBag.setTag("closed");
					ImageView button = (ImageView)openBag.findViewWithTag("openCloseButton");
					button.setBackgroundResource(R.drawable.open_bag);
				}
			}

			if(v.getTag() == null)
				v.setTag("closed");
			if(!v.getTag().equals("open"))
			{
				v.setTag("open");
				ImageView button = (ImageView)v.findViewWithTag("openCloseButton");
				button.setBackgroundResource(R.drawable.close_bag);
			}
			slotLayout.removeAllViews();
			
			ArrayList<String> urlList = new ArrayList<String>();
        	ArrayList<String> countList = new ArrayList<String>();
        	Slot slot = null;
        	
        	for(int i=0; i<subItems.size(); i++)
        	{
        		slot = subItems.get("slot_"+String.valueOf(i));
        		if(slot != null)
        		{
        			urlList.add(slot.getItemDef().getIconic_url());
        			countList.add(String.valueOf(slot.getCount()));
        		}
        		else
        		{
        			urlList.add(null);
        			countList.add(null);
        		}
        	}
        	
        	new SubIconTask(index).execute(urlList,countList);
		}
		else
		{
			Slot slot = inventory.getContents().get("slot_"+String.valueOf(index));
			ImageView iconView = (ImageView)v.findViewWithTag("icon");
			BitmapDrawable drawable = (BitmapDrawable) iconView.getDrawable();
			Bitmap icon = drawable.getBitmap();
			new InventoryItemDialog(activity,slot,icon);
		}
	}
	
	private class SubIconTask extends AsyncTask<ArrayList<String>, Void, ArrayList<FrameLayout>>
	{
		int parentBagIndex;
		
		public SubIconTask(int parentBagIndex)
		{
			this.parentBagIndex = parentBagIndex;
		}
		
		protected void onPreExecute()
		{
			FlowLayout slotLayout = (FlowLayout)activity.findViewById(R.id.slotLayout);
			ProgressBar progressBar = new ProgressBar(activity.getBaseContext());
			progressBar.setIndeterminate(true);
			slotLayout.addView(progressBar, new FlowLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT));
		}
		
		protected ArrayList<FrameLayout> doInBackground(ArrayList<String>... iconLists)
		{
			ArrayList<String> urls = iconLists[0];
			ArrayList<String> counts = iconLists[1];
			ArrayList<FrameLayout> frames = new ArrayList<FrameLayout>();
			
			for(int i=0; i<urls.size(); i++)
			{
				String url = urls.get(i);
				String count = counts.get(i);
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
			FlowLayout slotLayout = (FlowLayout)activity.findViewById(R.id.slotLayout);
			slotLayout.removeAllViews();
			for(int i=0; i<urlList.size(); i++)
			{
				manager.loadDrawable(urlList.get(i), viewList.get(i), activity.getResources().getDrawable(R.drawable.empty_slot_icon));
			}
			
			SubInventoryClickListener listener = new SubInventoryClickListener(activity, inventory, parentBagIndex);
			FrameLayout frame = null;
			for(int i=0; i<frames.size(); i++)
			{
				frame = frames.get(i);
				if(frame.getTag().equals("full"))
				{
					frame.setOnClickListener(listener);
				}
				
				slotLayout.addView(frame);
			}
		}
	}
}
