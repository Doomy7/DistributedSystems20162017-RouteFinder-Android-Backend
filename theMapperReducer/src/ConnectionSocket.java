import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ConnectionSocket {

	private ObjectOutputStream output;
	private ObjectInputStream input;
	private boolean isOnline;
	private int doulosID;
	private Socket connection;
	
	
	ConnectionSocket(Socket connection, ObjectInputStream input, int doulosID) throws IOException{
		this.connection = connection;
		this.doulosID = doulosID;
		this.output = new ObjectOutputStream(connection.getOutputStream());
		this.input = input;
		this.isOnline = true;
		
	}

	Socket getConnection(){
		return connection;
	}
	
	void setConnection(Socket newConnection) {
		this.connection = newConnection;
	}
	
	ObjectOutputStream getOutput() {
		return output;
	}
	
	void setOutput(ObjectOutputStream newOutput) {
		this.output = newOutput;
	}
	
	ObjectInputStream getInput() {
		return input;
	}
	
	void setInput(ObjectInputStream newInput) {
		this.input = newInput;
	}
	
	boolean getIsOnline() {
		return isOnline;
	}
	
	void setAlive(boolean isAlive) {
		this.isOnline = isAlive;
	}
	
	int getID() {
		return doulosID;
	}
	
}
