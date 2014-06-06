package com.mcdermotsoft;

import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.tinyspeck.android.GlitchRequest;
import com.tinyspeck.android.GlitchRequestDelegate;

public class HolidayDelegate implements GlitchRequestDelegate 
{
	AlarmSetReceiver receiver;
	String offset;
	SharedPreferences prefs;
	
	public HolidayDelegate(AlarmSetReceiver receiver, String offset)
	{
		this.receiver = receiver;
		this.offset = offset;
		prefs = receiver.context.getSharedPreferences("alarmPrefs", Preferences.MODE_PRIVATE); 
	}
	
	@Override
	public void requestFinished(GlitchRequest request)
	{
		if(request != null && request.method != null)
        {
        	JSONObject response = request.response;
        	if(request.method.equals("calendar.getHolidays"))
        	{
        		try 
        		{
					JSONArray holidayArray = response.getJSONArray("days");
					for(int i=0; i<holidayArray.length(); i++)
					{
						JSONObject holiday = holidayArray.getJSONObject(i);
						String name = holiday.optString("name");
						if(i>0 && name.equals(((JSONObject) holidayArray.get(i-1)).optString("name")))
								continue;
						int[] monthAndDay = GlitchWidgetProvider.getMonthAndDay();
						int month = holiday.optInt("month");
						if(month < monthAndDay[0])
							continue;
						int day = holiday.optInt("day");
						if(day < monthAndDay[1])
							continue;
						int seconds = getTimeInSecondsUntilHoliday(month,day);
						Calendar calendar = Calendar.getInstance();
						calendar.add(Calendar.SECOND, seconds);
						calendar = getOffset(calendar);
						
						Calendar now = Calendar.getInstance();
						if(now.getTimeInMillis() > calendar.getTimeInMillis())
							continue;
						Log.d("Glitch","Holiday alarm set for " + calendar.getTime().toLocaleString());
						AlarmManager manager = (AlarmManager)receiver.context.getSystemService(Context.ALARM_SERVICE);
						manager.cancel(receiver.makeNotification(AlarmSetReceiver.HOLIDAY));
						manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), receiver.makeNotification(AlarmSetReceiver.HOLIDAY));
						
						Editor edit = prefs.edit();
						edit.putString("holidayName", name);
						edit.commit();
					}
				} 
        		catch (JSONException e) 
				{
					Log.e("HolidayDelegate",e.getMessage());
				}
        	}
        }
	}
	
	private Calendar getOffset(Calendar calendar)
	{
		calendar.add(Calendar.MILLISECOND, calendar.get(Calendar.DST_OFFSET));
		
		if(offset.equals("1 Minute"))
			calendar.add(Calendar.MINUTE, -1);
		if(offset.equals("5 Minutes"))
			calendar.add(Calendar.MINUTE, -5);
		if(offset.equals("10 Minutes"))
			calendar.add(Calendar.MINUTE, -10);
		if(offset.equals("30 Minutes"))
			calendar.add(Calendar.MINUTE, -30);
		if(offset.equals("1 Hour"))
			calendar.add(Calendar.HOUR, -1);
		if(offset.equals("2 Hours"))
			calendar.add(Calendar.HOUR, -2);
		if(offset.equals("6 Hours"))
			calendar.add(Calendar.HOUR, -6);
		if(offset.equals("12 Hours"))
			calendar.add(Calendar.HOUR, -12);
		if(offset.equals("1 Day"))
			calendar.add(Calendar.DAY_OF_MONTH, -1);
		if(offset.equals("2 Days"))
			calendar.add(Calendar.DAY_OF_MONTH, -2);
		if(offset.equals("1 Week"))
			calendar.add(Calendar.WEEK_OF_MONTH, -1);
				
		return calendar;
	}
	
	private int getTimeInSecondsUntilHoliday(int month, int day)
	{
		int seconds = 0;
		int[] months = {0, 29, 3, 53, 17, 73, 19, 13, 37, 5, 47, 11, 1}; //0 is buffer to compensate for 1-based month variable
		
		for(int i=GlitchWidgetProvider.getMonthAndDay()[0]; i<month; i++)
		{
			seconds += months[i]*24*60*10;
		}
		
		seconds += (day-GlitchWidgetProvider.getMonthAndDay()[1])*24*60*10;
		double hours = 24-GlitchWidgetProvider.getHour();
		double minutes = 60-GlitchWidgetProvider.getMinute();
		seconds -= (24-hours)*60*10;
		seconds -= (60-minutes)*10;
				
		return seconds;
	}

	@Override
	public void requestFailed(GlitchRequest request) 
	{
		
	}
}
