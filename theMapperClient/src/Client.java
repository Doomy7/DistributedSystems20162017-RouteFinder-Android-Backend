import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;

public class Client extends Thread /****/ {
	 double latStart, lonStart, latDest, lonDest;
	 int id;
	 Client(double latStart, double lonStart, double latDest, double lonDest) {
		  this.latStart = latStart;
		  this.lonStart = lonStart;
		  this.latDest = latDest;
		  this.lonDest = lonDest;
		 }
	 
	 @SuppressWarnings("unchecked")
	 public void run() {
	  Socket requestSocket = null;
	  ObjectOutputStream out = null;
	  ObjectInputStream in = null;
	  try {
		    requestSocket = new Socket("127.0.0.1",7000);
		    out = new ObjectOutputStream(requestSocket.getOutputStream());
		    in = new ObjectInputStream(requestSocket.getInputStream());
		    
		    String message = "HiIAmPelatis";
		    System.out.println(message);
		    out.writeObject(message);
		    out.flush();
		    
		    double[] coor = {latStart, lonStart, latDest, lonDest};
		    out.writeObject(coor);
		    out.flush();
		   
		    String notification = (String) in.readObject();
		    
		    if(notification.equals("Answer")) {
			    List<List<HashMap<String,String>>> routes = (List<List<HashMap<String, String>>>) in.readObject();
			    if(routes == null){
			    	System.out.println("null");
			    }else{
			    	 for(int i = 0; i<routes.get(0).size();i++){
					    	System.out.println(routes.get(0).get(i));
					   }
			    	System.out.println("found path");
			    }
		    }else if(notification.equals("Error")) {
		    	System.out.println(in.readObject().toString());
		    }
		 
	  } catch (UnknownHostException unknownHost) {
		  System.err.println("You are trying to connect to an unknown host!");
	  }catch(SocketException e){
		    System.out.println("SocketException " + e);
	  }catch (IOException ioException) {
		  ioException.printStackTrace();
	  } catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}finally {
		  try {
			  in.close(); out.close();
			  requestSocket.close();
		  } catch (IOException ioException) {
			  ioException.printStackTrace();
		  }
	  }
	 }
 
	 public static void main(String args[]) {
		  new Client(38.6113747, 23.2217367, 38.6317411,23.1567787).start();
		 // new Client(15.6894875, 139.6917064, 43.0620958, 141.3543763).start();
		  //new Client(35.6894432, 139.69170234, 43.062095238, 141.35463).start();  
		 // new Client(38.6894875, 139.6917064, 43.0620958, 141.3543763).start(); 
		 // new Client(-22.9068467, -43.1728965, -35.675147, -71.542969).start();
		 // new Client(35.6894875, 139.6917064, 43.0620958, 141.3543763).start();
		//  new Client(38.6113747, 23.2217367, 38.6317411,23.1567787).start();
		//  new Client(38.5839424,23.2059071,38.5638012,23.2150451).start();


	 }
 }