package it.l_soft.barsGenerator.comms;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.apache.log4j.Logger;
import it.l_soft.barsGenerator.utils.JSONWrapper;

class ClientHandler extends Thread {
	final Logger log = Logger.getLogger(this.getClass());
    private Socket socket;
    private OutputStream output;
    
    public ClientHandler(Socket socket) 
    {
        this.socket = socket;
    }

    public void sendMessage(Object msgObject) throws IOException
    {
    	Message message = (Message) msgObject;
    	log.trace("sendMessage started");

        byte[] json = JSONWrapper.MAPPER.writeValueAsBytes(message);
        String jsonStr = new String(json, StandardCharsets.UTF_8);
        String header = String.format("%05d", json.length);
        output.write(header.getBytes(StandardCharsets.UTF_8));
        output.write(json);
        output.flush();

        log.debug("Message sent: topic=" + message.getTopic() +
                  " len=" + json.length + " json=" + jsonStr);
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
			output = socket.getOutputStream(); 

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