package com.mcdermotsoft;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.tinyspeck.android.Glitch;
import com.tinyspeck.android.GlitchRequest;

public class AlarmSetReceiver extends BroadcastReceiver
{
	Context context;
	boolean newDayNotification, newDayVibrate, skillNotification, skillVibrate, holidayNotification, holidayVibrate;
	String newDayPriorTime, newDayNotificationSound, skillNotificationSound, holidayPriorTime, holidayNotificationSound;
	static int NEW_DAY = 0, SKILL = 1, HOLIDAY = 2;
	static String NEW_DAY_ALARM = "com.mcdermotsoft.NEW_DAY_ALARM";
	static String SKILL_ALARM = "com.mcdermotsoft.SKILL_ALARM";
	static String HOLIDAY_ALARM = "com.mcdermotsoft.HOLIDAY_ALARM";
	String skillName;
	
	SharedPreferences alarmPrefs;
	
	Glitch glitch = new Glitch("278-cab1d5f245e5f07d842d90bce704426fe86da5d6", "glitchbuddyandroid://auth");
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		this.context = context;
		getSettings();
		
		if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
		{
			setNewDayAlarm();
			long skillAlarmTime = alarmPrefs.getLong("skillAlarmTime", -1);
			skillName = alarmPrefs.getString("skillName", "Glitch Skill");
			if(skillAlarmTime != -1)
				setSkillAlarm(skillAlarmTime);
			setHolidayAlarm();
		}
		if(intent.getAction().equals("com.mcdermotsoft.PREFS_CHANGED"))
		{
			setNewDayAlarm();
			long skillAlarmTime = alarmPrefs.getLong("skillAlarmTime", -1);
			skillName = alarmPrefs.getString("skillName", "Glitch Skill");
			if(skillAlarmTime != -1)
				setSkillAlarm(skillAlarmTime);
			setHolidayAlarm();
		}
		if(intent.getAction().equals("com.mcdermotsoft.SET_SKILL_ALARM"))
		{
			Bundle bundle = intent.getExtras();
			int max = bundle.getInt("max");
			int progress = bundle.getInt("progress");
			skillName = bundle.getString("skillName");
			setSkillAlarm(max, progress);
		}
	}
	
	private void getSettings()
	{
		alarmPrefs = context.getSharedPreferences("alarmPrefs", Preferences.MODE_PRIVATE);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
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
	
	private void setSkillAlarm(int max, int progress)
	{
		if(skillNotification)
		{
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.SECOND, max-progress);
			
			AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			manager.cancel(makeNotification(SKILL));
			manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), makeNotification(SKILL));
			
			Editor edit = alarmPrefs.edit();
			edit.putLong("skillAlarmTime", calendar.getTimeInMillis());
			edit.putString("skillName", skillName);
			edit.commit();
			
			Log.d("Glitch","Skill alarm set for " + calendar.getTime().toLocaleString());
		}
	}
	
	private void setSkillAlarm(long skillAlarmTime)
	{
		if(skillNotification)
		{
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(skillAlarmTime);
			
			AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			manager.cancel(makeNotification(SKILL));
			manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), makeNotification(SKILL));
		
			Editor edit = alarmPrefs.edit();
			edit.putLong("skillAlarmTime", calendar.getTimeInMillis());
			edit.putString("skillName", skillName);
			edit.commit();
			
			Log.d("Glitch","Skill alarm set for " + calendar.getTime().toLocaleString());
		}
	}
	
	private void setHolidayAlarm()
	{
		if(holidayNotification)
		{
			GlitchRequest request = new GlitchRequest("calendar.getHolidays",glitch);
			request.execute(new HolidayDelegate(this, holidayPriorTime));
		}
	}
	
	private void setNewDayAlarm()
	{
		if(newDayNotification)
		{
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MILLISECOND, calendar.get(Calendar.DST_OFFSET));
			
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			if(hour>=0 && hour<4)
				calendar.set(Calendar.HOUR_OF_DAY, 4);
			if(hour>=4 && hour<8)
				calendar.set(Calendar.HOUR_OF_DAY, 8);
			if(hour>=8 && hour<12)
				calendar.set(Calendar.HOUR_OF_DAY, 12);
			if(hour>=12 && hour<16)
				calendar.set(Calendar.HOUR_OF_DAY, 16);
			if(hour>=16 && hour<20)
				calendar.set(Calendar.HOUR_OF_DAY, 20);
			if(hour>=20 && hour<24)
				calendar.set(Calendar.HOUR_OF_DAY, 0);
			
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			
			if(newDayPriorTime.equals("1 Minute"))
				calendar.add(Calendar.MINUTE, -1);
			if(newDayPriorTime.equals("2 Minutes"))
				calendar.add(Calendar.MINUTE, -2);
			if(newDayPriorTime.equals("5 Minutes"))
				calendar.add(Calendar.MINUTE, -5);
			if(newDayPriorTime.equals("10 Minutes"))
				calendar.add(Calendar.MINUTE, -10);
			if(newDayPriorTime.equals("15 Minutes"))
				calendar.add(Calendar.MINUTE, -15);
			if(newDayPriorTime.equals("30 Minutes"))
				calendar.add(Calendar.MINUTE, -30);
			if(newDayPriorTime.equals("45 Minutes"))
				calendar.add(Calendar.MINUTE, -45);
			if(newDayPriorTime.equals("1 Hour"))
				calendar.add(Calendar.HOUR, -1);
			if(newDayPriorTime.equals("2 Hours"))
				calendar.add(Calendar.HOUR, -2);
						
			Calendar now = Calendar.getInstance();
			if(calendar.getTimeInMillis()<now.getTimeInMillis())
				return;
			
			AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			manager.cancel(makeNotification(NEW_DAY));
			manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000*60*60*4, makeNotification(NEW_DAY));
		
			Log.d("Glitch", "New Day alarm set for " + calendar.getTime().toLocaleString());
		}
	}
	
	protected PendingIntent makeNotification(int reason)
	{
		if(reason == NEW_DAY)
		{
			Intent intent = new Intent(NEW_DAY_ALARM);
			intent.putExtra("offset", newDayPriorTime);
			intent.putExtra("sound", newDayNotificationSound);
			intent.putExtra("vibrate", newDayVibrate);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			return pendingIntent;
		}
		if(reason == SKILL)
		{
			String skillName = alarmPrefs.getString("skillName", "Glitch Skill");
			Intent intent = new Intent(SKILL_ALARM);
			intent.putExtra("sound", skillNotificationSound);
			intent.putExtra("vibrate", skillVibrate);
			intent.putExtra("skillName", skillName);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			return pendingIntent;
		}
		if(reason == HOLIDAY)
		{
			String holidayName = alarmPrefs.getString("holidayName", "Glitch Holiday");
			Intent intent = new Intent("HOLIDAY_ALARM");
			intent.putExtra("offset", holidayPriorTime);
			intent.putExtra("sound", holidayNotificationSound);
			intent.putExtra("vibrate", holidayVibrate);
			intent.putExtra("holidayName", holidayName);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			return pendingIntent;
		}
		
		return null;
	}
}
