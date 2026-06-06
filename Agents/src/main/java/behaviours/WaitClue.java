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
    private int status;
    private boolean messageSent = false;
    private boolean clueRecieved = false;
    private int clueCount = 1;
    public WaitClue(){
        super();
        status=0;
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
        		status = 1;
    		}else {
    			status = 2; // go to GameClosed Behaviour
    		}
    		clueRecieved = true;//to trigger doneCondition
    	}else {
    		block();
    	}
        // recieve message, if message if of type "roomcode" save it but don't change status
        // for now we use a pre existing room for testing
        //agent.setGameId("bc6e0458ca2b4bea817afd9aede5d834");
        // recieve message, when message is Received, do stuff
        //agent.setClue(new Clue(2,"plant"));
        //counter ++;

        //ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        //msg.addReceiver(new AID("Peter",AID.ISLOCALNAME));
        //msg.setContent("Fuck you");
        //myAgent.send(msg);
    }
    public int onEnd(){
    	clueRecieved = false;
    	messageSent = false;
    	int temp = status;
        status=0;
        return temp;
    }
    /* 
    @Override
    public boolean done() {
        if(counter>=10)
            return true;
        return false;
    }
    */
	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return clueRecieved;
	}
}