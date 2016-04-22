package version04;

import java.util.ArrayList;

public class Packets {
	int packetID;
	int queueID;
	double arrQ, arrS, dprtS, waitingTime, serviceTime, nsTime;
	
	public Packets(int packetID, int queueID, double arrQ, double arrS, double dprtS, double waitingTime,
			double serviceTime, double nsTime) {
		super();
		this.packetID = packetID;
		this.queueID = queueID;
		this.arrQ = arrQ;
		this.arrS = arrS;
		this.dprtS = dprtS;
		this.waitingTime = waitingTime;
		this.serviceTime = serviceTime;
		this.nsTime = nsTime;
	}
	
	public static ArrayList<ArrayList<Packets>> packetList = new ArrayList<ArrayList<Packets>>(Server.numQueue);
}
