package com.mcdermotsoft;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyPlayerArrayAdapter extends ArrayAdapter<SearchResult> 
{
	ArrayList<SearchResult> list;
	Context context;
	static DrawableManager manager = new DrawableManager();
	
	public MyPlayerArrayAdapter(Context context, int textViewResourceId, ArrayList<SearchResult> objects) 
	{
		super(context, textViewResourceId, objects);
		this.context = context;
		list = objects;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View v = convertView;
		
		if (v == null) 
		{
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.player_search_result_row, null);
        }
        final SearchResult result = list.get(position);
        if (result != null) 
        {
	        TextView nameText = (TextView) v.findViewById(R.id.resultName);
	        nameText.setText(result.getName());
	        nameText.setTextSize(12f);
	        ImageView avatar = (ImageView)v.findViewById(R.id.avatarView);
	        String url;
	        if(result.getAvatarUrl() == null)
	        {
	        	url = "http://api.glitch.com/simple/players.fullInfo?player_tsid="+result.getId();
	        	new urlTask().execute(url,avatar,result);
	        }
	        else
	        	manager.loadDrawable(result.getAvatarUrl(), avatar, getContext().getResources().getDrawable(R.drawable.empty_slot_icon));
        }
		
		return v;
	}
	
	private class urlTask extends AsyncTask<Object, Void, String>
	{
		ImageView icon;
		SearchResult result;
		
		@Override
		protected String doInBackground(Object... arg)
		{
			try
			{
				URL url = new URL((String)arg[0]);
				URLConnection connection = url.openConnection();
				connection.setConnectTimeout(10000);
				connection.setReadTimeout(20000);
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String response = in.readLine();
				in.close();
				
				JSONTokener tokener = new JSONTokener(response);
				JSONObject playerInfo = new JSONObject(tokener);
				JSONObject avatar = playerInfo.optJSONObject("avatar");
				String output = avatar.optString("50");
				icon = (ImageView)arg[1];
				result = (SearchResult)arg[2];
				return output;
			}
			catch(Exception ex)
			{
				Log.e("Glitch",ex.getMessage()+"");
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String url)
		{
			manager.loadDrawable(url, icon, getContext().getResources().getDrawable(R.drawable.empty_slot_icon));
			result.setAvatarUrl(url);
		}
	}
}
