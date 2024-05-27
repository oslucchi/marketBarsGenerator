package it.l_soft.barsGenerator.comms;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import it.l_soft.barsGenerator.ApplicationProperties;
import it.l_soft.barsGenerator.MarketBar;

public class Publisher extends Thread {
	final Logger log = Logger.getLogger(this.getClass());
	ApplicationProperties props;
	ArrayList<ClientHandler> clientList = new ArrayList<ClientHandler>();

	private void shutdownClient(ClientHandler client, Exception e)
	{
		log.error("Error sending a bar", e);
		log.trace("Closing client socket and removing it from active list");
		client.closeConnection();
		clientList.remove(client);
	}

	public void sendBar(MarketBar mb)
	{
		MarketBarMessage msg = new MarketBarMessage(mb.getTimestamp(), mb.getOpen(), mb.getHigh(), 
													mb.getLow(), mb.getClose(), mb.getVolume());
		for(ClientHandler client : clientList)
		{
			try {
				client.sendMessage("B", msg);
			} 
			catch (IOException e) {
				shutdownClient(client, e);
			}
		}
	}
	
	public void sendTrade()
	{
		TradeMessage msg = new TradeMessage("A", 0, 0);
		for(ClientHandler client : clientList)
		{
			try {
				client.sendMessage("T", msg);
			} 
			catch (IOException e) {
				shutdownClient(client, e);
			}
		}

	}
	
    public void run() {
        int port = props.getPort();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);
            while (true) {
            	ClientHandler client = new ClientHandler(serverSocket.accept());
                clientList.add(client);
                client.start();
            }
        } 
        catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
            log.error("Server exception", ex);
    		for(ClientHandler client : clientList)
    		{
    			shutdownClient(client, ex);
    		}
        }
    }
}