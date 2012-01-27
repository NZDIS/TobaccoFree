/**
 * Copyright (C) 2011-2012 NZDIS.org. All Rights Reserved. See AUTHORS and LICENCE.
 */
package nzdis.tobaccofree;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Represents simple list of preferences managed by buttons.
 * 
 * @author Hamish Medlin
 * @author Mariusz Nowostawski <mariusz@nowostawski.org>
 *
 * @version $Revision$ <br>
 * Created: Dec 2011
 */
public class PreferencesActivity extends Activity implements OnClickListener {
	
	public static final int SET_DETAILS = 123;
	
	private SharedPreferences preferences;
	private Button btnViewInstructions, btnSound, 
			btnEmailSupport, btnSetUserCredentials,
			btnLeftRightMode;//, btnPracticeMode;
				
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        
        btnViewInstructions = (Button)findViewById(R.id.btnViewInstuctions);
        btnViewInstructions.setOnClickListener(this);
        
     
        btnLeftRightMode = (Button)findViewById(R.id.btnLeftRightMode);
        btnLeftRightMode.setOnClickListener(this);
        
        /*btnPracticeMode = (Button)findViewById(R.id.btnPractice);
        btnPracticeMode.setOnClickListener(this);
*/
        btnSound = (Button)findViewById(R.id.btnSound);
        btnSound.setOnClickListener(this);
        
        btnEmailSupport = (Button)findViewById(R.id.btnEmailSupport);
        btnEmailSupport.setOnClickListener(this);
        
        btnSetUserCredentials = (Button)findViewById(R.id.btnUsername);
        btnSetUserCredentials.setOnClickListener(this);
        
		if(!preferences.getBoolean("play_sound", true)){
			btnSound.setText(getString(R.string.sound_on));
		}
		
		if(preferences.getBoolean("left_hand", false)){
			btnLeftRightMode.setText(getString(R.string.right_hand));
		}
	}

	@Override
	public void onClick(View arg0) {
		if(arg0 == btnEmailSupport){
		    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		    emailIntent.setType("plain/text");
		    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "[TobbacoFree Android Feedback] ");
		    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"info@nzdis.org"}); 
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
		
		if(arg0 == btnSetUserCredentials){
			final Intent user_credentials = new Intent(this, ObserverAccountActivity.class);
			startActivity(user_credentials);
			return;
		}
		
		if(arg0 == btnLeftRightMode){
			SharedPreferences.Editor editor = preferences.edit();
			editor.putBoolean("left_hand", !preferences.getBoolean("left_hand", false));
			editor.commit();
			if(preferences.getBoolean("left_hand", false)){
				btnLeftRightMode.setText(getString(R.string.right_hand));
			}else{
				btnLeftRightMode.setText(getString(R.string.left_handed));
			}
			return;
		}
	}

}
