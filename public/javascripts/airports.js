$(function() {
	// Initially, loading image is hidden
	$("#loading-airports-img").hide();
	
	$("#country").change(function () {
		// Show loading image
		$("#loading-airports-img").show();

		$("#iataCode").empty();
		var selectedCountry = $(this).val();
		
		$.getJSON(
			"/GetAirportInformationByCountry/" + selectedCountry,
			function (data) {
		    	$.each(data, function(i, value) {
					$("#iataCode").append(
							'<option value="' 
							+ data[i].iata_code
							+ '">'
							+ data[i].airport_name
							+ '</option>');
				});
			}
		).done(function() {
			// Hide loading image
			$("#loading-airports-img").hide();
		});
	});
});

//Former get on JSON file

/*$.getJSON("/assets/json/city_airport_codes.json", function(data) {	
	$.each(data, function(i, value) {
		if (data[i].country == ' ' + selectedCountry) {
			$("#iataCode").append(
					'<option value="' 
					+ data[i].iata_code 
					+ '">'
					+ data[i].complete_location
					+ '</option>');
		}
	});
});*/