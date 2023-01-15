package iter3;

import java.io.Serializable;

public class SchedulerFloorData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final static int CONFIRM_MESSAGE = 0;
	public final static int UPDATE_MESSAGE = 1;
	
	private final int setting;
	private int floorLamps[]; 
	
	public SchedulerFloorData(int setting, int floorLamps[]) {
		this.setting = setting;
		this.floorLamps = floorLamps;
	}
	
	public SchedulerFloorData(int setting) {
		this.setting = setting;
	}

	public int[] getFloorLamps() {
		return floorLamps;
	}

	public int getsetting() {
		return setting;
	}

}
