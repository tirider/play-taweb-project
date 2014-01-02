$(function() {
	$.getJSON("/assets/json/countries.json", function(data) {	
		$.each(data, function(i, value) {
			$("#country").append(
					'<option value="' 
					+ data[i].name
					+ '">'
					+ data[i].name
					+ '</option>');	
		});
	});
});