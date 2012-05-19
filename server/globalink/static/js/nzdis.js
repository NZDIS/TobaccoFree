/*
 * JavaScripts utilities for Tobacco Free. 
 * 
 */    


    var geocoder;
    var map;

    google.load("visualization", "1", {packages:["corechart"]});
    google.maps.event.addDomListener(window, 'load', initialize);

    /**
     * Loads stats and draw some charts.
     */
	function drawStatsCharts() {
	      var jsonData_stats = $.ajax({
	          url: "/observation/stats",
	          dataType:"json",
	          async: false
	          }).responseText;
	      // Create our data table out of JSON data loaded from server.
	      var stats_data = new google.visualization.DataTable(jsonData_stats);
	      // Instantiate and draw our charts, passing in some options.
	      var options = {
	            title: 'Smoking in cars',
	            vAxis: {title: 'Country',  titleTextStyle: {color: '#0000FF'}},
	            hAxis: {title: 'Percentage [%]',  titleTextStyle: {color: '#0000FF'}},
	            legend: {position: 'top'}
	      };
	      var stats_chart = new google.visualization.BarChart(document.getElementById('stats_graph'), options);
	      if (stats_chart != null) {
	    	  stats_chart.draw(stats_data, options);
	      }
	}

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
    	drawStatsCharts();
    }

