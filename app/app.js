//var $, jQuery, d3 = require('d3');
//noinspection JSValidateTypes
//window.$ = window.jQuery = $ = jQuery = require('jquery/dist/jquery.js');
//require('bootstrap/dist/js/bootstrap.js');
var dnode = require("dnode");
var shoe = require('shoe');

jQuery(function() {		
	var stream = shoe('/dnode');
	var d = dnode();

	d.on('remote', function(remote) {
		remote.test("hello", function(a) {
			console.log("response", a);
		});
	});
	d.pipe(stream).pipe(d);
});
