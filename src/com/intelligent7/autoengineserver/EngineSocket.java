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
	 InetAddress  clientAddr;
	Logger log;
	ArrayBlockingQueue<String> socketOutDataQ ;
	ArrayBlockingQueue<String> socketInDataQ ;
	final int Q_SIZE=200;
	Vector<DataUpdateListener> sniffers;
	
	/*
	 * interface for owner to update my name
	 * like a tag
	 * 
	 */
	public interface DataUpdateListener //must be a data switch board
	{
		public void updateEngineName(String name);
		public void peerSocketDataReady(String myName, String data);// later if for byte[] data);
		public void peerSocketDataReady(String myName, String data, String peerName);// later if for byte[] data);
		}
	
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
		socketOutDataQ = new ArrayBlockingQueue<String>(Q_SIZE, true);
		sniffers=new Vector<DataUpdateListener>();
		log=Logger.getAnonymousLogger();
	}
	
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
			log.info("Broadcasted-> "+fixLine);
		}
	}

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
			log.info("Broadcasted-> "+fixLine);
		}
	}

	void updateMyName()
	{
		for (int i=0; i< sniffers.size(); i++)
		{
			final DataUpdateListener aF=sniffers.get(i);
			final String dropData=myName;
			if (aF != null)
			{
				new Thread(new Runnable(){
					public void run() {
						aF.updateEngineName(dropData);
					}
				}).start();
				
			}
			log.info("Got socket name -> "+myName);
		}
	}
	public void addFixSniffer(DataUpdateListener sniffer)
	{
		if (sniffers==null) sniffers=new Vector<DataUpdateListener>();
		sniffers.add(sniffer);
	}
	
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
		
		while (mySocket.isSktConnected() && mySocket.hasInStream())
		{
			if (stopFlag) break;
			processData();//readBytes);
		}
		if (imChatLine)
			{
				startChat();
			}
			try {
				respThd.join();
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
