package org.nzdis;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class ViewObservationsActivity extends Activity implements OnClickListener, OnItemSelectedListener {

	private Button btnSave;
	private Spinner spnObservations;
	private List<Observation> obs;
	private String[] names;
	private TextView tvNoSmokersCount,tvAdultSmokerCount,tvAdultSmokerOthersCount,tvChildSmokerCount,total;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_observations);
        
        btnSave = (Button)findViewById(R.id.btnSaveObservation);
        btnSave.setOnClickListener(this);
        spnObservations = (Spinner)findViewById(R.id.spnObservations);
        DatabaseHelper db = new DatabaseHelper(this);
        obs = db.getObservations();
        db.close();
        
        names = new String[obs.size()];
        
        for(int i = 0;i < names.length;i++){
        	names[i] = DateFormat.format("MMMM dd, yyyy h:mmaa",obs.get(i).getStart()).toString();
        }
        
        ArrayAdapter <String> adapter = new ArrayAdapter <String> (this, android.R.layout.simple_spinner_item,names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnObservations.setAdapter(adapter);
        spnObservations.setOnItemSelectedListener(this);
        
        tvNoSmokersCount = (TextView)findViewById(R.id.tvNoSmokersCount);
        tvAdultSmokerCount = (TextView)findViewById(R.id.tvAdultSmokerCount);
        tvAdultSmokerOthersCount = (TextView)findViewById(R.id.tvAdultSmokerOthersCount);
        tvChildSmokerCount = (TextView)findViewById(R.id.tvChildSmokerCount);
        total = (TextView)findViewById(R.id.total);
        
        if(names.length != 0){
        	updateCounts();
        }else{
        	spnObservations.setEnabled(false);
        	btnSave.setEnabled(false);
        }
        
    }
    
    private void updateCounts(){
    	int selectedId = spnObservations.getSelectedItemPosition();
    	tvNoSmokersCount.setText(obs.get(selectedId).getNoSmoking() + "");
    	tvAdultSmokerCount.setText(obs.get(selectedId).getNoOthers() + "");
    	tvAdultSmokerOthersCount.setText(obs.get(selectedId).getOther() + "");
    	tvChildSmokerCount.setText(obs.get(selectedId).getChild() + "");
    	total.setText(obs.get(selectedId).getTotal() + "");
    }
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
		updateCounts();		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

}
