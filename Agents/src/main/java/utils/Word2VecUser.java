package utils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.threadly.util.Pair;

import entities.PossibleClue;

public abstract class Word2VecUser {
	
	private static WordVectors wordVectors;
	private static HashMap<String,Double> freqPriors;
	
	public static void initialize() {
		if(wordVectors == null)
			try {
				String rootPath = System.getProperty("user.dir");
				//String classPath = System.getProperty("java.class.path");
				File freqFile = new File(rootPath +"/Agents/target/classes/FrequenciesCount.txt"); 
				//ClassLoader classloader = getClass().getClassLoader();
	            //File freqFile = new File(classloader.getClass().getResource("FrequenciesCount.txt").toURI());//"C:\\Users\\s.capani\\SdaiNlp\\FrequenciesCount.txt");
	            if (!freqFile.exists()) {
	                System.err.println("Frequencies file not found! Please download it first.");
	                System.err.println("https://www.ugent.be/pp/experimentele-psychologie/en/research/documents/subtlexus");
	                return;
	            }
	            //Calculate Frequency Priors
	            System.out.println("Loading Frequency priors...");
	            freqPriors = calculateFrequencyPriors(freqFile.toPath());
	            
	            // Path to pre-trained Word2Vec model
	            File modelFile = new File(rootPath +"/Agents/target/classes/GoogleNews-vectors-negative300.bin"); 
				//File modelFile = new File("C:\\Users\\s.capani\\SdaiNlp\\word2vec\\GoogleNews-vectors-negative300.bin");
	
	            if (!modelFile.exists()) {
	                System.err.println("Model file not found! Please download it first.");
	                System.err.println("Example: https://drive.google.com/file/d/0B7XkCwpI5KDYNlNUTTlSS21pQmM/edit?usp=sharing");
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
	
	private static HashMap<String, Double> calculateFrequencyPriors(Path path) throws IOException {
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

    public static HashMap<Integer,List<PossibleClue>> prepareClue(Collection<String> posWords, Collection<String> negWords, Collection<String> blankWords, String killer) {
    	HashMap<Integer,List<PossibleClue>> possibleClues = new HashMap<>();
    	for(Collection<String> pw: CodeNamesUtils.iterativePowerSet(posWords)) {
			int size = pw.size(); 
			if(size < 1 || size > 4)
				continue;
    		Collection<String> res = wordVectors.wordsNearest(CodeNamesUtils.repeatList(pw,5), negWords, 350);
			for(String s: res) {
    			if(s.contains("_")) continue;
    			boolean isOk = true;
    			for(String p: posWords)
    				if(CodeNamesUtils.tooMuchInCommon(s,p)) { 
    					isOk=false;
    					break;
    				}
    			if(!isOk) continue;
    			for(String n: negWords)
    				if(CodeNamesUtils.tooMuchInCommon(s,n))  { 
    					isOk=false;
    					break;
    				}
    			if(!isOk) continue;
    			for(String b: blankWords)
    				if(CodeNamesUtils.tooMuchInCommon(s,b))  { 
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
    				Double sim;
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
    			}
		}
		return possibleClues;
    }
    
    private static Double getFreqPrior(String word) {
    	//Double prior = freqPriors.getOrDefault(word, 0.02); // if the word does not exist in our frequency lists assign a low prior
    	Double prior = freqPriors.get(word.toLowerCase());
    	if(prior == null) {
    		prior=0.02;// if the word does not exist in our frequency lists assign a low prior
    	}
    	return prior;
    }

    public static List<String> getOrderedNClosestWords(String clue, List<String> boardWords, int N){
    	List<String> res = new ArrayList<>();
    	List<Pair<Double,String>> temp = new ArrayList<>();
    	for(String bw:boardWords) {
    		temp.add(new Pair<>(wordVectors.similarity(clue, bw.replace(" ", "_")),bw));
    	}
    	temp.sort((p1,p2) -> Double.compare(p2.getLeft(), p1.getLeft()));
    	System.out.println(temp);
    	for(Pair<Double,String> p:temp.subList(0,N)) //temp.size() -1 - N, temp.size() - 1
    		res.add(p.getRight());
    	System.out.println(res);
    	return res;
    }
}

