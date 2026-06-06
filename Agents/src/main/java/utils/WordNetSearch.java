package utils;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.IndexWordSet;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.relationship.AsymmetricRelationship;
import net.sf.extjwnl.data.relationship.Relationship;
import net.sf.extjwnl.data.relationship.RelationshipFinder;
import net.sf.extjwnl.data.relationship.RelationshipList;
import net.sf.extjwnl.dictionary.Dictionary;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.deeplearning4j.text.stopwords.StopWords;

public abstract class WordNetSearch {
	private static Dictionary dictionary;
	private static List<PointerType> allTypes = Collections.unmodifiableList(Arrays.asList(PointerType.ANTONYM,
			PointerType.HYPERNYM, PointerType.HYPONYM, PointerType.ENTAILMENT, PointerType.SIMILAR_TO,
			PointerType.MEMBER_HOLONYM, PointerType.SUBSTANCE_HOLONYM, PointerType.PART_HOLONYM,
			PointerType.MEMBER_MERONYM, PointerType.SUBSTANCE_MERONYM, PointerType.PART_MERONYM, PointerType.CAUSE,
			PointerType.PARTICIPLE_OF, PointerType.PERTAINYM, PointerType.ATTRIBUTE, PointerType.VERB_GROUP,
			PointerType.DERIVATION, PointerType.CATEGORY, PointerType.USAGE, PointerType.REGION,
			PointerType.CATEGORY_MEMBER, PointerType.USAGE_MEMBER, PointerType.REGION_MEMBER,
			PointerType.INSTANCE_HYPERNYM, PointerType.INSTANCES_HYPONYM)); //
//, PointerType.SEE_ALSO
	public static void initialize() throws FileNotFoundException, JWNLException, CloneNotSupportedException {
		dictionary = Dictionary.getDefaultResourceInstance();
	}
	/*public WordNetSearch() throws FileNotFoundException, JWNLException, CloneNotSupportedException {
		dictionary = Dictionary.getDefaultResourceInstance();
	}*/
	public static int getBestRelationshipDepth(String start, String end)
			throws FileNotFoundException, JWNLException, CloneNotSupportedException {
		IndexWordSet s = dictionary.lookupAllIndexWords(start); // the start comes from the Oracle (or a previous
																// iteration??), it could be any POS
		IndexWord e = dictionary.lookupIndexWord(POS.NOUN, end); // the end is from the Board, so it has to be a noun
		List<Synset> endSynsets = e.getSenses();
		int bestDepth = Integer.MAX_VALUE;
		Relationship best = null;
		for (IndexWord st : s.getIndexWordArray()) {
			List<Synset> startSynsets = st.getSenses();
			for (PointerType pt : allTypes) {
				for (Synset es : endSynsets) {
					for (Synset ss : startSynsets) {
						RelationshipList list = RelationshipFinder.findRelationships(ss, es, pt);
						if (list.size() == 0) {
							continue;
						}
						// get the relationship with the best(lower) depth, from the list
						for (Relationship r : list) {
							if (r.getDepth() < bestDepth) {
								bestDepth = r.getDepth();
								best = r;
							}
						}
					}
				}
			}
		}
		return bestDepth;
	}

	public static int getBestFlexibleRelationshipDepth(String start, String end) 
			throws FileNotFoundException, JWNLException, CloneNotSupportedException {
		int bestDepth = Integer.MAX_VALUE; // maxDepth?
		// Starting from the starting word explore the graph until end is found, or
		// maxDepth is hit
		IndexWordSet s = dictionary.lookupAllIndexWords(start); // the start comes from the Oracle (or a previous
		// iteration??), it could be any POS
		IndexWord e = dictionary.lookupIndexWord(POS.NOUN, end); // the end is from the Board, so it has to be a noun
		List<Synset> endSynsets = e.getSenses();		
		Relationship best = null;
		for (IndexWord st : s.getIndexWordArray()) {
			List<Synset> startSynsets = st.getSenses();
			for (PointerType pt : allTypes) {
				for (Synset es : endSynsets) {
					//for each Synset of end check if the start word (stemmed) is in the gloss // not enough! Also kinda stupid?
					String endGloss = es.getGloss();
					List<String> stemmedGloss = new ArrayList<>();
					for(String stemmedWord : endGloss.split("\\s")){
						stemmedGloss.add(stemmedWord);
					}
					String stemmedStart = Word2VecUser.stem(start);
					if(stemmedGloss.contains(stemmedStart)) {
						System.out.println("YEAH, it happened, with the words " + start + " and " + end + " the gloss of the second word was: " + endGloss);
						bestDepth = 1;
						break;
					}
					for (Synset ss : startSynsets) {
						String startGloss = ss.getGloss();
						stemmedGloss = new ArrayList<>();
						for(String stemmedWord : startGloss.split("\\s")){
							stemmedGloss.add(stemmedWord);
						}
						String stemmedEnd = Word2VecUser.stem(end); 
						if(stemmedGloss.contains(" "+stemmedEnd)) {
							System.out.println("YEAH, it happened, with the words " + end + " and " + start + " the gloss of the second word was: " + startGloss);
							bestDepth = 1;
							break;
						}
						RelationshipList list = RelationshipFinder.findRelationships(ss, es, pt);
						if (list.size() == 0) {
							continue;
						}
						// get the relationship with the best(lower) depth, from the list
						for (Relationship r : list) {
							if (r.getDepth() < bestDepth) {
								bestDepth = r.getDepth();
								best = r;
							}
						}
						// if(((AsymmetricRelationship) best).getCommonParentIndex()<=5)
						// best.getNodeList().print();
					}
				}
			}
		}
		/*
		if(best != null && bestDepth < 6)
			best.getNodeList().print(); // for each couple of words we print the best relationship
			*/
		return bestDepth;
	}

	public static int getBestFlexibleRelationshipDepthAllPOS(String start, String end) 
			throws FileNotFoundException, JWNLException, CloneNotSupportedException {
		int bestDepth = Integer.MAX_VALUE; // maxDepth?
		// Starting from the starting word explore the graph until end is found, or
		// maxDepth is hit
		IndexWordSet s = dictionary.lookupAllIndexWords(start); // the start comes from the Oracle (or a previous
		// iteration??), it could be any POS
		IndexWordSet eall =dictionary.lookupAllIndexWords(end);  // the end is from the Board, so it should be a noun, but the Oracle doesn't know this constraint!!!
		//IndexWord e = dictionary.lookupIndexWord(POS.NOUN, end);
		Relationship best = null;
		for(IndexWord e : eall.getIndexWordArray()){
			List<Synset> endSynsets = e.getSenses();		
			for (IndexWord st : s.getIndexWordArray()) {
				List<Synset> startSynsets = st.getSenses();
				for (PointerType pt : allTypes) {
					for (Synset es : endSynsets) {
						//for each Synset of end check if the start word (stemmed) is in the gloss // not enough! Also kinda stupid?
						String endGloss = es.getGloss();
						List<String> stemmedGloss = new ArrayList<>();
						for(String stemmedWord : endGloss.split("\\s")){
							stemmedGloss.add(stemmedWord);
						}
						String stemmedStart = Word2VecUser.stem(start);
						if(stemmedGloss.contains(stemmedStart)) {
							System.out.println("YEAH, it happened, with the words " + start + " and " + end + " the gloss of the second word was: " + endGloss);
							bestDepth = 1;
							break;
						}
						for (Synset ss : startSynsets) {
							String startGloss = ss.getGloss();
							stemmedGloss = new ArrayList<>();
							for(String stemmedWord : startGloss.split("\\s")){
								stemmedGloss.add(stemmedWord);
							}
							String stemmedEnd = Word2VecUser.stem(end); 
							if(stemmedGloss.contains(" "+stemmedEnd)) {
								System.out.println("YEAH, it happened, with the words " + end + " and " + start + " the gloss of the second word was: " + startGloss);
								bestDepth = 1;
								break;
							}
							RelationshipList list = RelationshipFinder.findRelationships(ss, es, pt);
							if (list.size() == 0) {
								continue;
							}
							// get the relationship with the best(lower) depth, from the list
							for (Relationship r : list) {
								if (r.getDepth() < bestDepth) {
									bestDepth = r.getDepth();
									best = r;
								}
							}
							// if(((AsymmetricRelationship) best).getCommonParentIndex()<=5)
							// best.getNodeList().print();
						}
					}
				}
			}
		}
		/*
		if(best != null && bestDepth < 6)
			best.getNodeList().print(); // for each couple of words we print the best relationship
			*/
		return bestDepth;
	}
	
	public static boolean exists(String word) throws FileNotFoundException, JWNLException, CloneNotSupportedException {
		IndexWordSet s = dictionary.lookupAllIndexWords(word);
		return s.size() > 0;
	}
	
	public static double getSemanticScore(String clue, String boardWord)
	        throws JWNLException, CloneNotSupportedException {

	    final int MAX_DEPTH = 6;

	    Map<PointerType, Double> relationWeights = Map.of(
	            PointerType.SIMILAR_TO, 1.0,
	            PointerType.HYPONYM, 0.9,
	            PointerType.HYPERNYM, 0.7,
	            PointerType.DERIVATION, 0.85,
	            PointerType.PART_MERONYM, 0.5,
	            PointerType.PART_HOLONYM, 0.5
	    );

	    double bestScore = 0.0;

	    IndexWordSet clueWords = dictionary.lookupAllIndexWords(clue);
	    IndexWord targetWord = dictionary.lookupIndexWord(POS.NOUN, boardWord);

	    if (targetWord == null)
	        return 0.0;
	    
	    List<Synset> targetSynsets = targetWord.getSenses();
	    for (IndexWord clueIndex : clueWords.getIndexWordArray()) {
	        List<Synset> clueSynsets = clueIndex.getSenses();
	        for (int clueSenseRank = 0; clueSenseRank < clueSynsets.size(); clueSenseRank++) {
	            Synset clueSynset = clueSynsets.get(clueSenseRank);
	            for (int targetSenseRank = 0; targetSenseRank < targetSynsets.size(); targetSenseRank++) {
	                Synset targetSynset = targetSynsets.get(targetSenseRank);
	                for (PointerType pt : relationWeights.keySet()) {
	                    RelationshipList relationships =
	                            RelationshipFinder.findRelationships(
	                                    clueSynset,
	                                    targetSynset,
	                                    pt
	                            );
	                    if (relationships.isEmpty())
	                        continue;
	                    for (Object obj : relationships) {
	                        Relationship r = (Relationship) obj;
	                        int depth = r.getDepth();
	                        // hard cutoff against semantic drift
	                        if (depth > MAX_DEPTH)
	                            continue;
	                        // relation importance
	                        double relationWeight =
	                                relationWeights.getOrDefault(pt, 0.2);
	                        // exponential depth decay
	                        double depthScore =
	                                Math.exp(-0.6 * depth);
	                        // penalize rare senses
	                        double sensePenalty =
	                                1.0 /
	                                ((clueSenseRank + 1.0)
	                                * (targetSenseRank + 1.0));
	                        // penalize generic common ancestors
	                        double specificityBonus = 1.0;
	                        if (r instanceof AsymmetricRelationship) {
	                        	AsymmetricRelationship ar = (AsymmetricRelationship) r;
	                            int commonParentDepth =
	                                    ar.getCommonParentIndex();
	                            specificityBonus =
	                                    1.0 /
	                                    (commonParentDepth + 1.0);
	                        }
	                        // optional weak gloss overlap
	                        double glossBonus = 0.0;
	                        String clueGloss =
	                                clueSynset.getGloss().toLowerCase();
	                        String targetGloss =
	                                targetSynset.getGloss().toLowerCase();
	                        if (containsToken(clueGloss, boardWord))
	                            glossBonus += 0.05;
	                        if (containsToken(targetGloss, clue))
	                            glossBonus += 0.05;
	                        double finalScore =
	                                relationWeight
	                                * depthScore
	                                * sensePenalty
	                                * specificityBonus
	                                + glossBonus;
	                        if (finalScore > bestScore) {
	                            bestScore = finalScore;
	                        }
	                    }
	                }
	            }
	        }
	    }
	    return -bestScore;
	}

	private static boolean containsToken(String gloss, String word) {

	    String stemmed =
	            Word2VecUser.stem(word.toLowerCase());

	    String[] tokens =
	            gloss.split("\\W+");

	    for (String token : tokens) {

	        if (Word2VecUser.stem(token).equals(stemmed))
	            return true;
	    }

	    return false;
	}

}
