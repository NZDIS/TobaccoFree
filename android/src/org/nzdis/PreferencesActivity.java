package org.nzdis;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PreferencesActivity extends Activity implements OnClickListener {

	private SharedPreferences preferences;
	private Button btnViewInstructions,btnLeftRightMode,btnPracticeMode,btnSound,btnEmailSupport, btnSetUsername;
	
	public static final int SET_DETAILS = 123;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        
        btnViewInstructions = (Button)findViewById(R.id.btnViewInstuctions);
        btnViewInstructions.setOnClickListener(this);
        
        btnLeftRightMode = (Button)findViewById(R.id.btnLeftRightMode);
        btnLeftRightMode.setOnClickListener(this);
        
        btnPracticeMode = (Button)findViewById(R.id.btnPractice);
        btnPracticeMode.setOnClickListener(this);
        
        btnSound = (Button)findViewById(R.id.btnSound);
        btnSound.setOnClickListener(this);
        
        btnEmailSupport = (Button)findViewById(R.id.btnEmailSupport);
        btnEmailSupport.setOnClickListener(this);
        
        btnSetUsername = (Button)findViewById(R.id.btnUsername);
        btnSetUsername.setOnClickListener(this);
        
		if(!preferences.getBoolean("play_sound", true)){
			btnSound.setText(getString(R.string.sound_on));
		}
	}

	@Override
	public void onClick(View arg0) {
		if(arg0 == btnEmailSupport){
		    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		    emailIntent.setType("plain/text");
		    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Globalink App Test");
		    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"medlin.hamish@gmail.com","vimal.patel@otago.ac.nz","mariusz@nowostawski.org"}); 
		    startActivity(Intent.createChooser(emailIntent, getString(R.string.send_feedback))); 
		    return;
		}
		
		if(arg0 == btnSound){			
			SharedPreferences.Editor editor = preferences.edit();
			editor.putBoolean("play_sound", !preferences.getBoolean("play_sound", true));
			editor.commit();
			if(!preferences.getBoolean("play_sound", true)){
				btnSound.setText(getString(R.string.sound_on));
			}else{
				btnSound.setText(getString(R.string.sound_off));
			}
			return;
		}
		
		if(arg0 == btnViewInstructions){
			Intent instructions = new Intent(this,InstructionsActivity.class);
			startActivity(instructions);
			return;
		}
		
		if(arg0 == btnSetUsername){
			showDialog(SET_DETAILS);
			return;
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int d){
		switch(d){
			case SET_DETAILS:
				return new UserDetailsDialog(this);			
			default:
				return null;
		}
	}
	
}
