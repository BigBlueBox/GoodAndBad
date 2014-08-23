package com.bigbluebox.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class Processor {
	String canonicalPath;
	String text;
	StanfordCoreNLP pipeline;
	
	static List<Long> times = new ArrayList<Long>();
	static int fileCount = 0;

	public Processor(String text, String canonicalPath, StanfordCoreNLP pipeline) {
		this.canonicalPath = canonicalPath;
		this.text = text;
		this.pipeline = pipeline;
		long timea = System.currentTimeMillis();
		Map<Integer, CorefChain> graph = parseDocument(text);
		long timeb = System.currentTimeMillis();
		System.out.println("----- " + canonicalPath + " ------ in " + (timeb - timea) + " ms.");
		fileCount++;
		times.add(new Long(timeb - timea));
		
		if (fileCount > 50) {
			average();
			System.exit(1);
		}
	}

	public static void average() {
		int cnt = times.size();
		int sum = 0;
		for (Long l : times) {
			sum += l;
		}
		System.out.println("Average time for " + cnt + " files = " + (sum / cnt) + " ms.");
	}

	private Map<Integer, CorefChain> parseDocument(String text) {
		// create an empty Annotation just with the given text
		Annotation document = new Annotation(text);

		// run all Annotators on this text
		pipeline.annotate(document);

		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and
		// has values with custom types
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		for (CoreMap sentence : sentences) {
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// this is the text of the token
				String word = token.get(TextAnnotation.class);
				// this is the POS tag of the token
				String pos = token.get(PartOfSpeechAnnotation.class);
				// this is the NER label of the token
				String namedEntity = token.get(NamedEntityTagAnnotation.class);
//				System.out.println(word + "\tpos " + pos + ":" + PartOfSpeechLookup.getName(pos) + "\tnamed entity " + namedEntity);
			}

			// this is the parse tree of the current sentence
			Tree tree = sentence.get(TreeAnnotation.class);
//			System.out.println("Tree " + tree);

			// this is the Stanford dependency graph of the current sentence
			SemanticGraph dependencies = sentence
					.get(CollapsedCCProcessedDependenciesAnnotation.class);
//			System.out.println("SemanticGraph " + dependencies);
		}

		// This is the coreference link graph
		// Each chain stores a set of mentions that link to each other,
		// along with a method for getting the most representative mention
		// Both sentence and token offsets start at 1!
		Map<Integer, CorefChain> graph = document.get(CorefChainAnnotation.class);
//		for (Integer i : graph.keySet()) {
//			System.out.println(i + " - " + graph.get(i));
//		}
		
		return graph;
	}	
	
}
