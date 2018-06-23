package bbps;

import java.util.*;

public class Scheduler {
	private Vector<Task> tasks;
	private Graph tree = new Graph();
	private Integer taskNumber;
	private Boolean scheduleCompose = false;
	
	public Scheduler() {
		this.tasks = null;
		this.taskNumber = 0;
	}
	
	public Scheduler(Vector<Task> work) {
		this.tasks = new Vector<Task>(work);
		this.taskNumber = work.size();
	}
	
	public void composeSchedule() {
		if (tasks != null && !scheduleCompose) {
			Vector<Task> newSchedule = new Vector<Task>();
			tree = new Graph();
			Integer sourceNodeIndex = 0; // Index of node from which continue branching
	
			while (newSchedule.size() < tasks.size()) {
				for (int i = 0; i < tasks.size(); i++) {
					if (!newSchedule.contains(tasks.get(i))) {
						Vector<Task> currSchedule = new Vector<Task>(newSchedule);
						currSchedule.add(tasks.get(i));
						
						Double evaluation = evaluateSchedule(currSchedule);
						tree.addNode(sourceNodeIndex, new Node(currSchedule, evaluation));
					}
				}
				
				// Looking for min evaluation among nodes on current level
				Double minEvaluation = Double.MAX_VALUE;
				Node minEvaluationNode = null;
				Node lastNode = tree.getNodeByIdx(tree.getNodesNum() - 1);
				Integer firstNodeOnCurrTreeLevelIdx = lastNode.getIndex() - taskNumber + newSchedule.size() + 1;
				for (int i = firstNodeOnCurrTreeLevelIdx; i <= lastNode.getIndex(); i++) {
					Node currNode = tree.getNodeByIdx(i);
					if (minEvaluation > currNode.getEvaluation() ) {
						minEvaluation = currNode.getEvaluation();
						minEvaluationNode = currNode;
					}
				}
				sourceNodeIndex = minEvaluationNode.getIndex();
				newSchedule = minEvaluationNode.getSchedule();
			}
			
			tasks = newSchedule;
			scheduleCompose = true;
		}
	}
	
	private Double evaluateSchedule(Vector<Task> schedule) {
		Integer n = schedule.size();		
		
		//
		// Calculate T_A(S)
		//
		Double T_A = 0.0;
		// Sum a[k] from 1 to n
		for (int k = 0; k < n; k++) {
			T_A += schedule.get(k).getDeviceTime(0);
		}
		
		//
		// Calculate T_B(S)
		//
		Double T_B = 0.0;
		Double max = 0.0;
		for (int n_1 = 0; n_1 < n; n_1++) {
			Double sum = 0.0;
			// Sum a[k] from 1 to n_1
			for (int k = 0; k <= n_1; k++) {
				sum += schedule.get(k).getDeviceTime(0);
			}
			
			// Sum b[k] from n_1 to n
			for (int k = n_1; k < n; k++) {
				sum += schedule.get(k).getDeviceTime(1);
			}
			
			if (max < sum) {
				max = sum;
			}
		}
		T_B = max;
		
		//
		// Calculate T_C(S)
		//
		Double T_C = 0.0;
		max = 0.0;
		for (int n_1 = 0; n_1 < n; n_1++) {
			for (int n_2 = n_1; n_2 < n; n_2++) {
				Double sum = 0.0;
				// Sum a[k] from 1 to n_1
				for (int k = 0; k <= n_1; k++) {
					sum += schedule.get(k).getDeviceTime(0);
				}
				
				// Sum b[k] from n_1 to n_2
				for (int k = n_1; k <= n_2; k++) {
					sum += schedule.get(k).getDeviceTime(1);
				}
				
				// Sum c[k] from n_2 to n
				for (int k = n_2; k < n; k++) {
					sum += schedule.get(k).getDeviceTime(2);
				}
				
				if (max < sum) {
					max = sum;
				}
			}
		}
		T_C = max;
		
		//
		// Calculate gamma(S)
		//
		
		// Evaluate A device 
		Double AEval = T_A;
		// Sum a[k] exclude already scheduled tasks
		for (int k = 0; k < tasks.size(); k++) {
			Task t = tasks.get(k);
			if (!schedule.contains(t)) {
				AEval += t.getDeviceTime(0);
			}
		}
		
		// Calculate min of sum b[k] and c[k] exclude already scheduled tasks
		Double min = Double.MAX_VALUE;
		for (int k = 0; k < tasks.size(); k++) {
			Task t = tasks.get(k);
			Double sum = (t.getDeviceTime(1) + t.getDeviceTime(2))/1.0; 
			if (!schedule.contains(t) && min > sum) {
				min = sum;
			}
		}
		if (min != Double.MAX_VALUE) {
			AEval += min;
		}
		
		
		// Evaluate B device
		Double BEval = T_B;
		// Sum b[k] exclude already scheduled tasks
		for (int k = 0; k < tasks.size(); k++) {
			Task t = tasks.get(k);
			if (!schedule.contains(t)) {
				BEval += t.getDeviceTime(1);
			}
		}
		
		// Calculate min of c[k] exclude already scheduled tasks
		min = Double.MAX_VALUE;
		for (int k = 0; k < tasks.size(); k++) {
			Task t = tasks.get(k);
			if (!schedule.contains(t) && min > t.getDeviceTime(2)) {
				min = t.getDeviceTime(2)/1.0;
			}
		}
		if (min != Double.MAX_VALUE) {
			BEval += min;
		}
		
		
		// Evaluate C device
		Double CEval = T_C;
		for (int k = 0; k < tasks.size(); k++) {
			Task t = tasks.get(k);
			if (!schedule.contains(t)) {
				CEval += t.getDeviceTime(2);
			}
		}
		
		Double gamma = Math.max(AEval, Math.max(BEval, CEval));
		
		return gamma;
	}
	
	public void reset() {
		Vector<Task> newTasks = new Vector<Task>();
		for (int i = 0; i < tasks.size(); i++) {
			newTasks.add(new Task(tasks.get(i)));
		}
		tasks = newTasks;
	}
	
	public Vector<Task> getTasks() {
		return this.tasks;
	}
	
	public Graph getGraph() {
		return tree;
	}
	
	public void setUnit(Task.Unit u) {
		for (Task t : tasks) {
			t.setUnit(u);
		}
	}
}
