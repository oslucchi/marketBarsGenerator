package it.l_soft.barsGenerator.comms;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
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
    	byte[] byteArray;
    	log.trace("sendMessage started");
    	
    	byteArray = message.getTopic().getBytes();
        output.write(byteArray);
        log.debug("Message: topic " + message.getTopic() + 
        				   " (byteArray length " + byteArray.length + ")");
        
        byteArray = ByteBuffer.allocate(Long.BYTES)
	                .order(java.nio.ByteOrder.BIG_ENDIAN)
	                .putLong(message.getTimestamp())
	                .array();
        output.write(byteArray);
        log.debug("Message: timestamp " + ByteBuffer.wrap(byteArray).getLong());

        byte[] json = JSONWrapper.MAPPER.writeValueAsBytes(message); // UTF-8 by default
        byteArray = ByteBuffer.allocate(4).putInt(json.length).array(); // big-endian
        log.debug("Message: sending json '" + 
					new String(json, 0, json.length, StandardCharsets.UTF_8) + "' " +
					"for length " + ByteBuffer.wrap(byteArray).getInt());
        output.write(byteArray);
        output.write(json);
        output.flush();
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