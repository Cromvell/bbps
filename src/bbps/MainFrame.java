package bbps;

import java.awt.*;
import java.awt.geom.*;
import java.io.File;
import java.util.*;
import java.awt.event.*;
import java.text.*;

import javax.swing.*;
import javax.swing.Timer;

import bbps.Task.Unit;

public class MainFrame extends JFrame implements ActionListener, PipelineListener {

	Task.Unit currentUnit = Unit.milliseconds;
	
	Pipeline pipeline = new Pipeline();
	Scheduler scheduler;
	Vector<Task> work = new Vector<Task>();
	
	Boolean pipelineRunning = false;
	Boolean graphShow = false;
	Boolean tasksEntered = false;
	
	Timer timer;
	long startTime;
	long workDuration;
	
	public MainFrame() {
		Initialize();
		
		pipeline.addListener(this);
	}
	
	////////////////////////////////////
	//
	// 	      Event handlers
	//
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(openFileMenuItem)) {  // <----- Open file menu item
			System.out.println("Open schedule");
		} else if (e.getSource().equals(saveFileMenuItem)) {  // <----- Save file menu item
			System.out.println("Save schedule");
		} else if (e.getSource().equals(exitFileMenuItem)) {  // <----- Exit program menu item
			System.exit(0);
		} else if (e.getSource().equals(enterDataButton)) {  // <----- Enter data button
			enterDataButton.setEnabled(false);
			
			// Reset pipeline if it's running
			if (pipeline.isEnabled()) {
				resetAll();
			}
			
			final EnterDataDialog dlg = new EnterDataDialog();
			new Thread( new Runnable() { public void run() {
				if (dlg.showDialogue() == 0) {
					work = dlg.getResult();
					
					scheduler = new Scheduler(work);
					scheduler.setUnit(currentUnit);
					
					pipeline.uploadWork(work);

					allowPipelineStart();
					showSchedule(work);
					scrollPane.setViewportView(null);
					tasksEntered = true;
					
					enterDataButton.setEnabled(true);
				}
			}}).start();
		} else if (e.getSource().equals(unitSelectComboBox)) {  // <----- Select unit combo box
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
			if (pipeline.isEnabled()) {
				resetAll();
			}
			
			if (tasksEntered) {
				scheduler.setUnit(currentUnit);
			}
		} else if (e.getSource().equals(scheduleButton)) {  // <----- Compose schedule button
			// Reset pipeline if it's running
			if (pipeline.isEnabled()) {
				resetAll();
			}
			
			scheduler.composeSchedule();
			work = scheduler.getTasks();
			pipeline.uploadWork(work);
			showSchedule(work);
			
			// Create label, that contains graph and add mouse listener for dragging image
			final JLabel graph = new JLabel(new ImageIcon(scheduler.getGraph().getGraphImage()));
			MouseAdapter ma = new MouseAdapter() {

                private Point origin;

                @Override
                public void mousePressed(MouseEvent e) {
                    origin = new Point(e.getPoint());
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (origin != null) {
                        JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, graph);
                        if (viewPort != null) {
                            int deltaX = origin.x - e.getX();
                            int deltaY = origin.y - e.getY();

                            Rectangle view = viewPort.getViewRect();
                            view.x += deltaX;
                            view.y += deltaY;

                            graph.scrollRectToVisible(view);
                        }
                    }
                }

            };
            graph.addMouseListener(ma);
            graph.addMouseMotionListener(ma);
			
			// Display Graph
			scrollPane.setViewportView(graph);
			
			// Set viewport position to center
			Rectangle bounds = scrollPane.getViewport().getViewRect();
			Dimension size = scrollPane.getViewport().getViewSize();
			int hostCenterX = (size.width - bounds.width) / 2;
			scrollPane.getViewport().setViewPosition(new Point(hostCenterX, 0));
			graphShow();
		} else if (e.getSource().equals(showGraphButton)) {  // <----- Show graph button
			if (graphShow) {
				graphHide();
			} else {
				graphShow();
			}
		} else if (e.getSource().equals(startPauseButton)) {  // <----- Start/Pause button
			if (!pipelineRunning) {
				if (!pipeline.isEnabled()) {
					new Thread(new Runnable() { public void run() { pipeline.run(); }}).start();
					startTime = Calendar.getInstance().getTimeInMillis();
				}
				pipeline.resume();
				allowPipelineStop();
				
				timerResume();
				
				stopResetButton.setText("Stop");
				startPauseButton.setText("\u23F8");
				pipelineRunning = true;
			} else {
				pipeline.suspend();
				allowPipelineStart();
				
				timerPause();
				
				stopResetButton.setText("Reset");
				startPauseButton.setText("\u25B6");
				pipelineRunning = false;
			}
		} else if (e.getSource().equals(nextStepButton)) {  // <----- Next step button
			if (!pipeline.isEnabled()) {
				new Thread(new Runnable() { public void run() { pipeline.run(); }}).start();
			}
			pipeline.nextStep();
			
		} else if (e.getSource().equals(stopResetButton)) {  // <----- Stop/Reset button
			resetAll();
		}
	}
	
	public void graphShow() {
		scrollPane.setVisible(true);
		this.setSize(815, 700);
		graphShow = true;
	}
	
	public void graphHide() {
		this.setSize(815, 310);
		scrollPane.setVisible(false);
		graphShow = false;
	}
	
	public void timerPause() {
		if (timer != null) {
			this.timer.stop();
		}
	}
	
	public void timerResume() {
		startTime = Calendar.getInstance().getTimeInMillis() - workDuration;
	    this.timer = new Timer(1, new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
	    		Calendar c = Calendar.getInstance();
	    		workDuration = (long) (c.getTimeInMillis() - startTime);
	    		timerLabel.setText("Timer: " + workDuration / 1000.0);
	    }});
	    this.timer.start();
	}
	
	public void timerReset() {
		if (timer != null) {
			this.timer.stop();
		}
	    this.workDuration = 0;
	    timerLabel.setText("Timer: 0.000");
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
		
		clearAllPanelTask();
		
		Color darkGreen = new Color(0, 184, 0);
		setPanelTaskColor(_1DevTaskIdx, Color.RED);
		setPanelTaskColor(_2DevTaskIdx, darkGreen);
		setPanelTaskColor(_3DevTaskIdx, Color.BLUE);
	}
	
	private void setPanelTaskColor(int taskIdx, Color col) {
		switch (taskIdx) {
		case 0:
			task1Panel.setBackground(col);
			task1Label.setForeground(Color.WHITE);
			break;
		case 1:
			task2Panel.setBackground(col);
			task2Label.setForeground(Color.WHITE);
			break;
		case 2:
			task3Panel.setBackground(col);
			task3Label.setForeground(Color.WHITE);
			break;
		case 3:
			task4Panel.setBackground(col);
			task4Label.setForeground(Color.WHITE);
			break;
		case 4:
			task5Panel.setBackground(col);
			task5Label.setForeground(Color.WHITE);
			break;
		case 5:
			task6Panel.setBackground(col);
			task6Label.setForeground(Color.WHITE);
			break;
		}
	}
	
	private void clearAllPanelTask() {
		int taskNumber = 6;
		for (int i = 0; i < taskNumber; i++) {
			switch (i) {
			case 0:
				task1Panel.setBackground(Color.WHITE);
				task1Label.setForeground(Color.BLACK);
				break;
			case 1:
				task2Panel.setBackground(Color.WHITE);
				task2Label.setForeground(Color.BLACK);
				break;
			case 2:
				task3Panel.setBackground(Color.WHITE);
				task3Label.setForeground(Color.BLACK);
				break;
			case 3:
				task4Panel.setBackground(Color.WHITE);
				task4Label.setForeground(Color.BLACK);
				break;
			case 4:
				task5Panel.setBackground(Color.WHITE);
				task5Label.setForeground(Color.BLACK);
				break;
			case 5:
				task6Panel.setBackground(Color.WHITE);
				task6Label.setForeground(Color.BLACK);
				break;
			}
		}
	}
	
	@Override
	public void workDone() {
		taskStage1Label.setText("A");
		taskStage2Label.setText("B");
		taskStage3Label.setText("C");
		stopResetButton.setText("Reset");
		clearAllPanelTask();
		
		forbidPipelineActions();
		timerPause();
	}
	
	private void resetAll() {
		pipeline.stop();
		pipeline.reset();
		scheduler.reset();
		pipeline.uploadWork(scheduler.getTasks());
		pipelineRunning = false;

		stopResetButton.setText("Reset");
		timerReset();
		clearAllPanelTask();
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
		stopResetButton.setEnabled(true);

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
	JButton stopResetButton, enterDataButton, startPauseButton, nextStepButton, scheduleButton, showGraphButton;
	JComboBox<String> unitSelectComboBox;
	JScrollPane scrollPane;
	JLabel taskListLabel, timerLabel;
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
		openFileMenuItem = new JMenuItem("Open schedule");
		openFileMenuItem.addActionListener(this);
		fileMenu.add(openFileMenuItem);
		
		// saveFileMenuItem
		saveFileMenuItem = new JMenuItem("Save schedule");
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
		stopResetButton = new JButton("Reset");
		stopResetButton.addActionListener(this);
		stopResetButton.setEnabled(false);
		controlButtonsPanel.add(stopResetButton);
		
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
		pipelineStage1Label.setForeground(Color.RED);
		visualizationPanel.add(pipelineStage1Label);
		
		// pipelineStage2Label
		pipelineStage2Label = new JLabel("Stage 2");
		pipelineStage2Label.setBounds(400, 80, 50, 20);
		pipelineStage2Label.setForeground(new Color(0, 184, 0));
		visualizationPanel.add(pipelineStage2Label);
		
		// pipelineStage3Label
		pipelineStage3Label = new JLabel("Stage 3");
		pipelineStage3Label.setBounds(500, 80, 50, 20);
		pipelineStage3Label.setForeground(Color.BLUE);
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
		
		// timerLabel
		timerLabel = new JLabel("Timer: 0.000");
		timerLabel.setBounds(580, 217, 100, 20);
		visualizationPanel.add(timerLabel);
		
		// showGraphButton
		showGraphButton = new JButton("Show Graph");
		showGraphButton.setBounds(670, 215, 120, 25);
		showGraphButton.addActionListener(this);
		visualizationPanel.add(showGraphButton);
		
		// scrollPane
		scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 250, 800, 390);
		scrollPane.setVisible(false);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.getViewport().setBackground(Color.white);
		scrollPane.setCursor(new Cursor(Cursor.MOVE_CURSOR));
		this.add(scrollPane);
		
		// this
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(815, 310);
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2-200);
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
