import java.util.List;

public class Base extends OnFieldObject {
	
	private BaseType base;
	private Fielder fielderOn = null;
	private Baserunner runnerOn = null;
	private boolean forceOut = false;

	public Base(Coordinate3D loc, BaseType base) {
		super(loc, loc);
		this.base = base;
	}

	public BaseType getBase() {
		return base;
	}

	public void setBase(BaseType base) {
		this.base = base;
	}

	public Fielder getFielderOn() {
		return fielderOn;
	}
	
	public void leaveBase (Fielder blank) {
		fielderOn = null;
	}
	
	public void leaveBase (Baserunner blank) {
		runnerOn = null;
	}
	
	public void arriveAtBase (Fielder arriving) {
		fielderOn = arriving;
	}
	
	//baserunner reaching the base.  sends message if the baserunner is out
	//forceOut is flipped since anytime a runner reaches a base safely a forceout cannot occur any longer
	public void arriveAtBase (Baserunner arriving) {
		
		//fielder 
		if (fielderOn != null && fielderOn.hasBall()) {
			Game.messages.add(new RunnerOutMsg(this,arriving,fielderOn));
		}
		
		else {
			runnerOn = arriving; 
			forceOut = false;
			
			if (base.equals(BaseType.HOME) && arriving.attempt != null) {
				Game.messages.add(new RunScoredMsg(arriving));
			}
			
		}
 		
	}
	
	public boolean runnerOn () {
		return runnerOn != null;
	}

	public Baserunner getRunnerOn() {
		return runnerOn;
	}

	public boolean isForceOut() {
		return forceOut;
	}

	public void setForceOut(boolean forceOut) {
		this.forceOut = forceOut;
	}
	
}
