/**
 * Copyright (C) 2011-2012 NZDIS.org. All Rights Reserved. See AUTHORS and LICENCE.
 */
package nzdis.tobaccofree;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Inflates a row for the list in DownloadedObservationActivity with the
 * required data
 * 
 * @author Hamish Medlin
 *
 * @version $Revision$ <br>
 * Created: Apr 2012
 */
public class DownloadedObservationListAdapter extends ArrayAdapter<ObservationStat> {

	private Context con;
	private ObservationStat[] data;
	private int layoutResourceId;
	
	public DownloadedObservationListAdapter(Context con,int layoutResourceId,ObservationStat[] data){
		super(con,layoutResourceId,data);
		this.con = con;
		this.data = data;
		this.layoutResourceId = layoutResourceId;
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if(row == null){
			LayoutInflater inflater = ((Activity) con).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
		}
		TextView top = (TextView)row.findViewById(R.id.tvStatTop);
		top.setText(data[position].getTopText());
		
		TextView bottom = (TextView)row.findViewById(R.id.tvStatBottom);
		bottom.setText(data[position].getBottomText());
		return row;
	}
}
