package com.mcdermotsoft;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tinyspeck.android.Glitch;
import com.tinyspeck.android.GlitchSessionDelegate;

public class AuthorizeActivity extends Activity implements GlitchSessionDelegate
{
	Glitch glitch = new Glitch("278-cab1d5f245e5f07d842d90bce704426fe86da5d6", "glitchbuddyandroid://auth");
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.authorize_activity);
		
		getSavedPrefs();
		getAuth();
	}
	
	private void getAuth()
	{
		Intent intent = getIntent();
		
		// Check if we've authenticated before and don't repeat it if so
        if (glitch.isAuthenticated())
        {
        	glitchLoginSuccess();
        }
        else if(intent.hasCategory(Intent.CATEGORY_BROWSABLE))
        {
        	glitch.handleRedirect(intent.getData(), this);
        }
        else
        {
        	glitch.authorize("write", this);
        }
	}

	@Override
	public void glitchLoginSuccess() 
	{
		if (glitch.isAuthenticated())
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
			
        	Intent intent = new Intent(this,GlitchActivity.class);
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	setResult(2);
        	startActivity(intent);
        	finish();
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

	@Override
	public void glitchLoginFail() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void glitchLoggedOut() {
		// TODO Auto-generated method stub
		
	}
}
