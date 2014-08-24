var shoe = require('shoe');
var dnode = require('dnode');

var methods = {
    test: function (s, cb) {
        var res = s.replace(/[aeiou]{2,}/, 'oo').toUpperCase();
        cb(res);
    }
};

var apiShoe = shoe(function (stream) {
    var d = dnode(require("./api.js"));
    d.pipe(stream).pipe(d);
});

module.exports = {
	install: function(server) {
		apiShoe.install(server, "/api");
	}
}