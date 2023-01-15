package iter3;



import java.util.ArrayList;
import junit.framework.TestCase;

public class MainTest extends TestCase {

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
		//Trigger Floor Request From floor 1 going up to floor 6
		f.goUp(1, 6);
		timeWait(500);
		ArrayList<Integer> elev0Requests = e.getElevator(0).getEleData().getRequestedFloors();
		
		//Check that elevator 0 has received the request
		assertEquals(elev0Requests.size(), 1);
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//Trigger Floor Request From floor 1 going up to floor 5
		f.goUp(1, 5);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		ArrayList<Integer> elev1Requests = e.getElevator(1).getEleData().getRequestedFloors();
		
		//Check that elevator 1 has received the request
		assertEquals(elev1Requests.size(), 1);
		
		//Wait till elevators reach their destination
		timeWait(12000);
		
		//Check that elevator 0 has reached floor 6
		int elev0floor = e.getElevator(0).getEleData().getCurrentFloor();
		assertEquals(elev0floor, 6);
		
		//check that elevator 1 has reached floor 5
		int elev1floor = e.getElevator(1).getEleData().getCurrentFloor();
		assertEquals(elev1floor, 5);
		
		boolean elev0doorOpen = e.getElevator(0).getEleData().isdoorOpen();
		boolean elev1doorOpen = e.getElevator(1).getEleData().isdoorOpen();
		
		//Check that the elevators have opened their doors
		assertTrue(elev0doorOpen);
		assertTrue(elev1doorOpen);
		
		elev0Requests = e.getElevator(0).getEleData().getRequestedFloors();
		elev1Requests = e.getElevator(1).getEleData().getRequestedFloors();
		
		//Check that the elevators have no more pending requests
		assertTrue(elev0Requests.isEmpty());
		assertTrue(elev1Requests.isEmpty());
		
	}
}
