/* Eric Frye
 * InGameTeam is a team that is taking part in a Game.
 * */

import java.util.HashSet;
import java.util.LinkedList;

public class GameTeam {
	
	CircularLinkedList <GamePlayer> lineup; //Lineup. This variable is a CLL because a lineup loops back up to the first spot once it reaches the end.
	LinkedList <GamePlayer> inTheField;
	GamePlayer pitcher; //Current player on the mound.
	HashSet <GamePlayer> bench; //Available players on bench.  This variable is a set because there is no ordering of the players.
	HashSet <GamePlayer> bullPen; //Available players in the bullpen.  No specific ordering.
	Manager manager; //Manager.
	Linescore score;
	
	public GameTeam (CircularLinkedList <GamePlayer> lineup, GamePlayer pitcher, HashSet <GamePlayer> bench, HashSet <GamePlayer> bullPen, Manager manager, boolean homeTeam, LinkedList <GamePlayer> inField) {
		this.lineup = lineup;
		this.pitcher = pitcher;
		this.bench = bench;
		this.bullPen = bullPen;
		this.manager = manager;
		this.score = new Linescore (homeTeam);
		this.inTheField = inField;
	}
	
	public GameTeam (GameTeam copy) {
		
		lineup = copy.lineup;
		pitcher = copy.pitcher;
		bench = copy.bench;
		bullPen = copy.bullPen;
		manager = copy.manager;
		score = copy.score;
		
	}

}
