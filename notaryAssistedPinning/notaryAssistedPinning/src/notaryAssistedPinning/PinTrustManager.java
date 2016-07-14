package notaryAssistedPinning;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public String certHash;
	public String certHashCheck;
	public String notaryResponse;

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
     List<String> pinList = new ArrayList<String>();
     Map <String, String> hkeys = new HashMap<String, String>();

    
     
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
    	Log.i(TAG, "ChekServer");
    	Log.i(TAG,"AuthType -> " +authType);
    	
    	
    	
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
			Log.i(TAG, "EC Algorithm");
			PublicKey pubkey =  certChain[0].getPublicKey();
			encoded = new BigInteger(1, pubkey.getEncoded()).toString(16);
		}else{
			Log.i(TAG, "RSA Algorithm");
			RSAPublicKey pubkey = (RSAPublicKey) certChain[0].getPublicKey();
			encoded = new BigInteger(1, pubkey.getEncoded()).toString(16);
		}
		/*** getting the hash ***/
		try {
			certHash = getThumbPrint(certChain[0]);
			certHashCheck = getThumbPrint(certChain[0])+".notary.icsi.berkeley.edu";
			Log.i(TAG, "Request: "+certHashCheck);

		} catch (NoSuchAlgorithmException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		/*** end of hash***/
		
		/*** Pin it!
		 * read file from Exrternal Storage
		 ***/
		try {
			File myFile = new File(uploadFilePath+"/"+uploadFileName);
			FileInputStream fIn = new FileInputStream(myFile);
			BufferedReader rd = new BufferedReader(new InputStreamReader(fIn));
			Log.i(TAG, "Getting the pins from the file: "+uploadFilePath+"/"+uploadFileName);
			Log.i(TAG, " Received Cert Chain:"+certChain[0].getSubjectDN()); 
			String hostTmp = certChain[0].getSubjectDN().toString();
			String [] splitted;
			String host;
			String key;
					  String line;
					  while ((line = rd.readLine()) != null ){
						  splitted = line.split("/");
						  host = splitted[0];
						  key = splitted[1];
					  if(host.equals(hostTmp) && key.equals(certHash)){
					    	  Log.i(TAG, "PIN MATCHED! "+certChain[0].getSubjectDN());    
							  Log.i(TAG, "Pin: "+line);
							  isPinned = true;
						         break;
					      }else if(host.equals(hostTmp) && !key.equals(certHash))
					      {
					    	  notaryResponse = run("nslookup "+certHashCheck);
					    	  Log.i(TAG,"RESPONSE: "+notaryResponse);
					    	  String bingo = notaryResponse.substring(notaryResponse.length()-10);
					    	  Log.i(TAG,"BINGO: " +bingo);
					    	  if(bingo.contains("127.0.0.2")){
					    		  Log.v(TAG,"CERTIFICATE IS VALID!"+certChain[0].getSubjectDN().toString());
					    		  HOst = certChain[0].getSubjectDN().toString();
					    		  isPinned = true;
					    		  updateHost();
					    		  break;
					    	  }
					    	  else{
					    		  isPinned = true;
					    		  Log.i(TAG, "The key for "+certChain[0].getSubjectDN() + " has changed!!");
					    		  HOst = certChain[0].getSubjectDN().toString();
					    		  oldKey = key;
					    		  newKey = certHash;
					    		  c = Hook.context;
					    		  Intent i = new Intent();
					    		  i.putExtra("host", certChain[0].getSubjectDN().toString());
							    	i.putExtra("newkey", certHash);
							    	i.putExtra("sigAlg", certChain[0].getSigAlgName());
							    	i.putExtra("issuer", certChain[0].getIssuerDN().toString());
							    	i.putExtra("created", certChain[0].getNotBefore().toString());
							    	i.putExtra("expires", certChain[0].getNotAfter().toString());
					    		  i.setComponent(new ComponentName("com.notification.AndroidNotifyService", "com.notification.AndroidNotifyService.NotifyService"));
					    		  c.startService(i);
					    		  Log.v(TAG, "Throwing CA Exception");
					    		  throw new CertificateException("Pinning TrustManager: The connection has been terminated due to a Key Missmatch");
					    	  }
					   
					      }else if(!host.equals(hostTmp) && key.equals(certHash))
					      {
					    	  	notaryResponse = run("nslookup "+certHashCheck);
					    	  	Log.i(TAG,"RESPONSE: "+notaryResponse);
					    	  	String bingo = notaryResponse.substring(notaryResponse.length()-10);
								Log.i(TAG,"BINGO: " +bingo);
								if(bingo.contains("127.0.0.2")){
									Log.v(TAG,"CERTIFICATE IS VALID!" + certChain[0].getSubjectDN().toString());
									HOst = certChain[0].getSubjectDN().toString();
									isPinned = true;
									updateHost();
									break;
								} 
								else{
									isPinned = true;
									Log.i(TAG, host+" The host assigned to the key "+key + " has changed!! NEW HOST: "+hostTmp);
									HOst = certChain[0].getSubjectDN().toString();
									oldKey = key;
									newHost = hostTmp;
									c = Hook.context;
									Intent i = new Intent();
									i.putExtra("host", certChain[0].getSubjectDN().toString());
							    	i.putExtra("newkey", certHash);
							    	i.putExtra("sigAlg", certChain[0].getSigAlgName());
							    	i.putExtra("issuer", certChain[0].getIssuerDN().toString());
							    	i.putExtra("created", certChain[0].getNotBefore().toString());
							    	i.putExtra("expires", certChain[0].getNotAfter().toString());
									i.setComponent(new ComponentName("com.notification.AndroidNotifyService", "com.notification.AndroidNotifyService.NotifyService"));
									c.startService(i);
									Log.v(TAG, "Throwing CA Exception");
									throw new CertificateException("Pinning TrustManager: The connection has been terminated due to a Hostname Missmatch");
								}
					    	  
					      }else if(!host.equals(hostTmp) && !key.equals(certHash))
					      {
					    	  Log.i(TAG, "NO ENTRY FOR "+certChain[0].getSubjectDN());
					    	  isPinned = false;

					      }
					  }
					  rd.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			Log.i(TAG, "No file "+uploadFilePath+"/"+uploadFileName);
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (!isPinned) {

			/*** Notary Check 
			 * 
			 * If the certificate is not pinned, pin it
			 * 
			 * ***/
			
			notaryResponse = run("nslookup "+certHashCheck);
			Log.i(TAG,"RESPONSE: "+notaryResponse);
			String bingo = notaryResponse.substring(notaryResponse.length()-10);
			notaryResponse = run("nslookup "+certHashCheck);
			Log.i(TAG,"RESPONSE: "+notaryResponse);

			if(bingo.contains("127.0.0.2")){
				Log.v(TAG,"CERTIFICATE IS VALID!"+certChain[0].getSubjectDN().toString());
				keys.put(certHash, certChain[0].getSubjectDN().toString());
				Log.i(TAG, "PIN ADDED TO THE FILE! FOR "+certChain[0].getSubjectDN());
			}
			else {
				c = Hook.context;
		    	Intent i = new Intent();
		    	i.putExtra("host", certChain[0].getSubjectDN().toString());
		    	i.putExtra("newkey", certHash);
		    	i.putExtra("sigAlg", certChain[0].getSigAlgName());
		    	i.putExtra("issuer", certChain[0].getIssuerDN().toString());
		    	i.putExtra("created", certChain[0].getNotBefore().toString());
		    	i.putExtra("expires", certChain[0].getNotAfter().toString());
		    	i.setComponent(new ComponentName("com.notification.AndroidNotifyService", "com.notification.AndroidNotifyService.NotifyService"));
		    	c.startService(i);
	    		Log.v(TAG, certHash);

		    	Log.v(TAG, "Throwing CA Exception");
		    	throw new CertificateException("Pinning TrustManager: The connection has been terminated due to an invalid Certificate");
			}
			
			
		}
		
		if (!isPinned) {
			
			/***
			 * Writing the list with public keys to the text file
			 * External Storage
			 * adding just the key that is not added not the whole list again
			***/
			try {
				uploadFilePath = Environment.getExternalStorageDirectory()
						.getPath() + "/";
				File resolveMeSDCard = new File(Environment
						.getExternalStorageDirectory().getPath(),
						uploadFileName);
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
	
   public String getThumbPrint(Certificate cert) 
	        throws NoSuchAlgorithmException, CertificateEncodingException {
			   		MessageDigest md = MessageDigest.getInstance("SHA-1");
			        byte[] der = cert.getEncoded();
			        md.update(der);
			        byte[] digest = md.digest();
			        return hexify(digest);
	    }

   public String hexify (byte bytes[]) {

	        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', 
	                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	        StringBuffer sbf = new StringBuffer(bytes.length * 2);

	        for (int i = 0; i < bytes.length; ++i) {
	            sbf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
	            sbf.append(hexDigits[bytes[i] & 0x0f]);
	        }

	        return sbf.toString();
	    }
   
   public String run(String command) {

		StringBuilder sb = new StringBuilder();
		BufferedReader reader = null;
		Process p;

		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			reader = new BufferedReader(
				new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}
   
   public void updateHost(){
  	 try {
  		 String [] splitted;
  		 String Fkey;
  		 String Fhost;
  		 String line;
 
		try {
			uploadFilePath = Environment.getExternalStorageDirectory().getPath() + "/";
			File myFile = new File(uploadFilePath+"/"+uploadFileName);
	  		 FileInputStream fIn;
			 File resolveMeSDCard = new File(Environment.getExternalStorageDirectory().getPath(),uploadFileName);
			 fOut = new FileOutputStream(resolveMeSDCard, true);
			 fIn = new FileInputStream(myFile);
			 BufferedReader rd = new BufferedReader(new InputStreamReader(fIn));
			 while ((line = rd.readLine()) != null ){
				 splitted = line.split("/");
				 Fhost = splitted[0];
				 Fkey = splitted[1];
				 hkeys.put(Fkey, Fhost);	
			 }
			 
			 rd.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			File resolveMeSDCard = new File(Environment.getExternalStorageDirectory().getPath(),uploadFileName);
			 Log.i(TAG, "UPDATE HOST Path:" + uploadFilePath + "/"
						+ uploadFileName);
				Log.i(TAG, "UPDATE HOST Writing to file: " + uploadFilePath + uploadFileName);
				fOut = new FileOutputStream(resolveMeSDCard);
				myOutWriter = new OutputStreamWriter(fOut);
				myBufferedWriter = new BufferedWriter(myOutWriter);
				myPrintWriter = new PrintWriter(myBufferedWriter);
				for (Map.Entry<String, String> entry : hkeys.entrySet()) 
				{
					if(entry.getValue().equals(HOst) && !entry.getKey().equals(certHash)){
						myPrintWriter.write(entry.getValue()+"/"+certHash + "\n");
						Log.i(TAG,"UPDATEKEY IS CHANGED: "+ entry.getValue()+"---->"+certHash+"---->"+entry.getKey());
						Log.i(TAG, entry.getValue()+" -- "+HOst);
					} else if(!entry.getValue().equals(HOst) && entry.getKey().equals(certHash)){
						myPrintWriter.write(HOst+"/"+certHash + "\n");
						Log.i(TAG,"UPDATEHOST IS CHANGED: "+ entry.getValue()+"---->"+HOst+"---->"+entry.getKey());
					}
					else
						myPrintWriter.write(entry.getValue()+"/"+entry.getKey() + "\n");
				}
				myPrintWriter.flush();
				myPrintWriter.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}