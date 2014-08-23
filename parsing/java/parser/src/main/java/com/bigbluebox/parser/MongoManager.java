package com.bigbluebox.parser;

import java.net.UnknownHostException;
import java.util.ResourceBundle;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class MongoManager {

    public static MongoClient mongoClient;
    public static DB mongoDb; 
    
    public MongoManager() throws UnknownHostException {
	ResourceBundle rb = ResourceBundle.getBundle("mongo");
	mongoClient = new MongoClient(rb.getString("server"), Integer.valueOf(rb.getString("port")));
	mongoDb = mongoClient.getDB(rb.getString("dbname"));
    }

}
