import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class setUpStreams extends Thread{
	
	ObjectOutputStream out;
	ObjectInputStream in;
	Socket connection = null;
	ArrayList<String> results;
	boolean dataNotFound;
	
	ArrayList<List<List<HashMap<String,String>>>> routesUnpacked;
	ReducerObject<double[],GenericQueueImpl<Object>> dataFromReducer;
	List<List<HashMap<String, String>>> entry0;
	List<List<HashMap<String, String>>> entry1;
	List<List<HashMap<String, String>>> entry2;
	@SuppressWarnings("unchecked")
	setUpStreams(Socket connection, ObjectOutputStream output, ObjectInputStream input) throws IOException, ClassNotFoundException, InterruptedException{
		
		synchronized(MasterClassFile.masterCache)
		{	
			synchronized(Master.masterConnections)
			{	
				try 
				{
					out = output; //server => client
					in = input; //client => server
					System.out.println( "Master informs: Streams are online"
									+ "\n##############################" );
					List<List<HashMap<String,String>>> route;
					double[] clientCoor = (double[]) in.readObject();
					double[] clientFloorCoor = new double[4];
					
					for( int i = 0; i < clientCoor.length; i++ )
					{
						clientFloorCoor[i] = Math.floor( clientCoor[i] * 100 ) / 100;
					}
				    
					route = MasterClassFile.masterCache.lookUp( clientFloorCoor );
				    if ( route != null )
				    {
				    	System.out.print( "Master informs: Found path in cache"
									+ "\n##############################" );
				    	sendResultsToClient( route );
				    }
				    else
				    {
				    	System.out.print( "Master informs: No path found in cache. Distributed to Workers"
								+ "\n##############################\n" );
				    	distributeToWorkers(clientFloorCoor);
				    	if(!Master.systemOffline) {
					    	waitForMappers();
				    	}
				    	if(!Master.systemOffline) {
					    	ackToReducers();
				    	}
				    	if(!Master.systemOffline) {
					    	collectDataFromReducers();
					    	routeProcessor(clientCoor, clientFloorCoor);	
				    	}
			    }
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException cnfe) {
					cnfe.printStackTrace();
				}finally{
					out.close();
					in.close();
					connection.close();
				}
			}
		}
	}
	
	int hash(double[] d) {
		 int hashValue = Math.abs( (int)(d[0]+d[1]+d[2]+d[3]) % 3);
	     return hashValue; 
	 }	
	
	private void distributeToWorkers(double[] clientFloorCoor) throws IOException{
		
		/** STELNOYME STON WORKER 0*/
		int dead = 0;
		try {
			Master.masterConnections.get(0).getOutput().writeObject("StartCache");
			Master.masterConnections.get(0).getOutput().flush();
			Master.masterConnections.get(0).getOutput().writeObject(clientFloorCoor);
			Master.masterConnections.get(0).getOutput().flush();
			Master.masterConnections.get(0).setAlive(true);
		}catch (Exception e) {
			Master.masterConnections.get(0).setAlive(false);
			dead++;
			System.out.println("Master informs: Worker with id 0 is dead" + "\n##############################");
		}
		
		/** STELNOYME STON WORKER 1*/
		try {
			Master.masterConnections.get(1).getOutput().writeObject("StartCache");
			Master.masterConnections.get(1).getOutput().flush();
			Master.masterConnections.get(1).getOutput().writeObject(clientFloorCoor);
			Master.masterConnections.get(1).getOutput().flush();
			Master.masterConnections.get(1).setAlive(true);
		}catch (Exception e) {
			Master.masterConnections.get(1).setAlive(false);
			dead++;
			System.out.println("Master informs: Worker with id 1 is dead" + "\n##############################");
		}
		
		/** STELNOYME STON WORKER 2*/
		try {		
			Master.masterConnections.get(2).getOutput().writeObject("StartCache");
			Master.masterConnections.get(2).getOutput().flush();
			Master.masterConnections.get(2).getOutput().writeObject(clientFloorCoor);
			Master.masterConnections.get(2).getOutput().flush();
			Master.masterConnections.get(2).setAlive(true);
		}catch (Exception e) {
			Master.masterConnections.get(2).setAlive(false);
			dead++;
			System.out.println("Master informs: Worker with id 2 is dead" + "\n##############################\n");
		}
	
		if (dead == 3) {
			System.err.println("All workers KIA...\n Shuting down main system...\n");
			out.writeObject("Workers KIA. System offline state : TRUE");
			out.flush();
			out.close();
			in.close();
			Master.systemOffline = true;
		}
	}
	
	private void waitForMappers() throws IOException{
		Master.reducerOutput.writeObject("waitForWorkers");
		Master.reducerOutput.flush();	
		boolean done = false, dw0 = false, dw1 = false, dw2 = false;
		int dead = 0;
		String answer;
		
			try {
				if(!dw0){
					answer = Master.masterConnections.get(0).getInput().readObject().toString();
					if(answer.equals("Worker0SearchedCache")){
						dw0 = true;
					}
					Master.masterConnections.get(0).setAlive(true);
				}
			}catch(Exception e) {
				dw0 = true;
				dead++;
				if(Master.masterConnections.get(0).getIsOnline()) {
					Master.masterConnections.get(0).setAlive(false);					
					System.out.println("Master informs: Worker with id 0 is dead" + "\n##############################");
				}
			}
			
			try {
				if(!dw1){
					answer = Master.masterConnections.get(1).getInput().readObject().toString();
					if ( answer.equals ( "Worker1SearchedCache" ) ){
						dw1 = true;
					}
					Master.masterConnections.get(1).setAlive(true);
				}
			}catch(Exception e) {
				dw1 = true;			
				dead++;
				if(Master.masterConnections.get(1).getIsOnline()) {
					Master.masterConnections.get(1).setAlive(false);
					System.out.println("Master informs: Worker with id 1 is dead" + "\n##############################");
				}
			}
			
			try {
				if(!dw2){
					answer = Master.masterConnections.get(2).getInput().readObject().toString();
					if( answer.equals ( "Worker2SearchedCache" ) ){
						dw2 = true;
					}
					Master.masterConnections.get(2).setAlive(true);
				}
			}catch(Exception e) {
				dw2 = true;
				dead++;
				if(Master.masterConnections.get(2).getIsOnline()) {
					Master.masterConnections.get(2).setAlive(false);			
					System.out.println("Master informs: Worker with id 2 is dead" + "\n##############################");
				}	
			}
			
			if (dead == 3) {
				System.err.println("All workers KIA...\n Shuting down main system...\n");
				out.writeObject("Workers KIA. System offline state : TRUE");
				out.flush();			
				Master.systemOffline = true;
			}
	}
		
	private void ackToReducers() throws IOException{
		System.out.println( "\nMaster informs: Workers finished searching cache. Ack to reducer will be sent"
							+ "\n##############################" );
		try {
			Master.reducerOutput.writeObject("ack");
			Master.reducerOutput.flush();
		}catch(Exception e) {
			System.err.println("Reducer KIA");
			System.exit(0);
		}
	}
	@SuppressWarnings("unchecked")
	private void collectDataFromReducers() throws ClassNotFoundException, IOException{
		
		/** * changes start here */
		//dataFromReducer = (ReducerObject<double[], GenericQueueImpl<Object> >) Master.reducerInput.readObject();		
		//dataFromReducer.getValue().printQueue(System.out);	
		/** * changes end here */
		
		try {
			entry0 = (List<List<HashMap<String, String>>>) Master.reducerInput.readObject();
			entry1 = (List<List<HashMap<String, String>>>) Master.reducerInput.readObject();
			entry2 = (List<List<HashMap<String, String>>>) Master.reducerInput.readObject();
		}catch (Exception e) {
			System.err.println("Reducer KIA");
			System.exit(0);
		}
		
		System.out.println("Master informs: Read results from Reducer"
				+ "\n##############################");
	}
	
	private void updateCache(double[] key, List<List<HashMap<String,String>>> value){
		MasterClassFile.masterCache.store(key, value);
		System.out.println( "\nMaster informs: Updated Cache"
				+ "\n##############################" );
	}
	
	private void sendResultsToClient(List<List<HashMap<String,String>>> route) throws IOException{
	

			out.writeObject(route);
			out.flush();
	    	System.out.println( "\nMaster informs: Route sent to client"
						+ "\n##############################" );   	
	
	}
	
	private void routeProcessor(double[] clientCoor, double[] clientFloorCoor) {
		try{				    		
    		if(!(entry0 == null && entry1 == null && entry2 == null))
    		{
    			double[] distance = {1000,1000};
	    		List<List<HashMap<String, String>>> nearestRoute = null;
	    		if(entry0 != null){
	 
	    			double seat0LatStart = Double.parseDouble(entry0.get(0).get(0).get("lat"));
	    			double seat0LonStart = Double.parseDouble(entry0.get(0).get(0).get("lng"));
	    			double seat0LatDest = Double.parseDouble(entry0.get(0).get(entry0.get(0).size()-1).get("lat"));
	    			double seat0LonDest = Double.parseDouble(entry0.get(0).get(entry0.get(0).size()-1).get("lng"));
	    			
	    			double firstComp0 = Directions.distanceTo(clientCoor[0], clientCoor[1], seat0LatStart, seat0LonStart);
	    			double secondComp0 = Directions.distanceTo(clientCoor[2], clientCoor[3], seat0LatDest, seat0LonDest);
	    			if(firstComp0 <= distance[0]){
	    				if(secondComp0 <= distance[1]){
	    					distance[0] = firstComp0;
	    					distance[1] = secondComp0;
	    					nearestRoute = entry0;
	    				}
	    			}
	    			
	    		}
	    		if(entry1 != null){
	 
	    			double seat1LatStart = Double.parseDouble(entry1.get(0).get(0).get("lat"));
	    			double seat1LonStart = Double.parseDouble(entry1.get(0).get(0).get("lng"));
	    			double seat1LatDest = Double.parseDouble(entry1.get(0).get(entry1.get(0).size()-1).get("lat"));
	    			double seat1LonDest = Double.parseDouble(entry1.get(0).get(entry1.get(0).size()-1).get("lng"));
	    			
	    			double firstComp1 = Directions.distanceTo(clientCoor[0], clientCoor[1], seat1LatStart, seat1LonStart);
	    			double secondComp1 = Directions.distanceTo(clientCoor[2], clientCoor[3], seat1LatDest, seat1LonDest);
	    			if(firstComp1 <= distance[0]){
	    				if(secondComp1 <= distance[1]){
	    					distance[0] = firstComp1;
	    					distance[1] = secondComp1;
	    					nearestRoute = entry1;
	    				}
	    			}
	    		}
	    		if(entry2 != null){
	    			
	    			double seat2LatStart = Double.parseDouble(entry2.get(0).get(0).get("lat"));
	    			double seat2LonStart = Double.parseDouble(entry2.get(0).get(0).get("lng"));
	    			double seat2LatDest = Double.parseDouble(entry2.get(0).get(entry2.get(0).size()-1).get("lat"));
	    			double seat2LonDest = Double.parseDouble(entry2.get(0).get(entry2.get(0).size()-1).get("lng"));
	    			

	    			double firstComp2 = Directions.distanceTo(clientCoor[0], clientCoor[1], seat2LatStart, seat2LonStart);
	    			double secondComp2 = Directions.distanceTo(clientCoor[2], clientCoor[3], seat2LatDest, seat2LonDest);
	    			if(firstComp2 <= distance[0]){
	    				if(secondComp2 <= distance[1]){
	    					distance[0] = firstComp2;
	    					distance[1] = secondComp2;
	    					nearestRoute = entry2;
	    				}
	    			}
	    		}
	    		sendResultsToClient(nearestRoute);
	    		updateCache(clientFloorCoor, nearestRoute);
    		}
    		else
    		{
    		
    		int hash = hash(clientFloorCoor);
    		boolean jobIsDone = false;
    		if(Master.masterConnections.get(hash).getIsOnline()) {			    			
    			try {
		    		System.out.println( "Master informs: No path found in workers cache. Distribute to  worker: "
							+ hash + "\n##############################" );
		    		Master.masterConnections.get(hash).getOutput().writeObject("API");
			    	Master.masterConnections.get(hash).getOutput().flush();
			    	Master.masterConnections.get(hash).getOutput().writeObject(clientFloorCoor);
			    	Master.masterConnections.get(hash).getOutput().flush();
			    	Master.masterConnections.get(hash).getOutput().writeObject(clientCoor);
			    	Master.masterConnections.get(hash).getOutput().flush();
			    	if(Master.masterConnections.get(hash).getInput().readObject().equals("doneSearchAPI")){
				    	List<List<HashMap<String,String>>> routeworker = (List<List<HashMap<String, String>>>) Master.masterConnections.get(hash).getInput().readObject();
				    	updateCache(clientFloorCoor,routeworker);
				    	sendResultsToClient(routeworker);
			    	}
			    	Master.masterConnections.get(hash).setAlive(true);
			    	jobIsDone = true;
    			}catch(Exception e) {
    				Master.masterConnections.get(hash).setAlive(false);
    			}
    		}
    		while(!jobIsDone) {
    			for(ConnectionSocket doyloi : Master.masterConnections) {
    					try {
				    		System.out.println( "Master informs: No path found in workers cache. Distribute to worker: "
									+ doyloi.getID() + "\n##############################" );
				    		doyloi.getOutput().writeObject("API");
				    		doyloi.getOutput().flush();
				    		doyloi.getOutput().writeObject(clientFloorCoor);
				    		doyloi.getOutput().flush();
				    		doyloi.getOutput().writeObject(clientCoor);
				    		doyloi.getOutput().flush();
					    	if(doyloi.getInput().readObject().equals("doneSearchAPI")){
						    	List<List<HashMap<String,String>>> routeworker = (List<List<HashMap<String, String>>>) doyloi.getInput().readObject();
						    	updateCache(clientFloorCoor,routeworker);
						    	sendResultsToClient(routeworker);
					    	}
					    	doyloi.setAlive(true);
					    	jobIsDone = true;
					    	break;
		    			}catch(Exception e) {
		    				doyloi.setAlive(false);
		    			}
    				
    				
    			}
    		}
    		if(!jobIsDone) {
    			System.err.println("All workers KIA...\n Shuting down main system...\n");
    			out.writeObject("Workers KIA. System offline state : TRUE");
				out.flush();
    			out.close();
				in.close();
				Master.systemOffline = true;
    		}
    	}
    	}catch (Exception e){
			e.printStackTrace();
    	}
	}
}

