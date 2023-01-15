package iter3;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;

import java.util.Queue;
import java.util.Scanner;
import java.time.*;

public class ParseFloors extends Thread {
	public final int initialFloor = 1;
	public final int reqtime = 0;
	
	public final boolean UP = true;
	
	private ArrayList<String[]> reqList;
	private int floors;
	private final int numInstructs = 4;
	public final int direction = 2;
	private Queue<String[]>[] downQueue;
	private String file;
	public final int destinationfloor = 3;
	private Queue<String[]>[] upQueue;
	
	public final boolean DOWN = false;
	
	private FloorSubsystem fSystem;
	
	private long starting;
	
	@SuppressWarnings("unchecked")
	public ParseFloors(FloorSubsystem fSystem, int floors, String file) {
		this.fSystem = fSystem;
		this.floors = floors;
		starting = System.currentTimeMillis();
		
		this.file = file;
		
		reqList = new ArrayList<String[]>();
		upQueue = new Queue[floors];
		downQueue = new Queue[floors];
	}

	public void parsestart() throws FileNotFoundException {
		File events = new File("Assets\\Request Files\\" + file);
		Scanner scan = new Scanner(events); 
		while (scan.hasNext()) {
			String request[] = new String[numInstructs];
			for (int i = 0; i < numInstructs; i++) {
				request[i] = scan.next();
			}
			reqList.add(request);
		}
		scan.close();
	}
	
	public long getElapsedTime() {
		return System.currentTimeMillis() - starting;
	}

	public long getRequestTimeInMillis(String requestTime) {
		LocalTime localTime = LocalTime.parse(requestTime);
		return localTime.toSecondOfDay() * 1000;
	}
	
	public void run() {
		try {
			parsestart();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		init();
		int count = 0;
		while(count < reqList.size()) {
			for(int i = 0; i< floors;i++) {
				if(upQueue[i].peek() != null) {
					String requestTime = upQueue[i].peek()[reqtime];
					if (getElapsedTime() >= getRequestTimeInMillis(requestTime)) {
						String currentReq[] = upQueue[i].poll();
						int thesource = Integer.parseInt(currentReq[initialFloor]);
						int thedestination = Integer.parseInt(currentReq[destinationfloor]);
						fSystem.goUp(thesource, thedestination);
						count ++;
					}
				}
				if(downQueue[i].peek() != null) {
					String requestTime = downQueue[i].peek()[reqtime];
					if (getElapsedTime() >= getRequestTimeInMillis(requestTime)) {
						String currentRequest[] = downQueue[i].poll();
						int source = Integer.parseInt(currentRequest[initialFloor]);
						int destination = Integer.parseInt(currentRequest[destinationfloor]);
						fSystem.goDown(source, destination);
						count ++;
					}
				}
			}
		}
	}
	public void init() {
		for (int i = 0; i < floors; i++) {
			upQueue[i] = new LinkedList<String[]>();
			downQueue[i] = new LinkedList<String[]>();
		}
		for (String[] request: reqList) { 
			int sourceFloor = Integer.parseInt(request[initialFloor]) - 1;
			if (request[direction].equals("up")) {
				upQueue[sourceFloor].add(request);
			} else {
				downQueue[sourceFloor].add(request);
			}
		}
	}
}
