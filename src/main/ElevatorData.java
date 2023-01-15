package iter3;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * Data Structure class for ElevatorData
 *
 */
public class ElevatorData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final static int NO_ERROR = 0;

	public final int UP = 0;
	public final int DOWN = 1;
	public final int IDLE = 2;

	private final int eleNum; 
	private final int currentFloor; 
	private ArrayList<Integer> requestedFloor; 
	@SuppressWarnings("unused")
	private boolean ismovingup; 
	@SuppressWarnings("unused")
	private boolean ismovingdown;
	private int currentDirection;
	private boolean isdoorOpen;
	private boolean stop; 
	private int errors; 
	private boolean replyNeeded; 
	private String status;


	public ElevatorData(int eleNum, int errors, int currentFloor, 
			ArrayList<Integer> requestedFloor, boolean ismovingup, boolean ismovingdown, int currentDirection, 
			boolean isdoorOpen, boolean stop, boolean replyNeeded) {
		
		this.eleNum = eleNum;
		this.errors = errors;
		this.currentFloor = currentFloor;
		this.requestedFloor = requestedFloor;
		this.ismovingup = ismovingup;
		this.ismovingdown = ismovingdown;
		this.currentDirection = currentDirection;
		this.isdoorOpen = isdoorOpen;
		this.stop = stop;
		this.replyNeeded = replyNeeded;

		switch(errors) {
		case NO_ERROR:
			status = "Elevator " + eleNum + ": Current Floor - " + currentFloor + ", requests " + requestedFloor.toString() + ", ";

			if (ismovingup) 
				status += "moving up";
			else if (ismovingdown) 
				status += "moving down";
			else
				status += "idle";

			if (isdoorOpen)
				status += ", door - open.";
			else
				status += ", door - closed.";
			break;
		}
	}

	public int getCurrentFloor() {
		return currentFloor;
	}

	
	public ArrayList<Integer> getRequestedFloors() {
		return requestedFloor;
	}

	public boolean ismovingup() {
		if (currentDirection == UP)
			return true;
		return false;
	}
	
	public String getStatus() {
		return status;
	}

	public boolean ismovingdown() {
		if (currentDirection == DOWN)
			return true;
		return false;
	}

	public boolean isIdle() {
		if (currentDirection == IDLE)
			return true;
		return false;
	}
	public boolean replyNeeded() {
		return replyNeeded;
	}

	public boolean isdoorOpen() {
		return isdoorOpen;
	}


	public int geteleNumber() {
		return eleNum;
	}
	
	public int geterrors() {
		return errors;
	}
	
	public boolean isOperational() {
		return !stop;
	}
	
	
	

}
