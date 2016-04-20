package version03;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class Queues {
	static double[] avgInterArrivalTime = new double[Server.numQueue] ;
	static int[] priority = new int[Server.numQueue] ;
	static int qSizeUniform = 10;
	static int[] packetDroped = new int[Server.numQueue];
	int queueID, queueSize;
	String queueName;
	PriorityQueue<Event> eventQueue;
	
	public Queues(int queueID, int queueSize, String queueName, PriorityQueue<Event> eventQueue) {
		super();
		this.queueID = queueID;
		this.queueSize = queueSize;
		this.queueName = queueName;
		this.eventQueue = eventQueue;
	}
	
	static ArrayList<Queues> queueList = new ArrayList<Queues>();
}
