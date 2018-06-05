package bbps;

import java.util.*;
import java.util.concurrent.*;

import java.awt.event.*;

public class Pipeline {
	private Vector<Device> devices;
	private int _1DevTaskIdx;
	private int _2DevTaskIdx;
	private int _3DevTaskIdx;
	private Vector<Task> assignWork = null;
	
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
		if (assignWork != null) {
			final Device d1 = devices.get(0);
			final Device d2 = devices.get(1);
			final Device d3 = devices.get(2);
			int numWorks = assignWork.size();
			
			for (int i = 0; !assignWork.get(numWorks - 1).isDone(); i++) {
				
				// Set task indexes with offsets for each device(stage)
				_1DevTaskIdx = i;
				_2DevTaskIdx = i - 1;
				_3DevTaskIdx = i - 2;
				
				// Run each device on independent thread
				if (_1DevTaskIdx < numWorks) {
					d1.uploadWork(assignWork.get(_1DevTaskIdx));
					new Thread(new Runnable() { public void run() { d1.doWork(); } }).start();
				}
				if (_2DevTaskIdx >= 0 && _2DevTaskIdx < numWorks) {
					d2.uploadWork(assignWork.get(_2DevTaskIdx));
					new Thread(new Runnable() { public void run() { d2.doWork(); } }).start();
				}
				if (_3DevTaskIdx >= 0) {
					d3.uploadWork(assignWork.get(_3DevTaskIdx));
					new Thread(new Runnable() { public void run() { d3.doWork(); } }).start();
				}
				
				System.out.println(String.format("Pipeline:  %d  %d  %d", _1DevTaskIdx, _2DevTaskIdx, _3DevTaskIdx));
				
				// Wait for all devices complete its work
				while (!d1.isAvailable() || !d2.isAvailable() || !d3.isAvailable()) try { TimeUnit.MILLISECONDS.sleep(1); } catch (InterruptedException e) {};
				
				// TODO: Here's need to fire event of stage completion
			}
			
			assignWork = null;
		}
	}
	
	public int get1DevTaskIdx() {
		return this._1DevTaskIdx;
	}
	
	public int get2DevTaskIdx() {
		return this._2DevTaskIdx;
	}
	
	public int get3DevTaskIdx() {
		return this._3DevTaskIdx;
	}
}
