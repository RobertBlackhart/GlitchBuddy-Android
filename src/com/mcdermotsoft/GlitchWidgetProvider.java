package com.mcdermotsoft;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class GlitchWidgetProvider extends AppWidgetProvider
{
	static String LOG_TAG = "GlitchWidget";
	//Custom Intent name that is used by the 'AlarmManager' to tell us to update
	static String CLOCK_WIDGET_UPDATE = "com.mcdermotsoft.GLITCH_WIDGET_UPDATE";
	  
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		for(int id: appWidgetIds)
		{						
			updateAppWidget(context, appWidgetManager, id);
		}
	}
	public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
	{
		// Perform this loop procedure for each App Widget that belongs to this
		// provider
	    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.clock_widget);
	    
	    Intent intent = new Intent(context, GlitchActivity.class); 
	    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0); 
        views.setOnClickPendingIntent(R.id.backgroundLayout, pendingIntent);
	    
	    double year = getYear();
	    double dayOfYear = getDayOfYear();
	    double hour = getHour();
	    double minute = getMinute();
	    
        //
	    //turn the 0-based day-of-year into a day & month
	    //
	    int[] monthAndDay = getMonthAndDay();


	    //
	    //get day-of-week
	    //

	    double daysSinceEpoch = dayOfYear + (307 * year);

	    int dayOfWeek = (int)(daysSinceEpoch % 8);
	    String[] dayString = {"Hairday","Moonday","Twoday","Weddingday","Theday","Fryday","Standday","Fabday"};
	    String[] monthString = {"Primuary","Spork","Bruise","Candy","Fever","Junuary","Septa","Remember","Doom","Widdershins","Eleventy","Recurse"};
	    
	    DecimalFormat df = new DecimalFormat();
	    df.applyPattern("00");
	    DecimalFormat hourFormat = new DecimalFormat();
	    hourFormat.applyPattern("#0");
	    String ampm = "";
	    if(hour>6 && hour<21)
	    	views.setImageViewResource(R.id.sunMoon, R.drawable.sun);
	    else
	    	views.setImageViewResource(R.id.sunMoon, R.drawable.moon);
	    if(hour<12)
	    {
	    	ampm = "am";
	    }
	    else if(hour==12)
	    	ampm = "pm";
	    else
	    {
	    	hour -= 12;
	    	ampm = "pm";
	    }
	    views.setTextViewText(R.id.widgetDayText, dayString[dayOfWeek]);
	    views.setTextViewText(R.id.widgetClockText, hourFormat.format(hour)+":"+df.format(minute)+" "+ampm);
	    views.setTextViewText(R.id.widgetDateText, monthAndDay[1]+ " "+monthString[monthAndDay[0]-1]+", year "+(int)year);
	    
	    // Tell the AppWidgetManager to perform an update on the current app
	    // widget
	    appWidgetManager.updateAppWidget(appWidgetId, views);
	}
	
	public static double getYear()
	{
		Date currentRealDate = new Date();
		
		// To update a label
	    double seconds = currentRealDate.getTime()/1000 - 1238562000;
	    
	    /*
	    there are 4435200 real seconds in a game year
	    there are 14400 real seconds in a game day
	    there are 600 real seconds in a game hour
	    there are 10 real seconds in a game minute
	    */
	    double year = Math.floor(seconds / 4435200);
	    seconds -= year * 4435200;

	    double dayOfYear = Math.floor(seconds / 14400);
	    seconds -= dayOfYear * 14400;

	    double hour = Math.floor(seconds / 600);
	    seconds -= hour * 600;

	    double minute = Math.floor(seconds / 10);
	    seconds -= minute * 10;
	    
	    return year;
	}
	
	public static double getDayOfYear()
	{
		Date currentRealDate = new Date();
		
		// To update a label
	    double seconds = currentRealDate.getTime()/1000 - 1238562000;
	    
	    /*
	    there are 4435200 real seconds in a game year
	    there are 14400 real seconds in a game day
	    there are 600 real seconds in a game hour
	    there are 10 real seconds in a game minute
	    */
	    double year = Math.floor(seconds / 4435200);
	    seconds -= year * 4435200;

	    double dayOfYear = Math.floor(seconds / 14400);
	    seconds -= dayOfYear * 14400;

	    double hour = Math.floor(seconds / 600);
	    seconds -= hour * 600;

	    double minute = Math.floor(seconds / 10);
	    seconds -= minute * 10;
	    
	    return dayOfYear;
	}
	
	public static double getHour()
	{
		Date currentRealDate = new Date();
		
		// To update a label
	    double seconds = currentRealDate.getTime()/1000 - 1238562000;
	    
	    /*
	    there are 4435200 real seconds in a game year
	    there are 14400 real seconds in a game day
	    there are 600 real seconds in a game hour
	    there are 10 real seconds in a game minute
	    */
	    double year = Math.floor(seconds / 4435200);
	    seconds -= year * 4435200;

	    double dayOfYear = Math.floor(seconds / 14400);
	    seconds -= dayOfYear * 14400;

	    double hour = Math.floor(seconds / 600);
	    seconds -= hour * 600;

	    double minute = Math.floor(seconds / 10);
	    seconds -= minute * 10;
	    
	    return hour;
	}
	
	public static double getMinute()
	{
		Date currentRealDate = new Date();
		
		// To update a label
	    double seconds = currentRealDate.getTime()/1000 - 1238562000;
	    
	    /*
	    there are 4435200 real seconds in a game year
	    there are 14400 real seconds in a game day
	    there are 600 real seconds in a game hour
	    there are 10 real seconds in a game minute
	    */
	    double year = Math.floor(seconds / 4435200);
	    seconds -= year * 4435200;

	    double dayOfYear = Math.floor(seconds / 14400);
	    seconds -= dayOfYear * 14400;

	    double hour = Math.floor(seconds / 600);
	    seconds -= hour * 600;

	    double minute = Math.floor(seconds / 10);
	    seconds -= minute * 10;
	    
	    return minute;
	}
	
	public static double getSeconds()
	{
		Date currentRealDate = new Date();
		
		// To update a label
	    double seconds = currentRealDate.getTime()/1000 - 1238562000;
	    
	    /*
	    there are 4435200 real seconds in a game year
	    there are 14400 real seconds in a game day
	    there are 600 real seconds in a game hour
	    there are 10 real seconds in a game minute
	    */
	    double year = Math.floor(seconds / 4435200);
	    seconds -= year * 4435200;

	    double dayOfYear = Math.floor(seconds / 14400);
	    seconds -= dayOfYear * 14400;

	    double hour = Math.floor(seconds / 600);
	    seconds -= hour * 600;

	    double minute = Math.floor(seconds / 10);
	    seconds -= minute * 10;
	    
	    return seconds;
	}
	
	static public int[] getMonthAndDay()
	{
		int dayOfYear = (int)getDayOfYear();
		int[] months = {29, 3, 53, 17, 73, 19, 13, 37, 5, 47, 11, 1};
		int cd = 0;
		int month = 0;
		int day = 0;
		int[] temp = new int[2];

		for(int i=0; i<months.length; i++)
		{
			cd += months[i];
			if (cd > dayOfYear)
			{
				month = i+1;
				day = dayOfYear+1 - (cd - months[i]);
				temp[0] = month;
				temp[1] = day;
				return temp;
			}
		}

		return temp;
	}
	
	private PendingIntent createClockTickIntent(Context context) 
	{
		Intent intent = new Intent(CLOCK_WIDGET_UPDATE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}
	
	@Override
	public void onEnabled(Context context)
	{
		super.onEnabled(context);
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Calendar calendar = Calendar.getInstance();
		long triggerAtTime = calendar.getTimeInMillis() + (1000*60);
		alarmManager.setRepeating(AlarmManager.RTC, triggerAtTime, 60000, createClockTickIntent(context));
	}
	
	@Override
	public void onDisabled(Context context) 
	{
		super.onDisabled(context);
		Log.d(LOG_TAG, "Widget Provider disabled. Turning off timer");
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(createClockTickIntent(context));
	}
	
	@Override   
	public void onReceive(Context context, Intent intent) 
	{
		super.onReceive(context, intent);
		final String action = intent.getAction();
		
		// v1.5 fix that doesn't call onDelete Action
		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action))
		{
			final int appWidgetId = intent.getExtras().getInt(
			AppWidgetManager.EXTRA_APPWIDGET_ID,
			AppWidgetManager.INVALID_APPWIDGET_ID);
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) 
			{
				this.onDeleted(context, new int[] { appWidgetId });
			}
		} 
		else 
		{
			if (CLOCK_WIDGET_UPDATE.equals(action))
			{
				// Get the widget manager and ids for this widget provider, then call the shared
				// clock update method.
				ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
				int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
				for (int appWidgetID: ids) 
				{
					updateAppWidget(context, appWidgetManager, appWidgetID);
				}
			}
		}
	}
}
