package version02;

public class Event {
	double timeStamp;
	int packetID, queueID;
	String eventType;
	
	public Event(double timeStamp, int requestID, int queueID, String eventType) {
		this.timeStamp = timeStamp;
		this.packetID = requestID;
		this.queueID = queueID;
		this.eventType = eventType;
	}
}
