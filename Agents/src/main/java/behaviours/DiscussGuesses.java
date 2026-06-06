package behaviours;
import java.util.ArrayList;
import java.util.List;

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
    private List<Opinion> opinions = new ArrayList<Opinion>();
    public DiscussGuesses(){
        super();
    }
    @Override
    public void action() {
        //System.out.println(myAgent.getLocalName() + " Start discussion session");
        PlayerAgent pa = (PlayerAgent) myAgent;
        if(pa.isTeamCoord()) {
        	//get number of players in team, wait for all the opinions, evaluate all the opinions and pick the N best(or the ones for which more people agree with), then guess in order
        	if(phase.equals("get-players")){
				MessageTemplate mt = MessageTemplate.MatchConversationId("get-player-count");
        		ACLMessage reply = myAgent.receive(mt);
        		if(!reqSent) {
        			//this is the first step, if there are any opinions left from the previous turns we manage those here
        			if(!opinions.isEmpty()) {
        				GameStatus gs = pa.getGameStatus();
        				List<Opinion> newOp = new ArrayList<>();
        				for(Opinion op: opinions) {
        					if(gs.isWordInBoard( op.getName())) {
        						newOp.add(new Opinion(op.getName(),pa.getClue().N+1d));//if the word is not guessed yet we put it back into the opinion list, with a score that is forcibly higher than the maximum for new proposals
        					}
        				}
        				opinions = newOp;
        				if(opinions.isEmpty())
        					useAdditionalGuess = false;
        			}
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
        	}else
    		if(phase.equals("group-opinions")) {
        		MessageTemplate mt = MessageTemplate.MatchOntology(ONT);
        		ACLMessage op = myAgent.receive(mt);
        		if(op != null) {
        			Double pos = 0d;
        			for(String s : op.getContent().split("\\s\\|\\s")) {
        				// here we could add a trust system that goes on and modifies the score of the opinion
        				if(!s.equals("")) {
        					Opinion opi = new Opinion(s);
        					opi.setScore(pos++); //the score is the position of the opinion
        					opinions.add(opi);
    					}
    				}
        			opinionsReceived++;
        			if(opinionsReceived == playerCount -1) {
        				phase = "evaluate-guess";
    				}
        		}else {
        			block();
        		}
    		} else
			if(phase.equals("evaluate-guess")) {
				String cont = "";
			    for(Opinion s : pa.getSelectedWords())
			    	cont += s.toString() + " | ";
				ACLMessage toChat = new ACLMessage(ACLMessage.INFORM);
				toChat.setOntology("update-chat");
		        toChat.addReceiver(pa.getGameMaster());
		        toChat.setContent(pa.getTeam()+"#"+"Player"+"#"+cont+" ");
		        myAgent.send(toChat);
	        	
        		//we add our selected words to the list, then pick the N most frequent words [TO-DO: better algo]
				Double pos = 0d;
		        for(Opinion s: pa.getSelectedWords())
					opinions.add(new Opinion(s.getName(),pos++));
				//HashMap<String,Integer> guessFreq = new HashMap<>();
				/*
				for(String s: opinions) {
					int i = guessFreq.getOrDefault(s, 0)+1;
					guessFreq.put(s, i);
					if(i > max)
						max = i;
				}
				*/

				//sort the opinions from lower to higher
				opinions.sort((o1,o2) -> Double.compare(o1.getScore(), o2.getScore()));
				//take only one opinion for name (take the one with the lowest score)
				List<Opinion> mergedOpinions = new ArrayList<>();
				for(Opinion o: opinions) {
					// each time we 
					if(!mergedOpinions.contains(o)) {
						mergedOpinions.add(o);
					}
				}

				int N = pa.getClue().N;
				List<String> toGuess = new ArrayList<>();
				for(int i=0; i< N + (useAdditionalGuess?1:0); i++) { // each turn we can send up to N+1 guesses, the +1 accounts for opinions from the previous turns, if it's the first turn we just send N guesses
					if(mergedOpinions.size() > i)
						if(i<N)
							toGuess.add(mergedOpinions.get(i).getName());
						else {
							System.out.println("Entering the bonus opinion selection");
							for(Opinion bonusOpinion: mergedOpinions) {
								if(bonusOpinion.getScore()>N) {
									toGuess.add(bonusOpinion.getName());
									break;
								}
							}
							
						}
				}
				System.out.println(myAgent.getLocalName() + ": Sending" +toGuess.toString());
				//guessFreq.forEach((k,v) -> {if(v==playerCount)toGuess.add(k);}); //if the choice is unanimous then we try to guess that
				/*if(toGuess.size() < N) {
					//we don't have enough guesses, we could avoid committing on thing that we don't like
					//add random words from opinions
					//for(int i=0; i< N - toGuess.size();)
					for(String s: guessFreq.keySet())
						if(!toGuess.contains(s) &&  N > toGuess.size()) {
							
							toGuess.add(s);
						}
					
				}*/
				String lastRes="";
				int guessSent = 0;
				if(toGuess.isEmpty()) {
					ACLMessage toChat2 = new ACLMessage(ACLMessage.INFORM);
					toChat2.setOntology("update-chat");
			        toChat2.addReceiver(pa.getGameMaster());
			        toChat2.setContent(pa.getTeam()+"#"+"Player"+"#"+"Selected to send nothing because there are no opinions");
			        myAgent.send(toChat2);
				}
				for(String s:toGuess) {
					// call api
					ApiCaller ca = new ApiCaller();
					GuessDto gd = new GuessDto();
					gd.NameGuess= s;
					gd.RoomId = pa.getGameId();
					lastRes = ca.callPutApi("http://localhost:8080/api/guess", gd);
					ACLMessage toChat2 = new ACLMessage(ACLMessage.INFORM);
					toChat2.setOntology("update-chat");
			        toChat2.addReceiver(pa.getGameMaster());
			        toChat2.setContent(pa.getTeam()+"#"+"Player"+"#"+"Selected: " + s);
			        myAgent.send(toChat2);
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
					if(!lastRes.equals(pa.getTeam()))
						break;	
					if(pa.getClue().N == guessSent) {
						//we can suppose that we got all the intended targets for the current turn, so we delete all the opinions with score <= N
						mergedOpinions.removeIf(o -> o.getScore() <= N);
					}
				}
				opinions = mergedOpinions;
				// we got out of the loop either because we hit the break or because we had finished the guesses
				if(lastRes.equals("black")) {
					System.out.println(CodeNamesUtils.getOtherTeam(pa.getTeam()).toUpperCase() + " TEAM WON");
					// We hit the killer word! Game has ended! Send to the GM the notify or notify all
					System.out.println("IL GIOCO é FINITO!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! SISTEMA QUESTA PARTE PER FAVORE");
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
        	if(phase.equals("wake-up-oracle")) {
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
        	
        }else {
        	// send all the selected words to the Team Coordinator
        	AID coord = pa.getCoordinator();
        	ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		    msg.addReceiver(coord);
		    String cont = "";
		    for(Opinion s : pa.getSelectedWords())
		    	cont += s.toString() + " | ";
		    msg.setContent(cont);
		    msg.setOntology(ONT);
		    myAgent.send(msg);
		    
		    ACLMessage toChat = new ACLMessage(ACLMessage.INFORM);
	        toChat.addReceiver(pa.getGameMaster());
	        toChat.setOntology("update-chat");
	        toChat.setContent(pa.getTeam()+"#Player#"+cont+ " ");
	        myAgent.send(toChat);
        	hasEnded = true;
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
    
}