package com.bigbluebox.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * Hello world!
 * 
 */
public class App {
    public static Random random;
    static int DEBUG = 20;

    public static void main(String[] args) throws IOException {
	random = new Random(1); // same seed during development

	MongoManager manager = new MongoManager();
	App app = new App();
	if (args.length < 1) {
	    System.out.println("Usage: include a path directory name after the command.\n");
	} else {
	    app.start(args[0]);
	    System.out.println("\n\n-------- DONE ---------\n\n");
	}
    }

    public void start(String path) throws IOException {
	DirectoryWalker.basePath = path;
	
	// creates a StanfordCoreNLP object, with POS tagging, lemmatization,
	// named entity recognition, parsing, and coreference resolution
	Properties props = new Properties();
	// parse and dcoref are too slow.
	// props.put("annotators","tokenize, ssplit, pos, lemma, ner, parse, dcoref");
	props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
	StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	File dir = new File(path);
	DirectoryWalker walker = new DirectoryWalker(dir, pipeline);
	walker.process();

	System.out.println("\nGlobal word stem counts:");
	List<String> wordStems = new ArrayList<String>();
	wordStems.addAll(Processor.corpusWordStemCounts.keySet());
	Collections.sort(wordStems);

	List<BasicDBObject> wordSet = new ArrayList<BasicDBObject>();
	for (String wordStem : wordStems) {
	    Integer count = Processor.corpusWordStemCounts.get(wordStem);
	    if (count > 1) {
		float freq = (float) count * 1000 / (float) Processor.corpusWordCount;
		
		BasicDBObject wordDBO = new BasicDBObject("wordStem", wordStem);
		wordDBO.append("wordCount", count);
		wordDBO.append("wordFrequencyPerThousand", freq);
		
//		System.out.println(wordStem + " " + count + "/" + Processor.corpusWordCount + "="
//			+ freq
//			+ " mentions per thousand.");
		wordSet.add(wordDBO);
	    }
	}
	BasicDBObject summaryStats = new BasicDBObject("name", "CorpusStats")
	.append("wordCountTotal", "" + Processor.corpusWordCount).append("count", 1)
	.append("corpusWordStems", wordSet);
	// TODO: Follow up named entities and noun phrases at the corpus level

	MongoManager.corpusStatisticsCollection.insert(summaryStats);
	System.out.println("CorpusStatisticsCollection contains " +  MongoManager.corpusStatisticsCollection.count() + " documents.");

    }

}
