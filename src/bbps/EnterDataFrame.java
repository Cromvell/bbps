package bbps;

import javax.swing.*;

import java.awt.*;

public class EnterDataFrame extends JFrame {
	public EnterDataFrame() {
		Initialize();
	}
	
	///////////////////////////////////////////////
	//
	//	All UI elements initialization
	//
	
	JLabel task1Label, task2Label, task3Label, task4Label, task5Label, task6Label;
	JSpinner task1Dev1Spinner, task1Dev2Spinner, task1Dev3Spinner;
	JSpinner task2Dev1Spinner, task2Dev2Spinner, task2Dev3Spinner;
	JSpinner task3Dev1Spinner, task3Dev2Spinner, task3Dev3Spinner;
	JSpinner task4Dev1Spinner, task4Dev2Spinner, task4Dev3Spinner;
	JSpinner task5Dev1Spinner, task5Dev2Spinner, task5Dev3Spinner;
	JSpinner task6Dev1Spinner, task6Dev2Spinner, task6Dev3Spinner;
	JButton randomButton, enterButton;
	
	private void Initialize() {
		
		// task1Label
		task1Label = new JLabel("Task 1");
		this.add(task1Label);
		
		// task1Dev1Spinner
		task1Dev1Spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
		this.add(task1Dev1Spinner);
		
		// task1Dev2Spinner
		task1Dev2Spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
		this.add(task1Dev2Spinner);
		
		// task1Dev3TextField
		task1Dev3Spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
		this.add(task1Dev3Spinner);
		
		// task2Label
		task2Label = new JLabel("Task 2");
		this.add(task2Label);
		
		// task2Dev1Spinner
		task2Dev1Spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
		this.add(task2Dev1Spinner);
		
		// task2Dev2Spinner
		task2Dev2Spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
		this.add(task2Dev2Spinner);
		
		// task2Dev3TextField
		task2Dev3Spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
		this.add(task2Dev3Spinner);
		
		// task3Label
		task3Label = new JLabel("Task 3");
		this.add(task3Label);
		
		// task3Dev1Spinner
		task3Dev1Spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
		this.add(task3Dev1Spinner);
		
		// task3Dev2Spinner
		task3Dev2Spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
		this.add(task3Dev2Spinner);
		
		// task3Dev3TextField
		task3Dev3Spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
		this.add(task3Dev3Spinner);
		
		// task4Label
		task4Label = new JLabel("Task 4");
		this.add(task4Label);
		
		// task4Dev1Spinner
		task4Dev1Spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
		this.add(task4Dev1Spinner);
		
		// task4Dev2Spinner
		task4Dev2Spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
		this.add(task4Dev2Spinner);
		
		// task4Dev3TextField
		task4Dev3Spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
		this.add(task4Dev3Spinner);
		
		// task5Label
		task5Label = new JLabel("Task 5");
		this.add(task5Label);
		
		// task5Dev1Spinner
		task5Dev1Spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
		this.add(task5Dev1Spinner);
		
		// task5Dev2Spinner
		task5Dev2Spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
		this.add(task5Dev2Spinner);
		
		// task5Dev3TextField
		task5Dev3Spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
		this.add(task5Dev3Spinner);
		
		// task6Label
		task6Label = new JLabel("Task 6");
		this.add(task6Label);
		
		// task6Dev1Spinner
		task6Dev1Spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
		this.add(task6Dev1Spinner);
		
		// task6Dev2Spinner
		task6Dev2Spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
		this.add(task6Dev2Spinner);
		
		// task6Dev3TextField
		task6Dev3Spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
		this.add(task6Dev3Spinner);
		
		// randomButton
		randomButton = new JButton("Random");
		this.add(randomButton);
		
		// enterButton
		enterButton = new JButton("Enter");
		this.add(enterButton);
		
		// this
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(200, 235);
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		this.setLayout(new FlowLayout());
		this.setResizable(false);
		this.setVisible(true);
	}
}
