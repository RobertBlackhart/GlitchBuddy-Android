package com.mcdermotsoft;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinyspeck.android.Glitch;
import com.tinyspeck.android.GlitchRequest;
import com.tinyspeck.android.GlitchRequestDelegate;
import com.tinyspeck.android.GlitchSessionDelegate;

public class GlitchActivity extends CustomWindow implements GlitchRequestDelegate, GlitchSessionDelegate
{
	Glitch glitch;
	Context context = this;
	TextView nameTextView, timeRemaining, currantsTextView, levelTextView, skillNameTextView, titleText;
	ImageView avatarImageView, skillIconView, moodImageView, energyImageView;
	FlowLayout bagLayout, slotLayout;
	static PlayerInventory inventory = null;
	ProgressBar skillProgressBar, xpProgressBar;
	ProgressDialog progressDialog;
	Handler handler = new Handler();
	boolean newDayNotification, newDayVibrate, skillNotification, skillVibrate, holidayNotification, holidayVibrate;
	String newDayPriorTime, newDayNotificationSound, skillNotificationSound, holidayPriorTime, holidayNotificationSound;
	String skillIconUrl = null, avatarUrl, skillName, playerName;
	int mood, moodMax, energy, energyMax, xp, xpMax, level, currants, requestFinished;
	
	static final int DIALOG_LOGIN_FAIL_ID = 0;
	static final int DIALOG_REQUEST_FAIL_ID = 1;
		
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		int orientation = getResources().getConfiguration().orientation;
		if(orientation == Configuration.ORIENTATION_PORTRAIT)
			setContentView(R.layout.info_activity);
		else
			setContentView(R.layout.info_activity_land);
		
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
		
		glitch = new Glitch("278-cab1d5f245e5f07d842d90bce704426fe86da5d6", "glitchbuddyandroid://auth");
		
		setupUIComponents();
        
    	getSavedPrefs();
        if(!glitch.isAuthenticated())
        {
        	Intent intent = new Intent(GlitchActivity.this,AuthorizeActivity.class);
        	startActivityForResult(intent,1);
        	this.finish();
        }
        else
        {
        	getSettings();
			
			requestFinished = 0;
			new GetInfoTask().execute();			
			glitchLoginSuccess();
			handler.post(runnable);
        }
	}
	
	private void setupUIComponents()
	{
		timeRemaining = (TextView)findViewById(R.id.skillProgressTextView);
		skillNameTextView = (TextView)findViewById(R.id.skillNameTextView);
        nameTextView = (TextView)findViewById(R.id.nameTextView);
        currantsTextView = (TextView)findViewById(R.id.currantsTextView);
        levelTextView = (TextView)findViewById(R.id.levelTextView);
        avatarImageView = (ImageView)findViewById(R.id.avatarImageView);
        moodImageView = (ImageView)findViewById(R.id.moodImageView);
        energyImageView = (ImageView)findViewById(R.id.energyImageView);
        xpProgressBar = (ProgressBar)findViewById(R.id.xpProgressBar);
        xpProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.my_progress_bar));
        skillIconView = (ImageView)findViewById(R.id.skillIcon);
        skillProgressBar = (ProgressBar)findViewById(R.id.skillProgressBar);
        skillProgressBar.setOnClickListener(new OnClickListener()
        {
        	public void onClick(View v)
        	{
        		Intent intent = new Intent(GlitchActivity.this,ChangeSkillActivity.class);
        		Bundle bundle = new Bundle();
        		bundle.putString("skillName", skillName);
        		bundle.putInt("progress", skillProgressBar.getProgress());
        		bundle.putInt("max", skillProgressBar.getMax());
        		bundle.putString("icon", skillIconUrl);
        		intent.putExtras(bundle);
        		startActivityForResult(intent, 1);
        	}
        });
	}
	
	private Bundle saveUI()
	{
		Bundle bundle = new Bundle();
		
		Bitmap avatar = ((BitmapDrawable)avatarImageView.getDrawable()).getBitmap();
		Bitmap mood = ((BitmapDrawable)moodImageView.getDrawable()).getBitmap();
		Bitmap energy = ((BitmapDrawable)energyImageView.getDrawable()).getBitmap();
		Bitmap skillIcon = ((BitmapDrawable)skillIconView.getDrawable()).getBitmap();
		bundle.putString("name", nameTextView.getText().toString());
		bundle.putString("skillName", skillNameTextView.getText().toString());
		bundle.putParcelable("avatar", avatar);
		bundle.putParcelable("mood", mood);
		bundle.putParcelable("energy", energy);
		bundle.putParcelable("skillIcon", skillIcon);
		bundle.putInt("xp", xp);
		bundle.putInt("xpMax", xpMax);
		bundle.putInt("level", level);
		bundle.putInt("currants", currants);
		bundle.putInt("skillMax", skillProgressBar.getMax());
		bundle.putInt("skillProgress",skillProgressBar.getProgress());
		handler.removeCallbacks(runnable);
				
		return bundle;
	}
	
	private boolean restoreUI(Bundle bundle)
	{	
		setupUIComponents();
		nameTextView.setText(bundle.getString("name"));
		skillNameTextView.setText(bundle.getString("skillName"));
		avatarImageView.setImageBitmap((Bitmap)bundle.getParcelable("avatar"));
		moodImageView.setImageBitmap((Bitmap)bundle.getParcelable("mood"));
		energyImageView.setImageBitmap((Bitmap)bundle.getParcelable("energy"));
		skillIconView.setImageBitmap((Bitmap)bundle.getParcelable("skillIcon"));
		xpProgressBar.setMax(bundle.getInt("xpMax"));
		xpProgressBar.setProgress(bundle.getInt("xp"));
		levelTextView.setText("Level " + bundle.getInt("level"));
		currantsTextView.setText(String.valueOf(bundle.getInt("currants")));
		skillProgressBar.setMax(bundle.getInt("skillMax"));
		skillProgressBar.setProgress(bundle.getInt("skillProgress"));
		handler.post(runnable);
		
		bagLayout = (FlowLayout)findViewById(R.id.bagLayout);
    	ArrayList<String> urlList = new ArrayList<String>();
    	ArrayList<String> countList = new ArrayList<String>();
    	ArrayList<String> categoryList = new ArrayList<String>();
    	
    	for(int i=0; i<inventory.getContents().size(); i++)
    	{
    		Slot slot = inventory.getContents().get("slot_"+String.valueOf(i));
    		if(slot != null)
    		{
    			urlList.add(slot.getItemDef().getIconic_url());
    			countList.add(String.valueOf(slot.getCount()));
    			categoryList.add(slot.getItemDef().getCategory());
    		}
    		else
    		{
    			urlList.add(null);
    			countList.add(null);
    			categoryList.add(null);
    		}
    	}
    	
    	new InventoryIconTask((GlitchActivity)context, inventory).execute(urlList,countList,categoryList);
	
    	return true;
	}
	
	@Override
	public void onConfigurationChanged(Configuration config)
	{
		super.onConfigurationChanged(config);
		
		Bundle bundle = saveUI();
		int orientation = config.orientation;
		if(orientation == Configuration.ORIENTATION_LANDSCAPE)
			setContentView(R.layout.info_activity_land);
		else
			setContentView(R.layout.info_activity);
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
	
	private class GetInfoTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(context, "", "Loading, Please wait...", true);
		}
		
		@Override
		protected Void doInBackground(Void...voids)
		{
			while(requestFinished < 4)
			{};
			return null;
		}
		
		@Override
		protected void onPostExecute(Void voids)
		{
			progressDialog.dismiss();
			Toast toast = Toast.makeText(context, "Create an auction by clicking on an item in your inventory", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			toast.show();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		super.onActivityResult(requestCode, resultCode, intent);
		Log.d("Glitch","resultCode:" + resultCode);
		//returned from ChangeSkillActivity having changed our skill
		if(resultCode == 1)
		{
			final GlitchRequest skillsListLearningRequest = glitch.getRequest("skills.listLearning");
	        handler.postDelayed(new Runnable()
	        {
	        	public void run()
	        	{
	        		skillsListLearningRequest.execute((GlitchRequestDelegate) context);
	        	}
	        }, 1000);
		}
		//logged in via AuthorizeActivity
		if(resultCode == 2)
		{
			getSettings();
			
			requestFinished = 0;
			new GetInfoTask().execute();			
			glitchLoginSuccess();
			handler.post(runnable);
		}
		//created auctions, need to refresh inventory view
		if(resultCode == 3)
		{
			FlowLayout bagLayout = (FlowLayout)findViewById(R.id.bagLayout);
			bagLayout.removeAllViews();
			FlowLayout slotLayout = (FlowLayout)findViewById(R.id.slotLayout);
			slotLayout.removeAllViews();
			ProgressBar progress = new ProgressBar(getBaseContext());
			progress.setIndeterminate(true);
			TextView refreshText = new TextView(getBaseContext());
			refreshText.setText("refreshing...");
			bagLayout.addView(progress);
			bagLayout.addView(refreshText);
			HashMap<String,String> params = new HashMap<String,String>();
	        params.put("defs", "1");
	        GlitchRequest playersInventoryRequest = glitch.getRequest("players.inventory", params);
	        playersInventoryRequest.execute(this);
		}
	}

	public boolean onCreateOptionsMenu(Menu menu)
    {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.options, menu);
    	return true;
    }
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.settings:
			{
				Intent preferences = new Intent(getBaseContext(),Preferences.class);
				startActivity(preferences);
				return true;
			}
			case R.id.about:
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    	builder.setMessage("Glitch Buddy uses the Glitch APIs but is not endorsed by or affiliated with Tiny Speck, Inc. (the makers of Glitch) in any way.")
		    	       .setCancelable(false)
		    	       .setNeutralButton("OK", new DialogInterface.OnClickListener()
		    	       {
		    	           public void onClick(DialogInterface dialog, int id) 
		    	           {
		    	        	   dialog.cancel();
		    	           }
		    	       });
		    	builder.show();
		    	return true;
			}
			case R.id.auctionScan:
			{
				Intent auctionScan = new Intent(getBaseContext(),AuctionScanActivity.class);
				startActivity(auctionScan);
				return true;
			}
			default:
				return super.onOptionsItemSelected(item);
		}
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
		catch(FileNotFoundException ex)
		{
			Log.i("Glitch","no auth file exists");
		}
		catch(IOException ex)
		{
			Log.e("GlitchActivity",ex.getMessage());
		}
		
		glitch.accessToken = auth;
	}
	
	private void getSettings()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		newDayNotification = prefs.getBoolean("newDayNotificationCheckbox", false);
		newDayVibrate = prefs.getBoolean("newDayNotificationVibrateCheckbox", false);
		newDayPriorTime = prefs.getString("newDayPriorTimeList", "15 Minutes");
		newDayNotificationSound = prefs.getString("newDayNotificationSound", "Default");
		
		skillNotification = prefs.getBoolean("skillNotificationCheckbox", false);
		skillVibrate = prefs.getBoolean("skillNotificationVibrateCheckBox", false);
		skillNotificationSound = prefs.getString("skillNotificationSound", "Default");
		
		holidayNotification = prefs.getBoolean("holidayNotificationCheckbox", false);
		holidayVibrate = prefs.getBoolean("holidayNotificationVibrateCheckbox", false);
		holidayPriorTime = prefs.getString("holidayPriorTimeList", "1 Day");
		holidayNotificationSound = prefs.getString("holidayNotificationSound", "Default");
	}
	
	protected void onPause()
	{
		super.onPause();

		if(glitch.isAuthenticated())
		{
			try 
			{
				 OutputStreamWriter out = new OutputStreamWriter(openFileOutput("auth_file",0));
				 out.write(glitch.accessToken);
				 out.close();
			} 
			catch (IOException ex) 
			{
				Log.e("GlitchActivity",ex.getMessage());
			}
		}
		
		if(progressDialog != null)
			progressDialog.cancel();
		
		SharedPreferences prefs = getSharedPreferences("timerPrefs", Preferences.MODE_PRIVATE);
		Editor edit = prefs.edit();
		edit.putInt("progress", skillProgressBar.getProgress());
		edit.putInt("max", skillProgressBar.getMax());
		edit.putLong("time", Calendar.getInstance().getTimeInMillis());
		edit.commit();
		
		handler.removeCallbacks(runnable);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		SharedPreferences prefs = getSharedPreferences("timerPrefs", Preferences.MODE_PRIVATE);
		long time = prefs.getLong("time", 0);
		long currentTime = Calendar.getInstance().getTimeInMillis();
		int diff = (int)(currentTime-time)/1000;
		skillProgressBar.setMax(prefs.getInt("max", 100));
		skillProgressBar.setProgress(prefs.getInt("progress", 100)+diff);
		
		handler.post(runnable);
	}
		
	private void setStats(JSONObject response)
	{
		mood = response.optInt("mood");
		moodMax = response.optInt("mood_max");
		energy = response.optInt("energy");
		energyMax = response.optInt("energy_max");
		xp = response.optInt("xp");
		xpMax = response.optInt("xp_max");
		currants = response.optInt("currants");
		level = response.optInt("level");
		
		xpProgressBar.setMax(xpMax);
		xpProgressBar.setProgress(xp);
		levelTextView.setText("Level " + level);
		currantsTextView.setText(String.valueOf(currants));
		setMood(mood, moodMax);
		setEnergy(energy, energyMax);
	}
	
	private void setEnergy(int energy, int energyMax)
	{
		energy = energy*100/energyMax;
		if(energy<=10)
			energyImageView.setImageResource(R.drawable.energy10);
		if(energy>10 && energy<=20)
			energyImageView.setImageResource(R.drawable.energy20);
		if(energy>20 && energy<=30)
			energyImageView.setImageResource(R.drawable.energy30);
		if(energy>30 && energy<=40)
			energyImageView.setImageResource(R.drawable.energy40);
		if(energy>40 && energy<=50)
			energyImageView.setImageResource(R.drawable.energy50);
		if(energy>50 && energy<=60)
			energyImageView.setImageResource(R.drawable.energy60);
		if(energy>60 && energy<=70)
			energyImageView.setImageResource(R.drawable.energy70);
		if(energy>70 && energy<=80)
			energyImageView.setImageResource(R.drawable.energy80);
		if(energy>80 && energy<=90)
			energyImageView.setImageResource(R.drawable.energy90);
		if(energy>90 && energy<=100)
			energyImageView.setImageResource(R.drawable.energy100);
	}
	
	private void setMood(int mood, int moodMax)
	{
		mood = mood*100/moodMax;
		if(mood<=10)
			moodImageView.setImageResource(R.drawable.mood10);
		if(mood>10 && mood<=20)
			moodImageView.setImageResource(R.drawable.mood20);
		if(mood>20 && mood<=30)
			moodImageView.setImageResource(R.drawable.mood30);
		if(mood>30 && mood<=40)
			moodImageView.setImageResource(R.drawable.mood40);
		if(mood>40 && mood<=50)
			moodImageView.setImageResource(R.drawable.mood50);
		if(mood>50 && mood<=60)
			moodImageView.setImageResource(R.drawable.mood60);
		if(mood>60 && mood<=70)
			moodImageView.setImageResource(R.drawable.mood70);
		if(mood>70 && mood<=80)
			moodImageView.setImageResource(R.drawable.mood80);
		if(mood>80 && mood<=90)
			moodImageView.setImageResource(R.drawable.mood90);
		if(mood>90 && mood<=100)
			moodImageView.setImageResource(R.drawable.mood100);
	}
	
	////GlitchRequester interface methods ////

	public void requestFinished(GlitchRequest request) 
	{
        if(request != null && request.method != null)
        {
        	JSONObject response = request.response;
        	
	        if(request.method == "players.stats")
	        {
	        	if(response != null)
	        	{
	        		setStats(response);
	        	}
	        	
	        	requestFinished++;
	        }
        	if(request.method == "players.info")
        	{	        	
	        	if(response != null)
	        	{
	        		playerName = response.optString("player_name");
	        		nameTextView.setText("Hello " + playerName + "!");
	        		
	        		try 
	        		{
	        			avatarUrl = response.optString("avatar_url");
						URL avatarURL = new URL(avatarUrl);
						Bitmap avatar = BitmapFactory.decodeStream(avatarURL.openConnection().getInputStream());
						avatarImageView.setImageBitmap(avatar);
					} 
	        		catch (IOException ex)
					{
						ex.printStackTrace();
					}
	        	}
	        	
	        	requestFinished++;
	    	}
	        if(request.method == "skills.listLearning")
	        {
	        	JSONObject learningObject;
	        	if(response.optJSONObject("learning") != null)
				{
					try 
					{
						learningObject = response.getJSONObject("learning");
						JSONArray names = learningObject.names();
						JSONObject skillObject = learningObject.getJSONObject((String)names.get(0));
						skillName = skillObject.optString("name");
						skillIconUrl = skillObject.optString("icon_44");
						Bitmap skillIcon = BitmapFactory.decodeStream(new URL(skillIconUrl).openConnection().getInputStream());
						skillIconView.setImageBitmap(skillIcon);
						
			        	int total = skillObject.optInt("total_time");
			        	int remaining = skillObject.optInt("time_remaining");
			        	int progress = total - remaining;
			        	skillProgressBar.setMax(total);
			        	skillProgressBar.setProgress(progress);
			            handler.postDelayed(runnable,1000);
			        	
			        	skillNameTextView.setText("Skill: " + skillName);
			        	
			        	if(skillNotification && skillProgressBar.getMax() != 100)
			        	{
			        		Intent intent = new Intent();
			        		intent.setAction("com.mcdermotsoft.SET_SKILL_ALARM");
			        		intent.putExtra("max", skillProgressBar.getMax());
			        		intent.putExtra("progress", skillProgressBar.getProgress());
			        		intent.putExtra("skillName", skillName);
			        		getBaseContext().sendBroadcast(intent);
			        	}
					}
					catch (Exception ex) 
					{
						Log.e("GlitchActivity",ex.getMessage());
					}
				}
	        	
	        	requestFinished++;
	        }
	        
	        if(request.method == "players.inventory")
	        {
	        	FlowLayout bagLayout = (FlowLayout)findViewById(R.id.bagLayout);
	        	bagLayout.removeAllViews();
	        	ObjectMapper mapper = new ObjectMapper();
	        	try
	        	{
					inventory = mapper.readValue(request.result, PlayerInventory.class);
	        	} 
	        	catch (Exception ex) 
	        	{
					Log.e("Glitch",""+ex.getMessage());
				}
	        		        	
	        	ArrayList<String> urlList = new ArrayList<String>();
	        	ArrayList<String> countList = new ArrayList<String>();
	        	ArrayList<String> categoryList = new ArrayList<String>();
	        	Slot slot = null;
	        	
	        	for(int i=0; i<inventory.getContents().size(); i++)
	        	{
	        		slot = inventory.getContents().get("slot_"+String.valueOf(i));
	        		if(slot != null)
	        		{
	        			urlList.add(slot.getItemDef().getIconic_url());
	        			countList.add(String.valueOf(slot.getCount()));
	        			categoryList.add(slot.getItemDef().getCategory());
	        		}
	        		else
	        		{
	        			urlList.add(null);
	        			countList.add(null);
	        			categoryList.add(null);
	        		}
	        	}
	        	
	        	new InventoryIconTask((GlitchActivity)context, inventory).execute(urlList,countList,categoryList);
	        } 
        }
	}
	
	private String getTimeString(int remaining)
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


	public void requestFailed(GlitchRequest request) 
	{
		showDialog(DIALOG_REQUEST_FAIL_ID);
	}
	
	
	//// GlitchSession interface methods ////

	public void glitchLoginSuccess() 
	{
		GlitchRequest playersInfoRequest = glitch.getRequest("players.info");
        playersInfoRequest.execute(this);
        GlitchRequest skillsListLearningRequest = glitch.getRequest("skills.listLearning");
        skillsListLearningRequest.execute(this);
        GlitchRequest playersStatsRequest = glitch.getRequest("players.stats");
        playersStatsRequest.execute(this);       
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("defs", "1");
        GlitchRequest playersInventoryRequest = glitch.getRequest("players.inventory", params);
        playersInventoryRequest.execute(this);
	}

	public void glitchLoginFail() 
	{
		showDialog(DIALOG_LOGIN_FAIL_ID);
	}

	public void glitchLoggedOut() 
	{
		// Called when user logs out (method stub, not yet implemented)
	}
	
	
	//// Dialog Creation ////
	
	protected Dialog onCreateDialog(int id) 
	{
	    Dialog dialog;
	    
	    switch(id) {
	    case DIALOG_LOGIN_FAIL_ID:
	    	
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage("Login failure!")
	    	       .setCancelable(false)
	    	       .setPositiveButton("Darn", new DialogInterface.OnClickListener()
	    	       {
	    	           public void onClick(DialogInterface dialog, int id) 
	    	           {
	    	        	   dialog.cancel();
	    	           }
	    	       });
	    	dialog = builder.create();
	    	
	        break;
	        
	    case DIALOG_REQUEST_FAIL_ID:
	    {	
	    	AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
	    	builder1.setMessage("Could not connect to the TinySpeck servers.\nIs your device connected to the internet?")
	    	       .setCancelable(false)
	    	       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
	    	       {
	    	           public void onClick(DialogInterface dialog, int id)
	    	           {
	    	        	   dialog.cancel();
	    	        	   progressDialog.cancel();
	    	        	   finish();
	    	           }
	    	       })
	    	       .setPositiveButton("Retry", new DialogInterface.OnClickListener()
	    	       {
	    	    	   public void onClick(DialogInterface dialog, int id)
	    	    	   {
	    	    		   progressDialog.cancel();
	    	    		   requestFinished = 0;
	    	    		   new GetInfoTask().execute();		
	    	    		   glitchLoginSuccess();
	    	    		   handler.post(runnable);
	    	    	   }
	    	       });
	    	
	    	dialog = builder1.create();
	    	
	        break;
	    }
	    default:
	        dialog = null;
	    }
	    
	    return dialog;
	}

	private Runnable runnable = new Runnable()
	{
		public void run() 
		{
			{
				handler.removeCallbacks(this);
				handler.postDelayed(this,1000);
				int progress = skillProgressBar.getProgress();
				int max = skillProgressBar.getMax();
				
				if(progress<max && max != 100)
				{
					progress++;
					
					skillProgressBar.setProgress(progress);
					timeRemaining.setText(getTimeString(max-progress));
				}
				else
				{
					handler.removeCallbacks(this);
					timeRemaining.setText("You are not currently learning a skill");
					TextView skillNameTextView = (TextView)findViewById(R.id.skillNameTextView);
		        	skillNameTextView.setText("Skill: None");
					skillIconUrl = null;
					skillIconView.setImageResource(0);
				}
			}
		}
	};
}
