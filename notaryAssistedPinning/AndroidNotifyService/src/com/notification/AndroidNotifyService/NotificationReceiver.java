package com.notification.AndroidNotifyService;

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
import java.util.HashMap;
import java.util.Map;

import com.notification.AndroidNotifyService.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class NotificationReceiver extends Activity{
	
	String uploadFileName = "Pinned.txt";
//	String uploadFileNameTMP = "TMPPinned.txt";
	 String uploadFilePath = Environment.getExternalStorageDirectory().getPath();
	 private static final String TAG = "NotificationP";
	 FileOutputStream fOut;
     OutputStreamWriter myOutWriter;
     BufferedWriter myBufferedWriter;
     PrintWriter myPrintWriter;
     FileOutputStream fos;
     String line;
     String hostTmp;
     String key;
     String Tkey;
     String newKey;
     String host;
     String [] splitted;
     Map <String, String> hkeys = new HashMap<String, String>();
     Button pin;
     Button cancel;
     TextView tv;
     String sigAlg;
     String issuer;
     
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		pin = (Button)findViewById(R.id.button1);
 		tv = (TextView)findViewById(R.id.tv1);
 		tv.setMovementMethod(new ScrollingMovementMethod());
// 		tv.setGravity(Gravity.CENTER_HORIZONTAL);
 		
 		tv.setText(Html.fromHtml("<b> NOTARY ALERT! </b>" +  "<br />"));
 		tv.append(Html.fromHtml("<small> <b> ISCI Notary cannot determine the origin of the Certificate! </b> </small> <br />"));
 		tv.append(Html.fromHtml("<b>Details:</b>"));
 		tv.append("\n\n");
 		
 		hostTmp = getIntent().getStringExtra("host");
 		newKey = getIntent().getStringExtra("newkey");
 		sigAlg = getIntent().getStringExtra("sigAlg");
 		issuer = getIntent().getStringExtra("issuer");
 		String expires = getIntent().getStringExtra("expires");
 		String created = getIntent().getStringExtra("created");
 		
 		tv.append(Html.fromHtml("<b>Issued for:</b> <small>"+hostTmp+"</small><br />"));
 		tv.append(Html.fromHtml("<b>Signature: </b> <small>"+newKey+"</small> <br />"));
		tv.append(Html.fromHtml("<b>Signature Algorithm: </b> <small> "+sigAlg+"</small> <br />"));
		tv.append(Html.fromHtml("<b>Issued by: </b><small>"+issuer+"</small><br />"));
		tv.append(Html.fromHtml("<b>Created on: </b><small>"+created+"</small><br />"));
		tv.append(Html.fromHtml("<b>Expires on: </b><small>"+expires+"</small><br />"));
		
// 		Log.i(TAG, "HOSTTMP "+hostTmp);	 Log.i(TAG, "NEWKEY "+newKey);
// 		findHost();
 		Log.i(TAG, "NOTIFICATION BODY CREATED ");
// 		pinListener();
 	}
 	
 	public void pinListener() {

		pin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				updateHost();
				Toast.makeText(getApplicationContext(), "The data has been updated", Toast.LENGTH_LONG).show();;
			}
		});
	}
 	
     public void findHost(){
    	 Log.i(TAG, "findHOST ");
     File myFile = new File(uploadFilePath+"/"+uploadFileName);
		FileInputStream fIn;
		try {
			File resolveMeSDCard = new File(Environment.getExternalStorageDirectory().getPath(),uploadFileName);
			fOut = new FileOutputStream(resolveMeSDCard, true);
			fIn = new FileInputStream(myFile);
			BufferedReader rd = new BufferedReader(new InputStreamReader(fIn));
			while ((line = rd.readLine()) != null ){
				  splitted = line.split("/");
				  host = splitted[0];
				  key = splitted[1];
				  hkeys.put(key, host);
				  Log.i(TAG, "HOST:"+host+" key:"+key);
				  if(host.equals(hostTmp) && !key.equals(newKey)){
					  tv.append("The Key has changed!"+"\n");
				 		tv.append("Host: "+host+"\n");
				 		tv.append("Pinned key: "+key+"\n");
				 		tv.append("New key: "+newKey+"\n");
				  } else if(!host.equals(hostTmp) && key.equals(newKey)){
					  Log.i(TAG, "HOST MISMATCH!!");
					  tv.append("The Hostname has changed!"+"\n");
				 		tv.append("Pinned Host: "+host+"\n");
				 		tv.append("Pinned key: "+key+"\n");
				 		tv.append("New Hostname: "+hostTmp+"\n");
				  }			    		  
			}
			rd.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
     }
     
     public void updateHost(){
    	 try {
			uploadFilePath = Environment.getExternalStorageDirectory()
						.getPath() + "/";
				File resolveMeSDCard = new File(Environment
						.getExternalStorageDirectory().getPath(),
						uploadFileName);
			 Log.i(TAG, "Notif P File CREATED! Path:" + uploadFilePath + "/"
						+ uploadFileName);
				Log.i(TAG, "NOtif P Writing to file: " + uploadFilePath + "/"
						+ uploadFileName);
				fOut = new FileOutputStream(resolveMeSDCard);
				myOutWriter = new OutputStreamWriter(fOut);
				myBufferedWriter = new BufferedWriter(myOutWriter);
				myPrintWriter = new PrintWriter(myBufferedWriter);
				for (Map.Entry<String, String> entry : hkeys.entrySet()) 
				{
					Log.i(TAG,"HOSTS "+ entry.getValue());
					if(entry.getValue().contains(hostTmp) && !entry.getKey().equals(newKey)){
						myPrintWriter.write(entry.getValue()+"/"+newKey + "\n");
						Log.i(TAG,"KEY IS CHANGED: "+ entry.getValue()+"---->"+newKey+"---->"+entry.getKey());
					} else if(!entry.getValue().contains(hostTmp) && entry.getKey().equals(newKey)){
						myPrintWriter.write(hostTmp+"/"+newKey + "\n");
						Log.i(TAG,"HOST IS CHANGED: "+ entry.getValue()+"---->"+hostTmp+"---->"+entry.getKey());
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