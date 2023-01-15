package iter3;

import java.io.Serializable;



public class SchedulerData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final static int CONTINUE_REQUEST = 0;
	public final static int FLOOR_REQUEST = 1;
	public final static int MOVE_REQUEST = 2;
	public final static int STOP_REQUEST = 3;
	public final static int DOOR_REQUEST = 4;
	

	private final int setting; 
	private final int eleNum; 
	private boolean floorLamps[]; 
	private int floor; 
	private int destinationFloor; 
	private boolean moveUp;
	private boolean moveDown;
	private boolean doorOpen;

	public SchedulerData(int eleNum, int setting, boolean floorLamps[], int floor, int destinationFloor) {
		this.setting = setting;
		this.eleNum = eleNum;
		this.floorLamps = floorLamps;
		this.floor = floor;
		this.destinationFloor = destinationFloor;
	}
	
	public SchedulerData(int eleNum, int setting, boolean moveUp, boolean moveDown, boolean doorOpen) {
		this.setting = setting;
		this.eleNum = eleNum;
		this.moveUp = moveUp;
		this.moveDown = moveDown;
		this.doorOpen = doorOpen;
	}
	public int getReqFloor() {
		return floor;
	}

	public SchedulerData(int eleNum, int setting) {
		this.eleNum = eleNum;
		this.setting = setting;
	}

	public int geteleNumber() {
		return eleNum;
	}

	
	
	public boolean moveDown() {
		return moveDown;
	}
	public int getdestinationFloor() {
		return destinationFloor;
	}
	
	public boolean stop() {
		if (!moveUp && !moveDown)
			return true;
		return false;
	}
	public boolean doorOpen() {
		return doorOpen;
	}
	public boolean[] getFloorLamps() {
		return floorLamps;
	}

	public boolean moveUp() {
		return moveUp;
	}
	

	




	

	public int getsetting() {
		return setting;
	}

}
