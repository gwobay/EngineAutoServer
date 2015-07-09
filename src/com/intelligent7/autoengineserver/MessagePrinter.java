package com.intelligent7.autoengineserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MessagePrinter extends Thread {

	static ArrayBlockingQueue<String> printQ=new ArrayBlockingQueue<String>(3000, false);
	static ReadWriteLock myLock=new ReentrantReadWriteLock();
	
	public MessagePrinter() {
		// TODO Auto-generated constructor stub
	}

	public void printMsg(String msg){
		final String data=msg;
		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					printQ.put(data);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		}).start();
	}
	
	static String fileHead="log";
	static File harCopy=null;
	static GregorianCalendar gToday=null;
	static String currentFileName=null;
	static DecimalFormat dF=new DecimalFormat("00");
	static FileOutputStream currentOutStream=null;
	
	static String getFileName()
	{
		gToday=new GregorianCalendar(TimeZone.getTimeZone("Hongkong"));
		return "log"+dF.format(gToday.get(Calendar.MONTH)+1)+dF.format(gToday.get(Calendar.DAY_OF_MONTH))+
				"_"+dF.format(gToday.get(Calendar.HOUR_OF_DAY))+".txt";		
	}
	
	void putInFile(String data){
		String aName=getFileName();
		String timeStamp=dF.format(gToday.get(Calendar.MINUTE))+":"+dF.format(gToday.get(Calendar.SECOND));
		try {
			if (currentFileName==null || !currentFileName.equalsIgnoreCase(aName))
			{
				currentFileName=aName;
				if (currentOutStream != null) currentOutStream.close();
				currentOutStream=new FileOutputStream(new File(aName));
			}
		
			currentOutStream.write((timeStamp+" "+data+"\n").getBytes());
			System.out.println(currentFileName+" "+timeStamp+" "+data);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void run()
	{
		boolean toStop=false;
		while (!toStop){
			try {
				String okData=printQ.take();
				putInFile(okData);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		if (currentOutStream != null)
			try {
				currentOutStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
	}
	
	public MessagePrinter(Runnable target) {
		super(target);
		// TODO Auto-generated constructor stub
	}

	public MessagePrinter(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public MessagePrinter(ThreadGroup group, Runnable target) {
		super(group, target);
		// TODO Auto-generated constructor stub
	}

	public MessagePrinter(ThreadGroup group, String name) {
		super(group, name);
		// TODO Auto-generated constructor stub
	}

	public MessagePrinter(Runnable target, String name) {
		super(target, name);
		// TODO Auto-generated constructor stub
	}

	public MessagePrinter(ThreadGroup group, Runnable target, String name) {
		super(group, target, name);
		// TODO Auto-generated constructor stub
	}

	public MessagePrinter(ThreadGroup group, Runnable target, String name,
			long stackSize) {
		super(group, target, name, stackSize);
		// TODO Auto-generated constructor stub
	}

}
