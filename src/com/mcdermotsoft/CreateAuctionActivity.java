package com.mcdermotsoft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tinyspeck.android.Glitch;
import com.tinyspeck.android.GlitchRequest;
import com.tinyspeck.android.GlitchRequestDelegate;

public class CreateAuctionActivity extends CustomWindow implements TextWatcher, GlitchRequestDelegate
{
	DrawableManager manager = new DrawableManager();
	EditText priceEach, stackSize, totalStacks;
	TextView totalUnits, totalPrice, itemName, searchString, itemAverage;
	ListView similarAuctions;
	ImageView itemIcon;
	DecimalFormat formatter = new DecimalFormat("#.##");
	Glitch glitch;
	Bundle bundle;
	int itemCount=0, stackCount=0, finished, unitsAvailable;
	double price=0;
	Context context = this;
	ProgressDialog progressDialog;
	ArrayList<Slot> stackList;
	JSONArray result;
	String tsid;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_auction);
		
		RelativeLayout background = (RelativeLayout)findViewById(R.id.background);
		double hour = GlitchWidgetProvider.getHour();
		int orientation = getResources().getConfiguration().orientation;
		if((hour > 5 && hour < 9) || (hour > 17 && hour < 22))
		{
			if(orientation == Configuration.ORIENTATION_PORTRAIT)
				background.setBackgroundResource(R.drawable.twilight);
			else
				background.setBackgroundResource(R.drawable.twilight_land);
		}
		else if(hour > 21 || hour < 6)
		{
			if(orientation == Configuration.ORIENTATION_PORTRAIT)
				background.setBackgroundResource(R.drawable.night);
			else
				background.setBackgroundResource(R.drawable.night_land);
		}
		else
		{
			if(orientation == Configuration.ORIENTATION_PORTRAIT)
				background.setBackgroundResource(R.drawable.day);
			else
				background.setBackgroundResource(R.drawable.day_land);
		}
		
		bundle = getIntent().getExtras();
		glitch = new Glitch("278-cab1d5f245e5f07d842d90bce704426fe86da5d6", "glitchbuddyandroid://auth");
		glitch.accessToken = bundle.getString("authToken");
		
		PlayerInventory inventory = GlitchActivity.inventory;
		stackList = new ArrayList<Slot>();
		unitsAvailable = 0;
		for(int i=0; i<inventory.getContents().size(); i++)
		{
			Slot outerSlot = inventory.getContents().get("slot_"+i);
		
			if(outerSlot != null && outerSlot.getLabel().equals(bundle.getString("name")))
			{
				stackList.add(outerSlot);
				unitsAvailable += outerSlot.getCount();
			}
			else if(outerSlot != null && outerSlot.getContents() != null)
			{
				for(int j=0; j<outerSlot.getContents().size(); j++)
				{
					Slot innerSlot = outerSlot.getContents().get("slot_"+j);
					if(innerSlot != null && innerSlot.getLabel().equals(bundle.getString("name")))
					{
						stackList.add(innerSlot);
						unitsAvailable += innerSlot.getCount();
					}
				}
			}
		}
		
		setupUIComponents();
		
		new SearchTask().execute(bundle.getString("name"));
	}
	
	private void setupUIComponents()
	{
		itemIcon = (ImageView)findViewById(R.id.itemIcon);
		itemName = (TextView)findViewById(R.id.itemName);
		itemName.setText(bundle.getString("name"));
		TextView itemAvailable = (TextView)findViewById(R.id.itemAvailable);
		itemAverage = (TextView)findViewById(R.id.averageAuctionPrice);
		priceEach = (EditText)findViewById(R.id.priceEach);
		stackSize = (EditText)findViewById(R.id.stackSize);
		totalStacks = (EditText)findViewById(R.id.totalStacks);
		totalUnits = (TextView)findViewById(R.id.totalUnits);
		totalPrice = (TextView)findViewById(R.id.totalPrice);
		Button postAuction = (Button)findViewById(R.id.postAuction);
		searchString = (TextView)findViewById(R.id.searchString);
		searchString.setText("Search results for \"" + itemName.getText() + "\"");
		similarAuctions = (ListView)findViewById(R.id.similarAuctions);
		
		priceEach.addTextChangedListener(this);
		stackSize.addTextChangedListener(this);
		totalStacks.addTextChangedListener(this);
		
		postAuction.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if(itemCount == 0 || stackCount == 0 || price == 0)
					return;
				
				int eligibleStacks = 0;
				ArrayList<Slot> removeList = new ArrayList<Slot>();
				for(int i=0; i<stackList.size(); i++)
				{
					Slot slot = stackList.get(i);
					
					if(slot.getCount() >= itemCount)
						eligibleStacks += slot.getCount()/itemCount;
					else
						removeList.add(slot);
				}
				stackList.removeAll(removeList);
				if(stackCount*itemCount <= eligibleStacks*itemCount)
				{
					finished=0;
					int stackNum=0;
					new PostTask().execute();
					for(int i=0; i<stackCount; i++)
					{
						Map<String,String> params = new HashMap<String,String>();
						params.put("stack_tsid", stackList.get(stackNum).getTsid());
						params.put("count", String.valueOf(itemCount));
						params.put("cost", String.valueOf(price*itemCount));
						Log.i("Glitch","Posting Acution: stack_tsid=" + bundle.getString("tsid")+", count="+itemCount+", cost="+price);
						GlitchRequest createAuctionRequest = glitch.getRequest("auctions.create",params);
						createAuctionRequest.execute((CreateAuctionActivity)context);
						if(stackList.get(stackNum).getCount() >= itemCount * 2)
							stackList.get(stackNum).setCount(stackList.get(stackNum).getCount()-itemCount);
						else
							stackNum++;
					}
				}
				else
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					AlertDialog alertDialog = builder.create();
					alertDialog.setTitle("Error");
					alertDialog.setMessage("You have " + eligibleStacks + " stack(s) of " + itemName.getText().toString() + " in your inventory\nwith " + itemCount + " or more pieces in it");
					alertDialog.show();
				}
			}
		});
		
		manager.loadDrawable(bundle.getString("icon"), itemIcon, getResources().getDrawable(R.drawable.empty_slot_icon));
		
		String avg = "";
		tsid = bundle.getString("tsid");
		
		if(bundle.getString("average") != null)
		{
			try
			{
				avg = formatter.format(Double.parseDouble(bundle.getString("average")));
			}
			catch(NumberFormatException ex)
			{
				Log.e("Glitch",ex.getMessage()+"");
			}
			itemAverage.setText("Average price at auction: " + avg + " ea");
		}
		else
			new PriceTask().execute(itemAverage);
		
		itemAvailable.setText("You have " + unitsAvailable + " unit(s) to auction in " + stackList.size() + " stack(s)");
	}
	
	private Bundle saveUI()
	{
		Bundle bundle = new Bundle();
		
		this.bundle.putString("average", itemAverage.getText().toString());
		
		bundle.putInt("itemCount", itemCount);
		bundle.putInt("stackCount", stackCount);
		bundle.putDouble("price", price);
		
		return bundle;
	}
	
	private void restoreUI(Bundle bundle)
	{
		setupUIComponents();
		
		double price = bundle.getDouble("price");
		int itemCount = bundle.getInt("itemCount");
		int stackCount = bundle.getInt("stackCount");
		if(price != 0)
			priceEach.setText(String.valueOf(bundle.getDouble("price")));
		if(itemCount != 0)
			stackSize.setText(String.valueOf(bundle.getInt("itemCount")));
		if(stackCount != 0)
			totalStacks.setText(String.valueOf(bundle.getInt("stackCount")));
		
		displayResults(result, itemName.getText().toString());
	}
	
	@Override
	public void onConfigurationChanged(Configuration config)
	{
		super.onConfigurationChanged(config);
		
		Bundle bundle = saveUI();
		int orientation = config.orientation;
		if(orientation == Configuration.ORIENTATION_LANDSCAPE)
			setContentView(R.layout.create_auction_land);
		else
			setContentView(R.layout.create_auction);
		restoreUI(bundle);
		
		RelativeLayout background = (RelativeLayout)findViewById(R.id.background);
		double hour = GlitchWidgetProvider.getHour();
		if((hour > 5 && hour < 9) || (hour > 17 && hour < 22))
		{
			if(orientation == Configuration.ORIENTATION_PORTRAIT)
				background.setBackgroundResource(R.drawable.twilight);
			else
				background.setBackgroundResource(R.drawable.twilight_land);
		}
		else if(hour > 21 || hour < 6)
		{
			if(orientation == Configuration.ORIENTATION_PORTRAIT)
				background.setBackgroundResource(R.drawable.night);
			else
				background.setBackgroundResource(R.drawable.night_land);
		}
		else
		{
			if(orientation == Configuration.ORIENTATION_PORTRAIT)
				background.setBackgroundResource(R.drawable.day);
			else
				background.setBackgroundResource(R.drawable.day_land);
		}
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
				URL url = new URL("http://mcdermottrial2008.appspot.com/averagesearch?searchText="+bundle.getString("classTsid"));
				URLConnection conn = url.openConnection();
				conn.setConnectTimeout(10000);
				conn.setReadTimeout(20000);
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String response = in.readLine();
				in.close();
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
				response = formatter.format(Double.parseDouble(response));
				avgPriceText.setText("Average price at auction: " + response);
			}
		}
	}
	
	private class SearchTask extends AsyncTask<String, Void, JSONArray>
	{
		String item;
		LinearLayout progressLayout = (LinearLayout)findViewById(R.id.progressLayout);
		
		@Override
		protected void onPreExecute()
		{
			ProgressBar progress = new ProgressBar(getBaseContext());
			progress.setIndeterminate(true);
			TextView progressText = new TextView(getBaseContext());
			progressText.setTextColor(Color.WHITE);
			progressText.setText("Searching for other auctions of " + itemName.getText());
			progressLayout.setVisibility(View.VISIBLE);
			progressLayout.addView(progress);
			progressLayout.addView(progressText);
		}
		
		@Override
		protected JSONArray doInBackground(String...itemNames)
		{
			result = null;
			item = itemNames[0].toLowerCase();
			
			result = AuctionScanActivity.searchForItem(item);
					
			return result;
		}
		
		@Override
		protected void onPostExecute(JSONArray result)
		{
			progressLayout.removeAllViews();
			progressLayout.setVisibility(View.GONE);
			displayResults(result, item);
		}
	}
	
	private class PostTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(context, "", "Posting Auctions, Please wait...", true);
		}
		
		@Override
		protected Void doInBackground(Void...voids)
		{
			while(finished < stackCount)
			{};
			return null;
		}
		
		@Override
		protected void onPostExecute(Void voids)
		{
			progressDialog.dismiss();
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("Success");
			builder.setMessage("Successfully posted:"+ stackCount + " auctions of " + itemName.getText().toString());
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					dialog.dismiss();
					setResult(3);
					finish();
				}
			});
			builder.show();
		}
	}
	
	private void displayResults(JSONArray resultArray, String itemName)
	{
		searchString = (TextView)findViewById(R.id.searchString);
		if(resultArray == null)
			searchString.setText("Error searching for " + this.itemName.getText() + ".\nMake sure you are connected to the internet\nand try again later.");
		else if(resultArray.length() == 0)
		{
			searchString.setText("No other auctions were found");
		}
		else
		{						
			ArrayList<SearchResult> list = new ArrayList<SearchResult>();			
			searchString.setText("Search results for \"" + this.itemName.getText() + "\"");
			
			for(int i=0; i<resultArray.length(); i++)
			{
				try
				{
					JSONObject resultObject = resultArray.getJSONObject(i);
					
					String classTsid = resultObject.getString("tsid");
					String name = resultObject.getString("itemName");
					double cost = resultObject.getDouble("cost");
					String id = resultObject.getString("id");
					int count = resultObject.getInt("count");
					double avgCost = resultObject.getDouble("averageCost");
					int created = Integer.valueOf(id.substring(0,id.indexOf("-")));
					id = id.substring(id.indexOf("-")+1);
					list.add(new SearchResult(classTsid,name,cost,created,id,count,avgCost));
				}
				catch(JSONException ex)
				{
					Log.e("Glitch",ex.getMessage()+"");
				}
			}
			
			final MyItemArrayAdapter arrayAdapter = new MyItemArrayAdapter(this, R.layout.item_search_result_row, list);
			similarAuctions.setAdapter(arrayAdapter);
			similarAuctions.setOnItemClickListener(new OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view,int position, long id) 
				{
					String url = "http://www.glitch.com/auctions/";
					url += arrayAdapter.list.get(position).getId() + "/";
					int created = arrayAdapter.list.get(position).getCreated();
					url += Integer.toHexString(created);
					
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url + "/purchase"));
			        startActivity(browserIntent);
				}
			});
			
			SortListener sortListener = new SortListener(list, arrayAdapter);
			SortTextView nameText = (SortTextView)findViewById(R.id.nameHeader);
			nameText.setOnClickListener(sortListener);
			nameText.setSortOrder(SortTextView.NONE);
			SortTextView unitCostText = (SortTextView)findViewById(R.id.unitCostHeader);
			unitCostText.setOnClickListener(sortListener);
			SortTextView percentAvgText = (SortTextView)findViewById(R.id.percentAvgHeader);
			percentAvgText.setOnClickListener(sortListener);
			percentAvgText.setSortOrder(SortTextView.NONE);
			SortTextView totalCostText = (SortTextView)findViewById(R.id.totalCostHeader);
			totalCostText.setOnClickListener(sortListener);
			totalCostText.setSortOrder(SortTextView.NONE);
			
			Collections.sort(list);
			unitCostText.setSortOrder(SortTextView.ASCENDING);
		}
	}

	@Override
	public void afterTextChanged(Editable s) 
	{
		String priceEachS = priceEach.getText().toString();
		String stackSizeS = stackSize.getText().toString();
		String totalStacksS = totalStacks.getText().toString();
		
		try
		{
			price = 0;
			price = Double.parseDouble(priceEachS);
			itemCount = 0;
			itemCount = Integer.parseInt(stackSizeS);
			stackCount = 0;
			stackCount = Integer.parseInt(totalStacksS);
			totalUnits.setText("Total unit(s) being sold: " + formatter.format(itemCount*stackCount));
			totalPrice.setText("Total price of auction(s): " + formatter.format(itemCount*stackCount*price));
		}
		catch(NumberFormatException ex){}
	}
	
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count){}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	@Override
	public void requestFinished(GlitchRequest request) 
	{
		finished++;
		
		JSONObject result = request.response;
		
		if(result.optString("ok").equals("0"))
		{
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Error");
			builder.setMessage("Could not post auction:\n\""+ result.optString("error") + "\"");
			builder.setNeutralButton("OK", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int num)
				{
					dialog.cancel();
				}
			});
			builder.show();
		}
	}

	@Override
	public void requestFailed(GlitchRequest request){}
}
