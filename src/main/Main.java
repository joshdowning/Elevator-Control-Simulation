package iter3;

import javax.swing.JOptionPane;

public class Main {
	
	public final static int DEFAULT_MODE = 0;
	
	public static void main(String[] args) {
		int numberFloors = 0;
		int numberElevators = 0;
		String[] options = {"Use Default values", "Input custom values"};
		int pop = JOptionPane.showOptionDialog(null, "Do you want to input values?", 
				"Confirmation", JOptionPane.INFORMATION_MESSAGE, 0, null, options, options[0]);
		switch(pop){
		case -1:
			System.exit(0);
		case 0:
			numberFloors = 22;
			numberElevators = 4;
			break;
		case 1:
			numberElevators = Integer.parseInt(JOptionPane.showInputDialog("How many elevators?"));
			numberFloors = Integer.parseInt(JOptionPane.showInputDialog("How many floors?"));
		}		
		@SuppressWarnings("unused")
		ElevatorSubsystem e = new ElevatorSubsystem(numberFloors, numberElevators, DEFAULT_MODE);
		@SuppressWarnings("unused")
		Scheduler s = new Scheduler(numberFloors, numberElevators, DEFAULT_MODE);
		@SuppressWarnings("unused")
		FloorSubsystem f = new FloorSubsystem(numberFloors, DEFAULT_MODE);	
	}
}
