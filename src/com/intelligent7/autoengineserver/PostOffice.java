package com.intelligent7.autoengineserver;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

public class PostOffice extends Thread
implements EngineSocket.DataUpdateListener {
	
	public static class UnknowReceiverMail{
		String sender;
		String data;
		long lastTryTime;
		public UnknowReceiverMail(String s1, String s2){
			sender=s1;
			data=s2;
		}
	}
	private ArrayBlockingQueue<UnknowReceiverMail> pendingMail;
	private ArrayList<UnknowReceiverMail> failedMail;
	private Logger log;
	private HashMap<String, EngineSocket > allUsers;
	private  HashMap<String, ArrayBlockingQueue<String> > allMailBoxes;
	private  HashMap<String, String> relationBook;
	private HashMap<String, String> k9Map;

	PostOffice()
	{
		log=Logger.getAnonymousLogger();
		pendingMail=new ArrayBlockingQueue<UnknowReceiverMail>(1000);
		failedMail=new ArrayList<UnknowReceiverMail>();
		allUsers=new HashMap<String, EngineSocket >();
		allMailBoxes=new HashMap<String, ArrayBlockingQueue<String> >();
		relationBook=new HashMap<String, String>();
		k9Map=new HashMap<String, String>();
	}
	
	public HashMap<String, String> getK9Map(){
		return k9Map;
	}
	
	public void addNewSocket(Socket aSkt)
	{
		EngineSocket new1=new EngineSocket(aSkt);
		//new1.addSwitchServer(this);
		new1.setMyPostOffice(this);
		new1.setMyPrinter(myPrinter);
		new1.start();
	}
	
	private final ReentrantReadWriteLock userInvetoryLock = new ReentrantReadWriteLock();
    
	public void addNewUser(String name, EngineSocket who)
	{
		userInvetoryLock.writeLock().lock();
		try {
			allUsers.put(name, who);
		} finally { userInvetoryLock.writeLock().unlock();}
	}
	
	public void userDisconnect(String name)
	{
		EngineSocket toRemove=null;
		userInvetoryLock.readLock().lock();
		try {
			toRemove=allUsers.get(name);
		} finally { 
			userInvetoryLock.readLock().unlock();
			if (toRemove==null) {
				log.warning("user "+name+" not in the list");
				return;
			}
		}
		userInvetoryLock.writeLock().lock();
		try {
			allUsers.remove(name);
		} finally { userInvetoryLock.writeLock().unlock();}
	}
	
	void broadcastNewIpPort(String criteria) //this is ICCID filtered from other criteria in main server
	{
		ArrayBlockingQueue aBox=allMailBoxes.get(criteria);
		aBox.add("M0-ip-port<000000>$"); //TODO need to read the ip and port in configuration file from main server
		String phones=relationBook.get(criteria);
		if (phones==null) return;
		String[] terms=phones.split(",");
		for (int i=0; i<terms.length; i++)
		{
			
		}
	}
	void removeAllUsers()
	{
		Iterator<String> itr;
		userInvetoryLock.readLock().lock();
		try {
			itr=allUsers.keySet().iterator();
		} finally { userInvetoryLock.readLock().lock();}
		userInvetoryLock.writeLock().lock();
		try {
			while (itr.hasNext())
			{
				EngineSocket toRemove=allUsers.remove(itr.next());
				if (toRemove!=null) 
				{
						toRemove.mySocket.close();
				}
			}
		} finally { userInvetoryLock.writeLock().unlock();}
	}
	private final  ReentrantReadWriteLock mailBoxLock = new ReentrantReadWriteLock();
    //private final Lock r = rwl.readLock();
    //private final Lock w = rwl.writeLock();
	public ArrayBlockingQueue<String> getMailBox(String forWhom)
	{
		ArrayBlockingQueue<String> mailBox=null;
		mailBoxLock.readLock().lock();
		try {
			mailBox=allMailBoxes.get(forWhom);
		} finally { mailBoxLock.readLock().unlock();		 }
		if (mailBox != null) return mailBox;
		mailBoxLock.writeLock().lock();
		try{
				mailBox=new ArrayBlockingQueue<String>(50);
				allMailBoxes.put(forWhom, mailBox);			
		} finally {
			mailBoxLock.writeLock().unlock();
		}
		System.out.println(forWhom+" has "+mailBox.size()+"mails in mailbox");
		return mailBox;
	}
	public ArrayBlockingQueue<String> getMatchedMailBox(String forWhom)
	{
		String sim=k9Map.get(forWhom);
		if (sim==null){
			System.out.println("BAD IMSI "+forWhom+" no match");
			return null;
		}
		return getMailBox(sim);
	}
	
	public String getMatchedName(String inName)
	{
		return k9Map.get(inName);
	}
	public void putNewMail(UnknowReceiverMail new1)
	{
		try {
			pendingMail.put(new1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ArrayBlockingQueue<String> getMyFriendMailBox(String friend)//useless should be removed
	{
		ArrayBlockingQueue<String> mailBox=null;
		mailBoxLock.readLock().lock();
		try {
			mailBox=allMailBoxes.get(friend);
		} finally { mailBoxLock.readLock().unlock();		 }
		System.out.println(friend+" has "+mailBox.size()+"mails in mailbox");
		return mailBox;
	}
	//Post office Updates relation books
	
	private final ReentrantLock lock2 = new ReentrantLock();
	public void updateRelationBook(String controller, String phones)
	{
		lock2.lock();
		try
		{
			String from=relationBook.get(controller);
			String[] inPhones=phones.split(",");			
		
			if (from == null) relationBook.put(controller, phones);
			else 
			{
				boolean hasNewPhone=false;
				String newList="";
				for (int i=0; i<inPhones.length; i++)
				{
					if (inPhones[i].length()<2) continue;
					if (from.contains(inPhones[i])) continue;
					hasNewPhone=true;
					newList += (inPhones[i]+",");					
				}
				if (!hasNewPhone) return;				
				relationBook.put(controller, newList+from );
			}
				 				
			setQ4Name(controller);
			//String[] newP=phones.split(",");
			for (int i=0; i<inPhones.length; i++) 
			{				
						setQ4Name(inPhones[i]);				
			}
		} finally { lock2.unlock();}
		
	}

	public String getPhones(String iMsi){
		String iMei=k9Map.get(iMsi);
		return relationBook.get(iMei);
	}
	
	boolean toStop;
	public void shutdown()
	{
		toStop=true;
	}
	
	MessagePrinter myPrinter;
	public void run()
	{
		log=Logger.getAnonymousLogger();
		myPrinter=new MessagePrinter();
		myPrinter.start();
		toStop=false;
		while(!toStop)
		{
			UnknowReceiverMail new1=null;
			try {
				new1=pendingMail.take();
			}
			catch (InterruptedException e){
				
			}
			if (new1 != null){
				peerSocketDataReady(new1.sender, new1.data);
			}
			int checkSize=failedMail.size();
			 ArrayList<UnknowReceiverMail> tempBox=new ArrayList<UnknowReceiverMail>();
			while (checkSize-- > 0)
			{
				new1=failedMail.remove(0);
				if (new1==null) continue;
				if (new1.lastTryTime > (new Date()).getTime()+60*1000) 
				{
					peerSocketDataReady(new1.sender, new1.data);
				}
				else tempBox.add(new1);
			}
			if (tempBox.size()> 0)
			{
				failedMail.addAll(tempBox);
			}			
		}
		//TODO
		//save (serialize current status into memory map file)
		pendingMail.clear();
		pendingMail=null;
		allMailBoxes.clear();
		allMailBoxes=null;
		relationBook.clear();
		relationBook=null;
		removeAllUsers();
		allUsers.clear();
		allUsers=null;
		k9Map.clear();
		k9Map=null;
	}
	
	//----------------------------- build switch --------
	private final ReentrantLock lockS = new ReentrantLock();
	public void engineSocketSignOn(String name, EngineSocket who)
	{	
		lockS.lock();
		try 
		{
			synchronized(allUsers)
			{
				allUsers.put(name, who);
			}
		//EngineSocket who
			setQ4Name(name);
			ArrayBlockingQueue<String> qedData=allMailBoxes.get(name);
			who.setOutDataQ(qedData);
		} finally {
			lockS.unlock();
		}
	}
	
	public void engineSocketQuit(String who){
		synchronized(allUsers)
		{
		allUsers.remove(who);
		}
	}
	
//-------------------------- for handling data switching --------------	
	
	void dropData(String clientName){
		
	}
	
	private final ReentrantLock lock1 = new ReentrantLock();
	void setQ4Name(String name)
	{
		lock1.lock();
		try {
			ArrayBlockingQueue<String> qedData=allMailBoxes.get(name);
			if (qedData==null) 
			{
				qedData=new ArrayBlockingQueue<String>(50);
				allMailBoxes.put(name, qedData);
			}
		}
		finally {
			lock1.unlock();
		}
	}
	

	public void engineSocketAddPeers(String controller, String phones){
		//as of now no real-time staff
	}
	private final ReentrantLock lockD = new ReentrantLock();
	public void peerSocketDataReady(String myName, String data)// later if for byte[] data);
	{
		String peer=null;
		synchronized(relationBook){
			peer=relationBook.get(myName);
		}
		if (peer==null)
		{
			UnknowReceiverMail failed1=new UnknowReceiverMail(myName, data);
			failed1.lastTryTime=new Date().getTime();
			failedMail.add(failed1);
		}
		lockD.lock();
		try {
			String[] peers=peer.split(",");
			for (int i=0; i<peers.length; i++) 
			{
				peerSocketDataReady(myName, data, peers[i]);
			}
		}finally { lockD.unlock();}
	}
	
	private final ReentrantLock lockD2 = new ReentrantLock();
	
	public void peerSocketDataReady(String myName, String data, String peerName)
	// later if for byte[] data);
	{
		lockD2.lock();
		try {
		ArrayBlockingQueue<String> qedData=allMailBoxes.get(peerName);
		if (qedData==null) 
		{
			qedData=new ArrayBlockingQueue<String>(50);
			allMailBoxes.put(peerName, qedData);
		}		
		qedData.add(data);
		}finally { lockD2.unlock();}
	}


}
