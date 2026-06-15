package behaviours;
//import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
//import jade.lang.acl.ACLMessage;
//import jade.core.*;
import agents.OracleAgent;
import entities.Clue;

public class ManageClueRequests extends Behaviour {
	private int newestClue = 0;
    public ManageClueRequests(){
        super();
    }
    @Override
    public void action() {
        // We don't want to receive requests before we have a clue ready!!
    	OracleAgent ag = (OracleAgent) myAgent;
        if(ag.getClue() == null)
        	return; 
    	MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("get-clue"),MessageTemplate.MatchContent(newestClue + ""));
        ACLMessage req = myAgent.receive(mt);
    	if(req != null) {
    		int clueNumber = Integer.parseInt(req.getContent());
    		Clue c = ag.getGameClues().get(clueNumber);
    		
    		if(c==null) {
    			System.out.println(myAgent.getLocalName() + ": I don't have an answer for this request yet");
    			return;
    		}
	        ACLMessage reply = req.createReply(ACLMessage.INFORM);
	        reply.setContent(c.toString()); // if we have a clue, respond with it
        	myAgent.send(reply);
    	}else {
    		block();
    	}
        
    }
	@Override
	public boolean done() {
		return false;
	}
	
	public void setNewestClue(int nclue) {
		this.newestClue = nclue;
	}
}