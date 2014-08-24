var _ = require("underscore");
var MuxDemux = require("mux-demux");

var models = require("./models.js");

// On new client connection, this function will get called
function newStream(stream) {
	var mx = MuxDemux();
	models.each(function(model, key, list) {
		var muxed = mx.createStream(key);

		muxed.pipe(model.createStream()).pipe(muxed);
	});

	stream.pipe(mx).pipe(stream);
}

module.exports = {
	install: function(server) {
		var streamShoe = shoe(newStream);
		streamShoe.install(server, '/streams');

		return server;
	}
}