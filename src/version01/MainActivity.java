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
//		int j=0;
//		for (ArrayList<Packets> pac : Packets.packetList) {
//			System.out.println("------------- Queue "+(j++)+" -------------\n");
//			for (Packets p : pac) {
//				if(p.arrS == -1 || p.dprtS == -1)	break;
//				System.out.println(p.packetID+", "+p.queueID+" || "+p.arrQ+", "+p.arrS+" : "+p.waitingTime+" || "
//						+ ""+p.arrS+", "+p.dprtS+" : "+p.serviceTime);
//			}
//			System.out.println();
//		}
		
		System.out.println("Total Time Server busy = "+Server.busyTime+" Total Time = "+endSimTime);
	}
	
	public static void initialize()	{
		for (int i = 0; i < Server.numQueue; i++) {
			Packets.packetList.add(new ArrayList<Packets>());
			Queues q = new Queues(i, Queues.qSizeUniform, ("Queue_"+i), new PriorityQueue<Event>(Queues.qSizeUniform, Comparator));
			Queues.queueList.add(q);
			
			Packets.packetList.get(i).add(new Packets(0, i, simTime, -1, -1, -1, -1));
			Queues.queueList.get(i).eventQueue.add(new Event(simTime, 0, i, "arriveQueue"));
			eventList.add(new Event(simTime, 0, i, "arriveQueue"));
		}
		Event first = Queues.queueList.get(0).eventQueue.poll();
		eventList.add(new Event(simTime, first.packetID, 0, "arriveServer"));
	}
	
	public static void simulate()	{
		Event e;
		while(simTime <= endSimTime)	{
			e = eventList.poll();
			if(e == null)	continue;
//			if(e.eventType.equals("arriveServer"))System.out.println(e.timeStamp+" : "+e.eventType+","+e.packetID+","+e.queueID);
			if(e.eventType.equals("departServer"))System.out.println(e.packetID+" "+ e.queueID+" " +Packets.packetList.get(e.queueID).get(e.packetID).waitingTime);
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
		double nxtArrival = getExponential(1/Queues.interArrivalTime);
		Packets.packetList.get(e.queueID).add(new Packets(e.packetID+1, e.queueID, e.timeStamp+nxtArrival, -1, -1, -1, -1));
		e = new Event(e.timeStamp+nxtArrival, e.packetID+1, e.queueID, "arriveQueue");
		eventList.add(e);
		Queues.queueList.get(e.queueID).eventQueue.add(e);
	}
	
	public static void departQueue(Event e)	{
//		System.out.println(e.eventType+","+e.packetID+","+e.queueID+","+e.timeStamp);
			eventList.add(new Event(e.timeStamp, e.packetID, e.queueID, "arriveServer"));
	}
	
	public static void arriveServer(Event e)	{
		double servTime = getExponential(1/Server.serviceTime);
		Packets p = Packets.packetList.get(e.queueID).get(e.packetID);
//		System.out.println(e.eventType+","+e.packetID+","+e.queueID+","+e.timeStamp);
		
		p.arrS = e.timeStamp;
		p.waitingTime = p.arrS - p.arrQ;
		p.serviceTime = servTime;
		eventList.add(new Event(e.timeStamp+servTime, p.packetID, p.queueID, "departServer"));
	}
	
	public static void departServer(Event e)	{
		Packets selectedPacket = null;
		int selectedQueue = ++roundRobin%Server.numQueue;
//		System.out.println(e.eventType+","+e.packetID+","+e.queueID+","+e.timeStamp);
		Server.busyTime += Packets.packetList.get(e.queueID).get(e.packetID).serviceTime;
		if(e.timeStamp < Server.lastDepart)	{
			e.timeStamp = Server.lastDepart + Packets.packetList.get(e.queueID).get(e.packetID).serviceTime;
		}
		Server.lastDepart = e.timeStamp;
		Packets.packetList.get(e.queueID).get(e.packetID).dprtS = e.timeStamp;
		
		try {
			Event winner = Queues.queueList.get(selectedQueue).eventQueue.poll();
			selectedPacket = Packets.packetList.get(winner.queueID).get(winner.packetID);
			eventList.add(new Event(e.timeStamp<selectedPacket.arrQ?selectedPacket.arrQ:e.timeStamp, selectedPacket.packetID, selectedQueue, "arriveServer"));
		} catch (NullPointerException e1) {}
	}
	
	public static double getExponential(double rand)	{
		return  Math.log(1-new Random().nextDouble())/(-rand);
	}
}
