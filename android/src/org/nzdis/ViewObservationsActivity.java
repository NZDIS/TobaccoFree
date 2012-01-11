package org.nzdis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

	private Button btnExportSelected,btnExportAll,btnDeleteSelected,btnDeleteAll,btnEmailObservation,btnEmailAll;
	private Spinner spnObservations;
	private List<Observation> obs;
	private String[] names;
	private TextView tvNoSmokersCount,tvAdultSmokerCount,tvAdultSmokerOthersCount,tvChildSmokerCount,total;
	
	private static final int NO_SDCARD = 0,DIR_ERROR = 1,SUCCESS = 2,FAIL = 3,DELETE_ALL = 4,DELETE_SELECTED = 5;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_observations);
        
        //buttons
        btnExportSelected = (Button)findViewById(R.id.btnExportObservation);
        btnExportSelected.setOnClickListener(this);
        btnExportAll = (Button)findViewById(R.id.btnExportAll);
        btnExportAll.setOnClickListener(this);
        btnDeleteSelected = (Button)findViewById(R.id.btnDeleteSelected);
        btnDeleteSelected.setOnClickListener(this);
        btnDeleteAll = (Button)findViewById(R.id.btnDeleteAll);
        btnDeleteAll.setOnClickListener(this);
        btnEmailObservation = (Button)findViewById(R.id.btnEmailObservation);
        btnEmailObservation.setOnClickListener(this);
        btnEmailAll = (Button)findViewById(R.id.btnEmailAll);
        btnEmailAll.setOnClickListener(this);
        
        //drop down
        spnObservations = (Spinner)findViewById(R.id.spnObservations);
        spnObservations.setOnItemSelectedListener(this);
        
        tvNoSmokersCount = (TextView)findViewById(R.id.tvNoSmokersCount);
        tvAdultSmokerCount = (TextView)findViewById(R.id.tvAdultSmokerCount);
        tvAdultSmokerOthersCount = (TextView)findViewById(R.id.tvAdultSmokerOthersCount);
        tvChildSmokerCount = (TextView)findViewById(R.id.tvChildSmokerCount);
        total = (TextView)findViewById(R.id.total);
        
        updateContent();
    }
    
    /* Updates the spinner and counts. Used when an item is deleted
     * and when the activity is created
     */
    private void updateContent(){
        DatabaseHelper db = new DatabaseHelper(this);
        obs = db.getObservations();
        db.close();
        
        names = new String[obs.size()];
        ArrayAdapter <String> adapter = new ArrayAdapter <String> (this, android.R.layout.simple_spinner_item,names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnObservations.setAdapter(adapter);
        
        for(int i = 0;i < names.length;i++){
        	names[i] = DateFormat.format("dd MMMM, yyyy h:mmaa",obs.get(i).getStart()).toString();
        }
        
        if(names.length != 0){
        	updateCounts();
        }else{
        	spnObservations.setEnabled(false);
        	btnExportSelected.setEnabled(false);
        	btnExportAll.setEnabled(false);
        	btnDeleteSelected.setEnabled(false);
        	btnDeleteAll.setEnabled(false);
        	btnEmailObservation.setEnabled(false);
        	btnEmailAll.setEnabled(false);
        	
        	tvNoSmokersCount.setText("0");
        	tvAdultSmokerCount.setText("0");
        	tvAdultSmokerOthersCount.setText("0");
        	tvChildSmokerCount.setText("0");
        	total.setText("0");
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
		if(arg0 == btnExportSelected || arg0 == btnEmailObservation){
			//save selected to SD card
			if(checkedDirectory()){
				//write here
				int selectedId = spnObservations.getSelectedItemPosition();
				File outputFile = new File(Environment.getExternalStorageDirectory() + "/globalink/" + DateFormat.format("yyyyMMdd-hh-mmaa", obs.get(selectedId).getStart()).toString() + "-" + obs.get(selectedId).getId() + ".csv");
				try {
					FileWriter writer = new FileWriter(outputFile);
					String outputHeader = "latitude,longitude,start,finish,no smokers,other adults,single adult,child\n";
					String outputContent = obs.get(selectedId).getCSV();
					writer.append(outputHeader);
					writer.append(outputContent);
					writer.flush();
					writer.close();
					if(arg0 == btnEmailObservation){
						sendEmail(outputFile);
					}else{
						onCreateDialog(SUCCESS);
					}
				} catch (IOException e) {
					e.printStackTrace();
					onCreateDialog(FAIL);
				}
				
			}else{
				//error, do nothing. Error handled in checkedDirectory()
			}
			return;
		}

		
		if(arg0 == btnExportAll || arg0 == btnEmailAll){
			if(checkedDirectory()){
				//write here
				File outputFile = new File(Environment.getExternalStorageDirectory() + "/globalink/" + DateFormat.format("yyyyMMdd-hh-mmaa", System.currentTimeMillis()).toString() + "-all.csv");
				try {
					FileWriter writer = new FileWriter(outputFile);
					String outputHeader = "latitude,longitude,start,finish,no smokers,other adults,single adult,child\n";
					writer.append(outputHeader);
					for(int i = 0;i < obs.size();i++){
						writer.append(obs.get(i).getCSV());
					}				
					writer.flush();
					writer.close();
					if(arg0 == btnEmailAll){
						sendEmail(outputFile);
					}else{
						onCreateDialog(SUCCESS);
					}
				} catch (IOException e) {
					e.printStackTrace();
					onCreateDialog(FAIL);
				}
				
			}else{
				//error, do nothing. Error handled in checkedDirectory()
			}
			return;
		}
		
		if(arg0 == btnDeleteAll){
			onCreateDialog(DELETE_ALL);
			return;
		}
		
		if(arg0 == btnDeleteSelected){
			onCreateDialog(DELETE_SELECTED);
			return;
		}
	}

	private boolean checkedDirectory(){		
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			File globalinkDir = new File(Environment.getExternalStorageDirectory() + "/globalink/");
			//Log.i("FILE",globalinkDir.getAbsolutePath());
			if(!globalinkDir.isDirectory()){
				//create directory
				if(globalinkDir.mkdir()){
					return true;
				}else{
					//can't make dir
					//unknown error? read only?
					onCreateDialog(DIR_ERROR);
					return false;
				}
			}else{
				//directory exists
				return true;
			}
		}else{
			//SD card not ready or doesn't exist
			//show message here
			onCreateDialog(NO_SDCARD);
			return false;
		}
	}
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
		updateCounts();		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		switch (id) {
			case NO_SDCARD:				
				alert.setIcon(android.R.drawable.ic_dialog_alert);
				alert.setTitle(getString(R.string.nosd_title));
				alert.setMessage(getString(R.string.nosd_message));
				alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   
			           }
			       });
			    alert.show();
			    return null;
			case DIR_ERROR:
				alert.setIcon(android.R.drawable.ic_dialog_alert);
				alert.setTitle(getString(R.string.dir_error_title));
				alert.setMessage(getString(R.string.dir_error_message));
				alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   
			           }
			       });
			    alert.show();
			    return null;
			case SUCCESS:
				alert.setIcon(android.R.drawable.ic_dialog_info);
				alert.setTitle(getString(R.string.export_success_title));
				alert.setMessage(getString(R.string.export_success));
				alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   
			           }
			       });
			    alert.show();
			    return null;
			case FAIL:
				alert.setIcon(android.R.drawable.ic_dialog_alert);
				alert.setTitle(getString(R.string.unknown_error_title));
				alert.setMessage(getString(R.string.unknown_error_message));
				alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   
			           }
			       });
			    alert.show();
			    return null;
			case DELETE_ALL:
				alert.setIcon(android.R.drawable.ic_dialog_alert);
				alert.setTitle(getString(R.string.warning));
				alert.setMessage(getString(R.string.delete_warning_all));
				
				alert.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   DatabaseHelper db = new DatabaseHelper(ViewObservationsActivity.this);
			        	   for(int i = 0;i < obs.size();i++){
			        		   db.deleteObservationDeep(obs.get(i).getId());
			        	   }
			        	   db.close();
			        	   updateContent();
			           }
			       });
			    alert.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       });
			    alert.show();
			    return null;
			case DELETE_SELECTED:
				alert.setIcon(android.R.drawable.ic_dialog_alert);
				alert.setTitle(getString(R.string.warning));
				alert.setMessage(getString(R.string.delete_warning_all));
				
				alert.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   DatabaseHelper db = new DatabaseHelper(ViewObservationsActivity.this);
			        	   db.deleteObservationDeep(obs.get(spnObservations.getSelectedItemPosition()).getId());
			        	   db.close();
			        	   updateContent();
			           }
			       });
			    alert.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       });
			    alert.show();
			    return null;
			default:
				return null;
		}
	}

	
	private void sendEmail(File outbound){
		Intent sendFileIntent = new Intent(Intent.ACTION_SEND);
		sendFileIntent.setType("text/csv");
		sendFileIntent.putExtra(Intent.EXTRA_STREAM,Uri.parse("file://" + outbound.getAbsolutePath()));
		startActivity(Intent.createChooser(sendFileIntent, "Email:"));
	}
}
