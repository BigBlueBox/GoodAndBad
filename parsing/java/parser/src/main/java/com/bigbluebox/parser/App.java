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
 * <li>part of speech tagged words</li>
 * <li>noun-verb-object sequences</li>
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
 * are in the corpus, including things the user might not expect to search for if
 * they first had to enter text to search. 
 * 
 * Parser supports random sampling across the entire corpus, so that the sample is 
 * reasonably representative of the entire corpus while not taking the entire processing
 * time.
 * 
 */
public class App {
    public static Random random;
    static int DEBUG = 200;

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

	// ----- corpus level summary -------
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

    }

}
