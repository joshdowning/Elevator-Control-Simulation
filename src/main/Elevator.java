package iter3;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;


import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

public class Elevator extends Thread {
	
	public final static int DEFAULT_MODE = 0;
		
    private int runsetting;

	
	@SuppressWarnings("unused")
	private SchedulerData s_data;

	private final int numEle;

	private final int numOfFloors;

	private boolean eleMovingUp;
	private boolean eleMovingDown;
	private int currentDirection;

	public final int UP = 0;
	public final int DOWN = 1;
	public final int IDLE = 2;

	private boolean doorIsOpen;

	private boolean stop;
	private boolean doorStuck;
	private boolean actionReady;
	
	@SuppressWarnings("unused")
	private ElevatorSubsystem e_system;

	private ElevatorCommunication communicator;

	private ArrayList<Integer> reqFloors;
	private ArrayList<Integer> subReqFloors;
	private ArrayList<Integer> destinationFloors[];
	private int currFloor;
	private boolean needReply;



	private JTextArea eleLog;


	@SuppressWarnings("unchecked")
	public Elevator(int numEle, int numOfFloors, ElevatorSubsystem e_system, int port, int runsetting) {
		this.numEle = numEle;
		this.numOfFloors = numOfFloors;
		this.e_system = e_system;
		this.runsetting = runsetting;
		eleMovingUp = false;
		eleMovingDown = false;
		currentDirection = IDLE;
		doorIsOpen = false;
		currFloor = 1;
		stop = false;
		doorStuck = false;
		reqFloors = new ArrayList<Integer>();
		subReqFloors = new ArrayList<Integer>();
		destinationFloors = new ArrayList[numOfFloors];
		actionReady = false;

		if (runsetting == DEFAULT_MODE)
			createAndShowGUI();

		communicator = new ElevatorCommunication(port, this);
		communicator.start();


		for (int i = 0; i < numOfFloors; i++) {
			destinationFloors[i] = new ArrayList<Integer>();
		}

	}

	public void createAndShowGUI() {

		eleLog = new JTextArea();
		eleLog.setFont(new Font("Arial", Font.ROMAN_BASELINE, 14));
		eleLog.setLineWrap(true);
		eleLog.setWrapStyleWord(true);
		JScrollPane scrollpane = new JScrollPane(eleLog);
		scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpane.setPreferredSize(new Dimension(800, 500));
		scrollpane.setBorder(
				BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(),
						BorderFactory.createEmptyBorder(5, 5, 5, 5)), scrollpane.getBorder()));

		DefaultCaret caret = (DefaultCaret) eleLog.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		JPanel schepanel = new JPanel(new BorderLayout());
		schepanel.add(scrollpane, BorderLayout.CENTER);

		JFrame f= new JFrame("Elevator " + numEle + " Log");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Container newContentPane = schepanel;

		f.setContentPane(newContentPane);
		f.setLocation(100 + (425 * 3), 50);
		f.setPreferredSize(new Dimension(600, 350));
		
		f.pack();
		f.setVisible(true);
	}

	public boolean isstop() {
		return stop;
	}

	public void processData(SchedulerData s) {
		s_data = s;
		int setting = s.getsetting();

		switch (setting) {
		case SchedulerData.CONTINUE_REQUEST:
			print("Received CONTINUE request.");
			actionReady = true;
			break;

		case SchedulerData.FLOOR_REQUEST:
			print("Received FLOOR request.");
			int floor = s.getReqFloor();

			if (!reqFloors.contains(floor)) {
				switch (currentDirection) {
				case UP:
					if (currFloor > floor) {
						subReqFloors.add(floor);
					} else if (currFloor < floor) {
						reqFloors.add(floor);
						Collections.sort(reqFloors);
						Collections.sort(subReqFloors);
					}
					break;
				case DOWN:
					if (currFloor < floor) {
						subReqFloors.add(floor);
					} else if (currFloor > floor) {
						reqFloors.add(floor);
						Collections.sort(reqFloors);
						Collections.reverse(reqFloors);
						Collections.sort(subReqFloors);
						Collections.reverse(subReqFloors);
					}
					break;
				case IDLE:
					reqFloors.add(floor);
					break;
				}

				if (!subReqFloors.isEmpty()) {
					switch (currentDirection) {
					case UP:
						if (currFloor < subReqFloors.get(0)) {
							reqFloors.addAll(subReqFloors);
							Collections.sort(reqFloors);
							subReqFloors.clear();
						}
						break;
					case DOWN:
						if (currFloor > subReqFloors.get(0)) {
							reqFloors.addAll(subReqFloors);
							Collections.sort(reqFloors);
							Collections.reverse(subReqFloors);
							subReqFloors.clear();
						}
						break;
					case IDLE:
						reqFloors.addAll(subReqFloors);
						subReqFloors.clear();
						break;
					}
				}

			}

			destinationFloors[floor - 1].add(s.getdestinationFloor());

			print("Current requests: " + reqFloors.toString());
			ArrayList<Integer> allRequests = new ArrayList<Integer>();
			allRequests.addAll(reqFloors);
			allRequests.addAll(subReqFloors);

			break;

		case SchedulerData.MOVE_REQUEST:
			print("Received MOVE request.");
			if (doorIsOpen) 
				closeDoor();

			if (s.moveUp()) { 
				moveUp();
				Collections.sort(reqFloors);
			} else {
				moveDown(); 
				Collections.sort(reqFloors);
				Collections.reverse(reqFloors);
			}
			actionReady = true;
			break;

		case SchedulerData.STOP_REQUEST:
			print("Received STOP request.");
			stopEle();
			openDoor();

			print("Arrived at floor " + currFloor + ".\n");

			if (!reqFloors.isEmpty()) {
				if (reqFloors.contains(currFloor))
					reqFloors.remove(new Integer(currFloor));

				if (reqFloors.isEmpty()) {
					currentDirection = IDLE;
				} else {
					if (currentDirection == UP) {
					} else {
					}
				}

				if (!destinationFloors[currFloor - 1].isEmpty()) {
					switch (currentDirection) {
					case UP:
						if (currFloor < destinationFloors[currFloor - 1].get(0)) {
							reqFloors.removeAll(destinationFloors[currFloor - 1]);
							reqFloors.addAll(destinationFloors[currFloor - 1]);
							Collections.sort(reqFloors);
							destinationFloors[currFloor - 1].clear();
						}
						break;
					case DOWN:
						if (currFloor > destinationFloors[currFloor - 1].get(0)) {
							reqFloors.removeAll(destinationFloors[currFloor - 1]);
							reqFloors.addAll(destinationFloors[currFloor - 1]);
							Collections.sort(reqFloors);
							Collections.reverse(reqFloors);
							destinationFloors[currFloor - 1].clear();
						}
						break;
					case IDLE:
						reqFloors.addAll(destinationFloors[currFloor - 1]);
						Collections.sort(reqFloors);
						destinationFloors[currFloor - 1].clear();
						if (currFloor < reqFloors.get(0)) {
							currentDirection = UP;
						} else {
							currentDirection = DOWN;
						}
						
						break;
					}
				}

			}
			break;
		case SchedulerData.DOOR_REQUEST:
			print("Received DOOR request.");
			break;
		}

		if (setting == SchedulerData.STOP_REQUEST) {
			needReply = true;
			communicator.send();
			actionReady = true;
			awaitInstruction();
		} else if (setting != SchedulerData.CONTINUE_REQUEST || setting != SchedulerData.DOOR_REQUEST) {
			needReply = false;
			communicator.send();
			actionReady = true;
		}
	}

	public void awaitInstruction() {
		print("Awaiting Instruction.\n");

		do {
			wait(50);
		} while (!actionReady);
	}

	public void moveAFloor() {
		while (doorIsOpen && !isIdle()) {
			wait(50);
		}

		if (!isIdle()) {

			if (!stop) {
				switch (currentDirection) {
				case UP:
					if (currFloor != reqFloors.get(0)) {
						currFloor++;
					}
					if (currFloor > numOfFloors) {
						currFloor = numOfFloors;
					}
					print("Currently on floor " + currFloor + ", moving up.");
					break;
				case DOWN:
					if (currFloor != reqFloors.get(0)) {
						currFloor--;
					}
					if (currFloor <= 0) {
						currFloor = 1;
					}
					print("Currently on floor " + currFloor + ", moving down.");
					break;
				case IDLE:
					break;
				}
				wait(1000);
			}
		}
	}

	public boolean isIdle() {
		if (currentDirection == IDLE)
			return true;
		return false;
	}

	public void moveUp() {
		print("Now moving up.");
		eleMovingUp = true;
		eleMovingDown = false;
		currentDirection = UP;
	}

	public void stopEle() {
		print("Now stopping.");
		eleMovingUp = false;
		eleMovingDown = false;
	}
	public void moveDown() {
		print("Now moving down.");
		eleMovingUp = false;
		eleMovingDown = true;
		currentDirection = DOWN;
	}

	

	public void closeDoor() {
		print("Closing door.");
		actionReady = false;
		wait(1000);
		print("Door closed.");
		doorIsOpen = false;
		actionReady = true;
	}
	public void run() {

		print("Started.");
		while (true) {
			if (isIdle() && reqFloors.isEmpty()) {
				print("STANDBY");
				timeout();
			}
			if (!reqFloors.isEmpty() && !stop && !doorStuck && actionReady) {
				moveAFloor();
				
				if (!stop) {
					needReply = true;
					actionReady = false;
					communicator.send();
					awaitInstruction();
				}

			}
			wait(500);
		}
	}
	public void openDoor() {
		print("Opening doors.");

		actionReady = false;

		wait(1000);
		print("Doors opened.");
		doorIsOpen = true;
		actionReady = true;
	}
	
	public synchronized ElevatorData getEleData() {
		int errType = ElevatorData.NO_ERROR;		
		return new ElevatorData(numEle, errType, currFloor, reqFloors, eleMovingUp, eleMovingDown, currentDirection,
				doorIsOpen, stop, needReply);
	}

	public void closeSocket() {
		communicator.closeSockets();
	}


	public void wait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void print(String message) {
		if (runsetting == DEFAULT_MODE)
		eleLog.append(" Elevator " + numEle + ": " + message + "\n");
	}
	

    public synchronized void timeout() {
    	try {
    		this.wait();
    	} catch (Exception e) {}
    }
    
    public synchronized void wakeUp() {
    	try {
    		this.notify();
    	} catch (Exception e) {}
    }
    

	

}
