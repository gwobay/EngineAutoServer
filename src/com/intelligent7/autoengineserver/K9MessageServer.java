package com.intelligent7.autoengineserver;

import java.net.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.io.*;
import java.lang.Thread;
import java.util.logging.Level;
import java.util.logging.Logger;

public class K9MessageServer extends Thread
{
	public K9MessageServer()
	{
		super();
	}

	void confirmMessage(SimpleSocket mySocket){
		int iTry = 0;
		String data="K9-OK";
		while (!mySocket.sendText(data)){
			try {
				Thread.sleep(800);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (mySocket.isClosed() || iTry++ > 5) 
			{
				log.info("failed to send :"+data);
				
				break;
			}
			
		}
		mySocket.close();
	}
	//k9-imei-imsi<imsi>
	//k6-imei-sim<personal_id> ; personal_id will be used to check if
	//                           this is a legal user
	@SuppressWarnings("resource")
	public void getK9Message(Socket clientSkt){
		if (clientSkt == null) return;
		SimpleSocket mySocket = new SimpleSocket(clientSkt);
		byte[] readData=mySocket.getStreamData();
		if (readData==null || readData.length < 5) return;
		String inData=new String(readData);
		int idx=inData.indexOf("<");
		if (idx<0) return;
		String[] terms=inData.substring(0, idx).split("-");
		if (terms.length < 3) {
			System.out.println("Bad DATA format "+inData);
			return;
			}
		if (terms[0].equalsIgnoreCase("K6")){
			updateK6Map(terms[1], terms[2]);
			
			log.log(Level.INFO, inData);
			confirmMessage(mySocket);
			return;
		}
		if (!terms[0].equalsIgnoreCase("K9")){
			log.log(Level.WARNING, "Unknown data "+inData);
			return;
		}
		String phone=k6Map.get(terms[1]);
		if (phone==null){
			log.log(Level.WARNING, "Unknown IMEI "+inData);
			return;
		}
		addK9Mapping(terms[1], terms[2]);
		k9Map.put(terms[2], phone);
		confirmMessage(mySocket);
			//if (!updateK9Map(terms[1], terms[2]))
			//return;		
	}
	
	public void buildMaps(Socket inSocket)
	{
		getK9Message(inSocket);
		try {
			inSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean updateK6Map(String iMei, String phone){
		if (k6Map == null){
			k6Map=new HashMap<String, String>();
		}
		k6Map.put(iMei, phone);
		addK6Mapping(iMei, phone);
		return true;
	}

	public void readMapFile()//String fileName, HashMap<String, String> params)
	{		  
	  BufferedReader reader=null;
	  try {
		  reader = new BufferedReader(new InputStreamReader(new FileInputStream(myFileName)));
	  
		  String aLine;
		  while ((aLine=reader.readLine())!=null)
		  {
			  if (aLine.length() < 2) continue;
			  String[] terms=aLine.split("=>");
			  if (terms.length<2) continue;
			  if (k6Map == null){
					k6Map=new HashMap<String, String>();
				}
				k6Map.put(terms[0], terms[1]);
		  }
	    
	  } catch (IOException e) {
		  e.printStackTrace();
	    System.out.println("Could not read file "+myFileName);
	  } finally {
	    try {
	      reader.close();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    	System.out.println("Exception closing "+myFileName);
	    }
	  }
	  
	  try {
		  reader = new BufferedReader(new InputStreamReader(new FileInputStream(yourFileName)));
	  
		  String aLine;
		  while ((aLine=reader.readLine())!=null)
		  {
			  if (aLine.length() < 2) continue;
			  String[] terms=aLine.split("=>");
			  if (terms.length<2) continue;
			  String phone=k6Map.get(terms[0]);
				if (phone==null)continue;
				//addK9Mapping(terms[1], terms[2]);
				k9Map.put(terms[1], phone);
		  }
	    
	  } catch (FileNotFoundException e) {
		  e.printStackTrace();
		    System.out.println("No file "+yourFileName);
		  } catch (IOException e) {
		  e.printStackTrace();
	    System.out.println("Could not read file "+yourFileName);
	  } finally {
	    try {
	      reader.close();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    	System.out.println("Exception closing "+yourFileName);
	    }
	  }  
	}

	public static void readFromResourceFile(String fileName, HashMap<String, String> params)
	{
		  
	  BufferedReader reader=null;
	  try {
		  reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
	  
		  String aLine;
		  while ((aLine=reader.readLine())!=null)
		  {
			  if (aLine.length() < 2) continue;
			  int i0=0; while (aLine.charAt(i0)<= ' ') i0++;
			  int iE=i0; while (++iE<aLine.length() && aLine.charAt(iE) > ' ');
			  String key=aLine.substring(i0, iE).toUpperCase();
			  i0=iE; 
			  while (aLine.charAt(i0)!= '=' && ++i0 <aLine.length());
			  iE=++i0; 
			  while (++iE<aLine.length() && aLine.charAt(iE) > ' ');
			  String value=aLine.substring(i0, iE);//no case change .toUpperCase();
			  if (key.length() > 0)
				  params.put(key, value);
		  }
	    
	  } catch (IOException e) {
		  e.printStackTrace();
	    System.out.println("Could not read file "+fileName);
	  } finally {
	    try {
	      reader.close();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    	System.out.println("Exception closing "+fileName);
	    }
	  }
	    
	}
	
	private static HashMap<String, String> config=new HashMap<String, String>();

//int mPort;

	private static HashMap<String, String> k6Map=new HashMap<String, String>();
	
	static BufferedWriter writer=null;
	public void addK9Mapping(String iMei, String iMsi){
		  try {
			  writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(yourFileName, true)));
			  writer.write(iMei+"=>"+iMsi);
			  writer.newLine();
			  writer.flush();
		  }catch (IOException e){}
	}

	public void addK6Mapping(String iMei, String sim){
		  try {
			  writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(myFileName, true)));
			  writer.write(iMei+"=>"+sim);
			  writer.newLine();
			  writer.flush();
		  }catch (IOException e){}
	}
int k9Port;
long mStopTime;

Logger log;	
static String myFileName="K6_information";
static String yourFileName="K9_information";

	void init()
	{
		//mGcmSender=new SendGcmMessager();
		log=Logger.getAnonymousLogger();
		readFromResourceFile("CommanderResource", config);
		String sPort=config.get("K9_PORT");
		if (sPort == null) {
			log.warning("Missing port information");
			System.exit(-1);
		}
		setPort(Integer.parseInt(sPort));
		setStopTime(0);
		
		String fileName=config.get("K9_FILE");
		if (fileName==null) fileName="K9_information";
		log=Logger.getLogger(fileName);	
		
		readMapFile();
		
	}
	HashMap<String, String> k9Map;
	public void setK9Map(HashMap<String, String> aMap){
		k9Map=aMap;
	}
	//k9 Map has iMsi->iMei and iMei->SIM phone#
	public boolean updateK9Map(String iMsi, String iMei){
		if (k9Map == null){
			System.out.println("No K9 Map set yet!!!!");
			return false;
		}
		k9Map.put(iMsi, iMei);
		return true;
	}
	public void setPort(int p)
	{
		k9Port=p;
	}
	public void setStopTime(long t)
	{
		mStopTime=t;;
	}

	boolean stopNow;
	public void shutdown()
	{
		stopNow=true;
	}
    public void run()
    {
    	
    	long stopAt=(new Date()).getTime()+60*1000*60*8;
    	init();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(k9Port);
            System.out.println("listening K9 message on port: "+k9Port);
        } catch (IOException e) {
            System.err.println("Could not listen on port: "+k9Port);
            System.exit(1);
        }
        
        try {
			serverSocket.setSoTimeout(5*60*1000);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        stopNow=false;
        while (!stopNow)
        {
        	Socket newUser=null;
        	try {
        			newUser = serverSocket.accept();
            } catch (SocketTimeoutException e) {
        		//stopNow=true;
        		System.err.print("alive!!");
        		//break;
        	} catch (IOException e) {
                	System.err.println("Accept K9 failed.");
                	System.err.println(e.getMessage());
                	try {
						sleep(10*1000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                }
        	
        	if (newUser!=null){
        
	        System.out.println("Got K9 call from "+newUser.getRemoteSocketAddress().toString());	        
	        final Socket dataSocket=newUser;
        	new Thread(new Runnable(){
	        	public void run()
	        	{
	        		buildMaps(dataSocket);
	        	}
	        }).start();       	
        } //while ((new Date()).getTime() < mStopTime || mStopTime==0);
        }
                try {
                        serverSocket.close();
                        
                } catch (IOException e) {}
        
    }

    public static void main(String[] args)
    {
        long stopAt=(new Date()).getTime()+60*1000*60*8;
            	stopAt=0;
 
        K9MessageServer aServer=new K9MessageServer();
        aServer.setK9Map(new HashMap<String, String>());
        //aServer.init();
        aServer.start();
                
        try {           		
            aServer.join();
            //Thread.sleep(stopAt - (new Date()).getTime());
       }catch(InterruptedException e){
    	  aServer.interrupt();
    		   try { 
				aServer.join();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	   }
       }
            	
 
}




