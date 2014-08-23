package com.bigbluebox.parser;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * Hello world!
 * 
 */
public class App {
	public static Random random;
	
	public static void main(String[] args)  throws IOException {
		random = new Random(1); // same seed during development
		
		App app = new App();
		if (args.length < 1) {
			System.out.println("Usage: include a path directory name after the command.\n");
		} else {
			app.start(args[0]);
			System.out.println("\n\n-------- DONE ---------\n\n");
		}
	}
	
	public void start(String path) throws IOException
	{
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization,
		// named entity recognition, parsing, and coreference resolution
		Properties props = new Properties();
		// parse and dcoref are too slow.
		//props.put("annotators","tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		props.put("annotators","tokenize, ssplit, pos, lemma, ner");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		
		File dir = new File(path);
		DirectoryWalker walker = new DirectoryWalker(dir, pipeline);
		walker.process();
	}

}
