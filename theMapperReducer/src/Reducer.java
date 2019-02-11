import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class Reducer extends Thread{

	public static void main(String args[]) throws IOException, ClassNotFoundException {
		new Reducer().startRunning();
	}
	
	private ServerSocket reducerSocket;
	private Socket reducerConnection;
	
	private Socket masterConnection;
	private ObjectInputStream masterInput,inTemp;
	private ObjectOutputStream masterOutput,outTemp;
	
	
	private static ArrayList<ConnectionSocket> reducerConnections = new ArrayList<ConnectionSocket>();
	private ReducerObject<double[], GenericQueueImpl<Object>> results;
	private GenericQueueImpl<Object> dataFromWorkers = new GenericQueueImpl<>();
	private ArrayList<Object> data = new ArrayList(3);
	private double[] key;
	public void startRunning() throws IOException, ClassNotFoundException{
		initialize();
		connectToMaster();
		waitForWorkers();
		try{
			while(true){
				System.out.println("Reducer is waiting...\n##############################");
				
				String message = (String) masterInput.readObject();
				if(message.equals("WorkerIsComing")) {
					reducerConnection = reducerSocket.accept();
					inTemp = new ObjectInputStream(reducerConnection.getInputStream());
					outTemp = new ObjectOutputStream(reducerConnection.getOutputStream());
					String ping = (String) inTemp.readObject();
					if( ping.equals( "WorkerIsReady" ) ){
						int workID = (int) inTemp.readObject();
						reducerConnections.get(workID).setAlive(true);
						reducerConnections.get(workID).setConnection(reducerConnection);
						reducerConnections.get(workID).setInput(inTemp);
						reducerConnections.get(workID).setOutput(outTemp);
						reducerConnections.get(workID).getOutput().writeObject("WorkerAccepted");
						reducerConnections.get(workID).getOutput().flush();
						System.out.println("Worker with id:" + workID + " Online Status: " + reducerConnections.get(workID).getIsOnline());
					}				
				}else if(message.equals("waitForWorkers")) {
					try{					
						waitForTasksThread();
					}catch(EOFException eofException){
						System.out.println("\nServer ended connection\n");
					}
				}
			}
		}catch(IOException ioException){
			ioException.printStackTrace();
		}finally{
			try{
				reducerSocket.close();
				System.out.println("Closing Main Server Connection\n");
			}catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}
	
	/**
	 * initializing Worker
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	private void initialize() throws IOException, ClassNotFoundException {
		reducerSocket = new ServerSocket(8003,200);
		System.out.print("Reducer is up\n##############################");
	}
	
	

	/**
	 * attempting connection to master
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	private void connectToMaster() throws IOException, ClassNotFoundException{
		masterConnection = new Socket("127.0.0.1",7000);
		masterOutput = new ObjectOutputStream(masterConnection.getOutputStream());
		masterOutput.writeObject("ReducerIsReady");
		masterOutput.flush();
		masterInput = new ObjectInputStream(masterConnection.getInputStream());
		String ack = (String) masterInput.readObject();
		if(ack.equals( "ReducerAccepted")){
			System.out.println("\nReducer connected with Master\n###############################");
		}
	}


	private void waitForWorkers() throws IOException, ClassNotFoundException {
		System.out.println("Reducer is waiting for workers\n##############################");
		try{
			int i = 0;
			while(reducerConnections.size() < 3){
					reducerConnection = reducerSocket.accept();
					ObjectInputStream validate = new ObjectInputStream(reducerConnection.getInputStream());
					String ping = (String) validate.readObject();
					if( ping.equals( "WorkerIsReady" ) ){
						int id = (int) validate.readObject();
						ConnectionSocket unknown = new ConnectionSocket(reducerConnection, validate, id);
						reducerConnections.add(unknown);
						reducerConnections.get(i).getOutput().writeObject("WorkerAccepted");
						reducerConnections.get(i).getOutput().flush();
						i++;
					}
			}		
		}catch(EOFException eofException){
			System.out.println("SYSTEM: Server ended connection\n");
		}
	}
	
	/**
	 * main worker task
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	private void waitForTasksThread() throws IOException, ClassNotFoundException {
		
	
		try {		
			double[] keyFromworker0 = (double[])reducerConnections.get(0).getInput().readObject();
			List<List<HashMap<String, String>>> work0data = (List<List<HashMap<String, String>>>)reducerConnections.get(0).getInput().readObject();
			
			dataFromWorkers.put(work0data);
			data.add(0,work0data);
			if(key == null) {
				key = keyFromworker0;		
			}				
		}catch(Exception e) {
			dataFromWorkers.put(null);
			data.add(0,null);
			reducerConnections.get(0).setAlive(false);
		}
						
		try {	
			double[] keyFromworker1 = (double[])reducerConnections.get(1).getInput().readObject();
			Object work1data = (List<List<HashMap<String, String>>>)reducerConnections.get(1).getInput().readObject();
			dataFromWorkers.put(work1data);
				data.add(1,work1data);
			if(key == null) {
				key = keyFromworker1;		
			}
		}catch(Exception e) {
			dataFromWorkers.put(null);
			data.add(1,null);
			reducerConnections.get(1).setAlive(false);
		}	
					
		try {
			double[] keyFromworker2 = (double[])reducerConnections.get(2).getInput().readObject();
			Object work2data = (List<List<HashMap<String, String>>>)reducerConnections.get(2).getInput().readObject();
			dataFromWorkers.put(work2data);
			data.add(2,work2data);
			if(key == null) {
				key = keyFromworker2;		
			}
		}catch(Exception e) {
			dataFromWorkers.put(null);
			data.add(2,null);
			reducerConnections.get(2).setAlive(false);
		}
	
		boolean ok = false;
		String command;
		while(!ok)
		{
			try{
				command = (String)masterInput.readObject();
				System.out.println(command);
				if(command.equals("ack")){
					System.out.println( "Reducer starts organising results from workers\n##############################" );
					
					results = new ReducerObject<double[], GenericQueueImpl<Object>>(key,dataFromWorkers);
					
					/**
					 * make changes here
					 */
					//masterOutput.writeUnshared(results);
					//masterOutput.writeObject(results);
					//masterOutput.flush(); 
					/**
					 * changes end here
					 */
				
					masterOutput.writeObject(data.get(0));
					masterOutput.flush();
					masterOutput.writeObject(data.get(1));
					masterOutput.flush();
					masterOutput.writeObject(data.get(2));
					masterOutput.flush();
					
					ok = true;
					System.out.println( "Reducer finished and sent results to master\n##############################" );
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}	
}
