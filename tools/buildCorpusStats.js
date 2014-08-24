var MongoClient = require('mongodb').MongoClient;


MongoClient.connect('mongodb://127.0.0.1:27017/dataset1', function (err, db) {
    if (err) throw err;

    var inputCollection = db.collection('documents'),
        outputCollection = db.collection('corpora'),
    //TODO, configurable
        limitCorpus = {'corpus': ''};

    var aggregateIt = ["wordStems", "namedEntities", "nounPhrases"],
        aggregateCounts = ["wordCount"/*, "sentenceCount"*/];

    outputCollection.remove(limitCorpus, {w:1}, function(err) {
        if (err) throw err;

        function finalDone() {
            console.log("all done");
            db.close();
        }

        var runningAggs = aggregateIt.length + aggregateCounts.length + 1;
        function done() {
            if (--runningAggs === 0) {
                finalDone();
            }
        }
        for (var i = 0; i < aggregateIt.length; i++) {
            aggArray(aggregateIt[i], done);
        }
        for (i = 0; i < aggregateCounts.length; i++) {
            aggField(aggregateCounts[i], done);
        }
        done();
    });
    function aggField(field, cb) {
        console.log("update aggregate for", field);

        var projection = {_id: false,corpus:true};
        projection[field] = true;

        inputCollection.aggregate([
            {"$match":limitCorpus}, {"$project": projection}, {"$group":{_id:"$corpus",total:{"$sum":"$"+field}}}
        ], {
            allowDiskUsage: true,
            cursor: {
                batchSize: 100
            }
        }).on('data', function (chunk) {
            var pushData = {
                corpus: limitCorpus.corpus
            };
            pushData[field] = chunk.total;
            outputCollection.update({corpus: ""}, pushData, {upsert:true, w: 1}, function(err) {
                if (err) throw err;

                console.log("aggregate for", field, "updated to", chunk.total);
                cb();
            });
        });
    }
    function aggArray(field, cb) {
        console.log("update aggregate for", field);
        var runningInserts = 0,
            cursorExhaused = false;
        function done() {
            if (--runningInserts === 0 && cursorExhaused) {
                console.log("aggregate for", field, "updated");
                cb();
            }
        }
        var projection = {_id: false};
        projection[field] = true;
        inputCollection.aggregate([
            {"$match": limitCorpus},
            {"$project": projection},
            {"$unwind": "$" + field},
            {"$group": {_id: "$" + field + ".text", count: {"$sum": "$" + field + ".count"}}}
        ], {
            allowDiskUsage: true,
            cursor: {
                batchSize: 100
            }
        }).on('data', function (chunk) {
            runningInserts++;
            var pushData = {};
            pushData[field] = {
                text: chunk._id,
                count: chunk.count
            };
            outputCollection.update(limitCorpus, {"$push": pushData}, function(err) {
                if (err) throw err;
                done();
            });
        }).on('end', function() {
            cursorExhaused = true;
            runningInserts++;
            done();
        });
    }
});