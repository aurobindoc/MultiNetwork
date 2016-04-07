package version01;
/**
 * Author                           
  ______                                
 /      \                               
|  $$$$$$\ __    __   ______    ______  
| $$__| $$|  \  |  \ /      \  /      \ 
| $$    $$| $$  | $$|  $$$$$$\|  $$$$$$\
| $$$$$$$$| $$  | $$| $$   \$$| $$  | $$
| $$  | $$| $$__/ $$| $$      | $$__/ $$
| $$  | $$ \$$    $$| $$       \$$    $$
 \$$   \$$  \$$$$$$  \$$        \$$$$$$ 
                                        
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

public class MainActivity {
	public static double simTime = 0, endSimTime = 100;
	public static String policy = null; 
	//overload compare operator for a Priority Queue so as to select events based on timeStamp
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
	
	//eventList is a public/global priority queue which contains all the events
	public static Queue<Event> eventList = new PriorityQueue<>(Comparator);
	public static int roundRobin = 0,lastServedQ = -1;
	
	public static void main(String[] args) throws IOException	{
		construct();
		/****************** Initialize Each Queue & add first packets to the Queues *******/
		initialize();
		/****************** The simulation Engine *******************/
		simulate();
		
		displayPacketData(); // Display data per packet
	}
	
	public static void construct() throws IOException	{
		File file = new File("inputData.txt");
		if (!file.exists()) 
		{
			System.out.println("Input file not exist");
			System.exit(0);
		}
		BufferedReader br = new BufferedReader(new FileReader(file));
		int i=0;
		String[] value;
		endSimTime = Double.parseDouble(br.readLine());
		Server.numQueue = Integer.parseInt(br.readLine());
		Queues.qSizeUniform = Integer.parseInt(br.readLine());
		value = br.readLine().split(",");
		for (String v : value) {
			Queues.avgInterArrivalTime[i++] = Double.parseDouble(v);
		}
		Server.avgServiceTime = Double.parseDouble(br.readLine());
		Server.networkSwitchingTime = Double.parseDouble(br.readLine());
		policy = br.readLine();
		if(policy.equals("RRP"))	{
			i=0;
			value = br.readLine().split(",");
			for (String v : value) {
				Queues.priority[i++] = Integer.parseInt(v);
			}
		}
		else	{
			for (i=0; i<Server.numQueue; i++) {
				Queues.priority[i++] = 1;
			}
		}
		
//		System.out.println(endSimTime+"\n"+Server.numQueue+"\n"+Queues.qSizeUniform);
//		for (double at : Queues.avgInterArrivalTime) {
//			System.out.print(at+",");
//		}
//		System.out.println();
//		System.out.println(Server.avgServiceTime+"\n"+Server.networkSwitchingTime+"\n"+policy);
//		for (int p : Queues.priority) {
//			System.out.print(p+",");
//		}
//		System.out.println();
	}
	
	public static void displayPacketData() {
		double waitTime=0, resTime=0;
		int numPack=0;
		int j=0;
		for (ArrayList<Packets> pac : Packets.packetList) {
			System.out.println("------------- Queue "+(j++)+" -------------\n");
			 waitTime=0;
			 resTime=0;
			 numPack=0;
			for (Packets p : pac) {
				if(p.arrS == -1 || p.dprtS == -1)	break;
				System.out.println(p.packetID+", "+p.queueID+" || "+p.arrQ+", "+p.arrS+" : "+p.waitingTime+" || "+ ""+p.arrS+", "+p.dprtS+" : "+p.serviceTime);
				waitTime+=p.waitingTime;
				resTime+=p.serviceTime+p.waitingTime;
				numPack++;
			}
			System.out.println();
			System.out.println("Number of Packets served = "+numPack);
			System.out.println("Avg. Waiting Time = "+waitTime/numPack);
			System.out.println("Avg. Response Time = "+resTime/numPack);
			System.out.println();
		}
		System.out.println("------------- Server Details -------------");
		System.out.println("Server Utilisation = "+Server.busyTime/endSimTime);
		System.out.println("Throughput = "+Server.numDepart/endSimTime);
		System.out.println("Total Network Switching Time = "+Server.totalNST);
	}
	
	public static void initialize()	{
		/**
		 * Create New Queues and Add it to list of Queue
		 * For each Queue, 
		 * 		Create the first packet in it.
		 * 		Add the packet to the Queue.
		 * 		Add the packet to the list of Packets
		 * 
		 * Trigger the 1st Event to occur
		 * */
		for (int i = 0; i < Server.numQueue; i++) {
			//initialize packetList ArrayList
			Packets.packetList.add(new ArrayList<Packets>());
			//create a Queue object q
			Queues q = new Queues(i, Queues.qSizeUniform, ("Queue_"+i), new PriorityQueue<Event>(Queues.qSizeUniform, Comparator));
			//add the Queue object q to queueList
			Queues.queueList.add(q);
			
			//create a new Packet object and add to ith index of packetList (ArrayList of ArrayList of packet)
			Packets.packetList.get(i).add(new Packets(0, i, simTime, -1, -1, -1, -1));
			
			Queues.queueList.get(i).eventQueue.add(new Event(simTime, 0, i, "arriveQueue"));
			eventList.add(new Event(simTime, 0, i, "arriveQueue"));
		}
		
		//trigger the first event - adding event to eventList is making it happen
		Event first = Queues.queueList.get(0).eventQueue.poll();
		eventList.add(new Event(simTime, first.packetID, 0, "arriveServer"));
	}
	
	public static void simulate()	{
		Event e;
		while(simTime <= endSimTime)	{
			e = eventList.poll();
			if(e == null)	continue;
//			if(e.eventType.equals("arriveServer"))System.out.println(e.timeStamp+" : "+e.eventType+","+e.packetID+","+e.queueID);
//			if(e.eventType.equals("departServer"))System.out.println(e.packetID+" "+ e.queueID+" " +Packets.packetList.get(e.queueID).get(e.packetID).waitingTime);
			switch(e.eventType)	{
				case "arriveQueue":		arriveQueue(e);
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
		double nxtArrival = getExponential(1/Queues.avgInterArrivalTime[e.queueID]);
		Packets.packetList.get(e.queueID).add(new Packets(e.packetID+1, e.queueID, e.timeStamp+nxtArrival, -1, -1, -1, -1));
		e = new Event(e.timeStamp+nxtArrival, e.packetID+1, e.queueID, "arriveQueue");
		eventList.add(e);
		Queues.queueList.get(e.queueID).eventQueue.add(e);
	}
	
	public static void arriveServer(Event e)	{
		double servTime;
		if(lastServedQ!=-1 && lastServedQ == e.queueID )	servTime = getExponential(1/Server.avgServiceTime);
		else	{
			servTime = Server.networkSwitchingTime + getExponential(1/Server.avgServiceTime);
			Server.totalNST += Server.networkSwitchingTime;
		}
		Packets p = Packets.packetList.get(e.queueID).get(e.packetID);
		lastServedQ = e.queueID;
		
		p.arrS = e.timeStamp;
		p.waitingTime = p.arrS - p.arrQ;
		p.serviceTime = servTime;
		eventList.add(new Event(e.timeStamp+servTime, p.packetID, p.queueID, "departServer"));
	}
	
	public static void departServer(Event e)	{
		Packets.packetList.get(e.queueID).get(e.packetID).dprtS = e.timeStamp;
		Server.numDepart++;
		
		Packets selectedPacket = null;
		int selectedQueue = ++roundRobin%Server.numQueue;
		Server.busyTime += Packets.packetList.get(e.queueID).get(e.packetID).serviceTime;
		
		boolean flag=false;
		int numQ=0;
		try {
			Event winner = Queues.queueList.get(selectedQueue).eventQueue.peek(), recent = winner;
			while(winner.timeStamp>e.timeStamp)	{
				if(numQ>=Server.numQueue)	{
					flag=true;
					break;
				}
				selectedQueue = ++roundRobin%Server.numQueue;
				winner = Queues.queueList.get(selectedQueue).eventQueue.peek();
				recent = winner.timeStamp<recent.timeStamp?winner:recent;
				numQ++;
			}
			winner = flag?Queues.queueList.get(recent.queueID).eventQueue.poll():Queues.queueList.get(winner.queueID).eventQueue.poll();
			selectedPacket = Packets.packetList.get(winner.queueID).get(winner.packetID);
			
			eventList.add(new Event(e.timeStamp<selectedPacket.arrQ?selectedPacket.arrQ:e.timeStamp, selectedPacket.packetID, selectedQueue, "arriveServer"));
		} catch (NullPointerException e1) {}
	}
	
	public static double getExponential(double rand)	{
		return  Math.log(1-new Random().nextDouble())/(-rand);
	}
}
