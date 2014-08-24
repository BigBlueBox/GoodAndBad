package com.bigbluebox.parser;

import java.net.UnknownHostException;
import java.util.ResourceBundle;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class MongoManager {

    public static MongoClient mongoClient;
    public static DB mongoDb;

    public static DBCollection documentCollection;
    public static DBCollection corpusStatisticsCollection;

    public MongoManager() throws UnknownHostException {
	ResourceBundle rb = ResourceBundle.getBundle("mongo");
	mongoClient = new MongoClient(rb.getString("server"), Integer.valueOf(rb.getString("port")));
	mongoDb = mongoClient.getDB(rb.getString("dbname"));

	corpusStatisticsCollection = mongoDb.getCollection("corpusStatisticsCollection");
	documentCollection = mongoDb.getCollection("documentCollection");
	

    }

}
