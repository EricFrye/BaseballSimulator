package view;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import game.Game;
import objects.GameTeam;
import player.Player;
import stadium.Stadium;
import ui.BaseballGameDisplay;
import ui.BasicBoard;
import ui.FieldEventDisplay;
import ui.GameBoxScores;
import ui.TeamBoxScore;
import ui.StatsDisplay;

//hold the ui for a game being played

public class GameContainer extends JLabel {
	
	//the on field graphics
	private FieldEventDisplay fieldDisplay;
	//box scores for both teams
	private GameBoxScores boxScore;

	public GameContainer (int width, int height) {
		
		//initialize game view
		setSize(width,height);
		setVisible(true);
		setLayout(new FlowLayout());
		
	}
	
	public GameContainer (int width, int height, int fieldViewWidth, int fieldViewHeight, int foulSize, Stadium playedIn, GameTeam home, GameTeam away) {
		
		setSize(width,height);
		setVisible(true);
		setLayout(new FlowLayout());
		
		this.fieldDisplay = new FieldEventDisplay(fieldViewWidth, fieldViewHeight, foulSize, playedIn);
		add(this.fieldDisplay.getFieldImage());
		
		this.boxScore = new GameBoxScores (new StatsDisplay(home), new StatsDisplay(away));
		add(this.boxScore);
		
		revalidate();
		repaint();
		
	}
	
	public void addBaseballGame (Game game) {
		
		fieldDisplay = game.getFieldEventDisp();
		add(fieldDisplay.getFieldImage());
		
		boxScore = new GameBoxScores (new StatsDisplay(game.getHomeTeam()), new StatsDisplay(game.getAwayTeam()));
		add(boxScore);
		
		revalidate();
		repaint();
		
	}
	
	//updates a whole teams box, regardless if there is a change to be made
	public void updateTeamBoxDisp (GameTeam homeTeam, GameTeam awayTeam) {
		
		boxScore.updateHomeBox(homeTeam);
		boxScore.updateAwayBox(awayTeam);
		
	}
		
	public void addScroll (JScrollPane toAdd) {
		
		add(toAdd);
		revalidate();
		repaint();
		
	}
	
	public FieldEventDisplay getFieldEventDisplay () {
		return fieldDisplay;
	}
	
	
}
