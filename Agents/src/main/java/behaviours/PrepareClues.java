package behaviours;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import agents.OracleAgent;
import entities.Clue;
import entities.GameStatus;
import entities.Message;
import entities.PossibleClue;
import utils.ApiCaller;
import utils.FormListener;
import utils.PopupForm;
import utils.Word2VecUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class PrepareClues extends OneShotBehaviour implements FormListener{
    private int counter;
    private GameStatus gs;
	private int intInput;
	private String textInput;
	private static CountDownLatch inputDone;

    public PrepareClues(){
        counter=0;
    }
    
    @Override
    public void action() { 
        OracleAgent agent = (OracleAgent) myAgent;
        ApiCaller ac = new ApiCaller();
        gs = ac.getGameStatus(agent.getGameId());
        agent.setGameStatus(gs);
        if(!gs.currentTeam.equals(agent.getTeam())) {
        	counter = 1;
        	return;
        }
        Clue c = null; 
        System.out.println(myAgent.getLocalName() + ": Starting clue generation");
        
		String strategy = agent.getStrategy();
        if(strategy.equals("user")){
			c = applyUserStrategy();
		}else{
			HashMap<Integer,List<PossibleClue>> possibleClues = collectPossibleClues();
			//we need to select the best clue (depending on the strategy of our agent)
			if(strategy.equals("pick-2")) 
			{
				c = applyPick2Strategy(possibleClues);
			}else if(strategy.equals("aggressive")) {
				//tends to pick a big N
				c = applyAggressiveStrategy(possibleClues);
			}else if(strategy.equals("conservative")) {
				//tends to pick a small N
				c = applyConservativeStrategy(possibleClues);
			}
		}
        agent.setClue(c); 
        HashMap<Integer,Clue> clues = agent.getGameClues();
        Integer nextKey = clues.size() + 1;
        clues.put(nextKey, c);
        agent.setGameClues(clues);
        ((ManageClueRequests)agent.getMngClueReq()).setNewestClue(nextKey);
        agent.getMngClueReq().restart(); //waking up the manager of clues
        System.out.println(myAgent.getLocalName() + ": The selected clue is: " + c.Word + " " + c.N);
        if(agent.isGameMgr()) {
        	agent.getGg().addMessage(new Message(agent.getLocalName(),agent.getTeam(),"Oracle",c.toString()));
		}else {
    		System.out.println(myAgent.getLocalName() + ": Clue Preparation Finished, sending new message for the chat to the Game Master");
        	ACLMessage toChat = new ACLMessage(ACLMessage.INFORM);
	        toChat.setOntology("update-chat");
	        toChat.addReceiver(agent.getGameMaster());
	        toChat.setContent(agent.getTeam()+"#"+"Oracle"+"#"+c.Word + " " + c.N);
	        myAgent.send(toChat);
	        System.out.println(myAgent.getLocalName() + ": Message Sent");
        }
        counter=1;
    }
    public int onEnd(){
		intInput = 0;
		textInput = "";
    	int temp = counter;
    	counter = 0;
        return temp;
    }
	@Override
	public void onFormSubmitted(String textValue, int numericValue){
		intInput = numericValue;
		textInput = textValue;
		//code for unlocking behaviour!!
		inputDone.countDown();
	}

	private Clue applyUserStrategy(){
		try{
			OracleAgent agent = (OracleAgent) myAgent;
			inputDone = new CountDownLatch(1);
			PopupForm popup = new PopupForm(this, "Select next Clue for " + agent.getTeam() + " team");
			popup.setVisible(true);
			//block until clue recieved!!
			inputDone.await();
			return new Clue(intInput,textInput);
		}catch(InterruptedException e){
			e.printStackTrace();
			return null;
		}
			
	}

	private HashMap<Integer,List<PossibleClue>> collectPossibleClues(){
		OracleAgent agent = (OracleAgent) myAgent;
		Collection<String> pos = new ArrayList<>();
		Collection<String> neg = new ArrayList<>();
		Collection<String> blank = new ArrayList<>();
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
		
		//HashMap<Integer,List<PossibleClue>> possibleClues =
		return Word2VecUser.prepareClue(pos, neg,blank,  killer);
	}

	private Clue applyPick2Strategy(HashMap<Integer,List<PossibleClue>> possibleClues){
		OracleAgent agent = (OracleAgent) myAgent;
		PossibleClue pc = null;
		int N = 0;
		List<String> usedClues = new ArrayList<>();
		agent.getGameClues().values().forEach(_c-> usedClues.add(_c.Word));
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
		return new Clue(N,pc.getWord());
	}

	private Clue applyAggressiveStrategy(HashMap<Integer,List<PossibleClue>> possibleClues){
		OracleAgent agent = (OracleAgent) myAgent;
		PossibleClue pc = null;
		int N = 0;
		List<String> usedClues = new ArrayList<>();
		agent.getGameClues().values().forEach(_c-> usedClues.add(_c.Word));
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
		return new Clue(N,pc.getWord());
	}

	private Clue applyConservativeStrategy(HashMap<Integer,List<PossibleClue>> possibleClues){
		OracleAgent agent = (OracleAgent) myAgent;
		PossibleClue pc = null;
		int N = 0;
		List<String> usedClues = new ArrayList<>();
		agent.getGameClues().values().forEach(_c-> usedClues.add(_c.Word));
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
		return new Clue(N,pc.getWord());
	}
}