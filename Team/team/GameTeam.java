package team;
/* Eric Frye
 * InGameTeam is a team that is taking part in a Game.
 * */

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import datatype.CircularLinkedList;
import game.Linescore;
import manager.Manager;
import objects.Fielder;
import player.Player;

public class GameTeam {
	
	private CircularLinkedList <Player> lineup; //Lineup. This variable is a CLL because a lineup loops back up to the first spot once it reaches the end.
	private List <Fielder> inTheField;
	private Player pitcher; //Current player on the mound.
	private HashSet <Player> bench; //Available players on bench.  This variable is a set because there is no ordering of the players.
	private HashSet <Player> bullPen; //Available players in the bullpen.  No specific ordering.
	private Manager manager; //Manager.
	private Linescore score;
	
	public GameTeam (CircularLinkedList <Player> lineup, Player pitcher, HashSet <Player> bench, HashSet <Player> bullPen, Manager manager, boolean homeTeam, List <Fielder> inField) {
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
	
	public List <Fielder> getFielders () {
		return inTheField;
	}
	
	//returns the player that will throw the next pitch
	public Player getCurrentPitcher () {
		return pitcher;
	}
	
	//returns the next player due up in the batting order
	public Player nextBatter () {
		return lineup.next();
	}
	
}
