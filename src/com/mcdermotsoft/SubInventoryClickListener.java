package com.mcdermotsoft;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class SubInventoryClickListener implements OnClickListener
{
	GlitchActivity activity;
	PlayerInventory inventory;
	int parentBagIndex;
	
	public SubInventoryClickListener(GlitchActivity activity, PlayerInventory inventory, int parentBagIndex)
	{
		this.activity = activity;
		this.inventory = inventory;
		this.parentBagIndex = parentBagIndex;
	}
	
	@Override
	public void onClick(View v)
	{
		FlowLayout parent = (FlowLayout)v.getParent();
		int index = parent.indexOfChild(v);
		Slot slot = inventory.getContents().get("slot_"+parentBagIndex).getContents().get("slot_"+index);
		ImageView iconView = (ImageView)v.findViewWithTag("icon");
		BitmapDrawable drawable = (BitmapDrawable) iconView.getDrawable();
		Bitmap icon = drawable.getBitmap();
		new InventoryItemDialog(activity,slot,icon);
	}
}
