package com.bigbluebox.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import com.mongodb.BasicDBObject;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * Parses a tree of text files. Produces:
 * <ul>
 * <li>noun phrases</li>
 * <li>named entity phrases and entity types</li>
 * <li>part of speech tagged words</li>
 * <li>noun-verb-object sequences (rough yet)</li>
 * <li>word frequency within the document (mentions per 1,000 words)</li>
 * <li>word frequency in corpus overall (mentions per 1,000 words)</li>
 * </ul>
 * 
 * Word frequency can be used to: identify skip-words (the, an, of, etc.),
 * identify which words are repeatedly mentioned in the corpus, identify which
 * documents are more relevant to that word than the average frequency (word
 * frequency in doc is greater than word frequency in corpus by some threshold
 * amount), and noun phrases can be used to fine tune keyword searching allowing
 * word sense disambiguation to some degree.
 * 
 * Noun phrases also make it easier to see a summary of what kinds of documents
 * are in the corpus, including things the user might not expect to search for
 * if they first had to enter text to search.
 * 
 * Parser supports random sampling across the entire corpus, so that the sample
 * is reasonably representative of the entire corpus while not taking the entire
 * processing time.
 * 
 */
public class App {
    public static Random random;
    // number of files after which to abort during debug
    static int DEBUG_STOP_AFTER = -1;

    // these two control the random sampling sample size
    static int TOTAL_NUM_FILES = 295000;
    static int NUM_FOR_SAMPLE = 1000;

    public static void main(String[] args) throws IOException {
	random = new Random(1); // same seed during development

	if (args.length < 6) {
	    System.out.println("Usage: java -jar server port dbname directoryPath numberForSampling nameForCorpus\n");
	} else {
	    MongoManager.server = args[0];
	    MongoManager.port = args[1];
	    MongoManager.dbName = args[2];

	    MongoManager manager = new MongoManager();
	    App app = new App();

	    Integer num = Integer.valueOf(args[4]);
	    if (num == -1) {
		NUM_FOR_SAMPLE = num;
	    }
	    
	    String corpusName = args[5];
	    
	    System.out.println("Looking for files inside " + new File(args[3]).getCanonicalPath()
		    + (NUM_FOR_SAMPLE == -1 ? " and processing all files " : " and processing at most " + NUM_FOR_SAMPLE + " files."));
	    app.start(args[3], corpusName);
	    System.out.println("\n\n-------- DONE ---------\n\n");
	}
    }

    public void start(String path, String corpusName) throws IOException {
	DirectoryWalker.basePath = path;

	// creates a StanfordCoreNLP object, with POS tagging, lemmatization,
	// named entity recognition, parsing, and coreference resolution
	Properties props = new Properties();
	// parse and dcoref are too slow.
	// props.put("annotators","tokenize, ssplit, pos, lemma, ner, parse, dcoref");
	props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
	StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	File dir = new File(path);
	DirectoryWalker walker = new DirectoryWalker(dir, pipeline, corpusName);
	walker.process();

	// ----- corpus level summary -------
	// @formatter:off
	/*
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
		wordSet.add(wordDBO);
	    }
	}

	BasicDBObject summaryStats = new BasicDBObject("name", "CorpusStats")
		.append("wordCountTotal", "" + Processor.corpusWordCount).append("count", 1)
		.append("corpusWordStems", wordSet);
	// TODO: Follow up named entities and noun phrases at the corpus level

	MongoManager.corpusStatisticsCollection.insert(summaryStats);
	System.out.println("CorpusStatisticsCollection contains "
		+ MongoManager.corpusStatisticsCollection.count() + " documents.");
	*/
	// @formatter:on
    }

}
