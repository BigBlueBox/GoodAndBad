package com.bigbluebox.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * Processes a single file based on the text and path given; runs NLP on it, and
 * saves the results to MongoDB live along the way. Also keeps track of word
 * counts, named entities, noun phrases, and totals for the entire corpus, since
 * it's easy to calculate during the same pass.
 * 
 * Yep, it's ugly. No separation of concerns here. That'd be in a cleanup
 * revision.
 * 
 * @author jenny
 * 
 */
public class Processor {
    String canonicalPath;
    String text;
    StanfordCoreNLP pipeline;
    int documentWordCount = 0;

    Map<String, Integer> namedEntityCounts = new HashMap<String, Integer>();
    Map<String, NamedEntity> namedEntities = new HashMap<String, NamedEntity>();

    Map<String, Integer> nounPhraseCounts = new HashMap<String, Integer>();
    Map<String, NounPhrase> nounPhrases = new HashMap<String, NounPhrase>();

    Map<String, Integer> wordStemCounts = new HashMap<String, Integer>();

    static Pattern allpunctuation = Pattern.compile("^\\W+$");
    static Pattern digit = Pattern.compile("\\d");
    static List<Long> times = new ArrayList<Long>();
    static int fileCount = 0;

    static String pathSeparator = System.getProperty("file.separator");

    static Map<String, Integer> corpusWordStemCounts = new HashMap<String, Integer>();
    static int corpusWordCount = 0;

    // want the directory portion that came after the basePath
    private String getDirectoryPortion(String canonicalPath, String basePath) {
	File f = new File(canonicalPath);
	String filePlace;
	if (!f.isDirectory()) {
	    filePlace = f.getParent();
	} else {
	    filePlace = canonicalPath;
	}
	String path = filePlace.substring(basePath.length());
	if (path.indexOf("\\") != -1) {
	    path = path.replaceAll("\\\\", "/");
	}
	return path;
    }
    
    private String getFilePortion(String canonicalPath) {
	return new File(canonicalPath).getName();
    }

    public Processor(String text, String basePath, String canonicalPath, StanfordCoreNLP pipeline) {
	this.canonicalPath = canonicalPath;
	
	this.text = text;
	text = text.replaceAll("\\.", ". ");
	this.pipeline = pipeline;

	if (!canonicalPath.endsWith("txt")) {
	    // System.out.println("Skipping unsupported file format " +
	    // canonicalPath);
	    return;
	}
	if (!randomSample()) {
	    return;
	}

	String path = getDirectoryPortion(canonicalPath, basePath);
	String filename = getFilePortion(canonicalPath);

	try {
	    long timea = System.currentTimeMillis();
	    parseDocument(path, filename, text);
	    long timeb = System.currentTimeMillis();
	    System.out.println("----- " + canonicalPath + " ------ in " + (timeb - timea) + " ms.");
	    fileCount++;
	    times.add(new Long(timeb - timea));

	    // if (fileCount > 10) {
	    // average();
	    // System.exit(1);
	    // }

	} catch (ForeignDocumentException fde) {
	    System.out.println("---- Skipping " + canonicalPath + " because of foreign language.");
	}
    }

    public boolean randomSample() {
	int rand = (int) (App.random.nextInt(App.TOTAL_NUM_FILES / App.NUM_FOR_SAMPLE));
	return rand == 1;
    }

    public static void average() {
	int cnt = times.size();
	int sum = 0;
	for (Long l : times) {
	    sum += l;
	}
	System.out.println("Average time for " + cnt + " files = " + (sum / cnt) + " ms.");
    }

    // MongoDB Structure for a document.
// @formatter:off 
/*
    documentCollection: list of documents {
	  path=
	  filename=
	  wordCount=
	  sentenceCount=
	  sentences=list{
		sentenceNumber=
		words=list{
			word=
			wordStem=
			partOfSpeech=
		}
	  }
	  namedEntities=set{
		phrase=
		entityType=
		count=
	  }
	  nounPhrases=set{
		phrase=
		count=
	  }
	  wordStems=set{
		wordStem=
		count=
		mentionsPerThousand=
	  }
	}
*/    
    
// @formatter:on

    private void parseDocument(String path, String filename, String text) throws ForeignDocumentException {
	int foreignWords = 0;

	// create an empty Annotation just with the given text
	Annotation document = new Annotation(text);

	// run all Annotators on this text
	pipeline.annotate(document);

	BasicDBObject documentDBO = new BasicDBObject("path", path);
	documentDBO.append("filename", filename);
	if (path.contains("/")) {
	    documentDBO.append("corpus", path.substring(0, path.indexOf("/")));
	} else {
	    documentDBO.append("corpus", path);
	}

	List<BasicDBObject> sentenceDBOs = new ArrayList<BasicDBObject>();
	documentDBO.append("sentences", sentenceDBOs);

	// these are all the sentences in this document
	// a CoreMap is essentially a Map that uses class objects as keys and
	// has values with custom types
	List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	NamedEntity currentEntity = null;
	NounPhrase currentNounPhrase = null;

	int sentenceNumber = 0;
	for (CoreMap sentence : sentences) {
	    BasicDBObject sentenceDBO = new BasicDBObject("sentenceNumber", sentenceNumber);
	    sentenceDBOs.add(sentenceDBO);
	    List<BasicDBObject> wordDBOs = new ArrayList<BasicDBObject>();
	    sentenceDBO.append("words", wordDBOs);
	    
	    NounVerbObject nvo = new NounVerbObject();

	    // traversing the words in the current sentence
	    // a CoreLabel is a CoreMap with additional token-specific methods
	    for (CoreLabel token : sentence.get(TokensAnnotation.class)) {

		// this is the text of the token
		String word = token.get(TextAnnotation.class);
		if (word.indexOf(",") != -1) {
		    word = word.replaceAll(",", "");
		}
		if (word != null && word.length() > 0 && word.charAt(0) == '-') {
		    continue; // skip
		}
		if (word.equals("'s")) {
		    continue; // skip
		}
		
		BasicDBObject wordDBO = new BasicDBObject("word", word);
		wordDBOs.add(wordDBO);

		Matcher punc = allpunctuation.matcher(word);
		if (punc.matches()) {
		    continue; // skip an all-punctuation word
		}
		documentWordCount++;
		corpusWordCount++;

		String wordStem = token.get(LemmaAnnotation.class);
		Integer c = wordStemCounts.get(wordStem);
		if (c == null) {
		    c = 0;
		}
		wordStemCounts.put(wordStem, c + 1);

		c = corpusWordStemCounts.get(wordStem);
		if (c == null) {
		    c = 0;
		}
		corpusWordStemCounts.put(wordStem, c + 1);

		String pos = token.get(PartOfSpeechAnnotation.class);
		String namedEntityType = token.get(NamedEntityTagAnnotation.class);
		// TODO: Save all part-of-speech words to database
		// System.out.println(word + "\tpos " + pos + ":" +
		// PartOfSpeechLookup.getName(pos)
		// + "\tnamed entity " + namedEntityType);
		currentEntity = detectNEPhrase(currentEntity, word, namedEntityType);
		currentNounPhrase = detectNounPhrase(currentNounPhrase, word, pos);

		if (pos.equals("FW")) {
		    foreignWords++;
		}
		if (foreignWords > 10) {
		    throw new ForeignDocumentException();
		}
		wordDBO.append("text", wordStem);
		wordDBO.append("partOfSpeech", pos);
		
		nvo.addPOS(wordStem, pos);
	    }
	    sentenceDBO.append("nounVerbObject", nvo.toString());
	    // The following steps were too slow for practical use.

	    // this is the parse tree of the current sentence
	    // Tree tree = sentence.get(TreeAnnotation.class);
	    // System.out.println("Tree " + tree);

	    // this is the Stanford dependency graph of the current sentence
	    // SemanticGraph dependencies = sentence
	    // .get(CollapsedCCProcessedDependenciesAnnotation.class);
	    // System.out.println("SemanticGraph " + dependencies);

	    sentenceNumber++;

	}

	documentDBO.append("wordCount", documentWordCount);
	documentDBO.append("sentenceCount", sentences.size());

	// dont lose the last entity in a document
	if (currentEntity != null) {
	    Integer i = namedEntityCounts.get(currentEntity.getPhrase());
	    if (i == null) {
		i = 0;
	    }
	    namedEntityCounts.put(currentEntity.getPhrase(), i + 1);
	    currentEntity = null;
	}
	// also dont lose the last noun phrase
	if (currentNounPhrase != null) {
	    Integer i = nounPhraseCounts.get(currentNounPhrase.getPhraseJoined());
	    if (i == null) {
		i = 0;
	    }
	    nounPhraseCounts.put(currentNounPhrase.getPhraseJoined(), i + 1);
	    currentNounPhrase = null;
	}

	// Now persist all the cool stuff we discovered.
	List<BasicDBObject> namedEntityDBOs = new ArrayList<BasicDBObject>();
	List<BasicDBObject> nounPhraseDBOs = new ArrayList<BasicDBObject>();
	List<BasicDBObject> wordStemDBOs = new ArrayList<BasicDBObject>();
	documentDBO.append("namedEntities", namedEntityDBOs);
	documentDBO.append("nounPhrases", nounPhraseDBOs);
	documentDBO.append("wordStems", wordStemDBOs);

	for (NamedEntity entity : namedEntities.values()) {
	    BasicDBObject namedEntityDBO = new BasicDBObject("text", entity.getPhrase());
	    namedEntityDBO.append("entityType", entity.getType());
	    namedEntityDBO.append("count", namedEntityCounts.get(entity.getPhrase()));
	    namedEntityDBOs.add(namedEntityDBO);
	}
	for (NounPhrase nounPhrase : nounPhrases.values()) {
	    if (nounPhrase.isValid()) {
		BasicDBObject nounPhraseDBO = new BasicDBObject("text", nounPhrase.getPhrase());
		nounPhraseDBO.append("textPhrase", nounPhrase.getPhraseJoined());
		nounPhraseDBO.append("count", nounPhraseCounts.get(nounPhrase.getPhraseJoined()));
		nounPhraseDBOs.add(nounPhraseDBO);
	    }
	}

	for (String wordStem : wordStemCounts.keySet()) {
	    int count = wordStemCounts.get(wordStem);
	    BasicDBObject wordStemDBO = new BasicDBObject("text", wordStem);
	    wordStemDBO.append("count", count);
	    wordStemDBO.append("mentionsPerThousand",((float) count * 1000 / (float) documentWordCount) );
	    wordStemDBOs.add(wordStemDBO);	    
	}
	
	// Too slow.

	// This is the coreference link graph
	// Each chain stores a set of mentions that link to each other,
	// along with a method for getting the most representative mention
	// Both sentence and token offsets start at 1!
	// Map<Integer, CorefChain> graph =
	// document.get(CorefChainAnnotation.class);
	// for (Integer i : graph.keySet()) {
	// System.out.println(i + " - " + graph.get(i));
	// }

	// return graph;
	
	MongoManager.documentCollection.insert(documentDBO);
	
    }

    private NounPhrase detectNounPhrase(NounPhrase currentNounPhrase, String word, String pos) {
	if (!pos.equals("NN") && !pos.equals("NNS")) {
	    if (currentNounPhrase != null && !currentNounPhrase.getPhraseJoined().equals("")) {
		Integer i = nounPhraseCounts.get(currentNounPhrase.getPhraseJoined());
		if (i == null) {
		    i = 0;
		}
		nounPhraseCounts.put(currentNounPhrase.getPhraseJoined(), i + 1);
		nounPhrases.put(currentNounPhrase.getPhraseJoined(), currentNounPhrase);
		currentNounPhrase = null;
	    }
	} else if (pos.equals("NN") || pos.equals("NNS")) {
	    if (currentNounPhrase == null) {
		currentNounPhrase = new NounPhrase();
	    }
	    currentNounPhrase.append(word, pos);
	}
	return currentNounPhrase;

    }

    private NamedEntity detectNEPhrase(NamedEntity currentEntity, String word, String namedEntityType) {
	if (namedEntityType.equals("O")
		|| (currentEntity != null && !namedEntityType.equals(currentEntity.getType()))) {
	    if (currentEntity != null && !currentEntity.getPhrase().equals("")) {
		Integer i = namedEntityCounts.get(currentEntity.getPhrase());
		if (i == null) {
		    i = 0;
		}
		namedEntityCounts.put(currentEntity.getPhrase(), i + 1);
		namedEntities.put(currentEntity.getPhrase(), currentEntity);
		currentEntity = null;
	    }
	} else {
	    if (currentEntity == null) {
		currentEntity = new NamedEntity(namedEntityType);
	    }
	    currentEntity.append(word);
	}
	return currentEntity;
    }

}
