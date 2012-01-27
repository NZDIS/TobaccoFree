/**
 * Copyright (C) 2011-2012 NZDIS.org. All Rights Reserved. See AUTHORS and LICENCE.
 */
package nzdis.tobaccofree;

import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;



/**
 * Represents activity responsible for 
 * managing user credentials.
 * 
 * @author Mariusz Nowostawski <mariusz@nowostawski.org>
 *
 * @version $Revision$ <br>
 * Created: Jan 2012
 */
public class ObserverAccountActivity extends Activity
	implements Constants {

	
	private EditText email,password;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observer_account);

		this.email = (EditText)findViewById(R.id.edtEmail);
		this.password = (EditText)findViewById(R.id.edtPassword);

        final Button btnRegister = (Button) findViewById(R.id.btnRegister);
		btnRegister.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://tobaccofree.nzdis.org/observer/register"));
				startActivity(browserIntent);
			}
		});
		
		final Button btnSave = (Button) findViewById(R.id.btnSaveCredentials);
		btnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final UsersDetails user = new UsersDetails();
				try {
					user.setPasswordHash(password.getText().toString(), false);
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				user.setUserEmail(email.getText().toString());
				final DatabaseHelper db = new DatabaseHelper(ObserverAccountActivity.this);
				db.setUsersDetails(user);
				db.close();
				ObserverAccountActivity.this.finish();
			}
		});
		
		
		
		final DatabaseHelper db = new DatabaseHelper(this);
		try {
			final UsersDetails user = db.getUserDetails();
			email.setText(user.getUserEmail());
		} catch (NoSuchAlgorithmException e) {
		} catch (UsernameNotSetException e) {
		}
		db.close();

    }
    
}
