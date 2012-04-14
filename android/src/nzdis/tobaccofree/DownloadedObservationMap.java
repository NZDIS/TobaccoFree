/**
 * Copyright (C) 2011-2012 NZDIS.org. All Rights Reserved. See AUTHORS and LICENCE.
 */
package nzdis.tobaccofree;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * Displays downloaded observations on GoogleMaps
 * 
 * @author Hamish Medlin
 *
 * @version $Revision$ <br>
 * Created: Apr 2012
 */
public class DownloadedObservationMap extends MapActivity {

	private MapView mapView;
	private int before;
	private List<Overlay> mapOverlays;
	private DatabaseHelper db;
	private DownloadedObservationMapOverlay itemizedOverlay;
	private List<OverlayItem> cites,observations;
	private Timer delay;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_observation_map);
	    mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    
	    mapOverlays = mapView.getOverlays();
	    Drawable drawable = this.getResources().getDrawable(R.drawable.marker);
	    itemizedOverlay = new DownloadedObservationMapOverlay(drawable,this,true,mapView.getController());
	    
	    db = new DatabaseHelper(this);
	    cites = db.getDownloadedCityOverlays();
	    observations = db.getDownloadedCountryOverlays();
	    db.close();
	    
	    itemizedOverlay.addOverlays(cites);
	    mapOverlays.add(itemizedOverlay);
	    

	}
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	

	/**
	 * Used to check if the zoom has changed as there is no onZoomListener. If 
	 * the zoom level is 8 or greater then show all the points. If it is less 
	 * than 8 then only show 1 marker per city
	 */
	@Override
	public void onUserInteraction(){
		if(delay != null){
			delay.cancel();
		}
		before = mapView.getZoomLevel();
		delay = new Timer();
		
		delay.schedule(new TimerTask(){
			@Override
			public void run() {
				if(mapView.getZoomLevel() != before){
					if(mapView.getZoomLevel() >=8 && before < 8){
						mapOverlays.clear();
					    itemizedOverlay.addOverlays(observations);
					    itemizedOverlay.setCities(false);
					    mapOverlays.add(itemizedOverlay);
					}else if(mapView.getZoomLevel() < 8 && before >=8){
						mapOverlays.clear();
					    itemizedOverlay.addOverlays(cites);
					    itemizedOverlay.setCities(true);
					    mapOverlays.add(itemizedOverlay);
					}
				}
				
			}				
		}, 800);
		
	}


}
