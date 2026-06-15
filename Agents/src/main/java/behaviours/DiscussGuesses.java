package behaviours;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import agents.PlayerAgent;
import entities.GameStatus;
import entities.GuessDto;
import entities.Opinion;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.ApiCaller;
import utils.CodeNamesUtils;

public class DiscussGuesses extends Behaviour{ //Behaviour {
    private boolean hasEnded = false;
    private int playerCount = 0;
    private boolean reqSent = false;
    private boolean useAdditionalGuess = false;
    private String phase = "get-players"; // there are four PHASES get-players, group-options, evaulate-guess, wake-up-oracle
    private final String ONT = "discuss";
    private int opinionsReceived = 0;
    private List<Opinion> opinions = new ArrayList<>();
	private Map<AID,Double> trustMap = new HashMap<>();
    public DiscussGuesses(){
        super();
    }
    @Override
    public void action() {
		if(!trustMap.containsKey(myAgent.getAID()))
			trustMap.put(myAgent.getAID(),0d);
        PlayerAgent pa = (PlayerAgent) myAgent;
        if(pa.isTeamCoord()) {
        	//get number of players in team, wait for all the opinions, evaluate all the opinions and pick the N best(or the ones for which more people agree with), then guess in order
        	if(phase.equals("get-players")){
				getPlayers();
        	}
			if(phase.equals("group-opinions")) {
        		groupOpinions();
    		}
			if(phase.equals("evaluate-guess")) {
				evaluateGuess();
			}
        	if(phase.equals("wake-up-oracle")) {
        		wakeUpOracle();
        	}
        }else {
        	// send all the selected words to the Team Coordinator
        	sendSelectedWords();
        }
    }

    public int onEnd(){
    	//reset all the internal variables
    	hasEnded = false;
        playerCount = 0;
        reqSent = false;
        phase = "get-players";
        opinionsReceived = 0;
        useAdditionalGuess = true; //will decide later if actually use it or not
        //opinions = new ArrayList<Opinion>(); //maybe we don't want to delete this!! Memory is a skill in this game
        return 1;
    }
     
    @Override
    public boolean done() {
        return hasEnded;
    }
    
    private String checkWinCon(String gameId) {
    	// possible outputs are "red-won", "blue-won" and "ongoing"
    	ApiCaller ca = new ApiCaller();
    	GameStatus gs = ca.getGameStatus(gameId);
    	boolean allBlue = true;
    	boolean allRed = true;
    	for(entities.Word w : gs.words) {
    		if(w.label.equals("blue")) {
    			allBlue = allBlue && w.isGuessed;
			}else if(w.label.equals("red")) {
				allRed = allRed && w.isGuessed;
			}
    	}
		if(allBlue)
    		return "blue-won";
    	if(allRed)
    		return "red-won";
    	return "ongoing";
    }

	private List<AID> mergeHolders(List<Opinion> ops, Opinion op){
		Set<AID> holderSet = new HashSet<>(); 
		for(Opinion op2: ops){
			if(op.equals(op2))
				holderSet.addAll(op2.getHolders());
		}
		return new ArrayList<>(holderSet);
	}

    private void updateTrustMap(String label, List<AID> holders){
		PlayerAgent pa = (PlayerAgent) myAgent;
		if(label != null){
			Double toAdd;
			if(label.equals(pa.getTeam())){
				toAdd = -0.1;
			}else{
				toAdd = 0.11;
			}
			for(AID holder: holders){
				trustMap.put(holder, trustMap.get(holder) + toAdd);
			}
		}
	}

	private void sendSelectedWords(){
		PlayerAgent pa =(PlayerAgent) myAgent; 
		AID coord = pa.getCoordinator();
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(coord);
		String cont = "";
		for(Opinion s : pa.getSelectedWords())
			cont += s.toString() + " | ";
		msg.setContent(cont);
		msg.setOntology(ONT);
		myAgent.send(msg);
		sendToChat(pa.getTeam()+"#Player#"+cont+ " ");
		hasEnded = true;
	}

	private void getPlayers(){
		PlayerAgent pa =(PlayerAgent) myAgent; 
		MessageTemplate mt = MessageTemplate.MatchConversationId("get-player-count");
		ACLMessage reply = myAgent.receive(mt);
		if(!reqSent) {
			//this is the first step, if there are any opinions left from the previous turns we manage those here
			manageOldOpinions();
			//send the request to game master
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(pa.getGameMaster());
			msg.setContent(pa.getTeam());
			msg.setOntology("player-count");
			msg.setConversationId("get-player-count");
			myAgent.send(msg);
			reqSent = true;
		}else if(reply != null) {
			//get the number from the response
			playerCount = Integer.parseInt(reply.getContent());
			phase = "group-opinions"; 
		}else {
			block();
		}
	}

	private void manageOldOpinions(){
		PlayerAgent pa =(PlayerAgent) myAgent; 
		if(!opinions.isEmpty()) {
			GameStatus gs = pa.getGameStatus();
			List<Opinion> newOp = new ArrayList<>();
			for(Opinion op: opinions) {
				if(gs.isWordInBoard( op.getName())) {
					//we want to remember all the holders of the opinions
					Opinion newOpinion=new Opinion(op.getName(),pa.getClue().N+1d);
					newOpinion.setHolders(mergeHolders(opinions, op)); 
					newOp.add(newOpinion);//if the word is not guessed yet we put it back into the opinion list, with a score that is forcibly higher than the maximum for new proposals
				}else{
					//we want to change the trust score of the opinions for which we have a result
					//if status == team trust -0.1 else trust +0.05
					String label = gs.getWordLabel(op.getName());
					updateTrustMap(label, op.getHolders());
				}
			}
			opinions = newOp;
			if(opinions.isEmpty())
				useAdditionalGuess = false;
		}
	}

	private void groupOpinions(){
		MessageTemplate mt = MessageTemplate.MatchOntology(ONT);
		ACLMessage op = myAgent.receive(mt);
		if(op != null) {
			Double pos = 0d;
			for(String s : op.getContent().split("\\s\\|\\s")) {
				if(!s.equals("")) {
					Opinion opi = new Opinion(s);
					opi.addHolder(op.getSender());
					opi.setScore(pos++); //the score is the position of the opinion
					opinions.add(opi);
				}
			}
			opinionsReceived++;
			if(opinionsReceived == playerCount -1) {
				phase = "evaluate-guess";
			}
			//if the player is not in the trustMap we add it with a score of 0
			if(!trustMap.containsKey(op.getSender())){
				trustMap.put(op.getSender(),0d);
			}
		}else {
			block();
		}
	}

	private void sendToChat(String content){
		PlayerAgent pa = (PlayerAgent) myAgent;
		ACLMessage toChat = new ACLMessage(ACLMessage.INFORM);
		toChat.setOntology("update-chat");
		toChat.addReceiver(pa.getGameMaster());
		toChat.setContent(content);
		myAgent.send(toChat);
	}

	private void addCoordinatorOpinions(){
		PlayerAgent pa = (PlayerAgent) myAgent;
		Double pos = 0d;
		for(Opinion s: pa.getSelectedWords()){
			Opinion tempOp = new Opinion(s.getName(),pos++);
			tempOp.addHolder(myAgent.getAID());
			opinions.add(tempOp);
		}
	}

	private List<Opinion> mergeOpinions(){
		//take only one opinion for name (take the one with the lowest score)
		List<Opinion> mergedOpinions = new ArrayList<>();
		for(Opinion o: opinions) {
			// each time we merge we have to add the other opinion holders!
			if(!mergedOpinions.contains(o)) {
				o.setHolders(mergeHolders(opinions,o));
				mergedOpinions.add(o);
			}
		}
		return mergedOpinions;
	}

	private List<String> selectWordsToGuess(List<Opinion> op){
		PlayerAgent pa = (PlayerAgent) myAgent;
		int N = pa.getClue().N;
		List<String> toGuess = new ArrayList<>();
		for(int i=0; i< N + (useAdditionalGuess?1:0); i++) { // each turn we can send up to N+1 guesses, the +1 accounts for opinions from the previous turns, if it's the first turn we just send N guesses
			if(op.size() > i){
				if(i<N)
					toGuess.add(op.get(i).getName());
				else {
					System.out.println("Entering the bonus opinion selection");
					for(Opinion bonusOpinion: op) {
						if(bonusOpinion.getScore()>N) {
							toGuess.add(bonusOpinion.getName());
							break;
						}
					}
				}
			}
		}
		return toGuess;
	}

	private String sendGuesses(List<String> toGuess, List<Opinion> mergedOpinions){
		PlayerAgent pa = (PlayerAgent) myAgent;
		String lastRes="";
		int guessSent = 0;
		for(String s:toGuess) {
			// call api
			ApiCaller ca = new ApiCaller();
			GuessDto gd = new GuessDto();
			gd.NameGuess= s;
			gd.RoomId = pa.getGameId();
			lastRes = ca.callPutApi("http://localhost:8080/api/guess", gd);
			sendToChat(pa.getTeam()+"#"+"Player"+"#"+"Selected: " + s);
			guessSent++;
			// check API answer
			if(lastRes.equals(pa.getTeam()) || lastRes.equals(CodeNamesUtils.getOtherTeam(pa.getTeam()))) {
				//Check for wincon only if a colored word has been guessed
				String wincon = checkWinCon(pa.getGameId());
				if(wincon.equals("red-won")) {
					System.out.println("RED TEAM WON");
					hasEnded=true;
					break;
				}
				if(wincon.equals("blue-won")) {
					System.out.println("BLUE TEAM WON");
					hasEnded=true;
					break;
				}
			}
			//Update trust score of every player that held an opinion on the guessed word 
			for(Opinion mo :mergedOpinions){
				if(mo.getName().equals(s)){	
					updateTrustMap(lastRes,mo.getHolders());
					break;
				}
			}
			mergedOpinions.removeIf(o -> o.getName().equals(s));
			if(!lastRes.equals(pa.getTeam()))
				break;	
			if(pa.getClue().N == guessSent) {
				//we can suppose that we got all the intended targets for the current turn, so we delete all the opinions with score <= N
				mergedOpinions.removeIf(o -> o.getScore() <= pa.getClue().N);
			}
		}
		opinions = mergedOpinions;
		return lastRes;
	}

	private void evaluateGuess(){
		PlayerAgent pa = (PlayerAgent) myAgent;
		String cont = "";
		for(Opinion s : pa.getSelectedWords())
			cont += s.toString() + " | ";
		sendToChat(pa.getTeam()+"#"+"Player"+"#"+cont+" ");
		addCoordinatorOpinions();
		//sort the opinions from lower to higher trustScore
		opinions.sort((o1,o2) -> Double.compare(o1.getTrustScore(trustMap), o2.getTrustScore(trustMap)));
		List<Opinion> mergedOpinions = mergeOpinions();
		
		List<String> toGuess = selectWordsToGuess(mergedOpinions);
		
		System.out.println(myAgent.getLocalName() + ": Sending" +toGuess.toString());
		if(toGuess.isEmpty()) {
			sendToChat(pa.getTeam()+"#"+"Player"+"#"+"Selected to send nothing because there are no opinions");
		}
		String lastRes = sendGuesses(toGuess, mergedOpinions);
		
		if(lastRes.equals("black")) {
			System.out.println(CodeNamesUtils.getOtherTeam(pa.getTeam()).toUpperCase() + " TEAM WON");
			// We hit the killer word! Game has ended! Send to the GM the notify or notify all
			System.out.println("IL GIOCO é FINITO!!!!!!!!!!!!!!!");
			hasEnded=true;
		}else {
			// in all the other cases we just pass the ball to the other team (we should check for win conditions here??)
			// send change current team request
			ACLMessage chgMessage = new ACLMessage(ACLMessage.INFORM);
			chgMessage.setOntology("team-change");
			chgMessage.setContent(CodeNamesUtils.getOtherTeam(pa.getTeam()));
			chgMessage.addReceiver(pa.getGameMaster());
			chgMessage.setConversationId("team-change-conv");
			pa.send(chgMessage);
		}
		if(hasEnded) {
			//send end game messages to game-master (they will broadcast it)
			ACLMessage endGameMsg = new ACLMessage(ACLMessage.INFORM);
			endGameMsg.setContent("game-has-ended");
			endGameMsg.setOntology("game-status");
			endGameMsg.addReceiver(pa.getGameMaster());
			pa.send(endGameMsg);
		}else {
			phase = "wake-up-oracle";
		}
	}

	private void wakeUpOracle(){
		PlayerAgent pa = (PlayerAgent) myAgent;
		// send wake-up to Other team Oracle (if sent too early could break everything!)
		ACLMessage asw = myAgent.receive(MessageTemplate.MatchConversationId("team-change-conv"));
		if(asw != null) {
			//after the game state has been updated we wake up the oracle
			ACLMessage wakeUpMessage = new ACLMessage(ACLMessage.INFORM);
			wakeUpMessage.setContent("wake-up");
			wakeUpMessage.addReceiver(pa.getEnemyOracle());
			pa.send(wakeUpMessage);
			hasEnded=true;
		}else{
			block();
		}
	}
}