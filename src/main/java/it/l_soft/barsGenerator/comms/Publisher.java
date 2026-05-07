package it.l_soft.barsGenerator.comms;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;

import it.l_soft.barsGenerator.ApplicationProperties;
import it.l_soft.barsGenerator.Bar;

public class Publisher extends Thread {
	final Logger log = Logger.getLogger(this.getClass());
	ApplicationProperties props;
	public ArrayList<ClientHandler> clientList = new ArrayList<ClientHandler>();
	
	private void shutdownClient(ClientHandler client, Exception e)
	{
		if (e != null)
		{
			log.error("Error sending a bar", e);
		}
		else
		{
			log.error("Error sending a bar");
		}
		log.trace("Closing client socket and removing it from active list");
		client.closeConnection();
	}

	public void sendBar(Bar mb)
	{
		MarketBar msg = new MarketBar(mb.getTimestamp(), mb.getOpen(), mb.getHigh(), 
													mb.getLow(), mb.getClose(), mb.getVolume());
		
	    for (Iterator<ClientHandler> it = clientList.iterator(); it.hasNext(); )
	    {
	    	ClientHandler client = it.next();
			try {
				client.sendMessage(msg);
			} 
			catch (IOException e) {
				System.out.println("Exception " + e.getMessage() + " on sendMessage");
				System.out.println("Client " + client.getName() + " will be shutdown");
				shutdownClient(client, e);
				System.out.println("The current ClientHandler is removed from the list");
				it.remove();
			}
		}
	}

	public void sendTickBar(Bar mb)
	{
		MarketBar msg = new MarketBar(mb.getTimestamp(), mb.getOpen(), mb.getHigh(),
													mb.getLow(), mb.getClose(), mb.getVolume());
		msg.setTopic("T");
	    for (Iterator<ClientHandler> it = clientList.iterator(); it.hasNext(); )
	    {
	    	ClientHandler client = it.next();
			try {
				client.sendMessage(msg);
			} 
			catch (IOException e) {
				System.out.println("Exception " + e.getMessage() + " on sendTickBar");
				System.out.println("Client " + client.getName() + " will be shutdown");
				shutdownClient(client, e);
				System.out.println("The current ClientHandler is removed from the list");
				it.remove();
			}
		}
	}

	public void sendMessageObject(Message msg)
	{
	    for (Iterator<ClientHandler> it = clientList.iterator(); it.hasNext(); )
	    {
	    	ClientHandler client = it.next();
			try {
				client.sendMessage(msg);
			} 
			catch (IOException e) {
				System.out.println("Exception " + e.getMessage() + " on sendMessageObject");
				System.out.println("Client " + client.getName() + " will be shutdown");
				shutdownClient(client, e);
				System.out.println("The current ClientHandler is removed from the list");
				it.remove();
			}
		}
	}

	public void sendTrade()
	{
		TradeMessage msg = new TradeMessage(new Date().getTime(), "A", 0, 0);
	    for (Iterator<ClientHandler> it = clientList.iterator(); it.hasNext(); )
	    {
	    	ClientHandler client = it.next();
			try {
				client.sendMessage(msg);
			} 
			catch (IOException e) {
				shutdownClient(client, e);
				it.remove();
			}
		}
		try {
			Thread.sleep(1000);
		}
		catch(Exception e)
		{
			;
		}

	}
	

	public void sendHeader(String sourceFileName)
	{
		StreamHeader msg = new StreamHeader(new Date().getTime(), sourceFileName);
	    for (Iterator<ClientHandler> it = clientList.iterator(); it.hasNext(); )
	    {
	    	ClientHandler client = it.next();
			try {
				client.sendMessage(msg);
			} 
			catch (IOException e) {
				shutdownClient(client, e);
				it.remove();
			}
		}
		try {
			Thread.sleep(1000);
		}
		catch(Exception e)
		{
			;
		}

	}

	
	public void run() {
 		props = ApplicationProperties.getInstance();
		int port = props.getPort();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);
            while (true) {
            	serverSocket.setSoTimeout(1000);
            	ClientHandler client = null;
            	boolean interrupted = false;
            	try {
            		client = new ClientHandler(serverSocket.accept());
            	}
            	catch(SocketTimeoutException e)
            	{
            		interrupted = true;
            	}
            	catch(Exception e)
            	{
            		throw e;
            	}
            	if (Thread.currentThread().isInterrupted())
            	{
            		for(ClientHandler item : clientList)
            		{
            			item.closeConnection();
            		}
            		break;
            	}
            	if (!interrupted)
            	{
                    clientList.add(client);
                    client.start();
            	}
            }
        } 
        catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
            log.error("Server exception", ex);
    	    for (Iterator<ClientHandler> it = clientList.iterator(); it.hasNext(); )
    	    {
    	    	ClientHandler client = it.next();
				shutdownClient(client, null);
    				it.remove();
    		}
        }
    }
}
