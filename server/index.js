var browserify = require('browserify-middleware');
var less = require('less');
var express = require('express');
var ecstatic = require('ecstatic');
var fs = require("fs");

var app = express();

var streams = require("./streams.js");

var api = require("./api.js");
var models = require("./models.js");

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

/*app.get('/templates.json', function(req, res) {
	utils.readJSONDir(__dirname + "/../app/templates", function(err, data) {
		res.end(JSON.stringify(data));
	});
});*/

app.get('/', function(req, res){
    res.render(__dirname + '/../templates/index.ejs');
});

app.use(ecstatic({root: __dirname + '/../public'}));

var listeningApp = app.listen(3000);

api.install(listeningApp);
streams.install(listeningApp);

console.log('Listening on port 3000');