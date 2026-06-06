package utils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import it.uniroma1.lcl.jlt.util.Pair;
import net.sf.extjwnl.JWNLException;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.lemmatizer.Lemmatizer;
import opennlp.tools.lemmatizer.LemmatizerME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bytedeco.opencv.opencv_dnn.BlankLayer;
import org.deeplearning4j.*;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.nd4j.linalg.api.ndarray.INDArray;


import entities.Clue;
import entities.PossibleClue;

//import deeplearning4j.models.embeddings.wordvectors.WordVectors;

public abstract class Word2VecUser {
	
	private static WordVectors wordVectors;
	private static HashMap<String,Double> freqPriors;
	
	public static void initialize() {
		if(wordVectors == null)
			try {
	            
	            File freqFile = new File("C:\\Users\\s.capani\\SdaiNlp\\FrequenciesCount.txt");
	            if (!freqFile.exists()) {
	                System.err.println("Frequencies file not found! Please download it first.");
	                System.err.println("Example: https://www.ugent.be/pp/experimentele-psychologie/en/research/documents/subtlexus");
	                return;
	            }
	            //Calculate Frequency Priors
	            System.out.println("Loading Frequency priors...");
	            freqPriors = calculateFrequencyPriors(freqFile.toPath());
	            
	            // Path to pre-trained Word2Vec model
	            File modelFile = new File("C:\\Users\\s.capani\\SdaiNlp\\word2vec\\GoogleNews-vectors-negative300.bin");
	
	            if (!modelFile.exists()) {
	                System.err.println("Model file not found! Please download it first.");
	                System.err.println("Example: https://code.google.com/archive/p/word2vec/");
	                return;
	            }
	            
	            // Load the pre-trained model
	            System.out.println("Loading Word2Vec model...");
	            //WordVector
	            wordVectors = WordVectorSerializer.readWord2VecModel(modelFile);
	            
	        }catch (Exception e) {
	            e.printStackTrace();
	        }
	}
	
	public static HashMap<String, Double> calculateFrequencyPriors(Path path) throws IOException {
		HashMap<String, Double> freq = new HashMap<>();

	    List<String> lines = Files.readAllLines(path);
	    Double minFreq = Double.MAX_VALUE;
	    Double maxFreq = Double.MIN_VALUE;
	    for(String line : lines) {
	    	//read from file
	        String[] parts = line.split("\\s+");

	        if(parts.length < 2)
	            continue;
	        if(parts[1].toLowerCase().equals("freqcount"))
	        	continue;

	        String word = parts[0].toLowerCase();
	        
	        double value = Double.parseDouble(parts[1]);
	        // compute the log frequency
	        double logFreq = Math.log(value + 1);
	        // compute min and max for normalization
	        if(logFreq > maxFreq)
	        	maxFreq = logFreq;
	        if(logFreq < minFreq)
	        	minFreq = logFreq;
	        
	        freq.put(word, logFreq);
	    }
	    
	    HashMap<String,Double> freqPrior = new HashMap<>();
	    for(String k: freq.keySet()) {
	    	double f = freq.get(k);
	    	double normalized = (f - minFreq)/ (maxFreq - minFreq);
	    	//sigmoid scaling
	    	double _k = 1.5; //8.0
	        double center = 0.35;

	        double prior =  1.0 / (1.0 + Math.exp(-_k * (normalized - center)));
	        freqPrior.put(k, prior);
	    }
	    
	    return freqPrior;
	}
	
    public static void example() {
        try {
            // Example: Find words similar to "king"
            String word = "plant";
            Collection<String> coll = new ArrayList<String>();
            coll.add(word);
            coll.add("daisy");
            INDArray res = wordVectors.getWordVectorsMean(coll);
            Collection<String> words = wordVectors.wordsNearest(res, 5);
            for(String w : words) {
                System.out.println("Word found 'between' plant and worker " + w);
            }
            if (wordVectors.hasWord(word)) {
                Collection<String> similarWords = wordVectors.wordsNearest(word, 10);
                System.out.println("Words similar to '" + word + "': " + similarWords);
            } else {
                System.out.println("Word not found in vocabulary: " + word);
            }

            // Example: Compute similarity between two words
            String w1 = "king";
            String w2 = "queen";
            
            // Per ogni gruppo di parole della squadra di dimensione N o inferiore cercare la parola (o le parole) che siano le meno simili possibili alla parola nera e alle parole della squadra avversaria
            wordVectors.wordsNearest(coll, words, 10); //questa funzione fa già quello che mi serviva!!!!! Pazzesco
            if (wordVectors.hasWord(w1) && wordVectors.hasWord(w2)) {
                double similarity = wordVectors.similarity(w1, w2);
                System.out.println("Similarity between '" + w1 + "' and '" + w2 + "': " + similarity);
            }
         	
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static HashMap<Integer,List<PossibleClue>> prepareClue(Collection<String> posWords, Collection<String> negWords, Collection<String> blankWords, String killer) {
    	// for now we are searching for a word that is near the pos and far from the neg, we need to refine that.
    	// maximizing the distance from the killer word (the killer is also included in the negWords)
    	
    	// for each subset of posWords we want to find the top [nearest] (3?10?) words, 
    	// verify the similarity with each word in the subset and the (non)similarity with each negWord, 
    	// maximize the first and minimize the second
    	//List<Pair<Clue,Double>> possibleClues = new ArrayList<Pair<Clue,Double>>();
    	//for(int i=1; i< posWords.size();i++) {
    		// for each possible N iterate (on the subsets) and save the best clue word
		HashMap<Integer,List<PossibleClue>> possibleClues = new HashMap<Integer,List<PossibleClue>>();
    	//double bestSim = 0;
		//String bestWord = "";
		//ArrayList<String> a = new ArrayList();
		//a.add("palm");
		//a.add("pool");
		//Collection<String> ab =  wordVectors.wordsNearest(a, new ArrayList(), 100);
		//System.out.println(ab);
		//int N = 1;
		//int i = 0;
		//int max = (int) Math.pow(2, posWords.size());
		for(Collection<String> pw: iterativePowerSet(posWords)) {
			//System.out.println("Clue preparation: " + i++ + "/" + max );
			//if(averagePairwiseSimilarity(pw) < 0.15) 
			//	continue;
			//System.out.println("Intresting set candidate found in: " + pw.toString());
			int size = pw.size(); 
			if(size < 1 || size > 4)
				continue;
    		Collection<String> res = wordVectors.wordsNearest(repeatList(pw,5), negWords, 350);
			//Collection<String> res = wordVectors.wordsNearest(pw, negWords , 10);
    		//System.out.println("Words found for: " + pw.toString() + ":\n " + res.toString());
    		for(String s: res) {
    			if(s.contains("_")) continue;
    			boolean isOk = true;
    			for(String p: posWords)
    				if(tooMuchInCommon(s,p)) { 
    					isOk=false;
    					break;
    				}
    			if(!isOk) continue;
    			for(String n: negWords)
    				if(tooMuchInCommon(s,n))  { 
    					isOk=false;
    					break;
    				}
    			if(!isOk) continue;
    			for(String b: blankWords)
    				if(tooMuchInCommon(s,b))  { 
    					isOk=false;
    					break;
    				}
    			if(!isOk) continue;
    			try {
    				if(!WordNetSearch.exists(s))  //we check if the selected word exists inside the WordNet Dictionary
						continue;
    			}catch(Exception e) {
    				e.printStackTrace();
    			}
    			double killerSim = wordVectors.similarity(killer, s); 
    			if(killerSim * killerSim < 0.01) { 
    				//we need to set a threshold for the killer word
    				Double score; //the score needs to take in account the sim with the targets - the sim with the rest of the board!! 
    				Double sim = 0d;
    				Double meanSim = 0d;
    				Double minSim = Double.MAX_VALUE;
    				for(String w: pw) {
    					double tempSim = wordVectors.similarity(w, s);
    					meanSim += Math.max(0,tempSim); // if it's under 0 treat it as a 0
    					minSim = Math.min(minSim, tempSim);
    				}
    				meanSim /= size; // mean of absolute values of similarities
    				sim = minSim *0.7 + meanSim * 0.3;
    				//leak
    				Double blankLeak = Double.MIN_VALUE;
    				for(String b: blankWords)
    					blankLeak = Math.max(blankLeak, wordVectors.similarity(b, s));
    				Double negLeak = Double.MIN_VALUE;
    				for(String n: negWords)
    					negLeak = Math.max(negLeak, wordVectors.similarity(n, s));
    				
    				double hubness = 0;
    				Collection<String> allBoardWords = new ArrayList<String>();
    				allBoardWords.addAll(blankWords);
    				allBoardWords.addAll(posWords);
    				allBoardWords.addAll(negWords);
    				for(String bw : allBoardWords) {
    				    hubness += Math.max(0,wordVectors.similarity(s, bw));
    				}

    				hubness /= allBoardWords.size();
    				
    				score = (sim - 0.3 * blankLeak - 0.6 * negLeak - 0.4 * hubness) * getFreqPrior(s);
    				List<PossibleClue> bestClues =  possibleClues.get(size);
    				if(bestClues != null) {
    					bestClues.sort((o1, o2) -> Double.compare(o1.getScore(), o2.getScore()));
    					if(bestClues.size() > 10) {
		    				Double bestScore = bestClues.get(bestClues.size()-1).getScore();
		    				if(score> bestScore) {
		    					bestClues.remove(bestClues.get(bestClues.size()-1)); //get rid of the last
		    					bestClues.add(new PossibleClue(s,score,pw));
		    					possibleClues.put(size, bestClues);
		    				}
	    				}else {
	    					bestClues.add(new PossibleClue(s,score,pw));
	    					possibleClues.put(size, bestClues);
    					}
    				}else {
    					bestClues = new ArrayList<>();
    					bestClues.add(new PossibleClue(s,score,pw));
    					possibleClues.put(size, bestClues);
    				}
    			}
    			//else{System.err.println("Threshold not passed!");}
    		}
		}
		/*
		for(int j = 1; j<= posWords.size();j++) {
			List<PossibleClue> res = possibleClues.get(j);
			if(res!= null) {
				System.out.println("Best Clues for N =" + j + ": ");
				for(PossibleClue pc: res)
					System.out.println(pc);
			}
		}*/
		
		return possibleClues;//n<ew Clue(N,bestWord);
    }
    
    private static List<Collection<String>> iterativePowerSet(Collection <String> words) {
    	List<Collection<String>> powerSet = new ArrayList<>();
        List<String> list = new ArrayList<String>(words);
        int num = list.size();
        for (int i = 0; i < (1 << num); i++) {
        	Collection<String> subset = new ArrayList<>();
            for (int j = 0; j < num; j++) {
                int grayEquivalent = i ^ (i >> 1);
                if(((1 << j) & grayEquivalent) > 0)
                	subset.add(list.get(j));
            }
        	powerSet.add(subset);
        }
        return powerSet;
    }
    
    private static Collection<String> repeatList(Collection<String> words, int N){
    	List<String> input = new ArrayList<String>(words);
    	Collection<String> out = new ArrayList<String>();
    	for(int i=0; i<N; i++) {
    		out.addAll(input);
    	}
    	return out;
    }
    
    private static Double getFreqPrior(String word) {
    	//Double prior = freqPriors.getOrDefault(word, 0.02); // if the word does not exist in our frequency lists assign a low prior
    	Double prior = freqPriors.get(word.toLowerCase());
    	if(prior == null) {
    		//System.err.println("The word: '" + word + "' does not appear in the frequency file.");
    		prior=0.02;// if the word does not exist in our frequency lists assign a low prior
    	}
    	return prior;
    }
    
    private static String normalize(String s) {
        return s
            .toLowerCase()
            .replaceAll("[^a-z]", "");
    }
    
    public static String stem(String s) {
    	PorterStemmer stemmer = new PorterStemmer();
        return stemmer.stem(s);
    }
    
    
    private static boolean tooMuchInCommon(String clue, String boardWord) {
    	//here we should check if the selected clue word has too much stuff in common with the words present in the board AKA if there is an equivalence between lemmas or stems
    	clue = normalize(clue);
        boardWord = normalize(boardWord);

        // exact match
        if(clue.equals(boardWord))
            return true;

        // substring overlap
        if(clue.length() >= 3 && boardWord.length() >= 3) {
            if(clue.contains(boardWord) || boardWord.contains(clue))
                return true;
        }
        
        //stem equivalence
        if(stem(clue).equals(stem(boardWord)))
        	return true;
    	return false; 
    }
    public static List<String> getOrderedNClosestWords(String clue, List<String> boardWords, int N){
    	List<String> res = new ArrayList<String>();
    	List<Pair<Double,String>> temp = new ArrayList<>();
    	for(String bw:boardWords) {
    		temp.add(new Pair<>(wordVectors.similarity(clue, bw.replace(" ", "_")),bw));
    	}
    	temp.sort((p1,p2) -> Double.compare(p2.getFirst(), p1.getFirst()));
    	System.out.println(temp);
    	for(Pair<Double,String> p:temp.subList(0,N)) //temp.size() -1 - N, temp.size() - 1
    		res.add(p.getSecond());
    	System.out.println(res);
    	return res;
    }
}

