package agents;

import jade.core.*;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import utils.CodeNamesUtils;
import utils.GameGUI;
import behaviours.*;
//import WebServer.src.main.java.entities.GameStatus.java;
import entities.Clue;
import entities.GameStatus;
import entities.Opinion;

import java.util.List;

public class PlayerAgent extends Agent {

	private static final String INITIALIZE = "INITIALIZE";
	private static final String INITIALIZE1 = "INITIALIZE1";
	private static final String WAIT = "WAIT";
	private static final String PREPARE = "PREPARE";
	private static final String DISCUSS = "DISCUSS";
	private static final String END = "END";

	private Clue currentClue;
	private String currentGameId;
	private GameStatus gs = null;
	//private HashMap<String, List<BabelSense>> boardSenses;
	private List<Opinion> selectedWords;
	private boolean isGameMngr = false;
	private boolean isTeamCoord = false;
	private String team;
	private String randomSeed; // Need to explore seeding random numbers from a string
	private AID gameMaster;
	private AID oracle;
	private AID enemyOracle;
	private AID coordinator;
	private GameGUI gg;
	private String strategy;

	protected void setup() {
		System.out.println("Hello! I am a player agent, my name is " + getAID().getName());

		Object[] args = getArguments();
		if (args != null) {
			// In the args we will expect a random seed (??), and the team code
			System.out.println("Args:");
			for (Object arg : args) {
				System.out.println("- " + arg);
				if (arg.equals("game-manager")) {
					setGameMngr(true);
					addBehaviour(new CreateRoom("8f6b434d794848c99adb70a44050d58a"));
				} else if (arg.equals("red") || arg.equals("blue"))
					setTeam(arg.toString());
				else if (arg.equals("team-coord")) {
					setTeamCoord(true);
				} else if (arg.toString().contains("str:")) {
					setStrategy(arg.toString().split(":")[1]); // get the strategy from args
				}
			}
		}

		if (isGameMngr) {
			addBehaviour(new GameManager());
			setGameMaster(getAID());
		}

		// for each kind of role that we have we need to subscribe with it as a service
		System.out.println(getName() + " Subscribing to DF as a Player Service");
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType(getTeam() + "-player"); // type of service
		sd.setName(getName()); // name of specific service
		dfd.addServices(sd);
		if (isTeamCoord) {
			ServiceDescription st = new ServiceDescription();
			st.setType("team-coord"); // type of service
			st.setName(team); // name of service/team
			dfd.addServices(st);
		}
		if (isGameMngr) {
			ServiceDescription sg = new ServiceDescription();
			sg.setType("game-manager"); // type of service
			sg.setName("codenames"); // name of service
			dfd.addServices(sg);
		}
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		if (!isTeamCoord) {
			// retrieve the team coord AID
			dfd = new DFAgentDescription();
			sd = new ServiceDescription();
			sd.setType("team-coord"); // type of service
			sd.setName(getTeam()); // name of service
			dfd.addServices(sd);
			AID[] gms = new AID[0]; // initialize as an empty array
			try {
				DFAgentDescription[] result = DFService.searchUntilFound(this, getDefaultDF(), dfd,
						new SearchConstraints(), 6000);
				// BLOCKING CALL!!
				gms = new AID[result.length];
				for (int i = 0; i < result.length; i++) {
					gms[i] = result[i].getName();
				}
			} catch (FIPAException fe) {
				fe.printStackTrace();
			}

			if (gms.length > 0) {
				System.out.println(getAID().getName() + ": Coordinator Found");
				setCoordinator(gms[0]);
			}
		}

		if (!isGameMngr) {
			// retrieve the game manager AID
			dfd = new DFAgentDescription();
			sd = new ServiceDescription();
			sd.setType("game-manager"); // type of service
			sd.setName("codenames"); // name of service
			dfd.addServices(sd);
			AID[] gms = new AID[0]; // initialize as an empty array
			try {
				DFAgentDescription[] result = DFService.searchUntilFound(this, getDefaultDF(), dfd,
						new SearchConstraints(), 6000);
				// BLOCKING CALL!!
				gms = new AID[result.length];
				for (int i = 0; i < result.length; i++) {
					gms[i] = result[i].getName();
				}
			} catch (FIPAException fe) {
				fe.printStackTrace();
			}

			if (gms.length > 0) {
				System.out.println(getAID().getName() + ": Coordinator Found");
				setGameMaster(gms[0]);
			}
		}

		// retrieve team oracle AID
		dfd = new DFAgentDescription();
		sd = new ServiceDescription();
		sd.setType("oracle"); // type of service
		sd.setName(getTeam()); // name of service
		dfd.addServices(sd);
		AID[] gms = new AID[0]; // initialize as an empty array
		try {
			// DFAgentDescription[] result = DFService.search(myAgent, dfd);
			DFAgentDescription[] result = DFService.searchUntilFound(this, getDefaultDF(), dfd, new SearchConstraints(),
					6000);
			gms = new AID[result.length];
			for (int i = 0; i < result.length; i++) {
				gms[i] = result[i].getName();
			}
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		if (gms.length > 0) {
			System.out.println(getAID().getName() + ": Oracle Found");
			setOracle(gms[0]);
		}

		// retrieve enemy oracle AID
		dfd = new DFAgentDescription();
		sd = new ServiceDescription();
		sd.setType("oracle"); // type of service
		System.out.println("I am of team: " + team + "my enemy team is:" + CodeNamesUtils.getOtherTeam(team));
		sd.setName(CodeNamesUtils.getOtherTeam(team)); // name of service
		dfd.addServices(sd);
		gms = new AID[0]; // initialize as an empty array
		try {
			// DFAgentDescription[] result = DFService.search(myAgent, dfd);
			DFAgentDescription[] result = DFService.searchUntilFound(this, getDefaultDF(), dfd, new SearchConstraints(),
					6000);
			gms = new AID[result.length];
			for (int i = 0; i < result.length; i++) {
				gms[i] = result[i].getName();
			}
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		if (gms.length > 0) {
			System.out.println(getAID().getName() + ": Enemy Oracle Found");
			setEnemyOracle(gms[0]);
		}

		// We will use FSMBehaviour to manage the transition between states.
		FSMBehaviour fsm = new FSMBehaviour(this);
		fsm.registerFirstState(new InitializePlayer(0), INITIALIZE);
		fsm.registerState(new InitializePlayer(1), INITIALIZE1);
		fsm.registerState(new WaitClue(), WAIT);
		fsm.registerState(new PrepareGuesses(), PREPARE);
		fsm.registerState(new DiscussGuesses(), DISCUSS);
		fsm.registerLastState(new GameClosed(), END);

		fsm.registerTransition(INITIALIZE, INITIALIZE, 0); // if there is no game manager initialized we need it
		fsm.registerTransition(INITIALIZE, INITIALIZE1, 1); // game manager found move to wait for a gameId
		fsm.registerTransition(INITIALIZE1, INITIALIZE1, 1); // still waiting for an answer
		fsm.registerTransition(INITIALIZE, WAIT, 2); // if game manager skip "INITIALIZE1"
		fsm.registerTransition(INITIALIZE1, WAIT, 2); // game id got start preparing clues
		// fsm.registerTransition(WAIT,WAIT,0); // may be not useful, check twice cut
		// once
		fsm.registerTransition(WAIT, PREPARE, 1); // When we are done waiting start preparing guesses
		fsm.registerDefaultTransition(PREPARE, DISCUSS); // After finding all the guesses discuss with other players
		fsm.registerTransition(DISCUSS, DISCUSS, 0); // We may need to continue discussing
		fsm.registerTransition(DISCUSS, WAIT, 1); // After discussing back to waiting for the next clue
		fsm.registerTransition(DISCUSS, END, 2); // After discussing if the game has ended go to the GameClosed Behavior
		fsm.registerTransition(WAIT, END, 2); // we may go to the end state while we are waiting
		fsm.registerTransition(INITIALIZE, END, 3); // we may go to the end state while we are waiting
		fsm.registerTransition(INITIALIZE1, END, 3); // we may go to the end state while we are waiting

		addBehaviour(fsm);
	}

	protected void takeDown() {
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		System.out.println("Me " + getAID().getName() + " is ded");
	}

	public void setClue(Clue clue) {
		currentClue = clue;
	}

	public Clue getClue() {
		return currentClue;
	}

	public void setGameId(String gameId) {
		currentGameId = gameId;
	}

	public String getGameId() {
		return currentGameId;
	}

	public void setGameStatus(GameStatus gamestatus) {
		gs = gamestatus;
	}

	public GameStatus getGameStatus() {
		return gs;
	}
	/* 
	public void setBoardSenses(HashMap<String, List<BabelSense>> bss) {
		boardSenses = bss;
	}

	public HashMap<String, List<BabelSense>> getBoardSenses() {
		return boardSenses;
	}*/

	public boolean isGameMngr() {
		return isGameMngr;
	}

	public void setGameMngr(boolean isGameMngr) {
		this.isGameMngr = isGameMngr;
	}

	public String getTeam() {
		return team;
	}

	public void setTeam(String team) {
		this.team = team;
	}

	public String getRandomSeed() {
		return randomSeed;
	}

	public void setRandomSeed(String randomSeed) {
		this.randomSeed = randomSeed;
	}

	public AID getOracle() {
		return oracle;
	}

	public void setOracle(AID oracle) {
		this.oracle = oracle;
	}

	public List<Opinion> getSelectedWords() {
		return selectedWords;
	}

	public void setSelectedWords(List<Opinion> selectedWords) {
		this.selectedWords = selectedWords;
	}

	public boolean isTeamCoord() {
		return isTeamCoord;
	}

	public void setTeamCoord(boolean isTeamCoord) {
		this.isTeamCoord = isTeamCoord;
	}

	public AID getCoordinator() {
		return coordinator;
	}

	public void setCoordinator(AID coordinator) {
		this.coordinator = coordinator;
	}

	public AID getGameMaster() {
		return gameMaster;
	}

	public void setGameMaster(AID gameMaster) {
		this.gameMaster = gameMaster;
	}

	public AID getEnemyOracle() {
		return enemyOracle;
	}

	public void setEnemyOracle(AID enemyOracle) {
		this.enemyOracle = enemyOracle;
	}

	public GameGUI getGg() {
		return gg;
	}

	public void setGg(GameGUI gg) {
		this.gg = gg;
	}

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}
}