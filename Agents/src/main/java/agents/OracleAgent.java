package agents;
import java.util.HashMap;

import behaviours.CreateRoom;
import behaviours.GameClosed;
import behaviours.GameManager;
import behaviours.InitializeOracle;
import behaviours.ManageClueRequests;
import behaviours.PrepareClues;
import behaviours.Wait;
import entities.Clue;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;


public class OracleAgent extends CodeNamesAgent{
	private static final String INITIALIZE = "INITIALIZE";
	private static final String INITIALIZE1 = "INITIALIZE1";
	private static final String GENERATE = "GENERATE";
	private static final String WAIT = "WAIT";
	private static final String END = "END";
	
    //private String currentGameId;
    private Clue selectedClue; //the last clue
    private HashMap<Integer,Clue> gameClues = new HashMap<>(); //the history of clues during the current game
    //private GameStatus gs = null;
    //private boolean isGameMngr = false;
    //private String team;
    //private AID gameManager;
    private Behaviour mngClueReq;
    //private String strategy;
    //private GameGUI gg;

	@Override
	protected void setup() {
		Object[] args = getArguments();
		//An oracle Agent could also be a GameManager (as a bonus role defined in the parameters)
		if(args != null){
			for(Object arg : args){
				if(arg.equals("game-manager")) {
					setGameMgr(true);
					addBehaviour(new CreateRoom(null));//"cf540d34000749b38c4346bb5f9be229"
				}else if(arg.equals("red") || arg.equals("blue"))
					setTeam(arg.toString());
				else if(arg.toString().contains("str:")){
					setStrategy(arg.toString().split(":")[1]); // get the strategy from args
				}
			}
		}
		System.out.println("Hello! I am a Oracle Agent, my name is "+getAID().getName() + " Properties: team:" +getTeam() + " strategy:" + getStrategy());
		
		subscribeToDF();
		if(isGameMgr()) {
			addBehaviour(new GameManager());
			setGameMaster(getAID());
		}else{
		//if(!isGameMngr) {
			//retrieve the game manager AID
			retrieveGameManager();
		}
		//The clue Manager is a special behavior, we could need to restart it from other behaviors.
		mngClueReq = new ManageClueRequests();
		addBehaviour(mngClueReq); //each oracle has to manage clue requests
		
		//We will use FSMBehaviour to manage the transition between states.
        FSMBehaviour fsm = new FSMBehaviour(this);
        fsm.registerFirstState(new InitializeOracle(0), INITIALIZE); // we will find a new beahviour for the initialization step
        fsm.registerState(new InitializeOracle(1), INITIALIZE1);
        fsm.registerState(new PrepareClues(), GENERATE);
        fsm.registerState(new Wait(), WAIT);
        fsm.registerLastState(new GameClosed(), END);

        fsm.registerTransition(INITIALIZE,INITIALIZE,0); // if there is no game manager initialized we need it
        fsm.registerTransition(INITIALIZE,INITIALIZE1,1); // game manager found move to wait for a gameId
        fsm.registerTransition(INITIALIZE1,INITIALIZE1,1); // still waiting for an answer
        fsm.registerTransition(INITIALIZE,GENERATE,2); //if game manager skip "INITIALIZE1"
        fsm.registerTransition(INITIALIZE1,GENERATE,2); // game id got start preparing clues
        fsm.registerTransition(GENERATE, GENERATE,0); //We may need to continue preparing // wait something
    	fsm.registerTransition(GENERATE, WAIT,1); //We have generated a clue, now we wait
    	fsm.registerTransition(WAIT, GENERATE,1); //We have waited enough, now we wake up
    	fsm.registerTransition(WAIT, END, 2); // Yeah, this game has ended
        fsm.registerTransition(INITIALIZE, END, 3); // we may go to the end state while we are initializing (errors with the game server)
        fsm.registerTransition(INITIALIZE1, END, 3); // we may go to the end state while we are initializing (errors with the game server)
        
        addBehaviour(fsm);
	}
	/* 
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
    
    public void setIsGameMgr(boolean gm) {
    	isGameMngr = gm;
    }
    
    public boolean getIsGameMgr() {
    	return isGameMngr;
    }
	public String getTeam() {
		return team;
	}
	public void setTeam(String team) {
		this.team = team;
	}*/
	public Clue getClue() {
		return selectedClue;
	}
	public void setClue(Clue selectedClue) {
		this.selectedClue = selectedClue;
	}
	public HashMap<Integer,Clue> getGameClues() {
		return gameClues;
	}
	public void setGameClues(HashMap<Integer,Clue> gameClues) {
		this.gameClues = gameClues;
	}
	public Behaviour getMngClueReq() {
		return mngClueReq;
	}
	/* 
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
	}*/

	@Override
	protected void subscribeToDF(){
		System.out.println(getName() + " Subscribing to DF as a Oracle Service");
		DFAgentDescription dfd = new DFAgentDescription(); 
        dfd.setName(getAID()); 
        ServiceDescription sd = new ServiceDescription(); 
        sd.setType("oracle"); // type of service 
        sd.setName(getTeam()); // name of specific service
        dfd.addServices(sd);
        if(isGameMgr()) {
        	ServiceDescription sg = new ServiceDescription(); 
        	sg.setType("game-manager"); // type of service 
            sg.setName("codenames"); // name of service
            dfd.addServices(sg);
        }
        try { 
        	DFService.register(this, dfd); 
        }
        catch (FIPAException fe) { 
        	fe.printStackTrace(); 
        }
	}
}