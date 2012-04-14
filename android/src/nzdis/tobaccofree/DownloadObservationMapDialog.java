/**
 * Copyright (C) 2011-2012 NZDIS.org. All Rights Reserved. See AUTHORS and LICENCE.
 */
package nzdis.tobaccofree;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

/**
 * Creates a dialog containing the data from the observation that is given to it
 * 
 * @author Hamish Medlin
 *
 * @version $Revision$ <br>
 * Created: Apr 2012
 */
public class DownloadObservationMapDialog extends Dialog implements android.view.View.OnClickListener {

	private DownloadedObservation item;
	private Button btnClose;
	private TextView tvNoSmokersCount,tvAdultSmokerCount,tvAdultSmokerOthersCount,tvChildSmokerCount,total;
	
	public DownloadObservationMapDialog(Context context,DownloadedObservation item) {
		super(context);
		setContentView(R.layout.dialog_observation);
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		this.item = item;
		setTitle(this.item.getCity());
		
		btnClose = (Button)findViewById(R.id.btnClose);
		
		btnClose.setOnClickListener(this);
		
        tvNoSmokersCount = (TextView)findViewById(R.id.tvNoSmokersCountDialog);
        tvAdultSmokerCount = (TextView)findViewById(R.id.tvAdultSmokerCountDialog);
        tvAdultSmokerOthersCount = (TextView)findViewById(R.id.tvAdultSmokerOthersCountDialog);
        tvChildSmokerCount = (TextView)findViewById(R.id.tvChildSmokerCountDialog);
        total = (TextView)findViewById(R.id.totalDialog);
        
        tvNoSmokersCount.setText(this.item.getNoSmokers() + "");
        tvAdultSmokerCount.setText(this.item.getAdult() + "");
        tvAdultSmokerOthersCount.setText(this.item.getAdults() + "");
        tvChildSmokerCount.setText(this.item.getChild() + "");
        total.setText(this.item.getTotal() + "");
        
	}

	@Override
	public void onClick(View arg0) {
		if(arg0 == btnClose){
			this.dismiss();
			return;
		}
		
	}

}
