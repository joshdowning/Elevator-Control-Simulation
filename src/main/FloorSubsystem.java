package iter3;


import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.io.*;
import java.net.*;
import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class FloorSubsystem extends Thread {
	
	public final static int DEFAULT = 0;
	
	private int runSetting;

	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendReceiveSocket; InetAddress address;
	
	private ParseFloors f_parser;
	private FloorCommunication floorCom;
	@SuppressWarnings("unused")
	private SchedulerData s_Data;
	
	
	private Floor floors[];
	private JTextArea floorLog;

	@SuppressWarnings("unused")
	private FloorData f_Data;
	

	

	public FloorSubsystem(int numFloors, int runSetting) {
		try {
			
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) { 
			se.printStackTrace();
			System.exit(1);
		}
		this.runSetting = runSetting;
		floors = new Floor[numFloors];
		
		

		for (int i = 0; i < numFloors; i++) {
			floors[i] = new Floor(i + 1, this);
		}
		
		floorCom = new FloorCommunication(this);
		floorCom.start();
		
		try {
			address = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		if (runSetting == DEFAULT) {
			makeGUI();
			addressGetter();
			f_parser = new ParseFloors(this, numFloors, selectFile());
			f_parser.start();
		}
	}
	
	public void makeGUI() {

		floorLog = new JTextArea();
		floorLog.setFont(new Font("Arial", Font.ROMAN_BASELINE, 12));
		floorLog.setLineWrap(true);
		floorLog.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(floorLog);
		scrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(850, 600));
		scrollPane.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createEmptyBorder(),
								BorderFactory.createEmptyBorder(5,5,5,5)),
						scrollPane.getBorder()));

		DefaultCaret caret = (DefaultCaret) floorLog.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		JPanel schePan = new JPanel(new BorderLayout());
		schePan.add(scrollPane, BorderLayout.CENTER);
		JFrame frame = new JFrame("Floor Subsystem");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.setPreferredSize(new Dimension(500, 300));
		frame.setLocation(100 + (425 * 3), 650);
		Container content = schePan;
		frame.setContentPane(content);
		frame.pack();
		frame.setVisible(true);
	}
	
	public String selectFile() {
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File("Assets\\Request Files"));
		fc.setLocation(100 + (425 * 3), 350);

        int returnVal = fc.showDialog(floorLog, "Select File");
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile().getName();
        } 
        return "default_requests.txt";
	}
	
	public void addressGetter() {
		String[] settings = {"One Computer Setup", "Multiple Computers (NOT BEING USED ANYMORE DUE TO COVID-19)"};
		int jPaneOpen = JOptionPane.showOptionDialog(null, "Select Floor Subsystem Configuration", 
				"Confirmation", JOptionPane.INFORMATION_MESSAGE, 0, null, settings, settings[0]);
		switch(jPaneOpen) {
		case -1:
			System.exit(0);
		case 0:
			try {
				address = InetAddress.getLocalHost();
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
			break;
		case 1:
			try {
				address = InetAddress.getByName(JOptionPane.showInputDialog("Enter the IP address of computer running scheduler:"));
			} catch (HeadlessException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}

	
	public void processData(SchedulerFloorData sfdata) {
		int mode = sfdata.getsetting();
		switch(mode) {
		case SchedulerFloorData.CONFIRM_MESSAGE:
			break;
		case SchedulerFloorData.UPDATE_MESSAGE:
			break;
		}
	}
	
	
	public void goUp(int currFloor, int destFloor) {
		Floor floor = getFloor(currFloor);
		floor.pressUp();
		floor.setDestination(destFloor);
		
		floorCom.sendData(floor.getFloorData());
	}


	public void goDown(int currFloor, int destFloor) {
		Floor floor = getFloor(currFloor);
		floor.pressDown();
		floor.setDestination(destFloor);
		
		floorCom.sendData(floor.getFloorData());
	}


	public Floor getFloor(int floorNum) {
		return floors[floorNum - 1];
	}
	
	public void socketClose() {
		floorCom.socketClose();
	}

	public void print(String message) {
		if (runSetting == DEFAULT)
			floorLog.append(" " + message + "\n");
	}

	public static void main(String args[]) { 
		
		String[] settings = {"Default", "User Inputs"};
		int numFloors = 0;
		int jPaneOpen = JOptionPane.showOptionDialog(null, "Enter Values For Floor Subsystem", 
				"Confirmation", JOptionPane.INFORMATION_MESSAGE, 0, null, settings, settings[0]);
		switch(jPaneOpen) {
		case -1:
			System.exit(0);
		case 0:
			numFloors = 22; //default floors
			break;
		case 1:
			numFloors = Integer.parseInt(JOptionPane.showInputDialog("number of floors?"));
		}
		
		@SuppressWarnings("unused")
		FloorSubsystem c = new FloorSubsystem(numFloors, FloorSubsystem.DEFAULT);
	}
}
