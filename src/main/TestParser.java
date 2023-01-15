package iter3;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import junit.framework.TestCase;

public class TestParser extends TestCase {
	private ArrayList<String[]> reqList;
	private Scheduler s;
	private FloorSubsystem f;
	private ElevatorSubsystem e;
	private long initialization;
	private String file;
	
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
		reqList = new ArrayList<String[]>();
		File events = new File("Assets\\Request Files\\" + "DefaultValues.txt");
		Scanner scan = new Scanner(events); 
		while (scan.hasNext()) {
			String request[] = new String[4];
			for (int i = 0; i < 4; i++) {
				request[i] = scan.next();
			}
			reqList.add(request);
		}
		scan.close();
	}

	public void testFunctionality() {
		assertTrue(reqList.size()!=0);
	}
}
