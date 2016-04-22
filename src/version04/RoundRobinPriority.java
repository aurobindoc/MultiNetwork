package version04;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

public class RoundRobinPriority {
	public static double simTime = 0, endSimTime = 0;
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
	
	public static int lastServedQ = -1;	// stores the last queue that is served
	
	/* q2bServed is the Queue which is being served
	 * pckQ is the number of packets left to be served from that queue based on priority */
	public static int q2bServed = -1, pckQ = -1;
	
	public static void rrpMain() throws IOException	{
		/****************** Take input from File and initialize variables *******/
		construct();
		/****************** Initialize Each Queue & add first packets to the Queues *******/
		initialize();
		/****************** The simulation Engine *******************/
		simulate();
		/****************** Print the output to a file *******************/
		displayPacketData(); // Display data per packet
		printToFile();
	}
	
	public static void construct() throws IOException	{
		File file = new File("inputData.txt");	// read input data from file "inputData.txt" 
		if (!file.exists()) 
		{
			System.out.println("Input file not exist");
			System.exit(0);
		}
		@SuppressWarnings("resource")
		BufferedReader br = new BufferedReader(new FileReader(file));
		int i=0;
		String[] value;
		try {
			policy = br.readLine();
			endSimTime = Double.parseDouble(br.readLine());
			Server.numQueue = Integer.parseInt(br.readLine());
			Queues.qSizeUniform = Integer.parseInt(br.readLine());
			i=0;
			value = br.readLine().split(",");
			for (String v : value) {
				Queues.avgInterArrivalTime[i++] = Double.parseDouble(v);
			}
			Server.avgTxTime = Double.parseDouble(br.readLine());
			Server.avgRxTime = Double.parseDouble(br.readLine());
			i=0;
			value = br.readLine().split(",");
			for (String v : value) {
				Queues.errorProb[i++] = Double.parseDouble(v);
			}
			value = br.readLine().split("-");
			Server.nstMin = Integer.parseInt(value[0]);
			Server.nstMax = Integer.parseInt(value[1]);
			
			i=0;
			value = br.readLine().split(",");
			for (String v : value) {
				Queues.priority[i++] = Integer.parseInt(v);
			}
		} catch (NullPointerException e) {
			System.out.println("There's some problem with your input data. Please check it!!!");
			System.exit(0);
		}
		
		/*System.out.print("EndSimTime,"+endSimTime+"\nNumber Of Queue,"+Server.numQueue+"\nSize Of Queue,"+Queues.qSizeUniform+"\nMean InterArrival time per Queue,");
		for (double at : Queues.avgInterArrivalTime) {
			System.out.print(at+",");
		}
		System.out.println("\nAvg Transmit Time,"+Server.avgTxTime+",Avg RecieveACK Time,"+Server.avgRxTime+"\nNetwork Switching Time,"+Server.nstMin+"-"+Server.nstMax+"\nScheduling Policy,"+policy+"\nPriority,");
		for (int p : Queues.priority) {
			System.out.print(p+",");
		}
		System.out.println("\n");*/
		
		for (int j = 0; j < Server.numQueue; j++) {
			Queues.packetDroped[j]=0;
		}
	}
	
	public static void printToFile() throws IOException {
		double waitTime=0, resTime=0;
		int numPack=0;
		int j=0;
		
		FileWriter writer = new FileWriter(MainActivity.opFile);
		writer.append("EndSimTime,"+endSimTime+"\nNumber Of Queue,"+Server.numQueue+"\nSize Of Queue,"+Queues.qSizeUniform+"\nMean InterArrival time per Queue,");
		for (double at : Queues.avgInterArrivalTime) {
			writer.append(at+",");
		}
		writer.append("\nAvg Transmit Time,"+Server.avgTxTime+",Avg RecieveACK Time,"+Server.avgRxTime+"\nNetwork Switching Time,"+Server.nstMin+"-"+Server.nstMax+"\nScheduling Policy,"+policy+"\nPriority\n");
		
		for (int p : Queues.priority) {
			writer.append(p+",");
		}
		writer.append("\n");
		
		for (ArrayList<Packets> pac : Packets.packetList) {
			writer.append("\nQueue "+(j++)+"\n\n");
			writer.append("QueueID,PacketID,ArriveQ,nsTime,ArriveServer,DepartServer,WaitingTime,ServiceTime,ResponseTime\n");
			waitTime=0;
			resTime=0;
			numPack=0;
			for (Packets p : pac) {
				if(p.arrS == -1 || p.dprtS == -1)	break;
				writer.append(p.queueID+","+p.packetID+","+p.arrQ+","+p.nsTime+","+p.arrS+","+p.dprtS+","+p.waitingTime+","+p.serviceTime+","+(p.waitingTime+p.serviceTime+p.nsTime)+"\n");
				waitTime+=p.waitingTime;
				resTime+=p.serviceTime+p.waitingTime+p.nsTime;
				numPack++;
			}
			writer.append("Number of Packets served,"+numPack+"\n");
			writer.append("Avg. Waiting Time,"+waitTime/numPack+"\n");
			writer.append("Avg. Response Time,"+resTime/numPack+"\n");
			writer.append("Throughput per Queue,"+numPack/endSimTime+"\n");
			writer.append("Number of Packets Droped,"+Queues.packetDroped[j-1]+"\n");
		}
		writer.append("\nServer Details\n");
		writer.append("Server Utilisation,"+Server.busyTime/endSimTime+"\n");
		writer.append("Throughput,"+Server.numDepart/endSimTime+"\n");
		writer.append("Total Network Switching Time,"+Server.totalNST+"\n");
		writer.close();
	}
	
	public static void displayPacketData() throws IOException {
		double waitTime=0, AvgWaitTime=0, resTime=0, AvgResTime=0, AvgErrorProb=0, TotIATime=0;
		int numPack=0,TotNumPack=0,TotDropped=0;
		int j=0;
		
		for (ArrayList<Packets> pac : Packets.packetList) {
			 waitTime=0;
			 resTime=0;
			 numPack=0;
			for (Packets p : pac) {
				if(p.arrS == -1 || p.dprtS == -1)	break;
				waitTime+=p.waitingTime;
				resTime+=p.serviceTime+p.waitingTime+p.nsTime;
				numPack++;
			}
			TotNumPack += numPack;
			AvgWaitTime += waitTime/numPack;
			AvgResTime += resTime/numPack;
			TotDropped += Queues.packetDroped[j];
			AvgErrorProb += Queues.errorProb[j];
			TotIATime += Queues.avgInterArrivalTime[j];
			System.out.println(j+","+Queues.avgInterArrivalTime[j]+","+Queues.errorProb[j]+","+numPack+","+waitTime/numPack+","+resTime/numPack+","+numPack/endSimTime+","+Queues.packetDroped[j]);
			j++;
		}
		System.out.println("Avg,"+TotIATime+","+(AvgErrorProb/j)+","+TotNumPack+","+(AvgWaitTime/j)+","+(AvgResTime/j)+","+TotDropped+","+Server.busyTime/endSimTime+","+Server.numDepart/endSimTime+","+Server.totalNST);
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
			
			//create a new Packet object and add to i index of packetList (ArrayList of ArrayList of packet)
			Packets.packetList.get(i).add(new Packets(0, i, simTime, -1, -1, -1, -1, -1));
			
			Queues.queueList.get(i).eventQueue.add(new Event(simTime, 0, i, "arriveQueue"));
			eventList.add(new Event(simTime, 0, i, "arriveQueue"));
		}
		
		//trigger the first event - adding event to eventList is making it happen
		Event first = Queues.queueList.get(0).eventQueue.poll();
		eventList.add(new Event(simTime, first.packetID, 0, "arriveServer"));
		q2bServed = 0;
		pckQ = Queues.priority[q2bServed];
	}
	
	
	public static void simulate()	{
		Event e;
		while(simTime <= endSimTime)	{	// loop until endSimTime
			e = eventList.poll();	// pops the event with the least time-stamp 
			if(e == null)	continue;
			switch(e.eventType)	{	// switch on type of event to call its respective methods
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
		double nxtArrival = getExponential(1/Queues.avgInterArrivalTime[e.queueID]);	//get the next arrival time
		if(Queues.queueList.get(e.queueID).eventQueue.size() >= Queues.qSizeUniform)	{
			Queues.packetDroped[e.queueID]++;
			e = new Event(e.timeStamp+nxtArrival, e.packetID, e.queueID, "arriveQueue");	// create a new arrival event
			eventList.add(e);
		}
		else	{
			Packets.packetList.get(e.queueID).add(new Packets(e.packetID+1, e.queueID, e.timeStamp+nxtArrival, -1, -1, -1, -1,-1));	// add new packet to packetlist
			e = new Event(e.timeStamp+nxtArrival, e.packetID+1, e.queueID, "arriveQueue");	// create a new arrival event
			Queues.queueList.get(e.queueID).eventQueue.add(e);	// add event to priority queue of its respective Queue
			eventList.add(e);
		}
		
	}
	
	
	public static void arriveServer(Event e)	{
		double servTime = getServiceTimeGeneral(e.queueID), nst, totalServTime;	//get the service time
		// if last depart from same Q, then no network switching required
		if(lastServedQ != -1 && lastServedQ != e.queueID)	{
			nst = uniform(Server.nstMin, Server.nstMax);
			Server.totalNST += nst;
			totalServTime = servTime + nst;
			Packets.packetList.get(e.queueID).get(e.packetID).nsTime = nst;
			Packets.packetList.get(e.queueID).get(e.packetID).arrS = e.timeStamp+nst;
		}
		else	{
			totalServTime = servTime;
			Packets.packetList.get(e.queueID).get(e.packetID).nsTime = 0;
			Packets.packetList.get(e.queueID).get(e.packetID).arrS = e.timeStamp;
		}
		lastServedQ = e.queueID;
		//Update packet parameters
		Packets.packetList.get(e.queueID).get(e.packetID).waitingTime = e.timeStamp - Packets.packetList.get(e.queueID).get(e.packetID).arrQ;
		Packets.packetList.get(e.queueID).get(e.packetID).serviceTime = servTime;
		Packets.packetList.get(e.queueID).get(e.packetID).dprtS = e.timeStamp+totalServTime;
		Server.busyTime += servTime;
		eventList.add(new Event(e.timeStamp+totalServTime, e.packetID, e.queueID, "departServer"));	// add depart event for the arrived packet
	}
	
	public static void departServer(Event e)	{
		Server.lastDepart = e.timeStamp;
		Server.numDepart++;
		pckQ--;		
		
		/***************** Schedule NEXT ARRIVAL event *******************/
		
		int selectedQ, qTraverse;
		//Check pckQ to decide which Q to select th enxt packet from
		if(pckQ > 0)	selectedQ = e.queueID;
		else		selectedQ = (e.queueID+1) % Server.numQueue;
		
		/* qTraverse : counter to ensure all the queue are traversed to find the recent-most packet
		 * winner : Event that is selected as the next event to schedule
		 */
		qTraverse = Server.numQueue;	
		Event winner = null, recent = null;		
		PriorityQueue<Event> pQ = Queues.queueList.get(selectedQ).eventQueue;
		while(qTraverse-- > 0)	{
			if(pQ.isEmpty())	{	// if Queue is empty move to next Q
				selectedQ =  (selectedQ+1)% Server.numQueue;
				pQ = Queues.queueList.get(selectedQ).eventQueue;
				continue;
			}
			if(pQ.peek().timeStamp <= e.timeStamp)	{	// If packet exist in Q take it
				winner = pQ.poll();
				break;
			}
			
			recent = (recent == null || pQ.peek().timeStamp < recent.timeStamp) ? pQ.peek() : recent; 
			selectedQ =  (selectedQ+1)% Server.numQueue;
			pQ = Queues.queueList.get(selectedQ).eventQueue;
		}
		if(winner == null)	{	//If no packet before event select the recent-most packet after now time  
			if(recent == null) return;
			winner = Queues.queueList.get(recent.queueID).eventQueue.poll();
		}
		if(winner.queueID != q2bServed)	{	// If a different Queue is selected, update q2bserved and pckQ 
			q2bServed = winner.queueID;
			pckQ = Queues.priority[q2bServed];
		}
		eventList.add(new Event(winner.timeStamp<e.timeStamp?e.timeStamp:winner.timeStamp, winner.packetID, winner.queueID, "arriveServer"));
	}
	
	/********************** return an random value from an exponential distribution with rate = rand ************************/ 
	public static double getExponential(double rand)	{
		return  Math.log(1-uniform())/(-rand);
	}
	
	public static int getGeometric(double prob)	{
		return (int) Math.ceil(Math.log(uniform()) / Math.log(1.0 - prob));
	}
	
	public static double uniform()	{
		return new Random().nextDouble();
	}
	
	public static double uniform(int max, int min)	{
		return min + (max - min) * uniform();
	}
	
	public static double getServiceTimeGeneral(int queueID)	{
		int N = getGeometric(1-Queues.errorProb[queueID]);
		double sum=0;
		for (int i = 0; i < N; i++) {
			sum += getExponential(Server.avgTxTime)+getExponential(Server.avgRxTime);
		}
		return sum;
	}
}
