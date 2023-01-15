package iter3;



import java.util.ArrayList;
import junit.framework.TestCase;

public class TestFloor extends TestCase {

	private Scheduler s;
	private FloorSubsystem f;
	private ElevatorSubsystem e;
	private long initialization;
	
	public void timeWait(long time) {
		boolean checkTest = false;
		
		while(!checkTest) {
			long elapsedTime = System.currentTimeMillis() - initialization;
			if (elapsedTime >= time) {
				checkTest = true;
			}
		}
	}
	
	public void setUp() throws Exception {
		
		// Set up system with 6 floors and 2 elevators
		e = new ElevatorSubsystem(6, 2, 1);
	    s = new Scheduler(6, 2, 1);
		f = new FloorSubsystem(6, 1);
		initialization = System.currentTimeMillis();
	}

	public void testBasicSystemFunctionality() {
		//Trigger Floor Request From floor 1 going up to floor 2
		f.goUp(1, 2);
		timeWait(5000);
		
		//Check that elevator 0 has reached floor 2
		int elev0floor = e.getElevator(0).getEleData().getCurrentFloor();
		assertEquals(elev0floor, 2);
		
	}
	
}
