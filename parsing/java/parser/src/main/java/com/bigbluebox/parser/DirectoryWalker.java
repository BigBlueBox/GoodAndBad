package com.bigbluebox.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class DirectoryWalker {
    static String basePath;
    
    File directory;
    StanfordCoreNLP pipeline;
    String corpusName;

    public DirectoryWalker(File directory, StanfordCoreNLP pipeline, String corpusName) {
	this.directory = directory;
	this.pipeline = pipeline;
	this.corpusName = corpusName;
    }

    public void setSource(File file) throws IOException {
	directory = file;
    }

    /**
     * For the specified directory, iterates the files, and makes recursive call
     * for any directories
     */
    public void process() throws IOException {
	File[] files = directory.listFiles();
	if (files == null) {
	    return;
	}
	for (File file : files) {
	    if (file.isDirectory()) {
		DirectoryWalker walker = new DirectoryWalker(file, pipeline, corpusName);
		walker.process();
	    } else {
		long size = file.length();
		if (size > 1024 * 1024 * 30) {
		    System.out.println("Excessive file size " + file.getCanonicalPath() + " of " + size
			    + ", skipping.");
		    continue;
		}

		BufferedReader fr = new BufferedReader(new FileReader(file));
		StringBuffer text = new StringBuffer();
		String line;
		while ((line = fr.readLine()) != null) {
		    text.append(line);
		}
		fr.close();
		Processor processor = new Processor(text.toString(), basePath, file.getCanonicalPath(), pipeline, corpusName);
		if (App.DEBUG_STOP_AFTER != -1 && Processor.fileCount > App.DEBUG_STOP_AFTER) {
		    return;
		}
	    }
	}

    }

}
