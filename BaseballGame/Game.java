import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import javax.swing.*;

/* Eric Frye
 * Game is a game of baseball. A game of baseball is played with two teams.  The rules of the game are governed by a RuleSet object.
 * */

public class Game {

	//these constants are to be used when accessing the fielders linkedlist
	static final int PITCHERNUM = 0;
	static final int CATCHERNUM = 1;
	static final int FIRSTNUM = 2;
	static final int SECONDNUM = 3;
	static final int THIRDNUM = 4;
	static final int SHORTNUM = 5;
	static final int LEFTNUM = 6;
	static final int CENTERNUM = 7;
	static final int RIGHTNUM = 8;

	static final int HOMEPLATE = 3;
	static final int FIRSTBASE = 0;
	static final int SECONDBASE = 1;
	static final int THIRDBASE = 2;
	
	final int WAIT;
	
	int gID = 0; //ID of the game being played. The default value is 0.
	boolean extraInnings = false; //If the game has entered extra innings.
	RuleSet rules; //Rules for the game. 
	GameTeam homeTeam; //Team for home team.
	GameTeam awayTeam; //Teams for away team.
	Linescore homeScore = new Linescore (true); //Hits Runs and Errors for home team.
	Linescore awayScore = new Linescore (false); //Hits Runs and Errors for away team.
	InningCounters inningsCTR = new InningCounters (); //Count of balls, strikes, outs, inning
	GameState state = GameState.STANDARD; //A game starts in standard state.
	GameTeam atBat ; //Away team always bats first.
	GameTeam inField; //Home team always fields first.
	Linescore toUpdate = awayScore; //Team at bat.
	Linescore nextToUpdate = homeScore; //Team in the field.
	GamePlayer curBatter;
	GamePlayer curPitcher;
	Stadium stadium;
	GameDisplay view;
	GameLogger log = new GameLogger ();
	//FieldEvent status = new FieldEvent ();
	Base [] bases = new Base [4]; 
	static Queue <Message> messages = new LinkedList <Message> ();
	
	public Game (RuleSet rules, int id, GameTeam homeTeam, GameTeam awayTeam, Stadium stadium, int wait) {

		this.rules = rules;
		this.stadium = stadium;
		gID = id;
		atBat = awayTeam;
		inField = homeTeam;
		this.homeTeam = homeTeam;
		this.awayTeam = awayTeam;
		curBatter = atBat.lineup.next();
		curPitcher = inField.pitcher;
		
		WAIT = wait;
		
		view = new GameDisplay (500,500, this.stadium.dim.get("f"), stadium);
		
		bases[0] = (new Base (FieldConstants.firstBase(), BaseType.FIRST));
		bases[1] = (new Base (FieldConstants.secondBase(), BaseType.SECOND));
		bases[2] = (new Base (FieldConstants.thirdBase(), BaseType.THIRD));
		bases[3] = (new Base (FieldConstants.homePlate(), BaseType.HOME));
		
		//view.drawField(-1*this.stadium.field.foulTerritory+1, this.stadium);
		//view.drawFieldOutline();
		
	}
	
	public void fieldEvent (LinkedList <Fielder> onTheField, BallInPlay hitBall, List <Baserunner> runners, GamePlayer batter) {
		
		Fielder lastBallHandler = null;
		Fielder currentlyHasBall = null;
		
		double time = 0;
		
		//recalculate landing spot whenever you need to
		BallInPlay airModel = hitBall.modelBallDistance(true);
		BallInPlay finalModel = hitBall.modelBallDistance(false);
		
		Map <String, BallInPlay> models = new HashMap <String, BallInPlay> ();
		models.put("aM", airModel);
		models.put("fM", finalModel);
		
		//add flag to make new decisions
		Message newFielderDes = new MakeNewDecisions();
		messages.add(newFielderDes);
		
		Baserunner newBaserunner = new Baserunner (batter,log, BaseType.HOME);
		hitBall.batter = newBaserunner;
		
		newBaserunner.batterBaseBrain(models, onTheField, hitBall);
		runners.add(newBaserunner);

		//place runners
		for (Baserunner curRunner: runners) {
			bases[curRunner.baseOn.num()].arriveAtBase(curRunner);
		}
		
		//first always starts as a force out
		bases[0].setForceOut(true);
			
		
		//set force outs for rest of bases
		for (int i = 1; i < bases.length; i++) {
			
			if (!bases[i-1].runnerOn()) {
				break;
			}

			bases[i].setForceOut(true);
			
		}
		
		while (!hitBall.state.equals(BallStatus.DEAD)) {
			
			//process messages
			for (Message cur: messages) {
				
				System.out.println(cur);
				
				if (cur instanceof AdvancingNumberOfBases) {
										
					//brain for all runners
					for (Baserunner curRunner: runners) {
						curRunner.baserunnerBrain(((AdvancingNumberOfBases) cur).getNumBases());
					}
					
				}
				
				else if (cur instanceof MakeNewDecisions) {
					
					Fielder chasing = fielderToGetBall(onTheField,models.get("fM"));
					decideRemainingFielders(onTheField,models.get("fM"), chasing);
					
				}
				
				
				else if (cur instanceof BaserunnerOutMsg) {
					
					runners.remove(((BaserunnerOutMsg) cur).runner);
					view.drawBall(((BaserunnerOutMsg) cur).runner.lastLoc, 0x000000,1); //remove baserunner from view
					
				}
				
				else if (cur instanceof RunScoredMsg) {
					runners.remove(((RunScoredMsg) cur).scorer);
				}
				
			}
			
			messages.clear(); //clear out messages for next turn
			
			time += Physics.tick;
						
			if (hitBall.state.equals(BallStatus.IN_AIR)) {
				view.drawBall(airModel.loc, 0x00FF00,0);
			}

			hitBall.tick(stadium, hitBall.thrown);
			
			//redraw how the field looks
			view.drawFieldOutline();
			view.drawBall(hitBall.lastLoc, 0x00000,1);
			view.drawBall(hitBall.loc, 0xFF0000,1);
			view.repaint();
			
			if (currentlyHasBall != null) {
				currentlyHasBall.throwingBrain(log, bases, runners, hitBall, onTheField);
				currentlyHasBall = null;
			}
			
			//move and draw location
			for (Fielder cur: onTheField) {
				cur.move(hitBall, log);
				
				//pick up ball
				if (cur != lastBallHandler && (cur.loc.diff(hitBall.loc).mag() < 4) && hitBall.loc.z < cur.getReach()) {
					cur.receiveBall(hitBall, log, runners);
					currentlyHasBall = cur;
					lastBallHandler = cur;
					cur.flipHasBall();
				}
				
				view.drawBall(cur.lastLoc, 0x000000,1);
				view.drawBall(cur.loc, 0x008000,1);
			}
						
			//moves and draw location
			for (Baserunner cur: runners) {
				cur.run(bases);
				view.drawBall(cur.lastLoc, 0x000000,1);
				view.drawBall(cur.loc, 0x0000FF,1);
				
			} 

			try {
				Thread.sleep(WAIT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
	}

	public void playGame (boolean debug) {

		Scanner delay = new Scanner (System.in); //For debugging.

		System.out.println("\nThe game is in the top of the 1.");
		System.out.println(curPitcher.fullName() + " is on the mound.");

		while (state != GameState.OVER) {

			System.out.println("New at bat.");
			System.out.println(curBatter.fullName() + " is at the plate.");
			System.out.println("Outs: " + inningsCTR.outs);

			AtBatState curAtBatState = AtBatState.ONGOING;

			while (curAtBatState == AtBatState.ONGOING) {

				ThrownPitch incomingPitch = curPitcher.throwPitch(curBatter, inField.inTheField.get(CATCHERNUM));

			}

			/*
			 * After this point the at bat has ended.  All baserunner positions must now be updated.  All counters must be updated or reset.
			 * */

			//move to next half inning.
			if (inningsCTR.outs == 3) {
				state = halfInningOver();
				GameTeam temp = new GameTeam (atBat);
				atBat = new GameTeam (inField);
				inField = new GameTeam (temp);
				curPitcher = inField.pitcher;

				if (state != GameState.OVER)
					System.out.println(curPitcher.fullName() + " is on the mound.");

			}

			//next batter up
			curBatter = atBat.lineup.next();
			curAtBatState = AtBatState.ONGOING;
			inningsCTR.nextAtBat();

		}

		System.out.println("Game Over!");

	}
	
	//returns the fielder that can get the a hit ball the earliest.  their destination field will be properly updated
	private Fielder fielderToGetBall (List <Fielder> fielders, BallInPlay fullModel) {
		
		double bestTime = Double.MAX_VALUE;
		Fielder toRet = null;
		Coordinate3D loc = null;
		
		for (Fielder curFielder: fielders) {
			
			LocationTracker cur = curFielder.firstReachableSpot(fullModel);
			
			double distance = cur.loc.diff(curFielder.loc).mag();
			
			if (distance < bestTime) {
				bestTime = distance;
				toRet = curFielder;
				loc = cur.loc;
			}
			
		}
		
		toRet.destination = loc;
		return toRet;
		
	}

	//Swaps which team is fielding and which is batting.  To be used after an inning is over.
	private GameState halfInningOver () {

		//end of game
		if (!inningsCTR.top && inningsCTR.inning == rules.numInnings) {
			return GameState.OVER;
		}

		System.out.println("Switching sides.");
		inningsCTR.nextHalfInning();

		for (int i = 0; i < 4; i++) {
			bases[i] = null;
		}
		
		return GameState.STANDARD;

	}
	
	/* 
	 * determines the action for the remaining fielders.  assumes that status.chasingBall has been updated
	 * @param model the ball in play being modelled
	 * */
	private void decideRemainingFielders (List <Fielder> fielders, BallInPlay model, Fielder chaser) {
		
		for (Fielder curFielder: fielders) {
				
			if (curFielder.destination == null) {
				curFielder.movementBrain(model, bases,fielders,chaser);
			}
			
		}
		
	} 
	
	

	/*
	 * GameState defines the state of a baseball game.  
	 * Delay refers to any event that would delay a baseball game.   
	 * Over ends the loop that lets the next batter come to the plate.
	 * */
	enum GameState {
		STANDARD,DELAY,NEXT_RUN_WINS,OVER
	}

	/*
	 * AtBatState defines the state of an at bat.
	 * Ongoing is the standard case.
	 * Over means that the at bat has ended and a next batter is due up.  A change of half inning is also possible.  The GameState should be checked for OVER
	 * */
	enum AtBatState {
		ONGOING,OVER,BALL_IN_PLAY
	}


}
