package com.bigbluebox.parser;

import java.util.HashMap;
import java.util.Map;

public class PartOfSpeechLookup {
	static Map<String, String> codeToName = new HashMap<String, String>();
	static {
		codeToName.put("CC", "Coordinating conjunction");
		codeToName.put("CD", "Cardinal number");
		codeToName.put("DT", "Determiner");
		codeToName.put("EX", "Existential there");
		codeToName.put("FW", "Foreign word");
		codeToName.put("IN", "Preposition or subordinating conjunction");
		codeToName.put("JJ", "Adjective");
		codeToName.put("JJR", "Adjective, comparative");
		codeToName.put("JJS", "Adjective, superlative");
		codeToName.put("LS", "List item marker");
		codeToName.put("MD", "Modal");
		codeToName.put("NN", "Noun, singular or mass");
		codeToName.put("NNS", "Noun, plural");
		codeToName.put("NNP", "Proper noun, singular");
		codeToName.put("NNPS", "Proper noun, plural");
		codeToName.put("PDT", "Predeterminer");
		codeToName.put("POS", "Possessive ending");
		codeToName.put("PRP", "Personal pronoun");
		codeToName.put("PRP$", "Possessive pronoun");
		codeToName.put("RB", "Adverb");
		codeToName.put("RBR", "Adverb, comparative");
		codeToName.put("RBS", "Adverb, superlative");
		codeToName.put("RP", "Particle");
		codeToName.put("SYM", "Symbol");
		codeToName.put("TO", "to");
		codeToName.put("UH", "Interjection");
		codeToName.put("VB", "Verb, base form");
		codeToName.put("VBD", "Verb, past tense");
		codeToName.put("VBG", "Verb, gerund or present participle");
		codeToName.put("VBN", "Verb, past participle");
		codeToName.put("VBP", "Verb, non­3rd person singular present");
		codeToName.put("VBZ", "Verb, 3rd person singular present");
		codeToName.put("WDT", "Wh­determiner");
		codeToName.put("WP", "Wh­pronoun");
		codeToName.put("WP$", "Possessive wh­pronoun");
		codeToName.put("WRB", "Wh­adverb");

	}
	
	public static String getName(String code) {
		return codeToName.get(code);
	}

}
