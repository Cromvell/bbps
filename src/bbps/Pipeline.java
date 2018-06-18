package bbps;

import java.util.*;
import java.util.concurrent.*;

public class Pipeline {
	private Vector<Device> devices;
	private int _1DevTaskIdx;
	private int _2DevTaskIdx;
	private int _3DevTaskIdx;
	private Vector<Task> assignWork = null;
	
	private Boolean isRunning = false;
	private Boolean isSuspend = false;
	private Boolean isNextStep = false;
	
	private List<PipelineListener> listeners = new ArrayList<PipelineListener>();
	
	public Pipeline() {
		devices = new Vector<Device>();
		devices.add(new Device(0));
		devices.add(new Device(1));
		devices.add(new Device(2));
	}
	
	public Pipeline(Vector<Task> work) {
		this();
		uploadWork(work);
	}
	
	public void uploadWork(Vector<Task> work) {
		assignWork = work;
	}
	
	public void run() {
		if (assignWork != null && !isRunning) {
			final Device d1 = devices.get(0);
			final Device d2 = devices.get(1);
			final Device d3 = devices.get(2);
			int numWorks = assignWork.size();
			
			isRunning = true;
			
			for (int i = 0; !assignWork.get(numWorks - 1).isDone(); i++) {
				
				// Set task indexes with offsets for each device(stage)
				_1DevTaskIdx = i;
				_2DevTaskIdx = i - 1;
				_3DevTaskIdx = i - 2;
				
				// Run each device on independent thread
				if (_1DevTaskIdx >= 0 &&_1DevTaskIdx < numWorks && assignWork != null) {
					d1.uploadWork(assignWork.get(_1DevTaskIdx));
					new Thread(new Runnable() { public void run() { d1.doWork(isNextStep); } }).start();
				}
				if (_2DevTaskIdx >= 0 && _2DevTaskIdx < numWorks && assignWork != null) {
					d2.uploadWork(assignWork.get(_2DevTaskIdx));
					new Thread(new Runnable() { public void run() { d2.doWork(isNextStep); } }).start();
				}
				if (_3DevTaskIdx >= 0 && _3DevTaskIdx < numWorks && assignWork != null) {
					d3.uploadWork(assignWork.get(_3DevTaskIdx));
					new Thread(new Runnable() { public void run() { d3.doWork(isNextStep); } }).start();
				}
				
				if (isNextStep && !isSuspend) {
					isSuspend = true;
				}
				isNextStep = false;
				
				//System.out.println(String.format("Pipeline:  %d  %d  %d", _1DevTaskIdx, _2DevTaskIdx, _3DevTaskIdx));
				
				// Fire event of stage beginning
				for (PipelineListener hl : listeners) {
		            hl.stepPerforming();
				}
				
				if (!isRunning || _3DevTaskIdx >= numWorks) {
					break;
				}
				
				// Wait for all devices complete its work
				while (isRunning && !isSuspend && (!d1.isAvailable() || !d2.isAvailable() || !d3.isAvailable())) try { TimeUnit.MILLISECONDS.sleep(1); } catch (InterruptedException e) {};
				
				// Wait if pipeline suspended
				while (isRunning && isSuspend) try { TimeUnit.MILLISECONDS.sleep(1); } catch (InterruptedException e) {};
			}
			
			// Fire event of complete work
			for (PipelineListener hl : listeners) {
				hl.workDone();
			}
			isRunning = false;
		}
	}
	
	public void stop() {
		isRunning = false;
	}
	
	public void nextStep() {
		isNextStep = true;
		isSuspend = false;
	}
	
	public void suspend() {
		isSuspend = true;
	}
	
	public void resume() {
		isSuspend = false;
	}
	
	public void reset() {
		isRunning = false;
		isSuspend = false;
		isNextStep = false;
		
		assignWork = null;
	}
	
	public void addListener(PipelineListener listener) {
		listeners.add(listener);
	}
	
	public Vector<Task> getAssignWork() {
		return assignWork;
	}
	
	public int get1DevTaskIdx() {
		if (assignWork != null) {
			return this._1DevTaskIdx >= 0 && this._1DevTaskIdx < assignWork.size() ? this._1DevTaskIdx : -1;
		} else {
			return -1;
		}
	}
	
	public int get2DevTaskIdx() {
		if (assignWork != null) {
			return this._2DevTaskIdx >= 0 && this._2DevTaskIdx < assignWork.size() ? this._2DevTaskIdx : -1;
		} else {
			return -1;
		}
	}
	
	public int get3DevTaskIdx() {
		if (assignWork != null) {
			return this._3DevTaskIdx >= 0 && this._3DevTaskIdx < assignWork.size() ? this._3DevTaskIdx : -1;
		} else {
			return -1;
		}
	}
	
	public Boolean isRunning() {
		return this.isRunning;
	}
}
