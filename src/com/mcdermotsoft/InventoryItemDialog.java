package com.mcdermotsoft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class InventoryItemDialog 
{
	GlitchActivity activity;
	Slot slot;
	Bitmap icon;
	AlertDialog alertDialog;
	DecimalFormat formatter = new DecimalFormat("#.##");
	String avg;
	
	public InventoryItemDialog(GlitchActivity activity, Slot slot, Bitmap icon)
	{
		this.slot = slot;
		this.activity = activity;
		this.icon = icon;
		showDialog();
	}
	
	private void showDialog()
	{		
		LayoutInflater inflater = (LayoutInflater) activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.inventory_item_dialog, null);
	
		final ImageView itemIcon = (ImageView)layout.findViewById(R.id.itemIcon);
		TextView itemName = (TextView)layout.findViewById(R.id.itemName);
		TextView itemDescription = (TextView)layout.findViewById(R.id.itemDescription);
		TextView streetPrice = (TextView)layout.findViewById(R.id.streetPrice);
		TextView vendorPrice = (TextView)layout.findViewById(R.id.vendorPrice);
		final TextView avgAuctionPrice = (TextView)layout.findViewById(R.id.averageAuctionPrice);
		Button makeAuctionButton = (Button)layout.findViewById(R.id.makeAuctionButton);
		Button cancelButton = (Button)layout.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				alertDialog.dismiss();
			}
		});
		
		itemIcon.setImageBitmap(icon);
		
		itemName.setText(slot.getLabel());
		itemDescription.setText(slot.getItemDef().getDesc());
		streetPrice.setText("Street Price: " + slot.getItemDef().getBase_cost());
		double baseCost = Double.parseDouble(slot.getItemDef().getBase_cost());
		vendorPrice.setText("Sell to tool vendor: " + formatter.format(baseCost*.8));
		avgAuctionPrice.setText("Average price at auction: *retrieving price from server*");
		
		new PriceTask().execute(avgAuctionPrice);
		
		makeAuctionButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{				
				Intent intent = new Intent(activity, CreateAuctionActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("icon", slot.getItemDef().getIconic_url());
				bundle.putString("name", slot.getLabel());
				bundle.putString("average", avg);
				bundle.putString("tsid", slot.getTsid());
				bundle.putString("classTsid", slot.getItemDef().getClass_tsid());
				bundle.putString("authToken", activity.glitch.accessToken);
				intent.putExtras(bundle);
				activity.startActivityForResult(intent,1);
				alertDialog.dismiss();
			}
		});
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setView(layout);
		alertDialog = builder.create();
		alertDialog.show();
	}
	
	private class PriceTask extends AsyncTask<TextView, Void, String>
	{
		TextView avgPriceText;
		
		@Override
		protected String doInBackground(TextView...views)
		{
			avgPriceText = views[0];
			
			try 
			{
				URL url = new URL("http://mcdermottrial2008.appspot.com/averagesearch?searchText="+slot.getItemDef().getClass_tsid());
				URLConnection conn = url.openConnection();
				conn.setConnectTimeout(10000);
				conn.setReadTimeout(20000);
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String response = in.readLine();
				in.close();
				avg = response;
				return response;
			} 
			catch (MalformedURLException e) 
			{
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String response)
		{
			if(response == null)
				avgPriceText.setText("Average price at auction: *connection error*");
			else
			{
				try
				{
					response = formatter.format(Double.parseDouble(response));
				}
				catch(NumberFormatException ex)
				{
					Log.d("Glitch",ex.getMessage()+"");
				}
				avgPriceText.setText("Average price at auction: " + response);
			}
		}
	}
}
