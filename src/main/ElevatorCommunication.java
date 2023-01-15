package iter3;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class ElevatorCommunication extends Thread {
	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendSocket, receiveSocket;
	
	private boolean isrunning;

	private Elevator elevator;
	private SchedulerData s_data;

	private InetAddress scheaddress;
	private int port;

	public ElevatorCommunication(int port, Elevator e) {
		try {
		
			sendSocket = new DatagramSocket();
			receiveSocket = new DatagramSocket(port);

		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		this.port = port;
		isrunning = true;
		elevator = e;
	}

	public void send() {

		ElevatorData e_data = elevator.getEleData();

		try {
			ByteArrayOutputStream bstream = new ByteArrayOutputStream();
			ObjectOutputStream outstream = new ObjectOutputStream(new BufferedOutputStream(bstream));
			outstream.flush();
			outstream.writeObject(e_data);
			outstream.flush();

			byte msg[] = bstream.toByteArray();
			
			sendPacket = new DatagramPacket(msg, msg.length, scheaddress, 3000);

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		elevator.print("Sending to address: " + scheaddress);
		elevator.print("Sent packet to scheduler.\n Containing:\n	" + e_data.getStatus() + "\n");
	}


	public void receive() {

		if (elevator.getEleData().isOperational()) {

			byte data[] = new byte[5000];
			receivePacket = new DatagramPacket(data, data.length);
			try {
				receiveSocket.receive(receivePacket);
				scheaddress = receivePacket.getAddress();

			} catch (IOException e) {
				elevator.print("IO Exception: likely:");
				elevator.print("Receive Socket Timed Out.\n" + e);
				e.printStackTrace();
				System.exit(1);
			}

			try {
				ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
				ObjectInputStream is;
				is = new ObjectInputStream(new BufferedInputStream(byteStream));
				Object o = is.readObject();
				is.close();
				
				if (o == null) {
					closeSocket();
				}

				s_data = (SchedulerData) o;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			
			elevator.print("Received packet from address: " + scheaddress);
			elevator.processData(s_data);
			elevator.wakeUp();
		}	
		
		else {
			closeSocket();
		}
	}

	public void run() {
		while (isrunning) {
			receive();
			wait(200);
		}
	}
	public void wait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void closeSockets() {
		try {
			sendSocket.send(new DatagramPacket(null, 1, 1, InetAddress.getLocalHost(), port));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeSocket() {
		isrunning = false;
		
		receiveSocket.close();
		sendSocket.close();
		
		
	}





}
