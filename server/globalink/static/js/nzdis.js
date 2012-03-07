/*
 * JavaScripts utilities for Tobacco Free. 
 * 
 */    


    var geocoder;
    var map;

    google.maps.event.addDomListener(window, 'load', initialize);


    /**
     * Setup a new maker for a given observation
     * @param geocoder current geocoder
     * @param o observation
     */
    function setup_marker(geocoder, o) {
    	var latlng = new google.maps.LatLng(o.latitude, o.longitude);
        var marker = new google.maps.Marker({
            position: latlng,
            map: map
        });
        google.maps.event.addListener(marker, 'click', function() {
        	var infowindow = new google.maps.InfoWindow();
			infowindow.setContent("<b>"
					+ new Date(o.date) + "</b> <p> Duration: " + o.duration + "</p><p>"
					+ o.description + "<br>");
			infowindow.open(map, marker);  
      	});
    }

    /**
     * Setup a map with a random location first. Then pan to last
     * recorded observation when the json data comes back.
     */
    function initialize() {
    	var latlng = new google.maps.LatLng(40.730885,-73.997383);
    	var myOptions = {
    	        zoom: 6,
    	        center: latlng,
    	        mapTypeId: google.maps.MapTypeId.ROADMAP
    	      }
		map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
    	$.get('/observation/all', function(data) {
    		geocoder = new google.maps.Geocoder();
  			for(i=0; i<data.length; i++) {
  				setup_marker(geocoder, data[i]);
  			}
  			o = data[data.length - 1];
  			latlng = new google.maps.LatLng(o.latitude, o.longitude);
  			map.panTo(latlng);
  			map.setZoom(8);
    	});

    }

