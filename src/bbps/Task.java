package bbps;

import java.util.*;

public class Task {
	public enum Unit {
		seconds,
		milliseconds,
		microseconds
	}
	
	private int index;
	private Vector<Integer> deviceTimes;
	private Vector<Boolean> partsDone;
	private Unit unit = Unit.milliseconds;
	private boolean done;
	
	public Task(int tA, int tB, int tC, int index) {
		setIndex(index);
		deviceTimes = new Vector<Integer>();
		deviceTimes.add(tA);
		deviceTimes.add(tB);
		deviceTimes.add(tC);
		partsDone = new Vector<Boolean>();
		partsDone.add(false);
		partsDone.add(false);
		partsDone.add(false);
		setDone(false);
	}
	
	public Task(int tA, int tB, int tC, int index, Unit unit) {
		this(tA, tB, tC, index);
		setUnit(unit);
	}
	
	public Task(Task t) {
		this(t.deviceTimes.get(0), t.deviceTimes.get(1), t.deviceTimes.get(2), t.index, t.unit);
	}
	
	public void partDone(int partIdx) {
		assert(partIdx >= 0 && partIdx < 3);
		
		setPartDone(partIdx, true);
		if (getPartDone(0) && getPartDone(1) && getPartDone(2)) {
			setDone(true);
		}
	}
	
	@Override
	public String toString() {
		return String.format("%d", getIndex()+1);
	}
	
	public String toBigString() {
		return String.format("(%d, %d, %d)", getDeviceTime(0), getDeviceTime(1), getDeviceTime(2));
	}

	public int getIndex() {
		return index;
	}

	private void setIndex(int index) {
		this.index = index;
	}

	public int getDeviceTime(int deviceIndex) {
		assert(deviceIndex >= 0 && deviceIndex < 3);
		
		return deviceTimes.get(deviceIndex);
	}
	
	private void setDeviceTime(int deviceIndex, int value) {
		this.deviceTimes.set(deviceIndex, value);
	}

	public boolean isDone() {
		return done;
	}

	private void setDone(boolean done) {
		this.done = done;
	}
	
	private Boolean getPartDone(int partIdx) {
		return partsDone.get(partIdx);
	}
	
	private void setPartDone(int partIdx, boolean value) {
		partsDone.set(partIdx, value);
	}
	
	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}
}
