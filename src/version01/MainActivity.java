package version01;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

public class MainActivity {
	public static double simTime=0, endSimTime=1000;
	public static Comparator<Event> Comparator = new Comparator<Event>() {
		@Override
		public int compare(Event c1, Event c2) {
			if (c1.timeStamp < c2.timeStamp)
				return -1;
			else if (c1.timeStamp > c2.timeStamp)
				return 1;
			return 0;
		}
	};
	public static Queue<Event> eventList = new PriorityQueue<>(Comparator);
	public static int roundRobin = 0;
	public static void main(String[] args)	{
		
		/*********** Checks whether the random function actually works!!! *******/
		/*double mean=0, s;
		for (int i = 0; i < 100; i++) {
			s = getExponential(0.5);
			System.out.println(s);
			mean += s;
		}
		System.out.println("Mean ="+ mean/100);*/
		
		
		initialize();
		simulate();
		int j=0;
		for (ArrayList<Packets> pac : Packets.packetList) {
			System.out.println("------------- Queue "+(j++)+" -------------\n");
			for (Packets p : pac) {
				System.out.println(p.packetID+", "+p.queueID+" || "+p.arrQ+", "+p.arrS+" : "+p.waitingTime+" || "+p.arrS+", "+p.dprtS+" : "+p.serviceTime);
			}
			System.out.println();
		}
	}
	
	public static void initialize()	{
		for (int i = 0; i < Server.numQueue; i++) {
			Packets.packetList.add(new ArrayList<Packets>());
			Queues q = new Queues(i, Queues.qSizeUniform, ("Queue_"+i), new PriorityQueue<Event>(Queues.qSizeUniform, Comparator));
			Queues.queueList.add(q);
			
			Packets.packetList.get(i).add(new Packets(0, i, simTime, -1, -1, -1, -1));
			eventList.add(new Event(simTime, 0, i, "arriveQueue"));
			eventList.add(new Event(simTime+getExponential(1/Server.serviceTime), 0, i, "departServer"));
		}
	}
	
	public static void simulate()	{
		Event e;
		while(simTime <= endSimTime)	{
			e = eventList.poll();
			switch(e.eventType)	{
				case "arriveQueue":		arriveQueue(e);
				break;
				case "departQueue":		departQueue(e);
				break;
				case "arriveServer":	arriveServer(e);
				break;
				case "departServer":	departServer(e);
				break;
				default :	break;	
			}
			simTime = e.timeStamp;
		}
	}
	
	public static void arriveQueue(Event e)	{
//		System.out.println(e.eventType+","+e.packetID+","+e.queueID+","+e.timeStamp);
		Packets.packetList.get(e.queueID).add(new Packets(e.packetID+1, e.queueID, e.timeStamp, -1, -1, -1, -1));
		eventList.add(new Event(e.timeStamp+getExponential(1/Queues.interArrivalTime), e.packetID+1, e.queueID, "arriveQueue"));
		Queues.queueList.get(e.queueID).eventQueue.add(e);
	}
	
	public static void departQueue(Event e)	{
//		System.out.println(e.eventType+","+e.packetID+","+e.queueID+","+e.timeStamp);
		Packets selectedPacket = null;
		
		try {
			Event winner = Queues.queueList.get(e.queueID).eventQueue.poll();
			selectedPacket = Packets.packetList.get(winner.queueID).get(winner.packetID);
			selectedPacket.arrS = e.timeStamp;
			selectedPacket.waitingTime = e.timeStamp - selectedPacket.arrQ;
			eventList.add(new Event(e.timeStamp, selectedPacket.packetID, e.queueID, "arriveServer"));
		} catch (NullPointerException e1) {}
	}
	
	public static void arriveServer(Event e)	{
		double servTime = getExponential(1/Server.serviceTime);
//		System.out.println(e.eventType+","+e.packetID+","+e.queueID+","+e.timeStamp);
		Packets.packetList.get(e.queueID).get(e.packetID).serviceTime = servTime;
		eventList.add(new Event(e.timeStamp+servTime, e.packetID, e.queueID, "departServer"));
	}
	
	public static void departServer(Event e)	{
//		System.out.println(e.eventType+","+e.packetID+","+e.queueID+","+e.timeStamp);
		
		Packets.packetList.get(e.queueID).get(e.packetID).dprtS = e.timeStamp;
		eventList.add(new Event(e.timeStamp, e.packetID, (roundRobin++%Server.numQueue), "departQueue"));
	}
	
	public static double getExponential(double rand)	{
		return  Math.log(1-new Random().nextDouble())/(-rand);
	}
}
