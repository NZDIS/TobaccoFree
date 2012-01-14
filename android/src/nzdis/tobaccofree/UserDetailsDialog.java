/**
 * Copyright (C) 2011-2012 NZDIS.org. All Rights Reserved. See AUTHORS and LICENCE.
 */
package nzdis.tobaccofree;

import java.security.NoSuchAlgorithmException;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;

/**
 * Represents dialog for acquiring user email and password. 
 * 
 * @author Hamish Medlin
 *
 * @version $Revision$ <br>
 * Created: Jan 2012
 */
public class UserDetailsDialog extends Dialog implements OnClickListener {

	private Button btnSave, btnCancel;
	private Context con;
	private UsersDetails dets;
	private EditText email,password;
	
	public UserDetailsDialog(Context context) {
		super(context);
		setContentView(R.layout.dialog_user_details);
		con = context;
		
		//makes sure the dialog is the full width
		LayoutParams params = getWindow().getAttributes();
		params.width = LayoutParams.FILL_PARENT;
		getWindow().setAttributes((android.view.WindowManager.LayoutParams)params);
		this.setTitle(R.string.enter_details);
		
		btnSave = (Button)findViewById(R.id.btnSaveDetails);
		btnCancel = (Button)findViewById(R.id.btnCancelDetails);
		
		btnCancel.setOnClickListener(this);
		btnSave.setOnClickListener(this);
		
		email = (EditText)findViewById(R.id.edtEmail);
		password = (EditText)findViewById(R.id.edtPassword);
		
		DatabaseHelper db = new DatabaseHelper(con);
		try {
			dets = db.getUserDetails();
			email.setText(dets.getUserEmail());
		} catch (NoSuchAlgorithmException e) {
		} catch (UsernameNotSetException e) {
		}
		db.close();
	}

	@Override
	public void onClick(View arg0) {
		if(arg0 == btnCancel){
			this.dismiss();
			return;
		}
		
		if(arg0 == btnSave){
			dets = new UsersDetails();
			try {
				dets.setPasswordHash(password.getText().toString(), false);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dets.setUserEmail(email.getText().toString());
			DatabaseHelper db = new DatabaseHelper(con);
			db.setUsersDetails(dets);
			db.close();
			dismiss();
		}
	}

}
