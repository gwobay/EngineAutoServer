package com.intelligent7.autoengineserver;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;

//import com.kou.utilities.ChatMaster;
//import com.kou.utilities.MainServer;

public class GcmHttpCommander {
	
	
	//private static final Executor threadPool = Executors.newFixedThreadPool(5);

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
	  
	MainServer mServer; //for receive command from commander
	//SendGcmMessager mGcmSender; //for message to GCM
	ChatMaster mChatMaster; 
	void init()
	{
		//mGcmSender=new SendGcmMessager();
		
		readFromResourceFile("CommanderResource", config);
		mServer=new MainServer();
		String sPort=config.get("CMD_PORT");
		if (sPort == null) System.exit(-1);
		mServer.setPort(Integer.parseInt(sPort));
		mServer.setStopTime(0);
		//mServer.addFIXDataSniffer(mGcmSender);
		mServer.start();
		mServer=new MainServer();
		sPort=config.get("CHAT_PORT");
		if (sPort != null) {
			if (mChatMaster==null) mChatMaster=new ChatMaster();
			mChatMaster.setPort(Integer.parseInt(sPort));
			mChatMaster.setStopTime(0);
			mChatMaster.start();
		}
		//mGcmSender=new SendGcmMessager();
		//mGcmSender.putInstruction("test line", "|170=agenda|151=2014-07-21|152=21:20|153=11çœ‹é€�æ–°è�ž19|154=ä¸­æ­£å�€å¿ å­�è¥¿è·¯1æ®µ72è™Ÿ62æ¨“|155=å�°åŒ—å¸‚|157=0986055745|156=é„­å¼˜å„€|158=ã€Œä½†æ˜¯æˆ‘å€‘çœŸçš„ä¸�è¦�å°�çœ‹åœ‹æ°‘é»¨ï¼Œç•¢ç«Ÿæ˜¯ç™¾å¹´è€�åº—ï¼Œå®ƒåœ¨åŸºå±¤çš„çµ„ç¹”å…¶å¯¦æ˜¯æ»¿å®Œæ•´çš„ï¼Œå�ªè¦�è³‡æº�ä¸‹åŽ»ï¼Œå®ƒçš„å‹•å“¡æ˜¯é�žå¸¸å¿«çš„ã€�ã€‚é ˆä»¥ä¸€å€‹è¬¹æ…Žæ…‹åº¦ä¾†é�¢å°�äºŒâ—‹ä¸€å››çš„é�¸èˆ‰ã€‚");
		//mGcmSender.putInstruction("test line", "|170=agenda|151=2014-07-22|152=19:20|153=22æ–°ä¸‰æ°‘ä¸»ç¾©19ï¼šåº¶æ°‘ã€�é„‰æ°‘ã€�å…¬æ°‘|154=ä¸­æ­£å�€å¿ å­�è¥¿è·¯1æ®µ72è™Ÿ62æ¨“|155=å�°åŒ—å¸‚|157=0946056745|156=æŸ¯æ–‡å“²|158=ã€Œç‚ºæ­¤ç©�æ¥µå‚™æˆ°ï¼Œè¿‘æ—¥å•Ÿå‹•é�’å¹´æµ·é�¸è¨ˆç•«å�¬å‹Ÿç«¶é�¸åœ˜éšŠäººæ‰�ï¼Œä¸¦æ��å‡ºåº¶æ°‘ä¸»ç¾©ã€�é„‰æ°‘ä¸»ç¾©ã€�å…¬æ°‘ä¸»ç¾©çš„ã€Œæ–°ä¸‰æ°‘ä¸»ç¾©ã€�ï¼Œç›¼é€�é�Žç¶²è·¯å�‘å¹´è¼•ä¸–ä»£æ‹›æ‰‹ã€‚ã€‚");
		//mGcmSender.putInstruction("test line", "|170=agenda|151=2014-07-23|152=17:20|153=33èˆ‰è¾¦æµ·é�¸é�¢è©¦19|154=ä¸­æ­£å�€å¿ å­�è¥¿è·¯1æ®µ72è™Ÿ62æ¨“|155=å�°åŒ—å¸‚|157=0986066745|156=æŸ¯æ–‡å“²|158=ã€Œä½†æ˜¯æˆ‘å€‘çœŸçš„ä¸�è¦�å°�çœ‹åœ‹æ°‘é»¨ï¼Œç•¢ç«Ÿæ˜¯ç™¾å¹´è€�åº—ï¼Œå®ƒåœ¨åŸºå±¤çš„çµ„ç¹”å…¶å¯¦æ˜¯æ»¿å®Œæ•´çš„ï¼Œå�ªè¦�è³‡æº�ä¸‹åŽ»ï¼Œå®ƒçš„å‹•å“¡æ˜¯é�žå¸¸å¿«çš„ã€�ã€‚é ˆä»¥ä¸€å€‹è¬¹æ…Žæ…‹åº¦ä¾†é�¢å°�äºŒâ—‹ä¸€å››çš„é�¸èˆ‰ã€‚");
		//mGcmSender.putInstruction("test line", "|170=agenda|151=2014-07-24|152=19:20|153=44çœ‹é€�æ–°è�ž19|154=ä¸­æ­£å�€å¿ å­�è¥¿è·¯1æ®µ72è™Ÿ62æ¨“|155=å�°åŒ—å¸‚|157=0986056845|156=é„­å¼˜å„€|158=ã€Œä½†æ˜¯æˆ‘å€‘çœŸçš„ä¸�è¦�å°�çœ‹åœ‹æ°‘é»¨ï¼Œç•¢ç«Ÿæ˜¯ç™¾å¹´è€�åº—ï¼Œå®ƒåœ¨åŸºå±¤çš„çµ„ç¹”å…¶å¯¦æ˜¯æ»¿å®Œæ•´çš„ï¼Œå�ªè¦�è³‡æº�ä¸‹åŽ»ï¼Œå®ƒçš„å‹•å“¡æ˜¯é�žå¸¸å¿«çš„ã€�ã€‚é ˆä»¥ä¸€å€‹è¬¹æ…Žæ…‹åº¦ä¾†é�¢å°�äºŒâ—‹ä¸€å››çš„é�¸èˆ‰ã€‚");
		//System.out.println("Sending @ "+Calendar.getInstance().getTime());
		//mGcmSender.start();
	}
	/*
	ç¶ ç‡Ÿå�°åŒ—å¸‚é•·åˆ�é�¸ï¼Œæ°‘é€²é»¨ç«‹å§”å§šæ–‡æ™ºèˆ‡ç„¡é»¨ç±�å�°å¤§é†«å¸«æŸ¯æ–‡å“²6æœˆ12æ—¥å°‡ä»¥æ°‘èª¿æ±ºå‹�è² ã€‚
	å§šã€�æŸ¯æ˜¨å�”èª¿é�”æˆ�å…±è­˜ï¼Œæ°‘èª¿å‰�å°‡åœ¨8å¤©å…§èˆ‰è¾¦3å ´è¾¯è«–å�Šæ”¿è¦‹æœƒï¼Œ
	å�°åŒ—å¸‚é•·äººé�¸å°‡åœ¨å‰�ä¸»å¸­è”¡è‹±æ–‡å›žé�‹é»¨é­�å¾Œå®Œæˆ�æ��å��ã€‚

	å�°åŒ—å¸‚é•·å�ƒé�¸äººå�°å¤§é†«å¸«æŸ¯æ–‡å“²ï¼Œæ˜¨å‰�å¾€å£«æ�±å¸‚å ´æ‹œç¥¨ï¼Œä»–è¡¨ç¤ºï¼Œ
	æ°‘çœ¾çš„é£Ÿè¡£ä½�è¡Œè‚²æ¨‚ã€Œæ¯”èŠ±å�šå’Œä¸–å¤§é�‹é‡�è¦�ã€�ï¼Œè‹¥ç•¶é�¸å¸‚é•·ï¼Œæœƒå„ªå…ˆç·¨é �ç®—æ”¹å»ºæ‰€æœ‰å…¬æœ‰å¸‚å ´ã€‚

	ã€ŒæŸ¯Pä¾†å›‰ï¼�æŸ¯Pä¾†å›‰ï¼�ã€�æ˜¨å¤©ä¸Šå�ˆå£«æ�±å¸‚å ´äººè�²é¼Žæ²¸ï¼ŒæŸ¯æ–‡å“²åˆ°å ´å¾Œä¸€æ”¤æ”¤æ�¡æ‰‹è‡´æ„�
	ï¼Œä¸€é‚Šè§€å¯Ÿå¸‚å ´è¨­å‚™ï¼Œæœ‰è²·è�œæ°‘çœ¾è¦�æ±‚å�ˆç…§ï¼Œä¹Ÿæœ‰äººç¬‘è‘—å’Œä»–èªªã€Œæœ¬äººæ¯”è¼ƒè‹±ä¿Šå–”ã€�ã€‚

	æŸ¯æ–‡å“²èªªï¼Œç•¶äº†å»¿å¹¾å¹´å¤–ç§‘é†«å¸«ï¼Œå€‹æ€§è®Šå¾—å¾ˆæ€¥ï¼Œã€Œå�šä¸€ä»¶äº‹å°±æƒ³åˆ°ä¸‹ä¸€æ­¥ã€�ï¼Œ
	æ­£åœ¨åŠªåŠ›æ”¹é€²ï¼Œåƒ�æ˜¯æ�¡æ‰‹æ™‚è¦�çœ‹è‘—å°�æ–¹çœ¼ç�›ï¼Œä¸�èƒ½å�·çœ‹åˆ¥è™•ï¼Œå°±æ˜¯è¦�è¨“ç·´çš„ã€ŒåŸºæœ¬åŠŸã€�ã€‚

	è¨±å¤šå¸‚å ´æ”¤å•†é¼“å‹µæŸ¯æ–‡å“²åŠ æ²¹ï¼Œä»–è¡¨ç¤ºï¼Œå�ƒè§€é�Žè¨±å¤šå‚³çµ±å¸‚å ´ï¼Œç’°å¢ƒéƒ½å¾ˆé«’äº‚ï¼Œ
	è‹¥ç•¶é�¸å¸‚é•·ï¼Œç·¨åˆ—é �ç®—æ™‚ï¼Œã€Œä¸�æœƒåŽ»è¾¦èŠ±å�šã€�ï¼Œè€Œæ˜¯å„ªå…ˆæ”¹å»ºæ‰€æœ‰å…¬æœ‰å¸‚å ´ã€‚

	
	*/

	void joinThreads()
	{
		try {
		if (mServer != null) mServer.join();
		//if (mGcmSender != null) mGcmSender.join();
		} catch (InterruptedException e){
			e.printStackTrace();
		}
		
		mServer.setStopTime(1);
		//mGcmSender.setStopTime(1);
	}

	public static void main(String[] args) {
		GcmHttpCommander aCmd=new GcmHttpCommander();
		aCmd.init();
		aCmd.joinThreads();		
	}

}
