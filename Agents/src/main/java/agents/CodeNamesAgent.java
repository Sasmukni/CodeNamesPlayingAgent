package agents;
import org.apache.commons.lang.NotImplementedException;

import entities.GameStatus;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import utils.GameGUI;


public class CodeNamesAgent extends Agent{
	/*private static final String INITIALIZE = "INITIALIZE";
	private static final String INITIALIZE1 = "INITIALIZE1";
	private static final String GENERATE = "GENERATE";
	private static final String WAIT = "WAIT";
	private static final String END = "END";
	*/
    private String currentGameId;
    private GameStatus gs = null;
    private boolean isGameMngr = false;
    private String team;
    private AID gameManager;
    private String strategy;
    private GameGUI gg;

	@Override
	protected void setup() {
        throw new NotImplementedException();
    }

	@Override
	protected void takeDown(){
		// Unregister from the yellow pages 
		try { 
		DFService.deregister(this); 
		} 
		catch (FIPAException fe) { 
		fe.printStackTrace(); 
		}
		System.out.println("Me " + getAID().getName() + " is dead");
	}
	
	public void setGameId(String gameId){
        currentGameId = gameId;
    }

    public String getGameId(){
        return currentGameId;
    }

    public void setGameStatus (GameStatus gamestatus){
        gs = gamestatus;
    }

    public GameStatus getGameStatus(){
        return gs;
    }
    
    public void setGameMgr(boolean gm) {
    	isGameMngr = gm;
    }
    
    public boolean isGameMgr() {
    	return isGameMngr;
    }
	public String getTeam() {
		return team;
	}
	public void setTeam(String team) {
		this.team = team;
	}
	public String getStrategy() {
		return strategy;
	}
	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}
	public GameGUI getGg() {
		return gg;
	}
	public void setGg(GameGUI gg) {
		this.gg = gg;
	}
	public AID getGameMaster() {
		return gameManager;
	}
	public void setGameMaster(AID gameManager) {
		this.gameManager = gameManager;
	}

	protected void retrieveGameManager(){
        DFAgentDescription dfd = new DFAgentDescription(); 
		ServiceDescription sd = new ServiceDescription(); 
		sd.setType("game-manager"); // type of service 
		sd.setName("codenames"); // name of service
		dfd.addServices(sd); 
		AID[] gms = new AID[0]; //initialize as an empty array
		try { 
			DFAgentDescription[] result = DFService.searchUntilFound(this,getDefaultDF(), dfd,new SearchConstraints(),6000);
			//BLOCKING CALL!!
			gms = new AID[result.length];
			for(int i=0; i<result.length; i++) {
				gms[i] = result[i].getName();
			}
		}
		catch (FIPAException fe) { 
			fe.printStackTrace(); 
		}
		if(gms.length > 0) {
			System.out.println(getAID().getName()+": Game Manager Found");
			setGameMaster(gms[0]);
		}
	}

    protected void subscribeToDF(){
        throw new NotImplementedException();
    }
}