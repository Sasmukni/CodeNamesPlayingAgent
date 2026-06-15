package utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import opennlp.tools.stemmer.PorterStemmer;

public abstract class CodeNamesUtils {
	public static String getOtherTeam(String myTeam) {
		return myTeam.equals("red")? "blue" : "red"; // if we are red the other team has to be blue, otherwise
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
    
    
    public static boolean tooMuchInCommon(String word, String boardWord) {
    	//here we should check if the selected clue word has too much stuff in common with the words present in the board AKA if there is an equivalence between lemmas or stems
    	word = normalize(word);
        boardWord = normalize(boardWord);

        // exact match
        if(word.equals(boardWord))
            return true;

        // substring overlap
        if(word.length() >= 3 && boardWord.length() >= 3) {
            if(word.contains(boardWord) || boardWord.contains(word))
                return true;
        }
        
        //stem equivalence
		return stem(word).equals(stem(boardWord));
    }

	public static List<Collection<String>> iterativePowerSet(Collection <String> words) {
    	List<Collection<String>> powerSet = new ArrayList<>();
        List<String> list = new ArrayList<>(words);
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
    
    public static Collection<String> repeatList(Collection<String> words, int N){
    	List<String> input = new ArrayList<>(words);
    	Collection<String> out = new ArrayList<>();
    	for(int i=0; i<N; i++) {
    		out.addAll(input);
    	}
    	return out;
    }
}