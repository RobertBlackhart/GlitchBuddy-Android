package com.mcdermotsoft;

import java.text.DecimalFormat;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.TextView;

public class CustomWindow extends Activity
{
	DecimalFormat formatter = new DecimalFormat();
	Handler handler = new Handler();
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		
		setContentView(R.layout.custom_title);
		
		if(customTitleSupported) 
			setCustomTitle();
	}
	
	private void setCustomTitle()
	{
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        final TextView titleText = (TextView)findViewById(R.id.titleTimeText);
        handler.post(new Runnable()
        {
        	public void run()
        	{
        		double hour = GlitchWidgetProvider.getHour();
        		double minute = GlitchWidgetProvider.getMinute();
        		String amPm = "am";
        		if(hour>12)
        		{
        			amPm = "pm";
        			hour -= 12;
        		}
        		if(hour == 12)
        			amPm = "pm";
        		if(hour == 0)
        			hour = 12;
        		formatter.applyPattern("##");
        		String hourString = formatter.format(hour);
        		formatter.applyPattern("00");
        		String minuteString = formatter.format(minute);
        		titleText.setText(hourString+":"+minuteString+amPm);
        		handler.postDelayed(this, 10000);
        	}
        });
	}
}
