package behaviours;
//import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
//import jade.lang.acl.ACLMessage;
//import jade.core.*;
import agents.PlayerAgent;
import entities.Clue;

public class WaitClue extends Behaviour {
    private int output;
    private boolean messageSent = false;
    private boolean clueReceived = false;
    private int clueCount = 1;
    public WaitClue(){
        super();
        output=0;
    }
    @Override
    public void action() {
    	PlayerAgent agent = (PlayerAgent) myAgent;
        
    	//counter++;
    	if(!messageSent) {
    		System.out.println(agent.getLocalName() + " From Wait: Sending Clue Request for clue number "+ clueCount);
    		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		    msg.addReceiver(agent.getOracle());
		    msg.setContent(clueCount + "");
		    msg.setOntology("get-clue");
		    msg.setConversationId("get-clue");
		    myAgent.send(msg);
		    messageSent = true;
		    clueCount++;
    	}
    	
    	MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchConversationId("get-clue"), MessageTemplate.MatchContent("game-closed"));
        ACLMessage req = myAgent.receive(mt);
    	if(req != null) {
    		if("get-clue".equals(req.getConversationId())){
    			agent.setClue(new Clue(req.getContent()));
        		output = 1;
    		}else {
    			output = 2; // go to GameClosed Behaviour
    		}
    		clueReceived = true;//to trigger doneCondition
    	}else {
    		block();
    	}
    }
    public int onEnd(){
    	clueReceived = false;
    	messageSent = false;
    	int temp = output;
        output=0;
        return temp;
    }
	@Override
	public boolean done() {
		return clueReceived;
	}
}