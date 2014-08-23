package com.bigbluebox.parser;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * Hello world!
 * 
 */
public class App {
	public static void main(String[] args)  throws IOException {
		App app = new App();
		if (args.length < 1) {
			System.out.println("Usage: include a path directory name after the command.\n");
		} else {
			app.start(args[0]);
		}
	}
	
	public void start(String path) throws IOException
	{
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization,
		// named entity recognition, parsing, and coreference resolution
		Properties props = new Properties();
		//props.put("annotators","tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		props.put("annotators","tokenize, ssplit, pos, lemma, ner, parse");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);		
		
		File dir = new File(path);
		DirectoryWalker walker = new DirectoryWalker(dir, pipeline);
		walker.process();
	}

}
