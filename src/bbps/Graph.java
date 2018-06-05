package bbps;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.image.*;
import java.util.*;

public class Graph {
	private Vector<Link> links = new Vector<Link>();

	private Integer nodesNum = 0;
	
	public Graph() {
		links.add(new Link(null, new Node(null, null, nodesNum++)));
	}
	
	public void addNode(int sourceIndex, Node node) {
		Node sourceNode = getNodeByIdx(sourceIndex);
		if (sourceNode != null) {
			links.add(new Link(sourceNode, new Node(node.getSchedule(), node.getEvaluation(), nodesNum++)));
		}
	}
	
	public BufferedImage getGraphImage() {
		BufferedImage bi;
		
		final int nodeDiameter = 40;
		final int horizontalNodeMargin = 70;
		final int verticalNodeMargin = 40;
		final int numberOfTasks = getOutDegree(0);
		final int imageWidth = 2500;
		final int imageHight = nodeDiameter * (numberOfTasks + 1) + verticalNodeMargin * (numberOfTasks + 2);
		
		bi = new BufferedImage(imageWidth, imageHight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) bi.createGraphics();
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, bi.getWidth() - 1, bi.getHeight() - 1);
		g.setStroke(new BasicStroke(3));
		g.setFont(new Font("Consolas", Font.PLAIN, 25));
		
		//
		// Drawing graph nodes
		//
		g.drawArc(imageWidth/2 - nodeDiameter/2, verticalNodeMargin, nodeDiameter, nodeDiameter, 0, 360);
		int prevNodeSourceIdx = 0;
		int currLayerIdx = 1;
		int prevLayerNodeIdx = 1; // With 0 works wrong, so despite nonsense of 1, it's selected as initial value
		int prevPrevLayerNodeIdx = -2; // Empirical founded value.
		int currNodeSourceIdx;
		int currNodeIdx;
		int delta = 0;
		int prevDelta = 0;
		for (int i = 1; i < links.size(); i++) {
			currNodeSourceIdx = links.get(i).getSource().getIndex();
			currNodeIdx = links.get(i).getDestination().getIndex();
			
			// Calculate nodes position
			if (prevNodeSourceIdx != currNodeSourceIdx) {
				currLayerIdx++;
				prevNodeSourceIdx = currNodeSourceIdx;
				prevPrevLayerNodeIdx = prevLayerNodeIdx;
				prevLayerNodeIdx = currNodeIdx;
				prevDelta += delta;
			}
			
			int additionalMarginBetweenNodes = (int)Math.round(1.055*Math.sqrt(currLayerIdx));
			int centerSourceNodeX = horizontalNodeMargin + (currNodeSourceIdx - prevPrevLayerNodeIdx)*(nodeDiameter + horizontalNodeMargin*(int)Math.round(1.055*Math.sqrt(currLayerIdx - 1))) + nodeDiameter/2;
			int centerOfCurrLayerX = (2*horizontalNodeMargin + getLayerNodeNum(currLayerIdx)*(nodeDiameter + horizontalNodeMargin*additionalMarginBetweenNodes) - horizontalNodeMargin*additionalMarginBetweenNodes)/2;
			if (currLayerIdx == 1) {
				delta = imageWidth/2 - centerOfCurrLayerX;
			} else {
				delta = centerSourceNodeX - centerOfCurrLayerX;
			}
			
			int nodeX = horizontalNodeMargin + delta + prevDelta + (currNodeIdx - prevLayerNodeIdx)*(nodeDiameter + horizontalNodeMargin*additionalMarginBetweenNodes);
			int nodeY = verticalNodeMargin + currLayerIdx*(nodeDiameter + verticalNodeMargin);
			
			// Draw node
			g.drawArc(nodeX, nodeY, nodeDiameter, nodeDiameter, 0, 360);
			
			Node currNode = getNodeByIdx(currNodeIdx);
			
			// Draw information about node
			g.drawString(""+(int)Math.round(currNode.getEvaluation()), nodeX + nodeDiameter/8, nodeY + 30);
			g.setFont(new Font("Consolas", Font.PLAIN, 16));
			g.drawString(""+currNode.getSchedule(), nodeX + nodeDiameter + 4, nodeY + nodeDiameter/2 + 5);
			g.setFont(new Font("Consolas", Font.PLAIN, 25));
			
			// Draw arrows between nodes
			g.setStroke(new BasicStroke(2));
			int lineStartX;
			int lineStartY = verticalNodeMargin + (currLayerIdx - 1)*(nodeDiameter + verticalNodeMargin) + nodeDiameter;
			int lineEndX = nodeX + nodeDiameter/2;
			int lineEndY = nodeY;
			if (currLayerIdx == 1) {
				lineStartX = imageWidth/2;			
			} else {
				lineStartX = centerSourceNodeX + prevDelta;
			}
			g.drawLine(lineStartX, lineStartY, lineEndX, lineEndY);
			g.setStroke(new BasicStroke(3));
			
			// Draw arrowheads
			int dx = lineEndX - lineStartX;
			int dy = nodeY - (verticalNodeMargin + (currLayerIdx - 1)*(nodeDiameter + verticalNodeMargin) + nodeDiameter);
			int scale = 15;
			DrawArrowhead(g, nodeX + nodeDiameter/2, nodeY, -dx/Math.sqrt(dx*dx + dy*dy)*scale, -dy/Math.sqrt(dx*dx + dy*dy)*scale);
		}
		
		return bi;
	}
	
	public int getLayerNodeNum(int layerIdx) {
		int count = 0;
		int prevSouceNodeIdx = -1;
		int currSouceNodeIdx = 0;
		int nodeCount = 0;
		
		if (layerIdx == 0) {
			return 1;
		}
		
		for (int i = 1; i < links.size(); i++) {
			currSouceNodeIdx = links.get(i).getSource().getIndex();
			
			if (prevSouceNodeIdx != currSouceNodeIdx) {
				count++;
				prevSouceNodeIdx = currSouceNodeIdx;
				if (nodeCount != 0) {
					return nodeCount;
				}
			}
			if (count == layerIdx) {
				nodeCount++;
			}
		}
		return nodeCount;
	}
	
	public Node getNodeByIdx(int nodeIdx) {
		for (int i = 0; i < links.size(); i++) {
			Node currNode = links.get(i).getDestination();
			if (currNode.getIndex() == nodeIdx) return currNode;
		}
		return null;
	}
	
	public int getOutDegree(int nodeIdx) {
		int outDegree = 0;
		for (int i = 1; i < links.size(); i++) {
			Node currNode = links.get(i).getSource();
			if (currNode.getIndex() == nodeIdx) outDegree++;
		}
		return outDegree;
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
	
	public int getNodesNum() {
		return nodesNum;
	}
	
	public Vector<Link> getLinks()
	{
		return links;
	}
}

class Node {
	private int Index;
	private Vector<Task> Schedule = new Vector<Task>();
	private Double Evaluation = 0.0;
	
	public Node(Vector<Task> schedule, Double evaluation) {
		this.setIndex(-1);
		this.setEvaluation(evaluation);
		this.setSchedule(schedule);
	}
	
	public Node(Vector<Task> schedule, Double evaluation, int index) { 
		this.setIndex(index);
		this.setEvaluation(evaluation);
		this.setSchedule(schedule);
	}
	
	@Override
	public String toString() {
		return String.format("{%s, %f}", Schedule != null ? Schedule.toString() : null, Evaluation);
	}

	public int getIndex() {
		return Index;
	}

	private void setIndex(int index) {
		Index = index;
	}

	public Double getEvaluation() {
		return Evaluation;
	}

	private void setEvaluation(Double evaluation) {
		Evaluation = evaluation;
	}

	public Vector<Task> getSchedule() {
		return Schedule;
	}

	private void setSchedule(Vector<Task> schedule) {
		Schedule = schedule;
	}
}

class Link {
	private Node source;
	private Node destination;
	
	public Link(Node src, Node dst) { 
		this.setSource(src);
		this.setDestination(dst);
	}

	public Node getDestination() {
		return destination;
	}

	private void setDestination(Node destination) {
		this.destination = destination;
	}
	
	public Node getSource() {
		return source;
	}

	private void setSource(Node source) {
		this.source = source;
	}
	
	@Override
	public String toString() {
		return String.format("Source: %s, Destination: %s", source, destination);
	}
}
