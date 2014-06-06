package com.mcdermotsoft;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	Context context = this;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
       
        Preference skillNotificationTest = (Preference)findPreference("skillNotificationTest");
        skillNotificationTest.setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
        	public boolean onPreferenceClick(Preference preference)
        	{
        		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        		boolean skillVibrate = prefs.getBoolean("skillNotificationVibrateCheckBox", false);
        		String skillNotificationSound = prefs.getString("skillNotificationSound", "Default");
        		Intent intent = new Intent("com.mcdermotsoft.SKILL_ALARM");
        		intent.putExtra("sound", skillNotificationSound);
        		intent.putExtra("vibrate", skillVibrate);
        		intent.putExtra("test",true);
        		context.sendBroadcast(intent);
        		
        		return true;
        	}
        });
        Preference newDayNotificationTest = (Preference)findPreference("newDayNotificationTest");
        newDayNotificationTest.setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
        	public boolean onPreferenceClick(Preference preference)
        	{
        		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        		boolean newDayVibrate = prefs.getBoolean("newDayNotificationVibrateCheckBox", false);
        		String newDayPriorTime = prefs.getString("newDayPriorTimeList", "15 Minutes");
        		String newDayNotificationSound = prefs.getString("newDayNotificationSound", "Default");
        		Intent intent = new Intent("com.mcdermotsoft.NEW_DAY_ALARM");
        		intent.putExtra("offset", newDayPriorTime);
        		intent.putExtra("sound", newDayNotificationSound);
        		intent.putExtra("vibrate", newDayVibrate);
        		intent.putExtra("test",true);
        		context.sendBroadcast(intent);
        		
        		return true;
        	}
        });
        Preference holidayNotificationTest = (Preference)findPreference("holidayNotificationTest");
        holidayNotificationTest.setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
        	public boolean onPreferenceClick(Preference preference)
        	{
        		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        		boolean holidayVibrate = prefs.getBoolean("holidayNotificationVibrateCheckBox", false);
        		String holidayPriorTime = prefs.getString("holidayPriorTimeList", "15 Minutes");
        		String holidayNotificationSound = prefs.getString("holidayNotificationSound", "Default");
        		Intent intent = new Intent("com.mcdermotsoft.HOLIDAY_ALARM");
        		intent.putExtra("offset", holidayPriorTime);
        		intent.putExtra("sound", holidayNotificationSound);
        		intent.putExtra("vibrate", holidayVibrate);
        		intent.putExtra("test",true);
        		context.sendBroadcast(intent);
        		
        		return true;
        	}
        });
    }

    @Override
    protected void onResume() 
    {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() 
    {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
    
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
	{
		boolean notificationsEnabled = prefs.getBoolean("notificationCheckbox", false);
		if(notificationsEnabled)
			context.sendBroadcast(new Intent("com.mcdermotsoft.PREFS_CHANGED"));
		else
		{
			AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(AlarmSetReceiver.NEW_DAY_ALARM);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			manager.cancel(pendingIntent);
			intent = new Intent(AlarmSetReceiver.HOLIDAY_ALARM);
			pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			manager.cancel(pendingIntent);
			intent = new Intent(AlarmSetReceiver.SKILL_ALARM);
			pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			manager.cancel(pendingIntent);
		}
		
	}
}