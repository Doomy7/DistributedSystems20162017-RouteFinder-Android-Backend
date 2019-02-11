import java.io.*;
import java.net.*;
import java.util.ArrayList;


//API KEY =  AIzaSyB0BInXSLyydj0VuO2QvN8aQmHZLh3GEf4 
public class Master extends Thread{
	
	public static void main(String args[]) throws IOException, ClassNotFoundException, InterruptedException{
		 new Master().startMaster();
	} //main
	
	private static ServerSocket masterSocket;
	private Socket connection = null;
	static ArrayList<ConnectionSocket> masterConnections = new ArrayList<ConnectionSocket>();
	static ArrayList<Thread> threadList = new ArrayList<Thread>();
	static ObjectOutputStream reducerOutput;
	static ObjectInputStream reducerInput;
	private ObjectInputStream inTemp;
	private ObjectOutputStream outTemp;
	static boolean systemOffline = false;
	public void startMaster() throws IOException, ClassNotFoundException, InterruptedException{
		try{
			initialize();		
			waitForWorkers();
			
			
			while( true ){
				
						System.out.println( "\nMaster is waiting for clients"
								+ "\n##############################" );
						
						connection = masterSocket.accept();
						System.out.println( "Master informs: Something connected"
								+ "\n##############################" );
						
						outTemp = new ObjectOutputStream(connection.getOutputStream());
						inTemp = new ObjectInputStream(connection.getInputStream());
						
						String message = (String) inTemp.readObject();
						
						if(message.equals("HiIAmPelatis")) {
							
								System.out.println( "Master informs: New Client connected"
										+ "\n##############################" );
								Thread t = new setUpStreams( connection, outTemp , inTemp );
								t.start();
							
							
						}else if(message.equals("WorkerIsReady")) {
							
							System.out.println( "Master informs: Resurrected worker connected"
									+ "\n##############################" );
							int workID = (int) inTemp.readObject();
							masterConnections.get(workID).setAlive(true);
							masterConnections.get(workID).setConnection(connection);
							masterConnections.get(workID).setInput(inTemp);
							masterConnections.get(workID).setOutput(outTemp);
							masterConnections.get(workID).getOutput().writeObject("WorkerAccepted");
							masterConnections.get(workID).getOutput().flush();
							System.out.println("Worker with id:" + workID + " Online Status: " + masterConnections.get(workID).getIsOnline());
							reducerOutput.writeObject("WorkerIsComing");
							if(systemOffline = true) {
								systemOffline = false;
							}
						}
						
			}
			
			
		}catch( EOFException eofException ){
			System.out.println( "SYSTEM: Server ended connection" );
		}finally{
			try{
				masterSocket.close();
				System.out.println( "SYSTEM: Closing Main Server Connection\n" );
			}catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	} //startMaster			
			
	private void initialize() throws IOException, ClassNotFoundException {
		masterSocket = new ServerSocket(7000,200);
		System.out.println( "Master is up\n##############################" );
	} //initialize
	
	private void waitForWorkers() throws IOException, ClassNotFoundException {
		System.out.println( "Master is waiting for workers\n##############################" );
		try{
			int i = 0;
			while( i < 3 ) {
				connection = masterSocket.accept();	
				ObjectInputStream validate = new ObjectInputStream(connection.getInputStream());
				String ping = (String) validate.readObject();
				if( ping.equals( "WorkerIsReady" ) && masterConnections.size() < 3 )
				{
					int id = (Integer) validate.readObject();
					ConnectionSocket worker = new ConnectionSocket(connection, validate, id);
					masterConnections.add(worker);
					masterConnections.get(i).getOutput().writeObject("WorkerAccepted");
					masterConnections.get(i).getOutput().flush();
					i++;
				}
				else if( ping.equals( "ReducerIsReady" ) )
				{
					reducerOutput = new ObjectOutputStream(connection.getOutputStream());
					reducerInput = validate;
				  	reducerOutput.writeObject("ReducerAccepted");
					reducerOutput.flush();
				}
				else 
				{
					System.out.println("Someone wants unauthorized access. Blocked");	
				}
			}	
				
		}catch(EOFException eofException){
			System.out.println( "SYSTEM: Server ended connection\n" );
		}
	} //waitForWorkers
} //Master
	
