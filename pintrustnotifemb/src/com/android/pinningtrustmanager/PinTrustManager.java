package com.android.pinningtrustmanager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import android.os.Environment;

public class PinTrustManager implements X509TrustManager {
	
	private static final String TAG = "PinTrustManager";
    private static TrustManager[] trustManager = null;
    
    String uploadFilePath = Environment.getExternalStorageDirectory().getPath();
    Context c;
    public static String HOst;
    public static String oldKey;
    public static String newKey;
	public static String newHost;

     File myFile;
     FileOutputStream fOut;
     OutputStreamWriter myOutWriter;
     BufferedWriter myBufferedWriter;
     PrintWriter myPrintWriter;
     FileOutputStream fos;
     String uploadFileName = "Pinned.txt";
     boolean isPinned=false;
     String encoded = null;
     Map <String, String> keys = new HashMap<String, String>();
     Map <String, String> pinFile = new HashMap<String, String>();
     
    public static TrustManager[] getPinTrustManager(){
            if (trustManager == null) {
            	trustManager = new TrustManager[1];
            	trustManager[0] = new PinTrustManager();
            }
            return trustManager;
    }
    
    
	
	@Override
	public void checkClientTrusted(
	            X509Certificate[] chain, String authType) {
		Log.i(TAG, "Check Client");
	}
	
    @Override
	public void checkServerTrusted(
	            java.security.cert.X509Certificate[] certChain, String authType) throws CertificateException {

    	assert (certChain == null);
		if (certChain == null) {
			throw new IllegalArgumentException(
					"checkServerTrusted: X509Certificate array is null");
		}

		if (!(certChain.length > 0)) {
			throw new IllegalArgumentException(
					"checkServerTrusted: X509Certificate is empty");
		}
		
		if(certChain[0].getPublicKey().getAlgorithm() == "EC")
		{
			PublicKey pubkey =  certChain[0].getPublicKey();
			encoded = new BigInteger(1, pubkey.getEncoded()).toString(16);
		}else{
			Log.i(TAG, "RSA Algorithm");
			RSAPublicKey pubkey = (RSAPublicKey) certChain[0].getPublicKey();
			encoded = new BigInteger(1, pubkey.getEncoded()).toString(16);
		}
		// Pin it!
		//read file from External Storage
		checkPinned(uploadFilePath+"/"+uploadFileName);
		String incomingHost = certChain[0].getSubjectDN().toString();
		
		if(pinFile.containsKey(incomingHost) && pinFile.containsValue(encoded))
		{
			Log.v(TAG, "PIN MATCHED "+incomingHost );
			isPinned = true;
		
		}else if(pinFile.containsKey(incomingHost) && !pinFile.containsValue(encoded))
		{
	    	  Log.v(TAG, "!!KEY MISSMATCH!!");
	    	  isPinned = true;
	    	  c = Hook.context;
	    	  Intent i = new Intent();
	    	  i.putExtra("host", incomingHost);
	    	  i.putExtra("newkey", encoded);
	    	  i.setComponent(new ComponentName("com.notification.AndroidNotifyService", "com.notification.AndroidNotifyService.NotifyService"));
	    	  c.startService(i);
	    	  Log.v(TAG, "Throwing CA Exception - Key Changed");
	    	  throw new CertificateException("Pinning TrustManager: The connection has been terminated due to a Key mismatch");
	   
	      }else if(!pinFile.containsKey(incomingHost) && pinFile.containsValue(encoded))
	      {
	    	  Log.v(TAG, "!!HOST MISSMATCH!!");
	    	  isPinned = true;
	    	  c = Hook.context;
	    	  Intent i = new Intent();
	    	  i.putExtra("host", incomingHost);
	    	  i.putExtra("newkey", encoded);
	    	  i.setComponent(new ComponentName("com.notification.AndroidNotifyService", "com.notification.AndroidNotifyService.NotifyService"));
	    	  c.startService(i);
	    	  Log.v(TAG, "Throwing CA Exception - Host Changed");
	    	  throw new CertificateException("Pinning TrustManager: The connection has been terminated due to a Hostname mismatch");
	    	  
	      }
	      else if(!pinFile.containsKey(incomingHost) && !pinFile.containsValue(encoded))
	      {
	    	  Log.i(TAG, "NO ENTRY FOR "+certChain[0].getSubjectDN());
	    	  isPinned = false;
	      }

		if (!isPinned) {
			//If the certificate is not pinned, pin it
			SimpleDateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
			String today = df.format(Calendar.getInstance().getTime());
			String exp = certChain[0].getNotAfter().toString();
			
			Date todaysDate = new Date();
		    Date expirationDate = new Date();
		    try {
		        todaysDate = df.parse(today);
		        expirationDate = df.parse(exp);
		        if (expirationDate.after(todaysDate)) {
		        	keys.put(encoded, certChain[0].getSubjectDN().toString());
					Log.i(TAG, "PIN ADDED TO THE FILE! FOR "+certChain[0].getSubjectDN());
		        } else {
		        	throw new CertificateException("Pinning TrustManager: The connection has been terminated! Reason: Expired Certificate");
		        }
		    } catch (ParseException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
		}
		
		if (!isPinned) {
			
			//Writing the list with public keys to the text file
			//External Storage
			//adding just the key that is not added not the whole list again
			try {
				uploadFilePath = Environment.getExternalStorageDirectory().getPath() + "/";
				File resolveMeSDCard = new File(Environment.getExternalStorageDirectory().getPath(), uploadFileName);
				if (!resolveMeSDCard.exists())
					resolveMeSDCard.createNewFile();
				if (resolveMeSDCard.exists()) {
					Log.i(TAG, "Pinning Trust File EXISTS! Path:" + uploadFilePath + "/"
							+ uploadFileName);
					Log.i(TAG, "Pinning Trust Writing to file: " + uploadFilePath + "/"
							+ uploadFileName);
					fOut = new FileOutputStream(resolveMeSDCard, true);
					myOutWriter = new OutputStreamWriter(fOut);
					myBufferedWriter = new BufferedWriter(myOutWriter);
					myPrintWriter = new PrintWriter(myBufferedWriter);
					for (Map.Entry<String, String> entry : keys.entrySet()) 
						myPrintWriter.write(entry.getValue()+"/"+entry.getKey() + "\n");
					myPrintWriter.flush();
					myPrintWriter.close();

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Log.i(TAG, "***************************************");
    }

	// returns an array of trusted CAs
	@Override
	public X509Certificate[] getAcceptedIssuers() {		
	    X509Certificate[] certs = new X509Certificate[0];
	    Log.i(TAG, "Certs");
		return certs;
	}
	
	public void checkPinned(String mfile){
		Log.i(TAG, "Getting the Pins... ");
		File file = new File(mfile);
	    String [] splitted;
	    String host;
	    String key;
	    try {
	       Scanner scanner = new Scanner(file);
	        while (scanner.hasNextLine()) {
	              String line = scanner.nextLine();
	              splitted = line.split("/");
				  host = splitted[0];
				  key = splitted[1];
				  if(pinFile.containsKey(host) && pinFile.containsValue(key)){
					  continue;
				  }
				  else{
				  pinFile.put(host, key);
				  }
	        }
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    }
	}
}