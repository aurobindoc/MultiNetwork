package version01;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

public class MainActivity {
	public static double simTime=0, endSimTime=10000;
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
	}
	
	public static void initialize()	{
//		for (int i = 0; i < Server.numQueue; i++) {
//			Queues q = new Queues(i, Queues.qSizeUniform, ("Queue_"+i));
//		}
		Queues q = new Queues(0, 1000, "Queue_0");
		eventList.add(new Event(0, 1, 0, "arrive"));
		
	}
	
	public static void simulate()	{
		Event e;
		while(simTime <= endSimTime)	{
			e = eventList.poll();
			switch(e.eventType)	{
				case "arrive":	arrive(e);
				break;
				case "depart":	depart(e);
				break;
				default :	break;	
			}
			simTime = e.timeStamp;
		}
	}
	
	public static void arrive(Event e)	{
//		System.out.println("Packet pid_"+e.packetID+" "+e.eventType+" in Queue "+e.queueID+" at time "+e.timeStamp);
		eventList.add(new Event(e.timeStamp+getExponential(1/Server.serviceTime), e.packetID, e.queueID, "depart"));
		eventList.add(new Event(e.timeStamp+getExponential(1/Packets.interArrivalTime), e.packetID+1, e.queueID, "arrive"));
	}
	
	public static void depart(Event e)	{
		System.out.println("Packet pid_"+e.packetID+" from Queue "+e.queueID+" "+e.eventType+" at time "+e.timeStamp);
	}
	
	public static double getExponential(double rand)	{
		return  Math.log(1-new Random().nextDouble())/(-rand);
	}
}
