package com.bigbluebox.parser;

import java.util.ArrayList;
import java.util.List;

public class NounVerbObject {
    List<String> nouns = new ArrayList<String>();
    List<String> verbs = new ArrayList<String>();
    List<String> objects = new ArrayList<String>();
    boolean seenPrepositionAfterVerb = false;

    public NounVerbObject() {
    }

    public void addPOS(String word, String pos) {
	if (verbs.size() > 0 && pos.equals("IN")) {
	    seenPrepositionAfterVerb = true;
    	} else if (pos.charAt(0) == 'N' && verbs.size() > 0 && !seenPrepositionAfterVerb) {
	    objects.add(word);
	} else if (pos.charAt(0) == 'N') {
	    nouns.add(word);
	}
	if (pos.charAt(0) == 'V') {
	    verbs.add(word);
	}
    }

    public List<String> getNouns() {
	return nouns;
    }

    public List<String> getVerbs() {
	return verbs;
    }

    public List<String> getObjects() {
	return objects;
    }

    public String toString() {
	StringBuffer nvo = new StringBuffer();
	for (String s : nouns) {
	    nvo.append(s);
	    nvo.append(" ");
	}
	for (String s : verbs) {
	    nvo.append(s);
	    nvo.append(" ");
	}
	for (String s : objects) {
	    nvo.append(s);
	    nvo.append(" ");
	}
	return nvo.toString().trim();
    }

}
