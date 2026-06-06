package behaviours;
import jade.core.behaviours.OneShotBehaviour;
import agents.PlayerAgent;
import entities.Clue;
import entities.GameStatus;
import entities.Opinion;
import utils.ApiCaller;
import utils.Word2VecUser;
import utils.WordNetSearch;

import java.util.ArrayList;
import java.util.List;

public class PrepareGuesses extends OneShotBehaviour {
    //private int counter;
    private GameStatus gs;
    //HashMap<String,List<BabelSense>> bss;

    public PrepareGuesses(){
        //counter=0;
    }
    
    @Override
    public void action() { 
        PlayerAgent agent = (PlayerAgent) myAgent;
        ApiCaller ac = new ApiCaller();
        gs = ac.getGameStatus(agent.getGameId());
        agent.setGameStatus(gs);
        Clue clue = agent.getClue();
        System.out.println(myAgent.getLocalName() + " Clue: " + clue.N + " " + clue.Word);
        
        /*
        if(agent.getBoardSenses() == null){
            bss = initializeBoardSenses();
            agent.setBoardSenses(bss); // this initialization should be made for all Players at once, for now we are making it for one player (?)
        }else{
            bss = agent.getBoardSenses();
        }
        //call ExploreGraph
        HashMap<String, Integer> res = exploreGraph(clue);
        System.out.println("Found: " + res.toString());
        */
        //List<String> selectedWords = new ArrayList<>();
        
        ArrayList<entities.Word> ws = new ArrayList<>();
        gs.words.forEach(w ->{ if(!w.isGuessed) {ws.add(w);}});
        /*
        for(Integer i: RandomIntegerSet(clue.N,ws.size())) {
        	selectedWords.add(ws.get(i).name);
        }*/
        //for each not guessed word in the board we search for relationships in WordNet
        List<Opinion> evaluedWords = new ArrayList<>();
        String strategy = agent.getStrategy();//"score";
        if(strategy.equals("word2vec")) {
        	//not sure if this is not cheating...
        	List<String> boardWords = new ArrayList<String>(); 
        	for(entities.Word w: ws) {
        		boardWords.add(w.name);
        	}
        	List<String> res = Word2VecUser.getOrderedNClosestWords(clue.Word, boardWords , clue.N);
        	int i=0;
        	for(String r : res)
        		evaluedWords.add(new Opinion(r,(double)i++));
        }else {
	        try{
	        	//WordNetSearch wns = new WordNetSearch();
	        	for(entities.Word w: ws) {
	        		//int res = wns.getBestFlexibleRelationshipDepth(clue.Word,w.name,20)
	        		if(strategy.equals("score")) {
		        		double res = WordNetSearch.getSemanticScore(clue.Word,w.name);
		        		if(res!= 0) //since this is a double it may fail, use the appropriate control!!
		        			// if res is equals to max then the clue word is not present in our dictionary!!
		        			evaluedWords.add(new Opinion(w.name,(double)res));
	        		}else if(strategy.equals("depth")) {
	        			double res = WordNetSearch.getBestFlexibleRelationshipDepth(clue.Word,w.name);
		        		if(res!= Integer.MAX_VALUE)
		        			// if res is equals to max then the clue word is not present in our dictionary!!
		        			evaluedWords.add(new Opinion(w.name,(double)res));
	        		}else if(strategy.equals("depth-allpos")) {
	        			double res = WordNetSearch.getBestFlexibleRelationshipDepthAllPOS(clue.Word,w.name);
		        		if(res!= Integer.MAX_VALUE)
		        			// if res is equals to max then the clue word is not present in our dictionary!!
		        			evaluedWords.add(new Opinion(w.name,(double)res));
	        		}
	        		
	            }
	        }
	        catch(Exception e){
	        	System.err.println(e.getMessage());
	        	agent.setSelectedWords(evaluedWords);
	        	return; 
	        }
        }
        if(evaluedWords.size()==0) {
        	System.out.println(myAgent.getLocalName() + "Clue word out of WordNet dictionary!");
        	agent.setSelectedWords(evaluedWords);
            return;
        }
        //order by Integer (ascending)
        evaluedWords.sort((p1,p2) -> Double.compare(p1.getScore(), p2.getScore()));
        //take top N
        /*for(int i = 0; i<clue.N; i++) {
        	System.out.println(agent.getLocalName() + "(" + agent.getTeam() + "): " +  evaluedWords.get(i));
    		selectedWords.add(evaluedWords.get(i).getSecond());
    	}*/
        if(evaluedWords.size()<=clue.N)
        	agent.setSelectedWords(evaluedWords);
        else
        	agent.setSelectedWords(evaluedWords.subList(0,clue.N)); // we are getting N + 1 possible guesses
        
    }
    /*
    public static List<Integer> RandomIntegerSet (int count, int max){
		List<Integer> uniqueNumbers = new ArrayList<>();

		Random random = new Random();
        while (uniqueNumbers.size() < count) {
            int num = random.nextInt(max);// + 1; 
            uniqueNumbers.add(num);
        }
        return uniqueNumbers;
	}*/
}