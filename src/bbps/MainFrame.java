package bbps;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Vector;
import java.awt.event.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;

import bbps.Task.Unit;

public class MainFrame extends JFrame implements ActionListener, PipelineListener {
	
	Task.Unit currentUnit = Unit.milliseconds;
	
	Pipeline pipeline = new Pipeline();
	Scheduler scheduler = new Scheduler();
	Vector<Task> work = new Vector<Task>();
	
	Boolean pipelineSuspend = false;
	
	public MainFrame() {
		Initialize();
		
		pipeline.addListener(this);
		/*
		Vector<Task> t = new Vector<Task>();
		t.add(new Task(0, 0, 0));
		t.add(new Task(0, 0, 0));
		t.add(new Task(0, 0, 0));
		t.add(new Task(0, 0, 0));
		t.add(new Task(0, 0, 0));
		t.add(new Task(0, 0, 0));
		
		scheduler = new Scheduler(t);
		scheduler.composeSchedule();
		pipeline = new Pipeline(t);*/
	}
	
	////////////////////////////////////
	//
	// 	      Event handlers
	//
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(openFileMenuItem)) {
			System.out.println("Open file");
		} else if (e.getSource().equals(saveFileMenuItem)) {
			System.out.println("Save file");
		} else if (e.getSource().equals(exitFileMenuItem)) {
			System.exit(0);
		} else if (e.getSource().equals(generateButton)) {
			// Reset pipeline if it's running
			if (pipeline.isRunning()) {
				resetAll();
			}
			
			int numOfTasks = 6;
			int maxGeneratedTime = 10;
			scheduler.generate(numOfTasks, maxGeneratedTime);
			scheduler.setUnit(currentUnit);
			work = scheduler.getTasks();
			pipeline.uploadWork(work);
			
			allowPipelineStart();
			showSchedule(work);
			scrollPane.setViewportView(null);
		} else if (e.getSource().equals(unitSelectComboBox)) {
			switch(unitSelectComboBox.getSelectedItem().toString()) {
			case "seconds":
				currentUnit = Task.Unit.seconds;
				break;
			case "milliseconds":
				currentUnit = Task.Unit.milliseconds;
				break;
			case "microseconds":
				currentUnit = Task.Unit.microseconds;
				break;
			}
			
			// Reset pipeline if it's running
			if (pipeline.isRunning()) {
				resetAll();
			}
			
			scheduler.setUnit(currentUnit);
		} else if (e.getSource().equals(scheduleButton)) {
			// Reset pipeline if it's running
			if (pipeline.isRunning()) {
				resetAll();
			}
			
			scheduler.composeSchedule();
			work = scheduler.getTasks();
			pipeline.uploadWork(work);
			showSchedule(work);
			
			// Draw Graph
			scrollPane.setViewportView(new JLabel(new ImageIcon(scheduler.getGraph().getGraphImage())));
			
			// Set viewport position to center
			Rectangle bounds = scrollPane.getViewport().getViewRect();
			Dimension size = scrollPane.getViewport().getViewSize();
			int x = (size.width - bounds.width) / 2;
			scrollPane.getViewport().setViewPosition(new Point(x, 0));
		} else if (e.getSource().equals(startPauseButton)) {
			if (!pipelineSuspend) {
				if (!pipeline.isRunning()) {
					new Thread(new Runnable() { public void run() { pipeline.run(); }}).start();
				}
				pipeline.resume();
				allowPipelineStop();
				
				startPauseButton.setText("\u23F8");
				pipelineSuspend = true;
			} else {
				pipeline.suspend();
				allowPipelineStart();
				
				startPauseButton.setText("\u25B6");
				pipelineSuspend = false;
			}
		} else if (e.getSource().equals(nextStepButton)) {
			if (!pipeline.isRunning()) {
				new Thread(new Runnable() { public void run() { pipeline.run(); }}).start();
			}
			pipeline.nextStep();
		} else if (e.getSource().equals(resetButton)) {
			resetAll();
		}
	}
	
	@Override
	public void stepPerforming() {
		Vector<Task> taskList = pipeline.getAssignWork();
		
		int _1DevTaskIdx = pipeline.get1DevTaskIdx();
		int _2DevTaskIdx = pipeline.get2DevTaskIdx();
		int _3DevTaskIdx = pipeline.get3DevTaskIdx();
		
		if (_1DevTaskIdx >= 0) {
			taskStage1Label.setText(taskList.get(_1DevTaskIdx).toString());
		} else {
			taskStage1Label.setText("A");
		}
		if (_2DevTaskIdx >= 0) {
			taskStage2Label.setText(taskList.get(_2DevTaskIdx).toString());
		} else {
			taskStage2Label.setText("B");
		}
		if (_3DevTaskIdx >= 0) {
			taskStage3Label.setText(taskList.get(_3DevTaskIdx).toString());
		} else {
			taskStage3Label.setText("C");
		}
	}
	
	@Override
	public void workDone() {
		taskStage1Label.setText("A");
		taskStage2Label.setText("B");
		taskStage3Label.setText("C");
		
		if (pipelineSuspend) {
			forbidPipelineActions();
		}
	}
	
	private void resetAll() {
		pipeline.stop();
		pipeline.reset();
		scheduler.reset();
		pipeline.uploadWork(scheduler.getTasks());
		pipelineSuspend = false;
		allowPipelineStart();
	}
	
	private void showSchedule(Vector<Task> s) {
		task1Label.setText(s.get(0).toBigString());
		task2Label.setText(s.get(1).toBigString());
		task3Label.setText(s.get(2).toBigString());
		task4Label.setText(s.get(3).toBigString());
		task5Label.setText(s.get(4).toBigString());
		task6Label.setText(s.get(5).toBigString());
		
		task1NumLabel.setText(String.format("1(%s)", s.get(0).toString()));
		task2NumLabel.setText(String.format("2(%s)", s.get(1).toString()));
		task3NumLabel.setText(String.format("3(%s)", s.get(2).toString()));
		task4NumLabel.setText(String.format("4(%s)", s.get(3).toString()));
		task5NumLabel.setText(String.format("5(%s)", s.get(4).toString()));
		task6NumLabel.setText(String.format("6(%s)", s.get(5).toString()));
	}
	
	private void allowPipelineStart() {
		scheduleButton.setEnabled(true);
		startPauseButton.setEnabled(true);
		nextStepButton.setEnabled(true);
		resetButton.setEnabled(true);

		startPauseButton.setText("\u25B6");
	}

	private void allowPipelineStop() {
		nextStepButton.setEnabled(false);
	}
	
	private void forbidPipelineActions() {
		startPauseButton.setEnabled(false);
		nextStepButton.setEnabled(false);
	}
	
	////////////////////////////////////
	//
	//     All initialize stuff
	//
	
	JMenuBar  menuBar;
	JMenu fileMenu;
	JMenuItem openFileMenuItem, saveFileMenuItem, exitFileMenuItem;
	SimulatorPanel simulatorPanel;
	JPanel controlButtonsPanel, visualizationPanel;
	JPanel task1Panel, task2Panel, task3Panel, task4Panel, task5Panel, task6Panel;
	JPanel pipelineStage1Panel, pipelineStage2Panel, pipelineStage3Panel;
	JButton resetButton, enterDataButton, generateButton, startPauseButton, nextStepButton, scheduleButton;
	JComboBox<String> unitSelectComboBox;
	JScrollPane scrollPane;
	JLabel taskListLabel;
	JLabel pipelineStage1Label, pipelineStage2Label, pipelineStage3Label;
	JLabel task1NumLabel, task2NumLabel, task3NumLabel, task4NumLabel, task5NumLabel, task6NumLabel;
	JLabel task1Label, task2Label, task3Label, task4Label, task5Label, task6Label;
	JLabel taskStage1Label, taskStage2Label, taskStage3Label;
	
	private void Initialize() {
		// menuBar
		menuBar = new JMenuBar();
		
		// menu
		fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		
		// openFileMenuItem
		openFileMenuItem = new JMenuItem("Open file");
		openFileMenuItem.addActionListener(this);
		fileMenu.add(openFileMenuItem);
		
		// saveFileMenuItem
		saveFileMenuItem = new JMenuItem("Save file");
		saveFileMenuItem.addActionListener(this);
		fileMenu.add(saveFileMenuItem);
		
		// exitFileMenuItem
		exitFileMenuItem = new JMenuItem("Exit");
		exitFileMenuItem.addActionListener(this);
		fileMenu.add(exitFileMenuItem);
		
		// simulatorPanel
		simulatorPanel = new SimulatorPanel();
		simulatorPanel.setBounds(0, 0, 800, 250);
		simulatorPanel.setLayout(new BorderLayout());
		this.add(simulatorPanel);
		
		// controlButtonsPanel
		controlButtonsPanel = new JPanel();
		controlButtonsPanel.setSize(800, 40);
		simulatorPanel.add(controlButtonsPanel);

		// generateButton
		generateButton = new JButton("Generate");
		generateButton.addActionListener(this);
		controlButtonsPanel.add(generateButton);
		
		// enterDataButton
		enterDataButton = new JButton("Enter data");
		enterDataButton.addActionListener(this);
		controlButtonsPanel.add(enterDataButton);
		
		// unitSelectComboBox
		unitSelectComboBox = new JComboBox<String>();
		unitSelectComboBox.addItem("seconds");
		unitSelectComboBox.addItem("milliseconds");
		unitSelectComboBox.addItem("microseconds");
		unitSelectComboBox.setSelectedItem(unitSelectComboBox.getItemAt(1));
		unitSelectComboBox.addActionListener(this);
		controlButtonsPanel.add(unitSelectComboBox);
		
		// scheduleButton
		scheduleButton = new JButton("Compose schedule");
		scheduleButton.addActionListener(this);
		scheduleButton.setEnabled(false);
		controlButtonsPanel.add(scheduleButton);
		
		// startButton
		startPauseButton = new JButton("\u25B6");
		startPauseButton.addActionListener(this);
		startPauseButton.setEnabled(false);
		startPauseButton.setFont(new Font("Unicode", Font.PLAIN, 12));
		controlButtonsPanel.add(startPauseButton);
		
		// nextStepButton
		nextStepButton = new JButton("Next step");
		nextStepButton.addActionListener(this);
		nextStepButton.setEnabled(false);
		controlButtonsPanel.add(nextStepButton);
		
		// resetButton	
		resetButton = new JButton("Reset");
		resetButton.addActionListener(this);
		resetButton.setEnabled(false);
		controlButtonsPanel.add(resetButton);
		
		// visualizationPanel
		visualizationPanel = new JPanel();
		visualizationPanel.setBounds(40, 0, 800, 200);
		visualizationPanel.setLayout(null);
		simulatorPanel.add(visualizationPanel);
		
		// taskListLabel
		taskListLabel = new JLabel("Task list:");
		taskListLabel.setBounds(70, 25, 100, 50);
		visualizationPanel.add(taskListLabel);
		
		// task1Panel
		task1Panel = new JPanel();
		task1Panel.setBounds(50, 60, 100, 25);
		task1Panel.setBorder(BorderFactory.createLineBorder(Color.gray));
		task1Panel.setBackground(Color.white);
		visualizationPanel.add(task1Panel);
		
		// task2Panel
		task2Panel = new JPanel();
		task2Panel.setBounds(50, 85, 100, 25);
		task2Panel.setBorder(BorderFactory.createLineBorder(Color.gray));
		task2Panel.setBackground(Color.white);
		visualizationPanel.add(task2Panel);
		
		// task3Panel
		task3Panel = new JPanel();
		task3Panel.setBounds(50, 110, 100, 25);
		task3Panel.setBorder(BorderFactory.createLineBorder(Color.gray));
		task3Panel.setBackground(Color.white);
		visualizationPanel.add(task3Panel);

		// task4Panel
		task4Panel = new JPanel();
		task4Panel.setBounds(50, 135, 100, 25);
		task4Panel.setBorder(BorderFactory.createLineBorder(Color.gray));
		task4Panel.setBackground(Color.white);
		visualizationPanel.add(task4Panel);
		
		// task5Panel
		task5Panel = new JPanel();
		task5Panel.setBounds(50, 160, 100, 25);
		task5Panel.setBorder(BorderFactory.createLineBorder(Color.gray));
		task5Panel.setBackground(Color.white);
		visualizationPanel.add(task5Panel);
		
		// task6Panel
		task6Panel = new JPanel();
		task6Panel.setBounds(50, 185, 100, 25);
		task6Panel.setBorder(BorderFactory.createLineBorder(Color.gray));
		task6Panel.setBackground(Color.white);
		visualizationPanel.add(task6Panel);
		
		// task1Label
		task1Label = new JLabel("(n/a, n/a, n/a)");
		task1Panel.add(task1Label);
		
		// task2Label
		task2Label = new JLabel("(n/a, n/a, n/a)");
		task2Panel.add(task2Label);
		
		// task3Label
		task3Label = new JLabel("(n/a, n/a, n/a)");
		task3Panel.add(task3Label);
		
		// task4Label
		task4Label = new JLabel("(n/a, n/a, n/a)");
		task4Panel.add(task4Label);
		
		// task5Label
		task5Label = new JLabel("(n/a, n/a, n/a)");
		task5Panel.add(task5Label);
		
		// task6Label
		task6Label = new JLabel("(n/a, n/a, n/a)");
		task6Panel.add(task6Label);
		
		// task1NumLabel
		task1NumLabel = new JLabel("1(1)");
		task1NumLabel.setBounds(20, 60, 100, 25);
		visualizationPanel.add(task1NumLabel);
		
		// task2NumLabel
		task2NumLabel = new JLabel("2(2)");
		task2NumLabel.setBounds(20, 85, 100, 25);
		visualizationPanel.add(task2NumLabel);
		
		// task3NumLabel
		task3NumLabel = new JLabel("3(3)");
		task3NumLabel.setBounds(20, 110, 100, 25);
		visualizationPanel.add(task3NumLabel);
		
		// task4NumLabel
		task4NumLabel = new JLabel("4(4)");
		task4NumLabel.setBounds(20, 135, 100, 25);
		visualizationPanel.add(task4NumLabel);
		
		// task5NumLabel
		task5NumLabel = new JLabel("5(5)");
		task5NumLabel.setBounds(20, 160, 100, 25);
		visualizationPanel.add(task5NumLabel);
		
		// task6NumLabel
		task6NumLabel = new JLabel("6(6)");
		task6NumLabel.setBounds(20, 185, 100, 25);
		visualizationPanel.add(task6NumLabel);
		
		// pipelineStage1Panel
		pipelineStage1Panel = new JPanel();
		pipelineStage1Panel.setBounds(300, 100, 50, 50);
		pipelineStage1Panel.setBorder(BorderFactory.createLineBorder(Color.gray));
		pipelineStage1Panel.setBackground(Color.white);
		visualizationPanel.add(pipelineStage1Panel);
		
		// pipelineStage2Panel
		pipelineStage2Panel = new JPanel();
		pipelineStage2Panel.setBounds(400, 100, 50, 50);
		pipelineStage2Panel.setBorder(BorderFactory.createLineBorder(Color.gray));
		pipelineStage2Panel.setBackground(Color.white);
		visualizationPanel.add(pipelineStage2Panel);
		
		// pipelineStage3Panel
		pipelineStage3Panel = new JPanel();
		pipelineStage3Panel.setBounds(500, 100, 50, 50);
		pipelineStage3Panel.setBorder(BorderFactory.createLineBorder(Color.gray));
		pipelineStage3Panel.setBackground(Color.white);
		visualizationPanel.add(pipelineStage3Panel);
		
		// pipelineStage1Label
		pipelineStage1Label = new JLabel("Stage 1");
		pipelineStage1Label.setBounds(300, 80, 50, 20);
		visualizationPanel.add(pipelineStage1Label);
		
		// pipelineStage2Label
		pipelineStage2Label = new JLabel("Stage 2");
		pipelineStage2Label.setBounds(400, 80, 50, 20);
		visualizationPanel.add(pipelineStage2Label);
		
		// pipelineStage3Label
		pipelineStage3Label = new JLabel("Stage 3");
		pipelineStage3Label.setBounds(500, 80, 50, 20);
		visualizationPanel.add(pipelineStage3Label);
		
		// taskStage1Label
		taskStage1Label = new JLabel("A");
		taskStage1Label.setFont(new Font("Consolas", Font.PLAIN, 35));
		pipelineStage1Panel.add(taskStage1Label);
		
		// taskStage2Label
		taskStage2Label = new JLabel("B");
		taskStage2Label.setFont(new Font("Consolas", Font.PLAIN, 35));
		pipelineStage2Panel.add(taskStage2Label);
		
		// taskStage3Label
		taskStage3Label = new JLabel("C");
		taskStage3Label.setFont(new Font("Consolas", Font.PLAIN, 35));
		pipelineStage3Panel.add(taskStage3Label);

		// scrollPane
		scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 250, 800, 390);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.getViewport().setBackground(Color.white);
		this.add(scrollPane);
		
		// this
		this.setSize(815, 700);
		this.setLayout(new BorderLayout());
		this.setJMenuBar(menuBar);
		this.setTitle("BBPS (Branch and Bound Pipeline Scheduler)");
		this.setResizable(false);
		this.setVisible(true);
	}
	
	class SimulatorPanel extends JPanel {
		@Override
        public void paint(Graphics g) {
            super.paint(g);

            g.drawLine(150, 60, 290, 125);
            g.drawLine(150, 208, 290, 125);
            DrawArrowhead(g, 300, 125, -15, 0);
            g.drawLine(350, 125, 400, 125);
            DrawArrowhead(g, 400, 125, -15, 0);
            g.drawLine(450, 125, 500, 125);
            DrawArrowhead(g, 500, 125, -15, 0);
            g.drawLine(550, 125, 600, 125);
            DrawArrowhead(g, 600, 125, -15, 0);
            
            g.dispose();
        }
		
		private void DrawArrowhead(Graphics g, int x, int y, double dx, double dy) {
			double cos = 0.866;
			double sin = 0.500;
			Graphics2D g2d = (Graphics2D) g;
			Color backupColor = g.getColor();
			
			GeneralPath arrowhead = new GeneralPath();
			arrowhead.moveTo(x, y);
			arrowhead.lineTo(x + dx * cos + dy * sin, y + dx * -sin + dy * cos);
			arrowhead.lineTo(x + dx * cos + dy * -sin, y + dx * sin + dy * cos);
			arrowhead.lineTo(x, y);
			arrowhead.closePath();
			
			g2d.setColor(Color.black);
			g2d.fill(arrowhead);
			
			g.setColor(backupColor);
		}
	}
}
