package version01;

public class Packets {
	static double interArrivalTime = 10;
	int packetID;
	int queueID;
	public Packets(int packetID, int queueID) {
		this.packetID = packetID;
		this.queueID = queueID;
	}
}
