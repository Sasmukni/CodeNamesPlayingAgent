package behaviours;
//import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
//import jade.lang.acl.ACLMessage;
//import jade.core.*;
import agents.PlayerAgent;
import entities.Clue;

public class Wait extends Behaviour {
    private int status;
    public Wait(){
        super();
        status=0;
    }
    @Override
    public void action() {
    	MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchContent("wake-up"), MessageTemplate.MatchContent("game-closed"));
    	ACLMessage msg = myAgent.receive(mt);
    	if(msg == null) {
    		block();
    	}else {
    		if("wake-up".equals(msg.getContent())){
    			System.out.println(myAgent.getLocalName()+": I AM AWAKE NOW");
    			status = 1;
    		}else {
    			status = 2; // go to GameClosed Behaviour
    		}
    	}
    }
    public int onEnd(){
    	int temp = status;
    	status = 0;
		System.out.println(myAgent.getLocalName()+": onEnd method is resetting the counter");
    	return temp;
    }
	@Override
	public boolean done() {
		return status > 0;
	}
}