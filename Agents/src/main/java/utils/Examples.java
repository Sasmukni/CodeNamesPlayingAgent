package utils;


import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.IndexWordSet;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.data.list.PointerTargetTree;
import net.sf.extjwnl.data.relationship.AsymmetricRelationship;
import net.sf.extjwnl.data.relationship.Relationship;
import net.sf.extjwnl.data.relationship.RelationshipFinder;
import net.sf.extjwnl.data.relationship.RelationshipList;
import net.sf.extjwnl.dictionary.Dictionary;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class to demonstrate the functionality of the library.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class Examples {

    private static final String USAGE = "Usage: Examples [properties file]";
    private static final Set<String> HELP_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "--help", "-help", "/help", "--?", "-?", "?", "/?"
    )));

    public static void main(String[] args) throws FileNotFoundException, JWNLException, CloneNotSupportedException {
        Dictionary dictionary = null;
        if (args.length != 1) {
            dictionary = Dictionary.getDefaultResourceInstance();
        } else {
            if (HELP_KEYS.contains(args[0])) {
                System.out.println(USAGE);
            } else {
                FileInputStream inputStream = new FileInputStream(args[0]);
                dictionary = Dictionary.getInstance(inputStream);
            }
        }

        if (null != dictionary) {
            new Examples(dictionary).go();
        }
    }

    private IndexWord ACCOMPLISH;
    private IndexWord DOG;
    private IndexWord CAT;
    private IndexWord FUNNY;
    private IndexWord DROLL;
    private final static String MORPH_PHRASE = "running-away";
    private final Dictionary dictionary;

    public Examples(Dictionary dictionary) throws JWNLException {
        this.dictionary = dictionary;
        ACCOMPLISH = dictionary.getIndexWord(POS.VERB, "accomplish");
        DOG = dictionary.getIndexWord(POS.NOUN, "dog");
        CAT = dictionary.lookupIndexWord(POS.NOUN, "cat");
        FUNNY = dictionary.lookupIndexWord(POS.ADJECTIVE, "funny");
        DROLL = dictionary.lookupIndexWord(POS.ADJECTIVE, "droll");
    }

    public void go() throws JWNLException, CloneNotSupportedException, FileNotFoundException {
        //demonstrateMorphologicalAnalysis(MORPH_PHRASE);
    	//demonstrateListOperation(ACCOMPLISH);
    	//demonstrateTreeOperation(DOG);
    	//demonstrateAsymmetricRelationshipOperation(dictionary.lookupIndexWord(POS.NOUN, "boxer"), dictionary.lookupIndexWord(POS.NOUN, "ring"));
    	//demonstrateSymmetricRelationshipOperation(FUNNY, DROLL);
    	//dictionary. Collegiate
    	exists("Collegiate");WordNetSearch.initialize();
    	//WordNetSearch wns = new WordNetSearch();
    	System.out.println("Best Relationship between Author and Novel");
    	//WordNetSearch.getBestRelationshipDepth("Author", "Novel");
    	//System.out.println("Best Relationship between Wizard and Magic");
    	//WordNetSearch.getBestRelationshipDepth("Wizard", "Magic");
    	//System.out.println("Best Relationship between Car and Engine");
    	//WordNetSearch.getBestRelationshipDepth("Car", "Engine");
        searchTroughAllRelationships(dictionary.lookupIndexWord(POS.NOUN, "Author"), dictionary.lookupIndexWord(POS.NOUN, "Novel"));
    }

    private void demonstrateMorphologicalAnalysis(String phrase) throws JWNLException {
        // "running-away" is kind of a hard case because it involves
        // two words that are joined by a hyphen, and one of the words
        // is not stemmed. So we have to both remove the hyphen and stem
        // "running" before we get to an entry that is in WordNet
        System.out.println("Base form for \"" + phrase + "\": " +
                dictionary.lookupIndexWord(POS.VERB, phrase));
    }

    private void demonstrateListOperation(IndexWord word) throws JWNLException {
        // Get all of the hypernyms (parents) of the first sense of <var>word</var>
        PointerTargetNodeList hypernyms = PointerUtils.getDirectHypernyms(word.getSenses().get(0));
        System.out.println("Direct hypernyms of \"" + word.getLemma() + "\":");
        hypernyms.print();
    }

    private void demonstrateTreeOperation(IndexWord word) throws JWNLException {
        // Get all the hyponyms (children) of the first sense of <var>word</var>
        PointerTargetTree hyponyms = PointerUtils.getHyponymTree(word.getSenses().get(0));
        System.out.println("Hyponyms of \"" + word.getLemma() + "\":");
        hyponyms.print();
    }

    private void demonstrateAsymmetricRelationshipOperation(IndexWord start, IndexWord end) throws JWNLException, CloneNotSupportedException {
        // Try to find a relationship between the first sense of <var>start</var> and the first sense of <var>end</var>
        RelationshipList list = RelationshipFinder.findRelationships(start.getSenses().get(0), end.getSenses().get(0), PointerType.HYPERNYM);
        System.out.println("Hypernym relationship between \"" + start.getLemma() + "\" and \"" + end.getLemma() + "\":");
        for (Object aList : list) {
            ((Relationship) aList).getNodeList().print();
        }
        System.out.println("Common Parent Index: " + ((AsymmetricRelationship) list.get(0)).getCommonParentIndex());
        System.out.println("Depth: " + list.get(0).getDepth());
    }

    private void demonstrateSymmetricRelationshipOperation(IndexWord start, IndexWord end) throws JWNLException, CloneNotSupportedException {
        // find all synonyms that <var>start</var> and <var>end</var> have in common
        RelationshipList list = RelationshipFinder.findRelationships(start.getSenses().get(0), end.getSenses().get(0), PointerType.SIMILAR_TO);
        System.out.println("Synonym relationship between \"" + start.getLemma() + "\" and \"" + end.getLemma() + "\":");
        for (Object aList : list) {
            ((Relationship) aList).getNodeList().print();
        }
        System.out.println("Depth: " + list.get(0).getDepth());
    }
    
    
    private void searchTroughAllRelationships(IndexWord start, IndexWord end)  throws JWNLException, CloneNotSupportedException {
        // Try to find a relationship between the first sense of <var>start</var> and the first sense of <var>end</var>
        List<PointerType> allTypes = Collections.unmodifiableList(Arrays.asList(
        		PointerType.ANTONYM, PointerType.HYPERNYM, PointerType.HYPONYM, PointerType.ENTAILMENT, 
        		PointerType.SIMILAR_TO, PointerType.MEMBER_HOLONYM, PointerType.SUBSTANCE_HOLONYM,
        		PointerType.PART_HOLONYM, PointerType.MEMBER_MERONYM, PointerType.SUBSTANCE_MERONYM, 
        		PointerType.PART_MERONYM,
        		PointerType.CAUSE, PointerType.PARTICIPLE_OF, PointerType.PERTAINYM, 
        		PointerType.ATTRIBUTE, PointerType.VERB_GROUP, PointerType.DERIVATION,
        		PointerType.CATEGORY, PointerType.USAGE, PointerType.REGION, PointerType.CATEGORY_MEMBER, PointerType.USAGE_MEMBER, 
        		PointerType.REGION_MEMBER,
        		PointerType.INSTANCE_HYPERNYM, PointerType.INSTANCES_HYPONYM
        ));// PointerType.SEE_ALSO,
        List<Synset> startSynsets = start.getSenses();
        List<Synset> endSynsets = end.getSenses();
    	for(PointerType pt : allTypes) {
    		System.out.println(pt.name() + " relationship between \"" + start.getLemma() + "\" and \"" + end.getLemma() + "\":");
	        int depth = 0;
    		for(Synset es: endSynsets) {
		        for(Synset ss: startSynsets) {
		    		RelationshipList list = RelationshipFinder.findRelationships(ss, es, pt);
		    		if(list.size() == 0) {
			        	continue;
			        }
			        /*for (Object aList : list) {
			            ((Relationship) aList).getNodeList().print();
			        }*/
		    		//get the relationship with the best(lower) depth, from the list
		    		Relationship best = list.get(0);
		    		int bestDepth = Integer.MAX_VALUE;
		    		for(Relationship r: list) {
		    			if(r.getDepth()< bestDepth) {
		    				bestDepth = r.getDepth();
		    				best = r;
		    			}
		    		}
			        System.out.println("Common Parent Index: " + ((AsymmetricRelationship) best).getCommonParentIndex());
			        System.out.println("Depth: " + best.getDepth());
			        depth = bestDepth;
			        if(((AsymmetricRelationship) best).getCommonParentIndex()<=5)
			        	best.getNodeList().print();
		    	}
    		}
    		if(depth == 0)
	        	System.out.println("No relationships found!");
        }
    } 
    private boolean exists(String word) 
			throws FileNotFoundException, JWNLException, CloneNotSupportedException {
		IndexWordSet s = dictionary.lookupAllIndexWords(word);
		return s.size() > 0;
	} 
}