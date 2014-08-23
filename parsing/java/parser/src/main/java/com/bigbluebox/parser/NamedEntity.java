package com.bigbluebox.parser;

public class NamedEntity {
	String type;
	String phrase = "";

	public NamedEntity(String type) {
		this.type = type;
	}
	
	public NamedEntity() {
	}

	public void append(String text) {
		phrase += " " + text;
		phrase = phrase.trim();
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPhrase() {
		return phrase;
	}

	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}

	
	
}
