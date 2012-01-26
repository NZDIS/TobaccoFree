/*
 * JavaScripts utilities for Tobacco Free. 
 * 
 */    


    var geocoder;
    var map;
    var infowindow = new google.maps.InfoWindow();
    var marker;

    google.maps.event.addDomListener(window, 'load', initialize);

    /*
    function find_locality(r) {
    	if (r.length > 0 )	{
		    for (i=0; i<r[0].address_components.length; i++) {
		    	for (j=0; j<r[0].address_components[i].types.length; j++) {
		    		if (r[0].address_components[i].types[j] == "locality") {	
		    			return r[0].address_components[i].long_name         	
		    		}
		    	}
		    }	
    	}
    	return "";
    }
    
    function find_country(r) {
    	if (r.length > 0 )	{
		    for (i=0; i<r[0].address_components.length; i++) {
		    	for (j=0; j<r[0].address_components[i].types.length; j++) {
		    		if (r[0].address_components[i].types[j] == "country") {	
		    			return r[0].address_components[i].long_name         	
		    		}
		    	}
		    }	
    	}
    	return "";
    }
    */
    
    function setup_marker(geocoder, o) {
    	latlng = new google.maps.LatLng(o.latitude, o.longitude);
        marker = new google.maps.Marker({
            position: latlng,
            map: map
        });
        google.maps.event.addListener(marker, 'click', function() {
			infowindow.setContent("<b>"
					+ Date(o.date) + "</b> <p> Duration: " + o.duration + "</p><p>"
					+ o.description + "<br>");
			infowindow.open(map, marker);  
      	});
    }
    
    function initialize() {
    	latlng = new google.maps.LatLng(40.730885,-73.997383);
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
    	/*
      
      var latlng = new google.maps.LatLng(40.730885,-73.997383);
      var myOptions = {
        zoom: 8,
        center: latlng,
        mapTypeId: google.maps.MapTypeId.ROADMAP
      }
      map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
      geocoder.geocode({'latLng': latlng}, function(results, status) {
          if (status == google.maps.GeocoderStatus.OK) {
        	  
            if (results[1]) {
              map.setZoom(11);
              marker = new google.maps.Marker({
                  position: latlng,
                  map: map
              });
              infowindow.setContent(results[1].formatted_address);
              
              var s = "";
              for (i=0; i<results.length; i++) {
            	  s += results[i].formatted_address + "\n";
            	  for (j=0; j<results[i].address_components.length; j++) {
            		s += "-> " + results[i].address_components[j].short_name
            		+ ", " + results[i].address_components[j].long_name;
            		for (k=0; k<results[i].address_components[j].types.length; k++) {
            			s += ": " + results[i].address_components[j].types[k] + "|";
            		}
            		s += "\n";
            	  }
              }
              alert(s);
              
              map.setZoom(11);
              marker = new google.maps.Marker({
                  position: latlng,
                  map: map
              });
              google.maps.event.addListener(marker, 'click', function() {
            	  infowindow.setContent("city: "+find_locality(results) + ", country: " + find_country(results));
                  infowindow.open(map, marker);  
            	});
              
          } else {
            alert("Geocoder failed due to: " + status);
          }
      });
    }*/
    }

