package Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import entities.GameStatus;
import entities.Word;

public final class CreateGame {
	public static String CreateGameMethod() {
		String randomString = UUID.randomUUID().toString().replace("-", "");
    	GameStatus game = new GameStatus(randomString);
    	//Find the list of Words
    	ArrayList<String> names = RandomWordExtractor.NRandomWords(25);
    	ArrayList<Word> words = new ArrayList<Word>();
    	//int pos = 0;
    	//names.forEach(w -> words.add(new Word(w,"", pos)));
    	for(int i = 0; i< 25; i++) {
    		String w = names.get(i);
    		words.add(new Word(w,"",i));
    	}
    	//decide which team gets to start
    	game.startingTeam = MyRandom.CoinFlip();
    	game.currentTeam = game.startingTeam;
    	//Assign Labels for each word 
    	Set<Integer> startingTeamWords = MyRandom.RandomIntegerSet(9, 25);
    	startingTeamWords.forEach(i -> {
    		Word w = words.get((int)i);
    		w.label = game.startingTeam;
    		game.words.add(w);
    	});
    	ArrayList<Integer> a = new ArrayList<Integer>();
    	a.addAll(startingTeamWords);
    	a.sort((i1,i2) -> i2 - i1 );
    	for(Integer i : a){
    		words.remove((int)i);
    	}
    	//a.forEach(i ->  words.remove((int)i));
    	
    	//Set<Integer> otherTeamWords = MyRandom.RandomIntegerSet(7, 17);
    	String otherTeam = game.startingTeam.equals("blue") ? "red" : "blue";
    	Collections.shuffle(words);

    	List<Word> selected = words.subList(0, 8);
    	for (Word w : selected) {
    	    w.label = otherTeam;
    	    game.words.add(w);
    	}
    	selected.clear();
    	
    	Set<Integer> killerWord = MyRandom.RandomIntegerSet(1, 8);
    	killerWord.forEach(i -> {
    		Word w  = words.remove((int)i);
    		//Word w = new Word(s,"black");
    		w.label = "black";
    		game.words.add(w);
    	});
    	
    	words.forEach(wo -> {
    		//Word w = new Word(wo,"blank");
    		wo.label = "blank";
    		game.words.add(wo);
    	});
    	// we have to save this gameStatus in a Json file and put it in a folder!!
    	
    	boolean isok = GameStatusIO.saveGameStatus(game);
    	return isok ? randomString : "oh no";
	}
}
