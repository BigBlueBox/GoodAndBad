var browserify = require('browserify-middleware');
var less = require('less');
var express = require('express');
var ecstatic = require('ecstatic');
var fs = require("fs");

var shoe = require('shoe');
var dnode = require('dnode');

var app = express();

var Model = require('scuttlebutt/model')

/* 
	Handler our known static resources 
*/

app.get('/app.js', browserify(__dirname + '/../app/app.js'));

// less middleware kept breaking on me... sorry we only have one file to worry about so....
app.get('/app.css',  function(req, res) {
	fs.readFile(__dirname + "/../less/app.less", 'utf8', function(err, lessSrc) {
		//TODO: Handle errors.. n stuff...
		var parser = new(less.Parser)({
			paths: [__dirname + "/../less"],
			filename: 'app.less'
		});

		parser.parse(lessSrc, function (e, tree) {
			if (e) {
				res.end(e);
			} else {
	  			var output = tree.toCSS({
	    			// Minify CSS output
	    			compress: true
	  			});

	  			res.end(output);
			}
		});
	});
});

app.get('/', function(req, res){
    res.render('../templates/index.ejs');
});

/*
	Create socket stream and dnode api handler 
*/

var apiShoe = shoe(function (stream) {
    var d = dnode(require("./api.js"));
    d.pipe(stream).pipe(d);
});

var s = new Model();
var modelShoe = shoe(function(stream) {
	stream.pipe(s.createStream()).pipe(stream);
});

s.on('update', function(key, value, source) {
	console.log("Change:", key, " to:", value);
});

app.use(ecstatic({root: __dirname + '/../public'}));

var listeningApp = app.listen(3000);

apiShoe.install(listeningApp, '/dnode');
modelShoe.install(listeningApp, '/model');

console.log('Listening on port 3000');