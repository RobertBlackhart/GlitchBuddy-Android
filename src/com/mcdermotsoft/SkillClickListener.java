package com.mcdermotsoft;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tinyspeck.android.Glitch;
import com.tinyspeck.android.GlitchRequest;
import com.tinyspeck.android.GlitchRequestDelegate;

public class SkillClickListener implements OnClickListener, GlitchRequestDelegate
{
	ChangeSkillActivity skillActivity;
	View v, layout;
	JSONObject skillObject;
	AlertDialog dialog;
	Glitch glitch;
	
	public SkillClickListener(ChangeSkillActivity skillActivity, View v, JSONObject skillObject, Glitch glitch)
	{
		this.skillActivity = skillActivity;
		this.v = v;
		this.skillObject = skillObject;  
		this.glitch = glitch;
	}
	
	public void onClick(View view)
	{
		dialog = createDialog();
	}
	
	private AlertDialog createDialog()
	{
		AlertDialog.Builder builder;
		AlertDialog alertDialog;
	
		Context mContext = skillActivity;
		LayoutInflater inflater = (LayoutInflater) skillActivity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layout = inflater.inflate(R.layout.skill_confirm_dialog,(ViewGroup)v.findViewById(R.id.layout_root));
	
		ImageView icon = (ImageView)layout.findViewById(R.id.dialogSkillIcon);
		TextView skillName = (TextView)layout.findViewById(R.id.dialogSkillName);
		TextView description = (TextView)layout.findViewById(R.id.dialogSkillDescription);
		TextView time = (TextView)layout.findViewById(R.id.dialogSkillTime);
		Button cancelButton = (Button)layout.findViewById(R.id.cancelButton);
		Button confirmButton = (Button)layout.findViewById(R.id.confirmButton);
		
		try
		{
			URL iconURL = new URL(skillObject.getString("icon_44"));
			Bitmap iconImage = BitmapFactory.decodeStream(iconURL.openConnection().getInputStream());
			icon.setImageBitmap(iconImage);
		}
		catch(Exception ex)
		{
			Log.e("ChangeSkillActivity",ex.getMessage());
		}
		skillName.setText(skillObject.optString("name"));
		description.setText(skillObject.optString("description"));
		time.setText(skillActivity.getTimeString(skillObject.optInt("time_remaining")));
		getSkillInfo(layout,skillObject.optString("class_tsid"));
		confirmButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				dialog.cancel();
				skillActivity.learnNewSkill(skillObject.optString("class_tsid"));
				skillActivity.setResult(1);
				skillActivity.finish();
			}
		});
		cancelButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				dialog.cancel();
			}
		});
		
		builder = new AlertDialog.Builder(mContext);
		builder.setView(layout);
		alertDialog = builder.create();
		alertDialog.show();
		
		return alertDialog;
	}
	
	private void getSkillInfo(View layout, String class_tsid)
	{
		String idString = class_tsid.substring(class_tsid.indexOf("_")+1);
		
		Map<String,String> args = new HashMap<String,String>();
		args.put("skill_id", idString);
		args.put("skill_class", class_tsid);
		
		GlitchRequest request = new GlitchRequest("skills.getInfo",args,glitch);
		request.execute(this);
	}

	@Override
	public void requestFinished(GlitchRequest request) 
	{
		try
		{
			JSONArray reqsArray = request.response.getJSONArray("reqs");
			for(int i=0; i<reqsArray.length(); i++)
			{
				JSONObject reqObject = reqsArray.getJSONObject(i);
				String name = reqObject.getString("name");
				TextView req = new TextView(skillActivity.getApplicationContext());
				/*req.setMovementMethod(LinkMovementMethod.getInstance());
				Spannable span = Spannable.Factory.getInstance().newSpannable(name);   
			    span.setSpan(new ClickableSpan() 
			    {  
			        public void onClick(View v) 
			        {  
			            TextView view = (TextView)v;
			            view.getText();
			        } 
			    },0,name.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				req.setText(span);*/
				req.setText(name);
				LinearLayout reqLayout = (LinearLayout)layout.findViewById(R.id.reqLayout);
				reqLayout.addView(req);
			}
		}
		catch(JSONException ex)
		{
			Log.e("Glitch",ex.getMessage());
		}
	}

	@Override
	public void requestFailed(GlitchRequest request) {
		// TODO Auto-generated method stub
		
	}
}
