package version01;

import java.util.ArrayList;

public class Packets {
	int packetID;
	int queueID;
	double arrQ, arrS, dprtS, waitingTime, serviceTime;
	
	public Packets(int packetID, int queueID, double arrQ, double arrS, double dprtS, double waitingTime,
			double serviceTime) {
		this.packetID = packetID;
		this.queueID = queueID;
		this.arrQ = arrQ;
		this.arrS = arrS;
		this.dprtS = dprtS;
		this.waitingTime = waitingTime;
		this.serviceTime = serviceTime;
	}
	public static ArrayList<ArrayList<Packets>> packetList = new ArrayList<ArrayList<Packets>>(Server.numQueue);
}
