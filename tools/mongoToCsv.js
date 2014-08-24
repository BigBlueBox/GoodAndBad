var MongoClient = require('mongodb').MongoClient,
    fs = require('fs');

var outFile = fs.createWriteStream('out.csv', {flags:'w'});
outFile.write("text,count,mentionsPerThousand\n");

MongoClient.connect('mongodb://127.0.0.1:27017/dataset1', function(err, db) {
    if(err) throw err;

    db.collection('corpora').aggregate([
        {"$match":{corpus:"articles"}},
        {"$project": {_id:false, wordStems: true}},
        {"$unwind":"$wordStems"}
    ], {
        allowDiskUsage: true,
        cursor: {
            batchSize: 100
        }
    }).on('data', function(chunk) {
        var item = chunk.wordStems;
        outFile.write(item.text + "," + item.count + "," + item.mentionsPerThousand + "\n");
    }).on('end', function() {
        outFile.end();
        db.close();
        console.log("done");
    });
});