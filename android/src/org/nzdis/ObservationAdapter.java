package org.nzdis;

import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

public class ObservationAdapter extends ArrayAdapter<Observation> {

	private List<Observation> observations;
	
	public ObservationAdapter(Context context, int textViewResourceId,List<Observation> objects) {
		super(context, textViewResourceId);
		observations = objects;
	}

	public long getItemIdFromPosition(int position){
		return observations.get(position).getId();
	}
	
	@Override
	public void add(Observation in){
		observations.add(in);
		this.notifyDataSetChanged();
	}
	
	@Override
	public Observation getItem(int position){
		return observations.get(position);
	}
}
