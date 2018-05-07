package ball;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import datatype.Coordinate3D;
import game.Game;
import main.Baserunner;
import main.Fielder;
import main.OnFieldObject;
import messages.BallOverWallMsg;
import physics.Physics;
import stadium.Stadium;
import stadium.Wall;

//a batted ball that obeys basic laws of physics.  measurments in feet and seconds
public class BallInPlay extends OnFieldObject {

	public final double launchSpeed;
	public final double launchAngle; //angle the ball was hit wrt ground
	public final double launchDir; //xy plane direction
	public Coordinate3D velocity; //velocity of ball in 3 directions
	public BallStatus state = BallStatus.IN_AIR; //if the ball is in the air.  if true subject to air resistance.  if false, subject to friction with field
	public double airDistance;
	public double airTime;
	public boolean canRecordOut = true;
	public boolean thrown = false;
	public InPlayType type;
	public Stadium stad;
	public Baserunner batter = null;
	private Fielder holding = null;

	public BallInPlay (Coordinate3D loc, double launchAngle, double launchDir, double launchSpeed, Stadium stad, int color) {
		super(loc,loc, color);
		this.launchSpeed = launchSpeed;
		this.launchAngle = launchAngle;
		this.launchDir = launchDir;
		this.velocity = Physics.calculateInitalVelo(launchSpeed, launchAngle, launchDir);
		this.lastLoc = new Coordinate3D(0,0,0);
		this.stad = stad;

		//determine what kind of ball was hit
		if (launchSpeed <= 20) {
			type = InPlayType.BUNT;
		}

		else if (Physics.radsToDegrees(launchAngle) <= 10) {
			type = InPlayType.GROUNDER;
		}

		else if (Physics.radsToDegrees(launchAngle) <= 20) {
			type = InPlayType.LINER;
		}

		else {
			type = InPlayType.FLYBALL;
		}

	}

	public BallInPlay (BallInPlay copy) {

		super(copy.loc, copy.loc, copy.getColor());
		this.launchSpeed = copy.launchSpeed;
		this.launchAngle = copy.launchAngle;
		this.launchDir = copy.launchDir;
		this.velocity = new Coordinate3D (copy.velocity.x, copy.velocity.y, copy.velocity.z);
		this.state = copy.state;
		this.stad = copy.stad;

	}

	public Fielder getHolding() {
		return holding;
	}

	public void setHolding(Fielder holding) {
		this.holding = holding;
	}

	//returns a BallInPlay that's loc is either the place it makes contact with ground or last resting spot
	//inAir - if the ball modelling should stop when it is in the air
	public BallInPlay modelBallDistance (boolean inAir) {

		double time = 0;
		final int split = 100; //time in between recordings
		int ctr = 0;

		if (inAir) {
			BallInPlay copy = new BallInPlay (this);

			do {


				copy.track(new LocationTracker(copy.loc, time,true));


				copy.tick(stad, true, true);

			} while (copy.canRecordOut);

			return copy;
		}

		else {

			BallInPlay copy = new BallInPlay (this);

			//starts tracking the ball after it cant be an out anymore
			do {
				copy.tick(stad, true, true);
				time += Physics.tick;
				ctr++;


				copy.track(new LocationTracker(copy.loc, time,false));


			} while (copy.inMotion());

			return copy;
		}

	}

	//returns a LocationTracker
	public List <LocationTracker> ballTracker () {
		return modelBallDistance(false).getTracker();		
	}

	//batted: true if ball is hit by a bat, false if the ball is thrown by a fielder
	public void tick (Stadium stad, boolean batted, boolean model) {

		List <Wall> walls = stad.getWalls();

		//controls a ball that is not being thrown by fielders
		if (state.equals(BallStatus.IN_AIR) || state.equals(BallStatus.ON_GROUND)) {

			lastLoc.x = loc.x;
			lastLoc.y = loc.y;

			//deals with colliding with floor
			Physics.handleGroundCollision(this);

			//tracks the amount of time the ball spends in the air
			if (canRecordOut) {
				airTime += Physics.tick;
			}

			//this can be improved.  we clip into slack slightly, but it should never go through wall unless the tick is very high
			int res = Physics.handleCollision(walls, loc);

			if (res == 1) {

				//i dont know why i have to do this.  multiplying this.velocity.y by -1/2 results in zero, idk
				this.velocity.y *= -1;
				double addY = this.velocity.y *-1/3;
				double addX = this.velocity.x * -1/3;
				this.velocity.y += addY;
				this.velocity.x += addX;
				this.lastLoc.y = loc.y;
				this.loc.y -= Physics.slack*2; 
				canRecordOut = false;

			}

			else if (res == 2) {

				this.velocity.x *= -1;
				double addY = this.velocity.y *-1/3;
				double addX = this.velocity.x * -1/3;
				this.velocity.y += addY;
				this.velocity.x += addX;
				this.loc.x -= Physics.slack*2;
				canRecordOut = false;

			}

			else if (res == 3) {
				if (!model) {
					Game.messages.add(new BallOverWallMsg(canRecordOut, batter));
				}
			}

			Coordinate3D newPos = Physics.tickPos(loc, velocity);

			Coordinate3D accl = Physics.calcAccel(this);
			Coordinate3D newVelo = Physics.tickVelo(velocity, accl);

			//if statments stop a very slowly moving ball
			if (Math.abs(newVelo.x) < .005) {
				newVelo.x = 0;
			}

			if (Math.abs(newVelo.y) < .005) {
				newVelo.y = 0;
			}

			loc = newPos;
			velocity = newVelo;

		}

		//ball being thrown by fielders
		else if (state.equals(BallStatus.THROWN)) {
			Coordinate3D newPos = Physics.tickPos(loc, velocity);
			velocity.multByFactor(.9995);
			lastLoc = loc;
			loc = newPos;
		}

		//the ball should follow the velocity of the player carrying it
		else if (state.equals(BallStatus.CARRIED)) {


		}

	}

	//true if the ball is still in motion
	public boolean inMotion () {
		return velocity.x != 0 || velocity.y != 0 || velocity.z != 0;
	}

	//handles a wall collision
	//can be solved by making sure the ball is within the x coordiantes of the end points
	//@depracated
	public void handleCollision (HashMap <String, Coordinate3D> dims) {

		double slack = .5;  //how close the ball must be to the wall for it to count as a collision

		LinkedList <Coordinate3D> allVals = new LinkedList <Coordinate3D> ();
		allVals.add(dims.get("l"));
		allVals.add(dims.get("lc"));
		allVals.add(dims.get("c"));
		allVals.add(dims.get("rc"));
		allVals.add(dims.get("r"));

		//iterate over all connecting sets of walls
		for (int i = 0; i < allVals.size()-1; i++) {

			Coordinate3D p1 = allVals.get(i);
			Coordinate3D p2 = allVals.get(i+1);

			//check if ball is reasonably close to wall
			if (Physics.calcPythag(loc.x-p1.x, loc.y-p1.y) <= 200 && (loc.x > p1.x && loc.x < p2.x)) {

				double m = calculateSlope(p1.x, p1.y, p2.x, p2.y);
				double targetY = 0;

				if (p1.x < p2.x)
					targetY = m*(loc.x-p1.x)+p1.y;
				else {
					targetY = p2.y+m*(loc.x-p2.x);
				}				

				//we have collided with the wall {p1,p2}.  we will now flip the velocity and leave function. walls are 10 feet high
				if (Math.abs(targetY-loc.y) < slack && loc.z < 10) {
					/*
					System.out.println("Wall " + (i+1) + " ");
					System.out.println(loc.x);
					System.out.println(loc.y);
					System.out.println(p1.x);
					System.out.println(p1.y);
					System.out.println(targetY);
					System.out.println((loc.x-p1.x));
					System.out.println(m);
					 */

					if (Math.abs(m) < 1) {
						velocity.y *= -1;
					}

					else {
						velocity.x *= -1;
					}

					return;

				}

			}

		}

	}

	//calculates slope
	private double calculateSlope (double x1, double y1, double x2, double y2) {
		/*System.out.println(y2);
			System.out.println(x2);
			System.out.println(y1);
			System.out.println(x1);
			System.out.println();*/
		return (y2-y1)/(x2-x1);
	}


}
