package bbps;

import java.util.concurrent.TimeUnit;

public class Device {
	private int index;
	private boolean available;
	private Task assignedTask;
	
	public Device(int idx) {
		setIndex(idx);
		setAvailable(true);
		assignedTask = null;
	}
	
	public void uploadWork(Task task) {
		assignedTask = task;
		setAvailable(false);
	}

	public void doWork(Boolean skip) {
		if (assignedTask != null) {
			//System.out.println(String.format("Task #%d starts on device #%d", assignedTask.getIndex(), index));
			
			// Simulate doing work
			if (!skip) {
				switch(assignedTask.getUnit()) {
				case seconds:
					try { TimeUnit.SECONDS.sleep(assignedTask.getDeviceTime(this.index)); } catch(InterruptedException e) {}
					break;
				case milliseconds:
					try { TimeUnit.MILLISECONDS.sleep(assignedTask.getDeviceTime(this.index)); } catch(InterruptedException e) {}
					break;
				case microseconds:
					try { TimeUnit.MICROSECONDS.sleep(assignedTask.getDeviceTime(this.index)); } catch(InterruptedException e) {}
					break;
				}
			}
			if (assignedTask != null) {
				assignedTask.partDone(this.index);
			}
			//System.out.println(String.format("Task #%d finish on device #%d", assignedTask.getIndex(), index));
			setAvailable(true);
			assignedTask = null;
		}
	}
	
	public int getIndex() {
		return index;
	}

	private void setIndex(int index) {
		this.index = index;
	}

	public boolean isAvailable() {
		return available;
	}

	private void setAvailable(boolean available) {
		this.available = available;
	}
}
