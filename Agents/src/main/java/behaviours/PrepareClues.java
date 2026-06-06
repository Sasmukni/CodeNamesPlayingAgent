package behaviours;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import agents.OracleAgent;
import entities.Clue;
import entities.GameStatus;
import entities.Message;
import entities.PossibleClue;
import utils.ApiCaller;
import utils.Word2VecUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import it.uniroma1.lcl.babelnet.*;

public class PrepareClues extends OneShotBehaviour {
    private int counter;
    private GameStatus gs;

    public PrepareClues(){
        counter=0;
    }
    
    @Override
    public void action() { 
        OracleAgent agent = (OracleAgent) myAgent;
        ApiCaller ac = new ApiCaller();
        gs = ac.getGameStatus(agent.getGameId());
        agent.setGameStatus(gs);
        //Word2VecUser.example();
        if(!gs.currentTeam.equals(agent.getTeam())) {
        	counter = 1;
        	return;
        }
        
        System.out.println(myAgent.getLocalName() + ": Starting clue generation");
        
        Collection<String> pos = new ArrayList<String>();
        Collection<String> neg = new ArrayList<String>();
        Collection<String> blank = new ArrayList<String>();
        String killer = "";
        
        for(entities.Word w : gs.words) {
        	if(!w.isGuessed) {
	        	if(w.label.equals(agent.getTeam())) {
	        		pos.add(w.name.replace(' ', '_'));
	        	}
	        	else if(w.label.equals("blank")) {
	        		blank.add(w.name.replace(' ', '_'));
	        	}
	        	else if(w.label.equals("black")) {
	        		neg.add(w.name.replace(' ', '_'));
	        		killer = w.name.replace(' ', '_');
	        	}
	        	else {// if(w.label.equals("blue")) {
	        		neg.add(w.name.replace(' ', '_'));
	        	}
        	}
        }
        
        HashMap<Integer,List<PossibleClue>> possibleClues = Word2VecUser.prepareClue(pos, neg,blank,  killer);
        //we need to select the best clue (in the opinion of our agent)
        PossibleClue pc = null;
        int N = 0;
        List<String> usedClues = new ArrayList<>();
        agent.getGameClues().values().forEach(c-> usedClues.add(c.Word));
        String strategy = agent.getStrategy();
        if(strategy.equals("pick-2")) 
        {
	        // Always pick the Clue with N = 2 if possible
	        if(possibleClues.get(2) != null) {
	        	pc = possibleClues.get(2).remove(possibleClues.get(2).size()-1);
	        	while(usedClues.contains(pc.getWord())) {
	        		//avoid using already used clues
	        		pc = possibleClues.get(2).remove(possibleClues.get(2).size()-1);//.removeLast();
	        	}
	        	N = 2;
	        }
	        else {
	        	pc = possibleClues.get(1).remove(possibleClues.get(1).size()-1);//removeLast();
		    	while(usedClues.contains(pc.getWord())) {
		    		//avoid using already used clues
		    		pc = possibleClues.get(1).remove(possibleClues.get(1).size()-1);//.removeLast();
		    	}
	        	N = 1;
	    	}
        }else if(strategy.equals("aggressive")) {
        	//tends to pick the biggest N
        	PossibleClue tempC = null;
        	Double bestScore = Double.MIN_VALUE;
        	for(int n = 1; n <= possibleClues.size(); n++) {
        		tempC = possibleClues.get(n).remove(possibleClues.get(n).size()-1);
	        	while(usedClues.contains(tempC.getWord())) {
	        		//avoid using already used clues
	        		tempC = possibleClues.get(n).remove(possibleClues.get(n).size()-1);
	        	} 
	        	double newScore = tempC.getScore() * Math.log(n+0.1);//Math.pow(n, 3/2); 
	        	if(newScore> bestScore) { //
	        		bestScore = newScore; 
	        		pc = tempC;
	        		N=n;
	        	}
	        	System.out.println(tempC.toString());
        	}
        }else if(strategy.equals("conservative")) {
        	//tends to pick a small N
        	PossibleClue tempC = null;
        	Double bestScore = Double.MIN_VALUE;
        	for(int n = 1; n <= possibleClues.size(); n++) {
        		tempC = possibleClues.get(n).remove(possibleClues.get(n).size()-1);
	        	while(usedClues.contains(tempC.getWord())) {
	        		//avoid using already used clues
	        		tempC = possibleClues.get(n).remove(possibleClues.get(n).size()-1);
	        	} 
	        	double newScore = tempC.getScore() * n ; 
	        	if(newScore > bestScore) {
	        		bestScore = newScore;
	        		pc = tempC;
	        		N=n;
	        	}
	        	System.out.println(tempC.toString());
        	}
        	
        }else if(strategy.equals("dynamic")) {
        	// looks at the board and then decides to be aggressive or conservative depending on the blue/red proportion 
        	// TO IMPLEMENT!!
        }
        
        Clue c = new Clue(N, pc.getWord());
        agent.setClue(c); //remove when done fixing stuff, or not, could have it's reasons to be here
        
        HashMap<Integer,Clue> clues = agent.getGameClues();
        Integer nextKey = clues.size() + 1;
        clues.put(nextKey, c);
        agent.setGameClues(clues);
        ((ManageClueRequests)agent.getMngClueReq()).setNewestClue(nextKey);
        agent.getMngClueReq().restart(); //waking up the manager of clues
        System.out.println(myAgent.getLocalName() + ": The selected clue is: " + pc.toString());
        if(agent.getIsGameMgr()) {
        	agent.getGg().addMessage(new Message(agent.getLocalName(),agent.getTeam(),"Oracle",pc.toString()));
		}else {
    		System.out.println(myAgent.getLocalName() + ": Clue Preparation Finished, sending new message for the chat to the Game Master");
        	ACLMessage toChat = new ACLMessage(ACLMessage.INFORM);
	        toChat.setOntology("update-chat");
	        toChat.addReceiver(agent.getGameMaster());
	        toChat.setContent(agent.getTeam()+"#"+"Oracle"+"#"+pc.toString());
	        myAgent.send(toChat);
	        System.out.println(myAgent.getLocalName() + ": Message Sent");
        }
        counter=1;
    }
    public int onEnd(){
    	OracleAgent oa = (OracleAgent) myAgent;
        //now we will send the clue to each player so we will send the clue message also to the "chat"
    	/*if(oa.getClue()!= null) {
    		
        }*/
    	int temp = counter;
    	counter = 0;
        return temp;
    }
}