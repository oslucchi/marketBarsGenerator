package it.l_soft.barsGenerator.comms;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

class ClientHandler extends Thread {
	final Logger log = Logger.getLogger(this.getClass());
    private Socket socket;
    private ObjectOutputStream objectOutput;
    
    public ClientHandler(Socket socket) 
    {
        this.socket = socket;
    }

    public void sendMessage(String topic, Object message) throws IOException
    {
    	objectOutput.writeObject(new Message(topic, message));
        objectOutput.flush();
    }
    
    public void closeConnection()
    {
    	try {
			socket.close();
		} 
    	catch (IOException e) {
    		log.trace("Error on socket close", e);
		}
    }
    
    public void run() {
        try {
			OutputStream output = socket.getOutputStream(); 
			objectOutput = new ObjectOutputStream(output);

            while (true) {
                Thread.sleep(1000);
            }
        } 
        catch (IOException | InterruptedException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}