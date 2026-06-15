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
    private GameStatus gs;

    public PrepareGuesses(){
    }
    
    @Override
    public void action() { 
        PlayerAgent agent = (PlayerAgent) myAgent;
        ApiCaller ac = new ApiCaller();
        gs = ac.getGameStatus(agent.getGameId());
        agent.setGameStatus(gs);
        Clue clue = agent.getClue();
        System.out.println(myAgent.getLocalName() + " Clue: " + clue.N + " " + clue.Word);
        
        ArrayList<entities.Word> ws = new ArrayList<>();
        gs.words.forEach(w ->{ if(!w.isGuessed) {ws.add(w);}});
        
        //for each not guessed word in the board we search for relationships in WordNet
        List<Opinion> evaluedWords = new ArrayList<>();
        String strategy = agent.getStrategy();//"score";
        if(strategy.equals("word2vec")) {
            evaluedWords = applyWord2VecStrategy(clue, ws);
        }else if(strategy.equals("score")) {
            evaluedWords = applyScoreStrategy(clue, ws);    
        }else if(strategy.equals("depth")) {
            evaluedWords = applyDepthStrategy(clue, ws);    
        }else if(strategy.equals("depth-allpos")) {
            evaluedWords = applyDepthAllPosStrategy(clue, ws);    
        }
        if(evaluedWords.size()==0) {
        	System.out.println(myAgent.getLocalName() + "Clue word out of WordNet dictionary!");
        	agent.setSelectedWords(evaluedWords);
            return;
        }
        //order by score (ascending)
        evaluedWords.sort((p1,p2) -> Double.compare(p1.getScore(), p2.getScore()));
        //take top N
        if(evaluedWords.size()<=clue.N)
        	agent.setSelectedWords(evaluedWords);
        else
        	agent.setSelectedWords(evaluedWords.subList(0,clue.N)); // we are getting N + 1 possible guesses
        
    }

    private List<Opinion> applyWord2VecStrategy(Clue clue, List<entities.Word> ws){
        List<String> boardWords = new ArrayList<String>(); 
        for(entities.Word w: ws) {
            boardWords.add(w.name);
        }
        List<Opinion> evaluedWords = new ArrayList<>();
        List<String> res = Word2VecUser.getOrderedNClosestWords(clue.Word, boardWords , clue.N);
        int i=0;
        for(String r : res)
            evaluedWords.add(new Opinion(r,(double)i++));
        return evaluedWords;
    }

    private List<Opinion> applyDepthStrategy(Clue clue, List<entities.Word> ws){
        List<Opinion> evaluedWords = new ArrayList<>();
        PlayerAgent agent = (PlayerAgent) myAgent;
        try{
            for(entities.Word w: ws) {
                double res = WordNetSearch.getBestFlexibleRelationshipDepth(clue.Word,w.name);
                if(res!= Integer.MAX_VALUE)
                    // if res is equals to max then the clue word is not present in our dictionary!!
                    evaluedWords.add(new Opinion(w.name,(double)res));
            
            }
        }catch(Exception e){
            System.err.println(e.getMessage());
            agent.setSelectedWords(evaluedWords);
        }
        return evaluedWords;
    }

    private List<Opinion> applyDepthAllPosStrategy(Clue clue, List<entities.Word> ws){
        List<Opinion> evaluedWords = new ArrayList<>();
        PlayerAgent agent = (PlayerAgent) myAgent;
        try{
            for(entities.Word w: ws) {
                double res = WordNetSearch.getBestFlexibleRelationshipDepthAllPOS(clue.Word,w.name);
                if(res!= Integer.MAX_VALUE)
                    // if res is equals to max then the clue word is not present in our dictionary!!
                    evaluedWords.add(new Opinion(w.name,(double)res));
            }
        }catch(Exception e){
            System.err.println(e.getMessage());
            agent.setSelectedWords(evaluedWords);
        }
        return evaluedWords;
    }

    private List<Opinion> applyScoreStrategy(Clue clue, List<entities.Word> ws){
        List<Opinion> evaluedWords = new ArrayList<>();
        PlayerAgent agent = (PlayerAgent) myAgent;
        try{
            for(entities.Word w: ws) {
                double res = WordNetSearch.getSemanticScore(clue.Word,w.name);
                if(res!= 0) //since this is a double it may fail, use the appropriate control!!
                    // if res is equals to max then the clue word is not present in our dictionary!!
                    evaluedWords.add(new Opinion(w.name,(double)res));	
            }
        }catch(Exception e){
            System.err.println(e.getMessage());
            agent.setSelectedWords(evaluedWords);
        }
        return evaluedWords;
    }
}