package com.bigbluebox.parser;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class MongoManager {
    public static String server;
    public static String port;
    public static String dbName;
    
    public static MongoClient mongoClient;
    public static DB mongoDb;

    public static DBCollection documentCollection;
    public static DBCollection corpusStatisticsCollection;

    public MongoManager() throws UnknownHostException {
	mongoClient = new MongoClient(server, Integer.valueOf(port));
	mongoDb = mongoClient.getDB(dbName);

	corpusStatisticsCollection = mongoDb.getCollection("corpusStatisticsCollection");
	DBObject obj = MongoManager.corpusStatisticsCollection.findOne();
	if (obj != null) { 
	    MongoManager.corpusStatisticsCollection.remove(obj);
	}
	
	documentCollection = mongoDb.getCollection("documentCollection");
	// clear it every time
	MongoManager.documentCollection.drop();
	documentCollection = mongoDb.getCollection("documentCollection");

    }

}
