package com.bigbluebox.parser;

import java.util.ArrayList;
import java.util.List;

public class NounPhrase {
    List<String> partsOfSpeech = new ArrayList<String>();
    List<String> words = new ArrayList<String>();

    public NounPhrase() {
    }

    public void append(String text, String pos) {
	words.add(text);
	partsOfSpeech.add(pos);
    }

    public String getPhrase() {
	return join(words);
    }
    
    private String join(List<String> words) {
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < words.size(); i++) {
	    sb.append(words.get(i));
	    if (i + 1 < words.size()) {
		sb.append(" ");
	    }
	}
	return sb.toString();
    }

    public boolean isValid() {
	// if we end in an adjective, remove it; we only keep adjectives followed by more nouns.
	if(partsOfSpeech.get(partsOfSpeech.size() - 1).equals("JJ")) {
	    partsOfSpeech.remove(partsOfSpeech.size() - 1);
	    words.remove(words.size() - 1);
	}
	
	if (words.size() > 1) {
	    return true;
	}
	return false;
    }
    
    
    /*
    public static void main(String args[]) {
	// test case for trailing adjective removal
	NounPhrase np = new NounPhrase();
	np.append("mynoun1", "NN");
	np.append("mynoun2", "NN");
	np.append("adj", "JJ");
	np.append("mynoun3", "NN");
	System.out.println("Expected valid for 1 " + np.isValid() + " with " + np.getPhrase());
	
	
	NounPhrase np2 = new NounPhrase();
	np2.append("mynoun1", "NN");
	np2.append("adj", "JJ");

	System.out.println("Expected invalid for 2 " + np2.isValid() + " with " + np2.getPhrase());
	
    }
    */
    
}
