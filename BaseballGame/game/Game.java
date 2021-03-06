package game;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JTable;

import ID.Serialized;
import atbat.HitTypeCalculator;
import helpers.DebuggingBuddy;
import objects.GameTeam;
import player.Player;
import stadium.Stadium;
import stats.BattingStatline;
import stats.PlateAppearance;
import team.Team;
import ui.BaseballGameDisplay;
import ui.BasicBoard;
import ui.FieldEventDisplay;
import ui.TeamBoxScore;
import view.GameContainer;
import view.StatsTable;
 
public class Game extends Serialized {
	
	private FieldEvent fieldEvent;
	
	private int numOuts = 0;
	private int inning = 1;
	private boolean topOfInning = true;
	
	private boolean teamsSwitched = false; //lets containing objects know when the teams are switched
	
	private GameContainer displayedOnScreen;
	
	//this will be swapped each half inning so that the same pointer can be used
	private GameTeam onOffense;
	private GameTeam onDefense;
	
	private Team homeTeam;
	private Team awayTeam;
	private GameMetadata info; 
	
	public final static String [] battingStatsDisplayed = {"", "Name", "AB", "H", "R", "RBI", "K", "BB"};
	public final static String [] pitchingStatsDisplayed = {"Name", "IP", "H", "R", "BB", "K", "HR"}; 
	
	public Game (Team home, Team away, Stadium stadium, int id, GameContainer displayedOnScreen, FieldEvent fieldEvent, int year, int leagueID) {
		
		super(id);
		
		this.onDefense = home.makeInGameTeam(true,0);
		this.onOffense = away.makeInGameTeam(false,0);
		
		this.homeTeam = home;
		this.awayTeam = away;
		
		this.fieldEvent = fieldEvent;
		
		this.info = new GameMetadata(year,leagueID,home.getID(),away.getID(),id);
		
		this.displayedOnScreen = displayedOnScreen;
		
		if (displayedOnScreen != null) {
			displayedOnScreen.addBaseballGame(this);
		}
		
	}
	
	public Game (Team home, Team away, int gameID, int leagueID, int year) {
		
		super(gameID);
		
		GameTeam homeGameTeam = home.makeInGameTeam(true, leagueID);
		GameTeam awayGameTeam =  away.makeInGameTeam(false, leagueID);
		
		this.onDefense = homeGameTeam;
		this.onOffense = awayGameTeam;
		this.homeTeam = home;
		this.awayTeam = away;
		
		Stadium stadiumForGame = Stadium.stdStadium();
		
		this.displayedOnScreen = new GameContainer (1600,1000,450,450,10,stadiumForGame,homeGameTeam,awayGameTeam);
		this.fieldEvent = new FieldEvent(1,stadiumForGame,displayedOnScreen.getFieldEventDisplay());
		this.info = new GameMetadata(year,leagueID,home.getID(),away.getID(),gameID);
		
	}
	
	public void playPlateAppearance () {
		
		Player nextBatter = onOffense.nextBatter();
		fieldEvent.setCurBatter(nextBatter);
		PlateAppearance paResult = fieldEvent.batterPitcherInteraction(onDefense.getFielders(), inning, numOuts);
				
		onOffense.updateBattersRBI(nextBatter.getpID(), fieldEvent.getIDRunnersScored().size());
		onOffense.updateBattersLine(nextBatter.getpID(), paResult);

		for (Integer cur: fieldEvent.getIDRunnersScored()) {
			
			Player curPlayer = onOffense.getPlayer(cur);
			onOffense.updateRunnersRuns(curPlayer.getpID());
			
		}
		
		onDefense.updatePitchersLine(onDefense.getCurrentPitcher().getpID(), paResult, (fieldEvent.getIDRunnersOut().size() + numOuts) > 3 ? 3 - numOuts : fieldEvent.getIDRunnersOut().size() ,fieldEvent.getIDRunnersScored().size());
		incrementOuts();
		
	}
	
	public void playGame () {
		
		while (!isGameOver()) {
			
			fieldEvent.setCurPitcher(onDefense.getCurrentPitcher());
			playPlateAppearance();
			displayedOnScreen.updateTeamBoxDisp(getHomeTeam(), getAwayTeam());
			
		}
		
		GameTeam homeGameTeam = onDefense;
		GameTeam awayGameTeam = onOffense;
				
	}
	
	public static Game basicGame () {
		
		Team home = new Team (1);
		Team away = new Team (2);
		Stadium stadium = Stadium.stdStadium();
		FieldEventDisplay disp = new FieldEventDisplay (450,450,10,stadium);
		
		home.addFakePlayers();
		away.addFakePlayers();
		
		return new Game (home, away, stadium, 1, new GameContainer(1600,1000), new FieldEvent(1,stadium,disp), 2018, 0);
		
	}

	public GameContainer getGameView () {
		return displayedOnScreen;
	}
	
	public boolean isGameOver () {
		return inning == 10 && topOfInning; 
	}
	
	public void incrementOuts () {
		
		numOuts += fieldEvent.getIDRunnersOut().size();
		
		//check for inning over
		if (numOuts >= 3) {
			nextHalfInning();
		}
		
	}
	
	public void nextHalfInning () {

		inning += topOfInning ? 0 : 1;
		topOfInning = !topOfInning;
		numOuts = 0;
		fieldEvent.nextHalfInning();
		
		teamsSwitched = true;
		
		GameTeam temp = onOffense;
		onOffense = onDefense;
		onDefense = temp;
		
		fieldEvent.setCurPitcher(onDefense.getCurrentPitcher());
		
	}
	
	public boolean didTeamsSwitch () {
		
		boolean result = teamsSwitched;
		
		if (teamsSwitched) {
			teamsSwitched = false;
		}
		
		return result;
		
	}
	
	public FieldEventDisplay getFieldEventDisp () {
		return fieldEvent.view;
	}

	public GameTeam getHomeTeam() {
		return topOfInning ? onDefense : onOffense;
	}

	public GameTeam getAwayTeam() {
		return topOfInning ? onOffense : onDefense;
	}
	
	public void saveGameStats (int leagueID, int year) {
		
		getHomeTeam().sendBattingStatsToTeam(homeTeam);
		getAwayTeam().sendBattingStatsToTeam(awayTeam);
		
	}
	
	public void shouldUIBeDrawn (boolean value) {
		fieldEvent.setDrawField(value);
	}
	
	
	
}
