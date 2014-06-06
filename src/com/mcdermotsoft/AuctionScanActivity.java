package com.mcdermotsoft;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.tinyspeck.android.Glitch;

public class AuctionScanActivity extends CustomWindow
{
	Glitch glitch;
	Context context = this;
	boolean first = true;
	static boolean exactMatch=false;
	int totalPages, currentPage, done;
	EditText itemSearchEditText, playerSearchEditText;
	CheckBox exactMatchCheckBox;
	ProgressDialog progressDialog;
	ViewFlipper flipper;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.auction_scan);
		
		ViewFlipper background = (ViewFlipper)findViewById(R.id.auctionScanFlipper);
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
		
		glitch = new Glitch("278-cab1d5f245e5f07d842d90bce704426fe86da5d6", "glitchbuddyandroid://auth");
		ImageButton itemSearchButton = (ImageButton)findViewById(R.id.itemSearchButton);
		ImageButton playerSearchButton = (ImageButton)findViewById(R.id.playerSearchButton);
		exactMatchCheckBox = (CheckBox)findViewById(R.id.exactMatchCheckBox);
		exactMatch = exactMatchCheckBox.isChecked();
		itemSearchEditText = (EditText)findViewById(R.id.itemSearchEditText);
		playerSearchEditText = (EditText)findViewById(R.id.playerSearchEditText);
		itemSearchButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				new SearchTask().execute("item");
			}
		});	
		playerSearchButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				new SearchTask().execute("player");
			}
		});
	}
	
	@Override
	public void onConfigurationChanged(Configuration config)
	{
		super.onConfigurationChanged(config);
		
		ViewFlipper background = (ViewFlipper)findViewById(R.id.auctionScanFlipper);
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
	}
	
	private class SearchTask extends AsyncTask<String, Void, JSONArray>
	{
		String type;
		String playerName;
		
		@Override
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(context, "", "Loading, Please wait...", true);
		}
		
		@Override
		protected JSONArray doInBackground(String... args) 
		{
			type = args[0];
			
			if(type.equals("item"))
			{
				String itemText = itemSearchEditText.getText().toString();
				itemText = itemText.toLowerCase();
				return searchForItem(itemText);
			}
			if(type.equals("player"))
				return searchForPlayer(playerSearchEditText.getText().toString());
			else
			{
				playerName = args[1];
				return searchForPlayerAuctions(args[1]);
			}
		}
		
		protected void onPostExecute(JSONArray response)
		{
			if(progressDialog != null)
				progressDialog.dismiss();
			if(response == null)
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
		    	builder.setMessage("There was a server error.\nPlease try again later.");
		    	builder.setTitle("Oops");
		    	builder.setCancelable(false);
		    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() 
		    	{
		    		public void onClick(DialogInterface dialog, int id)
		    		{
		    			dialog.cancel();
		    		}
		    	});
		    	AlertDialog dialog = builder.create();
		    	dialog.show();
			}
			else
			{
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(itemSearchEditText.getWindowToken(), 0);
				if(type.equals("item"))
				{
					displayResults(response, "item");
					return;
				}
				if(type.equals("player"))
				{
					displayResults(response, "player");
					return;
				}
				else
					displayResults(response, playerName);
			}
		}
		
	}
	
	private JSONArray searchForPlayer(String playerString)
	{
		if(playerString.equals(""))
			return null;
		
		String urlString;
		JSONArray response = null;
		
		try 
		{
			urlString = "http://mcdermottrial2008.appspot.com/playersearch?playerName="+URLEncoder.encode(playerString, "utf-8");
			response = getJSONResponse(urlString);
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}
		
		return response;
	}
	
	private JSONArray searchForPlayerAuctions(String playerString)
	{
		if(playerString.equals(""))
			return null;
		
		String urlString;
		JSONArray response = null;
		
		try 
		{
			urlString = "http://mcdermottrial2008.appspot.com/auctionsearch?searchText="+URLEncoder.encode(playerString, "utf-8")+"&isPlayer=true";
			response = getJSONResponse(urlString);
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}
				
		return response;
	}
	
	static protected JSONArray searchForItem(String itemString)
	{		
		if(itemString.equals(""))
			return null;
	
		String urlString;
		JSONArray response = null;
		try 
		{
			urlString = "http://mcdermottrial2008.appspot.com/auctionsearch?searchText="+URLEncoder.encode(itemString, "utf-8");
			if(exactMatch)
				urlString += "&exactMatch=true";
			
			response = getJSONResponse(urlString);
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}
		
		return response;
	}
	
	static private JSONArray getJSONResponse(String urlString)
	{
		try 
		{
			URL url = new URL(urlString);
			URLConnection urlconn = url.openConnection();
			urlconn.setConnectTimeout(10000);
			urlconn.setReadTimeout(20000);
			BufferedReader in = new BufferedReader(new InputStreamReader(urlconn.getInputStream()));
			String response = in.readLine();
			in.close();
			JSONTokener tokener = new JSONTokener(response);
			return new JSONArray(tokener);
		}
		catch(Exception ex) 
		{
			ex.printStackTrace();
		}
		
		return null;
	}
	
	private void displayResults(JSONArray resultArray, String type)
	{
		progressDialog.dismiss();
		if(resultArray.length() == 0)
		{
			String message = "";
			if(type.equals("item"))
				message = "No results for \"" + itemSearchEditText.getText().toString() + "\"";
			if(type.equals("player"))
				message = "No results for \"" + playerSearchEditText.getText().toString() + "\"";
			if(!type.equals("item") && !type.equals("player"))
				message = "No results for \"" + type + "\"";
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
	    	builder.setMessage(message);
	    	builder.setCancelable(false);
	    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() 
	    	{
	    		public void onClick(DialogInterface dialog, int id)
	    		{
	    			dialog.cancel();
	    		}
	    	});
	    	AlertDialog dialog = builder.create();
	    	dialog.show();
		}
		else
		{
			flipper = (ViewFlipper)findViewById(R.id.auctionScanFlipper);
			
			if(type.equals("player"))
			{		
				Button closeButton = (Button)findViewById(R.id.playerResultsCloseButton);
				closeButton.setOnClickListener(new OnClickListener()
				{
					public void onClick(View v)
					{
						flipper.setDisplayedChild(0);
					}
				});
				
				TextView searchString = (TextView)findViewById(R.id.playerResultTextView);
				searchString.setText("Search results for \"" + playerSearchEditText.getText().toString() + "\"");
				
				final ArrayList<SearchResult> list = new ArrayList<SearchResult>();
				ListView searchResults = (ListView)findViewById(R.id.playerResultListView);
				for(int i=0; i<resultArray.length(); i++)
				{
					try
					{
						JSONObject resultObject = resultArray.getJSONObject(i);
						
						String name = resultObject.getString("name");
						SearchResult result = new SearchResult(name);
						result.setId(resultObject.getString("tsid"));
						list.add(result);
					}
					catch(JSONException ex)
					{
						Log.e("Glitch",ex.getMessage()+"");
					}
				}
				
				MyPlayerArrayAdapter arrayAdapter = new MyPlayerArrayAdapter(this, R.layout.player_search_result_row, list);
				searchResults.setAdapter(arrayAdapter);
				searchResults.setOnItemClickListener(new OnItemClickListener()
				{
					public void onItemClick(AdapterView<?> parent, View view, int position, long id)
					{
						new SearchTask().execute("playerItems", list.get(position).getName());
					}
				});
				
				flipper.setDisplayedChild(2);
			}
			else
			{
				Button closeButton = (Button)findViewById(R.id.itemResultsCloseButton);
				if(type.equals("item"))
				{
					closeButton.setOnClickListener(new OnClickListener()
					{
						public void onClick(View v)
						{
							flipper.setDisplayedChild(0);
						}
					});
				}
				else
				{
					closeButton.setOnClickListener(new OnClickListener()
					{
						public void onClick(View v)
						{
							flipper.setDisplayedChild(2);
						}
					});
				}
				
				ArrayList<SearchResult> list = new ArrayList<SearchResult>();
				ListView searchResults = (ListView)findViewById(R.id.itemResultListView);
				
				TextView searchString = (TextView)findViewById(R.id.itemResultTextView);
				if(type.equals("item"))
					searchString.setText("Search results for \"" + itemSearchEditText.getText().toString() + "\"");
				else
					searchString.setText("Item search results for \"" + type + "\"");
				
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
				searchResults.setAdapter(arrayAdapter);
				searchResults.setOnItemClickListener(new OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> parent, View view,int position, long id) 
					{
						String url = "http://www.glitch.com/auctions/";
						url += arrayAdapter.list.get(position).getId() + "/";
						int created = arrayAdapter.list.get(position).getCreated();
						url += Integer.toHexString(created);
						
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url + "/purchase"));
				        context.startActivity(browserIntent);
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
				
				flipper.setDisplayedChild(1);
			}
		}
	}
}