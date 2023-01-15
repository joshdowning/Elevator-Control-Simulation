package iter3;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class FloorCommunication extends Thread {
	private FloorSubsystem system;
	private SchedulerFloorData scheflordata;
	
	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendReceiveSocket;

	


	public FloorCommunication(FloorSubsystem system) {
		try {
	
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		
		try {
			system.address = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		this.system = system;
	}
	

	public void run() {
		while (true) {
			receive();
			try {
				Thread.sleep(110);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void receive() {
		byte data[] = new byte[5000];
		receivePacket = new DatagramPacket(data, data.length);
		try {
			sendReceiveSocket.receive(receivePacket);
			system.address = receivePacket.getAddress();

		} catch (IOException e) {
			system.print("IO Exception: likely:");
			system.print("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}

		try {
			ByteArrayInputStream byStream = new ByteArrayInputStream(data);
			ObjectInputStream is;
			is = new ObjectInputStream(new BufferedInputStream(byStream));
			Object o = is.readObject();
			is.close();

			scheflordata = (SchedulerFloorData) o;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		system.print("Received packet from address: " + system.address);
		system.processData(scheflordata);

	}

	public void sendData(FloorData floorDat) {
		try {
			ByteArrayOutputStream bstream = new ByteArrayOutputStream();
			ObjectOutputStream outstream = new ObjectOutputStream(new BufferedOutputStream(bstream));
			outstream.flush();
			outstream.writeObject(floorDat);
			outstream.flush();

			byte msg[] = bstream.toByteArray();

			sendPacket = new DatagramPacket(msg, msg.length, system.address, 3000);

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		system.print("Sending to address: " + system.address);
		system.print("Sent packet to scheduler.\n Containing:\n	" + floorDat.getStatus() + "\n");
	}

	

	public void socketClose() {
		sendReceiveSocket.close();
	}


}
