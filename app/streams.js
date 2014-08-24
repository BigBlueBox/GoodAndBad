var _ = require("underscore");
var MuxDemux = require("mux-demux");
var shoe = require('shoe');

var apiShoe = shoe('/streams');
var mx = MuxDemux();

apiShoe.pipe(mx).pipe(apiShoe);

window.mx = mx;

module.exports = {

};