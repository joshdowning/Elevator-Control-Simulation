package iter3;


public class Floor {
	
	private final int floorNum;
	@SuppressWarnings("unused")
	private final FloorSubsystem fSystem;
	
	private boolean upPressed; 
	private boolean downPressed; 
	private int destFloor; 
	

	public Floor(int floorNum, FloorSubsystem fSystem) {
		this.floorNum = floorNum; 
		this.fSystem = fSystem;
		
	}
	
	
	public FloorData getFloorData() {
		if (upPressed)
			return new FloorData(floorNum, true, destFloor);
		else {
			return new FloorData(floorNum, false, destFloor);
		}
					
	}
	
	public boolean downPressed() {
		return downPressed;
	}
	
	public boolean upPressed() {
		return upPressed;
	}
	
	public void pressDown() {
		downPressed = true;
	}


	public void setDestination(int destFloor) {
		this.destFloor = destFloor;
	}
	public void pressUp() {
		upPressed = true;
	}
	

	

}
