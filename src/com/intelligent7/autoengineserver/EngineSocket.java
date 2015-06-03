/**
 * 
 */
package com.intelligent7.autoengineserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Date;
<<<<<<< HEAD
<<<<<<< HEAD
import java.util.HashMap;
import java.util.Iterator;
=======
>>>>>>> origin/master
=======
>>>>>>> origin/master
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author eric
 *
 */
public class EngineSocket extends Thread 
{
//this socket always has id either for SIM or PHONE
	//an instance is created whenever server accept
	//an client socket which is aliased as mySocket
	//then spawn a writeThread (which will poll the outboundQ,
	//this thread (act as readThread) then loop to read and dump data to peer;
	//then join write thread
	//socket owner should take care of data processing
	
	String myName;
	SimpleSocket mySocket;
	InputStream in;
	OutputStream out;
	Vector<Byte> data;
	byte[] inDataBuffer;
	boolean zipped_channel;
	String readPage;
	int totalRead;
	int myDatabaseId;
<<<<<<< HEAD
<<<<<<< HEAD
	InetAddress  clientAddr;
	Logger log;
	ArrayBlockingQueue<String> socketOutDataQ ;
	ArrayBlockingQueue<String> socketInDataQ ;
	HashMap<String, ArrayBlockingQueue<String> > friendQ ;
	PostOffice myPostOffice;
	final int Q_SIZE=20;
=======
=======
>>>>>>> origin/master
	 InetAddress  clientAddr;
	Logger log;
	ArrayBlockingQueue<String> socketOutDataQ ;
	ArrayBlockingQueue<String> socketInDataQ ;
	final int Q_SIZE=200;
<<<<<<< HEAD
>>>>>>> origin/master
=======
>>>>>>> origin/master
	Vector<DataUpdateListener> sniffers;
	
	/*
	 * interface for owner to update my name
	 * like a tag
<<<<<<< HEAD
<<<<<<< HEAD
	 * this is for realtime staff
	 * as of now, Post Office scheme will be used for this
	 */
	public interface DataUpdateListener //must be a data switch board
	{
		public void engineSocketSignOn(String name, EngineSocket who);
		public void engineSocketQuit(String who);
		public void engineSocketAddPeers(String controller, String phones);
		public void peerSocketDataReady(String myName, String data);// later if for byte[] data);
		public void peerSocketDataReady(String myName, String data, String peerName);// later if for byte[] data);
	}
=======
=======
>>>>>>> origin/master
	 * 
	 */
	public interface DataUpdateListener //must be a data switch board
	{
		public void updateEngineName(String name);
		public void peerSocketDataReady(String myName, String data);// later if for byte[] data);
		public void peerSocketDataReady(String myName, String data, String peerName);// later if for byte[] data);
		}
<<<<<<< HEAD
>>>>>>> origin/master
=======
>>>>>>> origin/master
	
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private EngineSocket(){};
	
	public EngineSocket(Socket clientSkt) {
		// TODO Auto-generated constructor stub
		if (clientSkt == null) return;
		myName=null;
		mySocket = new SimpleSocket(clientSkt);
		clientAddr=clientSkt.getInetAddress();
		socketInDataQ = new ArrayBlockingQueue<String>(Q_SIZE, true);
<<<<<<< HEAD
<<<<<<< HEAD
		//socketOutDataQ = new ArrayBlockingQueue<String>(Q_SIZE, true);
		friendQ=new HashMap<String, ArrayBlockingQueue<String> >();
=======
		socketOutDataQ = new ArrayBlockingQueue<String>(Q_SIZE, true);
>>>>>>> origin/master
=======
		socketOutDataQ = new ArrayBlockingQueue<String>(Q_SIZE, true);
>>>>>>> origin/master
		sniffers=new Vector<DataUpdateListener>();
		log=Logger.getAnonymousLogger();
	}
	
<<<<<<< HEAD
<<<<<<< HEAD
	public void setOutDataQ(ArrayBlockingQueue<String> newQ)
	{
		socketOutDataQ=newQ;
	}
	
	public void setMyPostOffice(PostOffice new1)
	{
		myPostOffice=new1;
	}	
	
	void setFriendMailBox()
	{
		
	}
	
	void doFirstSignOn(String info){ //info= "phone1, phone2@controller"
		String[] users=info.split("@");
		myName=users[0];
		if (users.length == 1)
		{
			socketOutDataQ=myPostOffice.getMailBox(info);
			startWriteThread();
			//no need to wake-up post, the mail for MCU is always in its box
			String temp=myPostOffice.getPhones(myName);
			if (temp==null) return;
			String[] phones=temp.split(",");
			for (int i=0; i<phones.length; i++){
				friendQ.put(phones[i], myPostOffice.getMailBox(phones[i]));
			}		
			return;
		}
		myPostOffice.updateRelationBook(users[1],  users[0]);			
		String[] phones=users[0].split(",");
		myName=phones[0];
		socketOutDataQ=myPostOffice.getMailBox(myName);	
		startWriteThread();
		friendQ.put(users[1], myPostOffice.getMailBox(users[1]));
		//have to wake-up PostMan to deliver unknown receiver mail to me 
		myPostOffice.interrupt();				
	}
	
=======
>>>>>>> origin/master
=======
>>>>>>> origin/master
	public void setZippedFlag(boolean T_F) { mySocket.setZippedFlag(T_F);}

	void dropToPeer(String fixLine)
	{
		for (int i=0; i< sniffers.size(); i++)
		{
			final DataUpdateListener aF=sniffers.get(i);
			final String dropData=fixLine;
			if (aF != null)
			{
				new Thread(new Runnable(){
					public void run() {
						aF.peerSocketDataReady(myName, dropData);
					}
				}).start();
				
			}
<<<<<<< HEAD
<<<<<<< HEAD
			//log.info("Broadcasted-> "+fixLine);
		}
	}


=======
=======
>>>>>>> origin/master
			log.info("Broadcasted-> "+fixLine);
		}
	}

<<<<<<< HEAD
>>>>>>> origin/master
=======
>>>>>>> origin/master
	void dropToPeer(String receiver, String fixLine)
	{
		for (int i=0; i< sniffers.size(); i++)
		{
			final DataUpdateListener aF=sniffers.get(i);
			final String dropData=fixLine;
			final String toWhom=receiver;
			if (aF != null)
			{
				new Thread(new Runnable(){
					public void run() {
						aF.peerSocketDataReady(myName, dropData, toWhom);
					}
				}).start();
				
			}
<<<<<<< HEAD
<<<<<<< HEAD
		}
	}

	void addMyPeers(String controller, String phones)
	{
		for (int i=0; i< sniffers.size(); i++)
		{
			final DataUpdateListener aF=sniffers.get(i);
			final String car_controller=controller;
			final String peers=phones;
			if (aF != null)
			{
				new Thread(new Runnable(){
					public void run() {
						aF.engineSocketAddPeers(car_controller, peers);
					}
				}).start();
				
			}
		}
	}
	void updateMyName()
	{
		
=======
=======
>>>>>>> origin/master
			log.info("Broadcasted-> "+fixLine);
		}
	}

	void updateMyName()
	{
<<<<<<< HEAD
>>>>>>> origin/master
=======
>>>>>>> origin/master
		for (int i=0; i< sniffers.size(); i++)
		{
			final DataUpdateListener aF=sniffers.get(i);
			final String dropData=myName;
<<<<<<< HEAD
<<<<<<< HEAD
			final EngineSocket me=this;
=======
>>>>>>> origin/master
=======
>>>>>>> origin/master
			if (aF != null)
			{
				new Thread(new Runnable(){
					public void run() {
<<<<<<< HEAD
<<<<<<< HEAD
						aF.engineSocketSignOn(dropData, me);
=======
						aF.updateEngineName(dropData);
>>>>>>> origin/master
=======
						aF.updateEngineName(dropData);
>>>>>>> origin/master
					}
				}).start();
				
			}
			log.info("Got socket name -> "+myName);
		}
	}
<<<<<<< HEAD
<<<<<<< HEAD
	public void addSwitchServer(DataUpdateListener sniffer)
=======
	public void addFixSniffer(DataUpdateListener sniffer)
>>>>>>> origin/master
=======
	public void addFixSniffer(DataUpdateListener sniffer)
>>>>>>> origin/master
	{
		if (sniffers==null) sniffers=new Vector<DataUpdateListener>();
		sniffers.add(sniffer);
	}
	
<<<<<<< HEAD
<<<<<<< HEAD
	//------------ socket data processing -----------
	void putMsgInFriendQ(String msg){
		Iterator<String> itr=friendQ.keySet().iterator();
		while (itr.hasNext())
		{
			String key=itr.next();
			ArrayBlockingQueue<String> aFriend=friendQ.get(key);
			if (aFriend != null){
				aFriend.add(msg);
			}
		}
	}
	//------------------------------------------------
	void startWriteThread()
	{
		writeThread=new Thread(){
			public void run()
			{
				dumpResponse();
			}
		};
		writeThread.start();
	}
=======
>>>>>>> origin/master
=======
>>>>>>> origin/master
	MessageParser mParser;
	boolean stopFlag;
	boolean imChatLine;
	FixDataBundle mFDB=null;
	void processData()//byte[] readData)
	{
		byte[] readData=null;
		long timeEnd=(new Date()).getTime()+3000;
		while (readData==null || readData.length < 1)
		{
			if (readData==null && mySocket.isSktClosed())
				{
				stopFlag=true;
				return;
				}
			readData=mySocket.getStreamData();
			if (readData==null && mySocket.getReadFlag() <0)
				{
					stopFlag=true;
					try {
						sleep(30*1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mySocket.close();
					break;
				}
			if ((new Date()).getTime() > timeEnd) break;
		}
		if (readData==null || readData.length < 1) return;
		
		String sData=new String(readData);
<<<<<<< HEAD
<<<<<<< HEAD
		log.info("got : "+sData);
				//format "msg"+"<sender, backup-sender@receiver>$" i.e., "...<phone1, phone2@receiver>" from phone 
		//and only <sim number> from control
		int iUser=sData.indexOf('<');
		int idx=sData.indexOf('>');
		if (iUser < 0 || idx < 0){
			System.out.println(sData+" !!Bad data format: missing '<', unknown sender");
			return;
		}
		
		if (sData.charAt(0)=='<')
		{
			String name=sData.substring(1,  idx);
			if (myName==null){
				doFirstSignOn(name);
				return;
			}
		} 

			int i0x=iUser;
			
			if (myName==null){
				doFirstSignOn(sData.substring(i0x+1,  idx));
			}
			String sender=sData.substring(i0x+1,  idx);
			String msg=(sData.substring(0, i0x)+"<"+myName+">$");
			idx=sender.indexOf('@');
			if (idx < 0) {
				//dropToPeer(msg);
				if (friendQ.size()==0)
				{
					myPostOffice.putNewMail(new PostOffice.UnknowReceiverMail(myName, msg));
					return;
				}
				putMsgInFriendQ(msg);
			}
			else {
				//ArrayBlockingQueue<String> myFriendQ=null;
				String myFriend=sender.substring(idx+1);
				if (friendQ.size()==0) friendQ.put(myFriend, myPostOffice.getMailBox(myFriend));
				friendQ.get(myFriend).add(msg);				
				//dropToPeer(sender.substring(idx+1), msg);
			}

			//dropToPeer(sData);
		
=======
=======
>>>>>>> origin/master
		if (sData.charAt(0)=='<')
		{
			int idx=sData.indexOf('>');
			if (idx < 0) 
				{
				System.out.println("Bad data format: unknown sender");
				return;
				
				}
			String name=sData.substring(1,  idx);
			if (myName==null){
				myName=name;
				updateMyName();
			}
		} else {
			int i0x=sData.indexOf('<');
			int idx=sData.indexOf('>');
			if (i0x < 0 || idx < 0) 
			{
				System.out.println("Bad data format: unknown sender");
				return;
			}
			String sender=sData.substring(i0x,  idx);
			idx=sender.indexOf('-');
			if (idx < 0) dropToPeer(sData.substring(0,  i0x)+"$");
			else {
				String receiver=sender.substring(idx+1);
				dropToPeer(receiver, sData.substring(0,  i0x)+"$");
			}

			dropToPeer(sData);
		}
		log.info("got : "+sData);
<<<<<<< HEAD
>>>>>>> origin/master
=======
>>>>>>> origin/master
		/*
		FixDataBundle aFDB=new FixDataBundle(readData);
		mFDB=aFDB;
		
		
		String command=aFDB.getCommand(201); //also the list in 220='citizen_id','citizen_id','',...
		if (command != null && command.equalsIgnoreCase("broadcast"))
		{
			String temp=aFDB.getFixLine();
			int i0=temp.indexOf("170=");
			if (i0>0) {
				temp=aFDB.getFixLine().substring(i0);
			}
			dropToPeer(temp);
		}
		
		String table=aFDB.getCommand(170);
		if (table != null && table.equalsIgnoreCase("chatroom"))
		{
			ChatMaster.plugInChatLine(aFDB, mySocket);
			log.info("Sent to Chatroom");
			imChatLine=true; //switch in/out stream to Chatmaster's room
			stopFlag=true;
			return;
		}
		
		
		//mParser.setMessage(readData, readData.length);
		//mParser.setDbClientId(myDatabaseId);
		mParser.process(aFDB, socketOutDataQ);
		
		
		return;
		/*Vector<String> dV=mParser.getDbResponseToClient();
		for (int i=0; i<dV.size();i++)
		{
			mySocket.sendSocketText(dV.get(i));
		}
		*/
		
	}
	public void startChat()
	{
		
	}
	
	public void putOutBoundMsg(String msg)
	{
		if (socketOutDataQ.size() > Q_SIZE){
			System.out.println("Warning : too many msg in my Q");
			return;
		}
		try {
			socketOutDataQ.put(msg);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//inbound msg is Qed in the switchBoard who has Hash<String, Vector<String>>
	//when Qed will be dropped to the new EngineSocket when updateName is called
	
	public void dumpResponse()
	{
		while (mySocket.isSktConnected() && mySocket.hasOutStream() ||
				socketOutDataQ.size() > 0)
		{			
			String socketData=null;
			try {
				if (socketOutDataQ.size() > 0) 
					socketData=socketOutDataQ.poll(200, TimeUnit.MILLISECONDS);
				else
					socketData = socketOutDataQ.take();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
			if (socketData != null)
			{
				int iTry = 0;
				String data=new String(socketData);
				while (!mySocket.sendText(socketData)){
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						if (mySocket.isClosed() || iTry++ > 10) 
							{
								log.info("failed to send :"+data);
								return;
							}
					}
				}
				log.info("resp: "+data);
			}		
		}
			
	}
	
<<<<<<< HEAD
<<<<<<< HEAD
	Thread writeThread;
	public void run()
	{
		writeThread=null;
		mParser=new MessageParser();
		stopFlag=false;
		imChatLine=false;
		
		
=======
=======
>>>>>>> origin/master
	public void run()
	{
		mParser=new MessageParser();
		stopFlag=false;
		imChatLine=false;
		Thread respThd=new Thread(){
			public void run()
			{
				dumpResponse();
			}
		};
		
		respThd.start();
<<<<<<< HEAD
>>>>>>> origin/master
=======
>>>>>>> origin/master
		
		while (mySocket.isSktConnected() && mySocket.hasInStream())
		{
			if (stopFlag) break;
			processData();//readBytes);
		}
		if (imChatLine)
			{
				startChat();
			}
<<<<<<< HEAD
<<<<<<< HEAD
		if (writeThread != null)
			try {
				writeThread.join();
=======
			try {
				respThd.join();
>>>>>>> origin/master
=======
			try {
				respThd.join();
>>>>>>> origin/master
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (mParser != null)
			mParser.finish();
			if (mFDB != null) mFDB.cleanUp();
			if (mySocket!=null && !imChatLine)
				mySocket.close();
			System.out.println("I am done with processing and closed at "+
								DateFormat.getTimeInstance().format(new Date()));		
	
			log.info("server socket closed connection with "+clientAddr);
<<<<<<< HEAD
<<<<<<< HEAD
			
=======
>>>>>>> origin/master
=======
>>>>>>> origin/master
	}
	/**
	 * @param arg0
	 */
	public EngineSocket(Runnable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public EngineSocket(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public EngineSocket(ThreadGroup arg0, Runnable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public EngineSocket(ThreadGroup arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public EngineSocket(Runnable arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public EngineSocket(ThreadGroup arg0, Runnable arg1, String arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public EngineSocket(ThreadGroup arg0, Runnable arg1, String arg2,
			long arg3) {
		super(arg0, arg1, arg2, arg3);
		// TODO Auto-generated constructor stub
	}

}
