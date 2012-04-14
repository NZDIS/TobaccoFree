/**
 * Copyright (C) 2011-2012 NZDIS.org. All Rights Reserved. See AUTHORS and LICENCE.
 */
package nzdis.tobaccofree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * Handles the overlay for GoogleMap and 'tapping' of points on the map
 * 
 * @author Hamish Medlin
 *
 * @version $Revision$ <br>
 * Created: Apr 2012
 */
public class DownloadedObservationMapOverlay extends ItemizedOverlay {

	private List<OverlayItem> overlays = new ArrayList<OverlayItem>();
	private Context con;
	private Dialog dialog;
	private boolean cities;
	private MapController mapController;
	
	public DownloadedObservationMapOverlay(Drawable defaultMarker,Context con,MapController mapController){
		super(boundCenterBottom(defaultMarker));
		this.con = con;
		this.cities = false;
	}
	
	public DownloadedObservationMapOverlay(Drawable defaultMarker,Context con,boolean cities,MapController mapController){
		super(boundCenterBottom(defaultMarker));
		this.con = con;
		this.cities = cities;
		this.mapController = mapController;
	}
	
	public void addOverlay(OverlayItem overlay){
		overlays.add(overlay);
	}
	
	public void setCities(boolean cities){
		this.cities = cities;
	}
	
	@Override
	protected OverlayItem createItem(int arg0) {
		return overlays.get(arg0);		
	}

	@Override
	public int size() {
		return overlays.size();
	}
	
	@Override
	protected boolean onTap(int index) {
		if(cities){
			//zoom into city
			DatabaseHelper db = new DatabaseHelper(con);
			DownloadedObservation item = db.getDownloadedObservationFromId(overlays.get(index).getTitle());
			db.close();
			mapController.setZoom(9);
			mapController.animateTo(item.getGeoPoint());
			
		}else{
			//show observation stats
			DatabaseHelper db = new DatabaseHelper(con);
			DownloadedObservation item = db.getDownloadedObservationFromId(overlays.get(index).getTitle());
			db.close();
			dialog = new DownloadObservationMapDialog(con,item);
			dialog.show();
		}
		return true;
	}

	public void addOverlays(List<OverlayItem> downloadedCountryOverlays) {
		overlays = downloadedCountryOverlays;
		populate();
	}


}
