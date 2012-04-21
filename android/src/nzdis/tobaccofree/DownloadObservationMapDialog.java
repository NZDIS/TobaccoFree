/**
 * Copyright (C) 2011-2012 NZDIS.org. All Rights Reserved. See AUTHORS and LICENCE.
 */
package nzdis.tobaccofree;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
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
	private TextView tvNoSmokersCount,tvAdultSmokerCount,tvAdultSmokerOthersCount,tvChildSmokerCount,total,startDialog,finishDialog,durationDialog;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("H:m:s");
	
	public DownloadObservationMapDialog(Context context,DownloadedObservation item) {
		super(context);
		setContentView(R.layout.dialog_observation);
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		this.item = item;
		
		String[] startTime = this.item.getStart().split("T");
		String[] finishTime = this.item.getFinish().split("T");

		setTitle(this.item.getCity() + " - " + startTime[0]);

		
		btnClose = (Button)findViewById(R.id.btnClose);
		
		btnClose.setOnClickListener(this);
		
        tvNoSmokersCount = (TextView)findViewById(R.id.tvNoSmokersCountDialog);
        tvAdultSmokerCount = (TextView)findViewById(R.id.tvAdultSmokerCountDialog);
        tvAdultSmokerOthersCount = (TextView)findViewById(R.id.tvAdultSmokerOthersCountDialog);
        tvChildSmokerCount = (TextView)findViewById(R.id.tvChildSmokerCountDialog);
        
        startDialog = (TextView)findViewById(R.id.startDialog);
        finishDialog = (TextView)findViewById(R.id.finishDialog);
        durationDialog = (TextView)findViewById(R.id.durationDialog);
        
        total = (TextView)findViewById(R.id.totalDialog);
        
        tvNoSmokersCount.setText(this.item.getNoSmokers() + "");
        tvAdultSmokerCount.setText(this.item.getAdult() + "");
        tvAdultSmokerOthersCount.setText(this.item.getAdults() + "");
        tvChildSmokerCount.setText(this.item.getChild() + "");
        total.setText(this.item.getTotal() + "");
        
        startDialog.setText(startTime[1].substring(0, startTime[1].indexOf(".")));
        finishDialog.setText(finishTime[1].substring(0, finishTime[1].indexOf(".")));
        
        long duration;
		try {
			duration = dateFormat.parse(finishTime[1]).getTime() - dateFormat.parse(startTime[1]).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			duration = -1;
		}
		
		if(duration > 0){
			int minutes = (int) Math.round((duration * 1.6E-5));
			durationDialog.setText("~" + minutes + " mins");
		}else{
			durationDialog.setText("Error");
		}
        
	}

	@Override
	public void onClick(View arg0) {
		if(arg0 == btnClose){
			this.dismiss();
			return;
		}
		
	}

}
