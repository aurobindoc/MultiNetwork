package version01;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class Queues {
	static double interArrivalTime = 30;
	static int qSizeUniform = 1000;
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
