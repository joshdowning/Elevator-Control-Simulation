package iter3;



import javax.swing.JOptionPane;

public class ElevatorSubsystem extends Thread {

	public final static int DEFAULT_MODE = 0;	
	private int runSetting;


	private Elevator elevatorList[];
	
	public ElevatorSubsystem(int numFloors, int numElevators, int runSetting) {
		elevatorList = new Elevator[numElevators];
	

		for (int i = 0; i < numElevators; i ++) {
			elevatorList[i] = (new Elevator(i, numFloors, this, 2000 + i, runSetting));
		}
		
		for (Elevator e: elevatorList) {
			e.start();
		}
	}
	
	public ElevatorSubsystem(boolean measureValues) {
		
		elevatorList = new Elevator[22];

		for (int i = 0; i < 22; i ++) {
			elevatorList[i] = (new Elevator(i, 22, this, 2000 + i, runSetting));
		}

		
		for (Elevator e: elevatorList) {
			e.start();
		}
	}

	public Elevator getElevator(int elevatorNum) {
		return elevatorList[elevatorNum];
	}
	
	public void closeSockets() {
		for (Elevator e: elevatorList) {
			e.closeSocket();
		}
		System.exit(0);
	}
	
	public void print(String message) {
		System.out.println("ELEVATOR SUBSYSTEM: " + message);
	}
	
	public void wait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		int numFloors = 0, numElevators = 0;
		String[] options = {"Use Defaults", "Use User Inputs"};
		int popUp = JOptionPane.showOptionDialog(null, "Enter Set Up Values For Elevator Subsystem", 
				"Confirmation", JOptionPane.INFORMATION_MESSAGE, 0, null, options, options[0]);
		switch(popUp) {
		case -1:
			System.exit(0);
		case 0:
			numFloors = 22;
			numElevators = 4; 
			break;
		case 1:
			numElevators = Integer.parseInt(JOptionPane.showInputDialog("How many elevators?"));
			numFloors = Integer.parseInt(JOptionPane.showInputDialog("How many floors?"));
		}
		@SuppressWarnings("unused")
		ElevatorSubsystem c = new ElevatorSubsystem(numFloors, numElevators, DEFAULT_MODE);
	}
}
