package com.mcdermotsoft;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.tinyspeck.android.Glitch;
import com.tinyspeck.android.GlitchRequest;
import com.tinyspeck.android.GlitchRequestDelegate;

public class ChangeSkillActivity extends CustomWindow implements GlitchRequestDelegate
{
	ProgressBar progressBar;
	TextView timeRemaining, skillNameTextView;
	ImageView skillIcon;
	Glitch glitch;
	ChangeSkillActivity skillActivity = this;
	Handler handler;
	boolean requestFinished = false;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_skill);
		
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
		
		Bundle bundle = this.getIntent().getExtras();
		
		handler = new Handler();
		
		progressBar = (ProgressBar)findViewById(R.id.changeSkillProgressBar);
		progressBar.setMax(bundle.getInt("max"));
		progressBar.setProgress(bundle.getInt("progress"));
		timeRemaining = (TextView)findViewById(R.id.changeSkillProgressTextView);
		timeRemaining.setText(getTimeString(bundle.getInt("max")-bundle.getInt("progress")));
		skillNameTextView = (TextView)findViewById(R.id.changeSkillNameTextView);
    	skillNameTextView.setText("Currently Learning Skill: " + bundle.getString("skillName"));
		skillIcon = (ImageView)findViewById(R.id.skillIcon);
		try
		{
			String iconString = bundle.getString("icon");
			if(iconString == null)
				skillIcon.setImageDrawable(getResources().getDrawable(R.drawable.familiar));
			else
			{
				URL iconURL = new URL(iconString);
				Bitmap iconImage = BitmapFactory.decodeStream(iconURL.openConnection().getInputStream());
				skillIcon.setImageBitmap(iconImage);
			}
		}
		catch(IOException ex)
		{
			Log.e("Glitch",ex.getMessage()+"");
		}
		handler.post(runnable);
		glitch = new Glitch("278-cab1d5f245e5f07d842d90bce704426fe86da5d6", "glitchbuddyandroid://auth");
		getSavedPrefs();
		
		new GetInfoTask().execute();
		getAvailableSkills();
	}
	
	private class GetInfoTask extends AsyncTask<Void, Void, Void>
	{
		LinearLayout progressLayout;
		
		@Override
		protected void onPreExecute()
		{
			ProgressBar progressBar = new ProgressBar(getBaseContext());
			progressBar.setIndeterminate(true);
			progressLayout = (LinearLayout)skillActivity.findViewById(R.id.progressLayout);
			progressLayout.setVisibility(View.VISIBLE);
			progressLayout.addView(progressBar);
			TextView progressText = new TextView(getBaseContext());
			progressText.setText("Loading");
			progressText.setTextColor(Color.WHITE);
			progressLayout.addView(progressText);
		}
		
		@Override
		protected Void doInBackground(Void...voids)
		{
			while(!requestFinished)
			{};
			return null;
		}
		
		@Override
		protected void onPostExecute(Void voids)
		{
			progressLayout.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration config)
	{
		super.onConfigurationChanged(config);
		
		RelativeLayout background = (RelativeLayout)findViewById(R.id.background);
		double hour = GlitchWidgetProvider.getHour();
		int orientation = config.orientation;
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
	
	public void onPause()
	{
		super.onPause();
		handler.removeCallbacks(runnable);
	}
	
	private void getSavedPrefs()
	{
		FileInputStream fis;
		String auth=null;
		try
		{
			fis = openFileInput("auth_file");
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
			auth = reader.readLine();
			if(auth.equals(""))
				auth=null;
			fis.close();
		}
		catch(IOException ex)
		{
			Log.e("GlitchActivity",ex.getMessage());
		}
		
		glitch.accessToken = auth;
	}

	private void getAvailableSkills()
	{
		GlitchRequest request = new GlitchRequest("skills.listAvailable",glitch);
		request.execute(this);		
	}
	
	@Override
	public void requestFinished(GlitchRequest request)
	{
		requestFinished = true;
		
		 if(request != null && request.method != null)
        {
        	JSONObject response = request.response;
        	
        	if(request.method == "skills.listAvailable")
        	{
        		JSONObject skillsList = new JSONObject();
        		try 
        		{
					skillsList = response.getJSONObject("skills");
					JSONArray names = skillsList.names();
					for(int i=0; i<names.length(); i++)
					{
						JSONObject skillObject = skillsList.getJSONObject(names.getString(i));
						LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						final View v = vi.inflate(R.layout.change_skill_option, null);
						Button infoButton = (Button)v.findViewById(R.id.skillInfoButton);
						infoButton.setOnClickListener(new OnClickListener()
						{
							public void onClick(View b)
							{
								v.performClick();
							}
						});
						SkillClickListener listener = new SkillClickListener(skillActivity,v,skillObject,glitch);
						v.setOnClickListener(listener);

						// fill in any details dynamically here
						ImageView icon = (ImageView)v.findViewById(R.id.skillIcon);
						try
						{
							URL iconURL = new URL(skillObject.getString("icon_44"));
							Bitmap iconImage = BitmapFactory.decodeStream(iconURL.openConnection().getInputStream());
							icon.setImageBitmap(iconImage);
							icon.setPadding(0, 0, 5, 0);
						}
						catch(IOException ex)
						{
							Log.e("ChangeSkillActivity",ex.getMessage());
						}
						TextView skillName = (TextView) v.findViewById(R.id.skillName);
						skillName.setText(skillObject.getString("name"));
						ImageView pausedImage = (ImageView) v.findViewById(R.id.pauseImage);
						if(skillObject.getInt("time_remaining")<skillObject.getInt("total_time"))
						{
							pausedImage.setBackgroundDrawable(getResources().getDrawable(R.drawable.pause));
							pausedImage.setPadding(0, 0, 2, 0);
						}
						else
							pausedImage.setVisibility(View.GONE);
						TextView skillTimeRemaining = (TextView) v.findViewById(R.id.skillTime);
						skillTimeRemaining.setText(getTimeString(skillObject.getInt("time_remaining")));
						
						// insert into main view
						TableLayout insertPoint = (TableLayout)findViewById(R.id.changeSkillGrid);
						insertPoint.addView(v);					
					}
				} 
        		catch (JSONException ex) 
        		{
					Log.e("ChangeSkillActivity",ex.getMessage());
				}
        	}
        }
	}
	
	protected void learnNewSkill(String class_tsid)
	{
		String idString = class_tsid.substring(class_tsid.indexOf("_")+1);
				
		Map<String,String> args = new HashMap<String,String>();
		args.put("skill_id", idString);
		args.put("skill_class", class_tsid);
				
		GlitchRequest request = new GlitchRequest("skills.learn",args,glitch);
		request.execute(this);
	}
	
	protected String getTimeString(int remaining)
	{
		int days=0,hours=0,minutes=0,seconds=0;
    	String daysString = "",hoursString="",minutesString="",secondsString="";

    	if(remaining/86400>0)
    	{
    		days = remaining/86400;
    		remaining -= days*86400;
    		daysString = days + " day ";
    	}
    	if(remaining/3600>0)
    	{
    		hours = remaining/3600;
    		remaining -= hours*3600;
    		hoursString = hours + " hr ";
    	}
    	if(remaining/60>0)
    	{
    		minutes = remaining/60;
    		remaining -= minutes*60;
    		minutesString = minutes + " min ";
    	}
    	if(remaining>0)
    	{
    		seconds = remaining;
    		secondsString = seconds + " sec ";
    	}
    	
    	return daysString+hoursString+minutesString+secondsString;
	}

	@Override
	public void requestFailed(GlitchRequest request) 
	{
		Log.e("ChangeSkillActivity","failed");
	}
	
	private Runnable runnable = new Runnable()
	{
		public void run() 
		{
			{
				handler.removeCallbacks(this);
				handler.postDelayed(this,1000);
				int progress = progressBar.getProgress();
				int max = progressBar.getMax();
				
				if(progress<max && max != 100)
				{
					progress++;
					
					progressBar.setProgress(progress);
					timeRemaining.setText(getTimeString(max-progress));
				}
				else
				{
					handler.removeCallbacks(this);
					timeRemaining.setText("You are not currently learning a skill");
					TextView skillNameTextView = (TextView)findViewById(R.id.changeSkillNameTextView);
		        	skillNameTextView.setText("Skill: None");
				}
			}
		}
	};
}
