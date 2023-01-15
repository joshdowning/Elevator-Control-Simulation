package iter3;


/**
 * The Implementation of the Scheduler Class
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class Scheduler extends Thread {
	
	public final static int DEFAULT_MODE = 0;
		
	private int runMode;
	private boolean running;
	
	DatagramPacket floorSendPacket, elevatorSendPacket, receivePacket;
	DatagramSocket sendSocket, receiveSocket;
	
	InetAddress eleAddress;
	InetAddress f_address;
	private int f_port;
	ArrayList<DatagramPacket> received_queue;

	private boolean floorLamps[];
	@SuppressWarnings("unused")
	private boolean arrivingSensor[];

	@SuppressWarnings("unused")
	private final int numberOfFloors;

	private ElevatorData eleList[];
	private boolean utd[];

	private ArrayList<ElevatorData> possibleRoutes;
	private int pathedElevator;

	private SchedulerData s_data;
	private FloorData floorDat;
	private ElevatorData e_data;

	private ArrayList<FloorData> addRequests;
	
	private JTextArea logScheduler;
	
	public Scheduler(int numberOfFloors, int numElevators, int runMode) {
		try {
			sendSocket = new DatagramSocket();
			receiveSocket = new DatagramSocket(3000);

		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		
		try {
			eleAddress = InetAddress.getLocalHost();
			f_address = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		this.runMode = runMode;
		running = true;

		received_queue = new ArrayList<DatagramPacket>();

		this.numberOfFloors = numberOfFloors;
		floorLamps = new boolean[numberOfFloors];
		arrivingSensor = new boolean[numberOfFloors];
		addRequests = new ArrayList<FloorData>(); 

		eleList = new ElevatorData[numElevators];
		utd = new boolean[numElevators];
		for (int i = 0; i < numElevators; i++) {
			eleList[i] = new ElevatorData(i, ElevatorData.NO_ERROR, 
					1, new ArrayList<Integer>(), false, false, 2, false, false, true);
			utd[i] = true;
		}

		if (runMode == DEFAULT_MODE ) {
			makeGUI();
			requestAddress();
		}
		
		this.start();
	}
	
	public void requestAddress() {
		String[] options = {"One Computer Setup", "Separate Computer (NOT BEING USED ANYMORE DUE TO COVID-19)"};
		int popUp = JOptionPane.showOptionDialog(null, "Select Scheduler Run Configuration", 
				"Confirmation", JOptionPane.INFORMATION_MESSAGE, 0, null, options, options[0]);
		switch(popUp) {
		case -1:
			System.exit(0);
		case 0:
			try {
				eleAddress = InetAddress.getLocalHost();
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
			break;
		case 1:
			try {
				eleAddress = InetAddress.getByName(JOptionPane.showInputDialog("Enter the IP address of the elevator subsystem:"));
			} catch (HeadlessException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void makeGUI() {
		
		logScheduler = new JTextArea();
        logScheduler.setFont(new Font("Arial", Font.ROMAN_BASELINE, 14));
        logScheduler.setLineWrap(true);
        logScheduler.setWrapStyleWord(true);
        JScrollPane areaScrollPane = new JScrollPane(logScheduler);
        areaScrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setPreferredSize(new Dimension(800, 500));
        areaScrollPane.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createEmptyBorder(),
                                BorderFactory.createEmptyBorder(5,5,5,5)),
                areaScrollPane.getBorder()));
        
        DefaultCaret caret = (DefaultCaret) logScheduler.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
 
		JPanel schedulerPanel = new JPanel(new BorderLayout());
		schedulerPanel.add(areaScrollPane, BorderLayout.CENTER);
		
        JFrame frame = new JFrame("Scheduler Log");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        Container newContentPane = schedulerPanel;
        frame.setContentPane(newContentPane);
        frame.setPreferredSize(new Dimension(500, 300));
        frame.setLocation(100 + (425 * 3), 350);
        frame.pack();
        frame.setVisible(true);
	}

	public void closeSockets() {
		sendSocket.close();
		receiveSocket.close();
		running = false;
	}

	public void floorSend(SchedulerFloorData data) {

		try {
			ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
			ObjectOutputStream ooStream;
			ooStream = new ObjectOutputStream(new BufferedOutputStream(baoStream));
			ooStream.flush();
			ooStream.writeObject(data);
			ooStream.flush();
			byte msg[] = baoStream.toByteArray();

			floorSendPacket = new DatagramPacket(msg, msg.length, f_address,
					f_port);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			sendSocket.send(floorSendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		print("Scheduler: Sent packet to FloorSubsystem.");
	}

	public void elevatorSend(SchedulerData s_data) {

		this.s_data = s_data;
		int targetPort = 2000 + s_data.geteleNumber();
		try {
			ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
			ObjectOutputStream ooStream;
			ooStream = new ObjectOutputStream(new BufferedOutputStream(baoStream));
			ooStream.flush();
			ooStream.writeObject(s_data);
			ooStream.flush();
			byte msg[] = baoStream.toByteArray();

			elevatorSendPacket = new DatagramPacket(msg, msg.length, eleAddress, targetPort);


		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			sendSocket.send(elevatorSendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		print("Scheduler: Sent packet to Elevator " + s_data.geteleNumber() + ".");
		wait(100);
	}

	/**
	 * Receive a packet
	 */
	public void receive() {
		byte data[] = new byte[5000];
		receivePacket = new DatagramPacket(data, data.length);
		try {
			receiveSocket.receive(receivePacket);
			received_queue.add(receivePacket);
		} catch (IOException e) {
			print("IO Exception: likely:");
			print("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void processAndSend() {

		try {
			if (!received_queue.isEmpty()) {
				for (DatagramPacket dPacket : received_queue) {
					ByteArrayInputStream byteStream = new ByteArrayInputStream(dPacket.getData());
					ObjectInputStream is;
					is = new ObjectInputStream(new BufferedInputStream(byteStream));
					Object o = is.readObject();
					is.close();

					if (o instanceof FloorData) {
						floorDat = (FloorData) o;
						print("Scheduler: Packet received.");
						print("Containing:\n	" + floorDat.getStatus() + "\n");
						
						f_address = receivePacket.getAddress();
						f_port = receivePacket.getPort();
						
						updateRequests();
						floorSend(new SchedulerFloorData(SchedulerFloorData.CONFIRM_MESSAGE));
					} else {
						e_data = (ElevatorData) o;
					
						
						eleAddress = receivePacket.getAddress();
						eleList[e_data.geteleNumber()] = e_data;
						utd[e_data.geteleNumber()] = true;
						displayElevatorStates();
						manageElevators();
					}
					
					if (!addRequests.isEmpty())
						routeElevator();
				}
			}

			received_queue.clear();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}


	}

	public void wait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Update the scheduler's floor requests
	 */
	public void updateRequests() {
		if (!addRequests.contains(floorDat))
			addRequests.add(floorDat);
	}

	/**
	 * Display all elevator statuses 
	 */
	public void displayElevatorStates() {
		print("ELEVATOR STATUS:");
		for (ElevatorData e : eleList) {
			print("	" + e.getStatus());
		}
		print("\n");
	}

	/**
	 * Manage the elevator that last updated its status
	 */
	public void manageElevators() {
		ElevatorData e = e_data;
		SchedulerData s = null;
		int errType = e.geterrors();
		int currentFloor = e.getCurrentFloor();

		if (e.replyNeeded()) {
			switch(errType) {
			case ElevatorData.NO_ERROR:
				// If elevator is on the current requested floor
				if (e.getRequestedFloors().contains(currentFloor)) {
					//If motor is still active, stop and open doors
					print("SIGNAL STOP to Elevator: " + e.geteleNumber() + ".");
					s = new SchedulerData(e.geteleNumber(), SchedulerData.STOP_REQUEST, false, false, true);

				}

				else if (!e.getRequestedFloors().isEmpty()) {
					if (currentFloor > e.getRequestedFloors().get(0) && (e.isdoorOpen() || e.isIdle())) {
						print("SIGNAL MOVE DOWN to elevator: " + e.geteleNumber());
						s = new SchedulerData(e.geteleNumber(), SchedulerData.MOVE_REQUEST, false, true,
								false);
					}
					else if (currentFloor < e.getRequestedFloors().get(0) && (e.isdoorOpen() || e.isIdle())) {
						print("SIGNAL MOVE UP to elevator: " + e.geteleNumber());
						s = new SchedulerData(e.geteleNumber(), SchedulerData.MOVE_REQUEST, true, false,
								false);
					}
					else {
						print("SIGNAL CONTINUE to elevator: " + e.geteleNumber());
						s = new SchedulerData(e.geteleNumber(), SchedulerData.CONTINUE_REQUEST);
					}

				}
			}
			
			if (s != null)
				elevatorSend(s);
		}
		
	}
	public void updateLamps() {
		floorLamps[e_data.getCurrentFloor() - 1] = true;
	}

	public boolean elevatorSameFloor(int floor) {
		possibleRoutes.clear();
		boolean caseTrue = false;
		for (int i = 0; i < eleList.length; i++) {
			if (floor == eleList[i].getCurrentFloor() && utd[i]) {
				caseTrue = true;
				possibleRoutes.add(eleList[i]);
			}
			//}
		}

		return caseTrue;
	}

	public boolean elevatorAboveFloor(int floor) {
		possibleRoutes.clear();
		boolean caseTrue = false;
		for (int i = 0; i < eleList.length; i++) {
			if(eleList[i].isOperational() && eleList[i].getCurrentFloor() > floor) {
				possibleRoutes.add(eleList[i]);
				caseTrue = true;
			}
		}
		return caseTrue;
	}

	public boolean elevatorBelowFloor(int floor) {
		possibleRoutes.clear();
		boolean caseTrue = false;
		for (int i = 0; i < eleList.length; i++) {
			if(eleList[i].isOperational() && eleList[i].getCurrentFloor() < floor) {
				possibleRoutes.add(eleList[i]);
				caseTrue = true;
			}
		}
		return caseTrue;
	}

	public boolean allElevatorsAboveFloor(int floor) {
		possibleRoutes.clear();
		for (int i = 0; i < eleList.length; i++) {
			if(eleList[i].isOperational() && eleList[i].getCurrentFloor() < floor) {
				possibleRoutes.add(eleList[i]);
				return false;
			}
		}
		return true;
	}


	public boolean allElevatorsBelowFloor(int floor) {
		possibleRoutes.clear();
		for (int i = 0; i < eleList.length; i++) {
			possibleRoutes.add(eleList[i]);
			if(eleList[i].isOperational() && eleList[i].getCurrentFloor() < floor) {
				return false;
			}
		}
		return true;
	}

	public int closestElevator() {
		if (!possibleRoutes.isEmpty()) {
			ElevatorData closest = possibleRoutes.get(0);
	
			for (ElevatorData e: possibleRoutes) {
				if (Math.abs((e.getCurrentFloor() - floorDat.getFloorNum())) < Math
						.abs((closest.getCurrentFloor() - floorDat.getFloorNum()))) {
					closest = e;
				} 
			}
	
			return closest.geteleNumber();
		}
		
		return -1;

	}

	public void determineAbove(int floor) {
		ArrayList<ElevatorData> remove = new ArrayList<ElevatorData>();
		for (ElevatorData ed: possibleRoutes) {
			if(ed.getCurrentFloor() >= floor) {
				remove.add(ed);
			}
		}
		possibleRoutes.removeAll(remove);
	}

	public void determineBelow(int floor) {
		ArrayList<ElevatorData> remove = new ArrayList<ElevatorData>();
		for (ElevatorData ed: possibleRoutes) {
			if(ed.getCurrentFloor() <= floor) {
				remove.add(ed);
			}
		}
		possibleRoutes.removeAll(remove);
	}

	public void determineMovingUp() {
		ArrayList<ElevatorData> remove = new ArrayList<ElevatorData>();
		for (ElevatorData ed: possibleRoutes) {
			if(!ed.ismovingup()) {
				remove.add(ed);
			}
		}
		possibleRoutes.removeAll(remove);
	}

	public void determineMovingDown() {
		ArrayList<ElevatorData> remove = new ArrayList<ElevatorData>();
		for (ElevatorData ed: possibleRoutes) {
			if(!ed.ismovingdown()) {
				remove.add(ed);
			}
		}
		possibleRoutes.removeAll(remove);
	}

	public void determineIdle() {
		ArrayList<ElevatorData> remove = new ArrayList<ElevatorData>();
		for (ElevatorData ed: possibleRoutes) {
			if(!ed.isIdle()) {
				remove.add(ed);
			}
		}
		possibleRoutes.removeAll(remove);
	}

	public boolean isAnyMovingUp() {
		for (ElevatorData ed: possibleRoutes) {
			if(ed.ismovingup()) {
				return true;
			}
		}
		return false;
	}

	public boolean isAnyMovingDown() {
		for (ElevatorData ed: possibleRoutes) {
			if(ed.ismovingdown()) {
				return true;
			}
		}
		return false;
	}

	public boolean isAnyIdle() {
		for (ElevatorData ed: possibleRoutes) {
			if(ed.isIdle()) {
				return true;
			}
		}
		return false;
	}

	public void routeElevator() {

		possibleRoutes = new ArrayList<ElevatorData>();
		ArrayList<FloorData> completedRequests = new ArrayList<FloorData>();
		pathedElevator = -1;

		for(FloorData fd: addRequests) {
			int floor = fd.getFloorNum();
			if (elevatorSameFloor(floor) && isAnyIdle()) {
				determineIdle();
				if (!possibleRoutes.isEmpty())
					pathedElevator = possibleRoutes.get(0).geteleNumber(); 
				print("ROUTING CASE 0 - potential routes " + possibleRoutes.size());
			}
			else if (allElevatorsAboveFloor(floor)) {
				if(isAnyMovingDown() && fd.downPressed()) { 
					print("ROUTING CASE 1 - potential routes " + possibleRoutes.size());
					determineMovingDown(); 
					pathedElevator = closestElevator(); 
				}
				else if(isAnyIdle()) { 
					determineIdle(); 
					pathedElevator = possibleRoutes.get(0).geteleNumber(); //Return first elevator
					print("ROUTING CASE 2 - potential routes " + possibleRoutes.size());
				}
				
			}

			
			else if (allElevatorsBelowFloor(floor)) {
				if(isAnyMovingUp() && fd.upPressed()) { 
					determineBelow(floor);
					determineMovingUp(); 
					pathedElevator = closestElevator();
					print("ROUTING CASE 3 - potential routes " + possibleRoutes.size());
				}
				else if(isAnyIdle()) { 
					determineIdle(); 
					pathedElevator = possibleRoutes.get(0).geteleNumber();
					print("ROUTING CASE 4 - potential routes " + possibleRoutes.size());
				}
				
			}
			else {
				possibleRoutes.clear();
				for (ElevatorData e: eleList) {
					if (e.isOperational())
						possibleRoutes.add(e);
				}

				if(fd.upPressed() && isAnyMovingUp()) {
					determineMovingUp(); 
					determineBelow(floor);
					print("ROUTING CASE 5 - potential routes " + possibleRoutes.size());
				}
				else if(fd.downPressed() && isAnyMovingDown()) {
					determineMovingDown();
					determineAbove(floor);
					print("ROUTING CASE 6 - potential routes " + possibleRoutes.size());
				}
				
				if(!possibleRoutes.isEmpty()) {
					possibleRoutes.clear();
					for (ElevatorData e: eleList) {
						if (e.isOperational())
							possibleRoutes.add(e);
					}
					if(isAnyIdle()) {
						determineIdle();
						print("ROUTING CASE 7 - potential routes " + possibleRoutes.size());
						pathedElevator = closestElevator();
					}
				} else {
					pathedElevator = closestElevator();
				}

			}
			if(pathedElevator != -1) {
				print("Sending request to Elevator " + pathedElevator + ".\n");
				s_data = new SchedulerData(pathedElevator, SchedulerData.FLOOR_REQUEST, floorLamps, floor, floorDat.getDestFloor());
				elevatorSend(s_data);
				completedRequests.add(fd);
				utd[pathedElevator] = false;
			}
		}
		addRequests.removeAll(completedRequests);
	}


	public ElevatorData getElevatorData() {
		return e_data;
	}

	public SchedulerData getSchedulerData() {
		return s_data;
	}


	public FloorData getFloorData() {
		return floorDat;
	}


	public void print(String message) {
		if (runMode == DEFAULT_MODE)
			logScheduler.append(" " + message + "\n");
	}
	
	public void run() {
		/**
		 * Scheduler Logic
		 */
		while (running) {
			receive();
			processAndSend();
			wait(50);
		}
	}

	public static void main(String args[]) {
		int numberOfFloors = 0, numElevators = 0;
		String[] options = {"Use Defaults", "Use User Inputs"};
		int popUp = JOptionPane.showOptionDialog(null, "Enter Values For Scheduler", 
				"Confirmation", JOptionPane.INFORMATION_MESSAGE, 0, null, options, options[0]);
		switch(popUp) {
		case -1:
			System.exit(0);
		case 0:
			numberOfFloors = 22; 
			numElevators = 4; 
			break;
		case 1:
			numElevators = Integer.parseInt(JOptionPane.showInputDialog("number of elevators?"));
			numberOfFloors = Integer.parseInt(JOptionPane.showInputDialog("number of floors?"));
		}
		
		@SuppressWarnings("unused")
		Scheduler c = new Scheduler(numberOfFloors, numElevators, DEFAULT_MODE);
	}
}
