//var $, jQuery, d3 = require('d3');
//noinspection JSValidateTypes
//window.$ = window.jQuery = $ = jQuery = require('jquery/dist/jquery.js');
//require('bootstrap/dist/js/bootstrap.js');
var dnode = require("dnode");
var shoe = require('shoe');

var Model = require('scuttlebutt/model');

jQuery(function() {
	var a = new Model();
	window.model = a;

	var modelShoe = shoe('/model');
	modelShoe.pipe(a.createStream()).pipe(modelShoe);

	a.on('update', function(key, value, source) {
		console.log("Change:", key, " to:", value);
	});

	var apiShoe = shoe('/dnode');
	var apiNode = dnode();
	apiNode.on('remote', function(remote) {
		remote.test("hello", function(a) {
			console.log("response", a);
		});
	});
	apiNode.pipe(apiShoe).pipe(apiNode);
});
