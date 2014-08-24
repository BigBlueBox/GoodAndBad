//var $, jQuery, d3 = require('d3');
var dnode = require("dnode");
var shoe = require('shoe');

var streams = require('./streams.js');

var Model = require('scuttlebutt/model');

$.get('/templates.json', function(templates) {

});

jQuery(function() {
	var apiShoe = shoe('/dnode');
	var apiNode = dnode();
	apiNode.on('remote', function(remote) {
		remote.test("hello", function(a) {
			console.log("response", a);
		});
	});
	apiNode.pipe(apiShoe).pipe(apiNode);
});
