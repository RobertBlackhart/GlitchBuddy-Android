package com.mcdermotsoft;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

public class GlitchBuddyNotifier extends BroadcastReceiver
{
	static int NEW_DAY = 1, SKILL = 2, HOLIDAY = 3;
	Context context;
	Intent intent;
	SharedPreferences prefs;
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		prefs = context.getSharedPreferences("alarmPrefs", Preferences.MODE_PRIVATE);
		this.context = context;
		this.intent = intent;
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(ns);
		String action = intent.getAction();
		Notification notification;
		
		if(action.equals("com.mcdermotsoft.NEW_DAY_ALARM"))
		{
			String offset = intent.getStringExtra("offset");
			if(offset != null)
				offset = "in " + offset + "!";
			else
				offset = "now!";
			String contentText = "New Day (12:00am) " + offset;
			
			notification = makeNotification("New Day Starting", contentText);
			mNotificationManager.notify(NEW_DAY, notification);
		}
		if(action.equals("com.mcdermotsoft.HOLIDAY_ALARM"))
		{
			String offset = intent.getStringExtra("offset");
			String holidayName = prefs.getString("holidayName", "Glitch Holiday");
			if(offset != null)
				offset = "in " + offset + "!";
			else
				offset = "now!";
			String contentText = holidayName + " Starting " + offset;
			notification = makeNotification("Holiday Approaching",contentText);
			mNotificationManager.notify(HOLIDAY, notification);
		}
		if(action.equals("com.mcdermotsoft.SKILL_ALARM"))
		{
			String skillName = prefs.getString("skillName", "Glitch Skill");
			notification = makeNotification("Skill Done",skillName + " Complete");
			mNotificationManager.notify(SKILL, notification);
		}
	}
	
	private Notification makeNotification(String tickerText, String contentText)
	{
		String sound = intent.getStringExtra("sound");	
		boolean vibrate = intent.getBooleanExtra("vibrate",false);
		boolean test = intent.getBooleanExtra("test", false);
		
		Notification notification = new Notification(R.drawable.icon, tickerText, System.currentTimeMillis());
		if(sound != null)
			notification.sound = Uri.parse(sound);
		else
			notification.defaults |= Notification.DEFAULT_SOUND;
		
		if(vibrate)
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		if(!test)
			intent = new Intent(context,GlitchActivity.class);
		
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

		notification.setLatestEventInfo(context, "Glitch Buddy", contentText, contentIntent);
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		return notification;
	}
}
