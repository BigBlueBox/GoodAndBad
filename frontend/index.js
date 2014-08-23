var browserify = require('browserify-middleware'),
    less = require('less-middleware'),
    express = require('express');

var app = express();

app.get('/public', express.static(__dirname + '/public'));
app.get('/app.js', browserify('./client/js/app.js'));
app.get('/app.css', less('./client/css'), express.static('./client/css'));

app.get('/', function(req, res){
    res.render('index.ejs');
});

app.listen(3000);
console.log('Listening on port 3000');