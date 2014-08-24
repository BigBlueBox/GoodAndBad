var Model = require('scuttlebutt/model');
var _ = require("underscore");

var streams = require("./streams.js");

var models = {};

var methods = {
	each: function(cb) {

	},
	
	create: function(name) {
		models[name] = new Model();
		return models[name];
	},
}


module.exports = methods;