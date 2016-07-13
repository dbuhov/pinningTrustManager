package com.notification.AndroidNotifyService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.notification.AndroidNotifyService.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class NotificationReceiver extends Activity{
	
	String uploadFileName = "Pinned.txt";
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
     
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		pin = (Button)findViewById(R.id.button1);
 		tv = (TextView)findViewById(R.id.tv1);
 		tv.setMovementMethod(new ScrollingMovementMethod());
 		
 		hostTmp = getIntent().getStringExtra("host");
 		newKey = getIntent().getStringExtra("newkey");
 		findHost();
 		pinListener();
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
    	 File myFile = new File(uploadFilePath+"/"+uploadFileName);
    	 try {
    		 Scanner scanner = new Scanner(myFile);
    		 while (scanner.hasNextLine()) {
    			 String line = scanner.nextLine();
    			 splitted = line.split("/");
    			 host = splitted[0];
    			 key = splitted[1];
    			 hkeys.put(key, host);
    			 if(host.equals(hostTmp) && !key.equals(newKey)){
    				 tv.setText(Html.fromHtml("<b><font color=\"red\">The Key has changed!</font></b><br />"));
				 	 tv.append(Html.fromHtml("<b> <font color=\"yellow\">Pinned Host: </font></b>"+host+"<br />"));
				 	 tv.append(Html.fromHtml("<b><font color=\"yellow\">Pinned key: </font></b>"+key+"<br />"));
				 	 tv.append(Html.fromHtml("<b><font color=\"red\">New key: </font></b>"+newKey+"<br />"));
    			 }else if(!host.equals(hostTmp) && key.equals(newKey)){
    				 tv.setText(Html.fromHtml("<b><font color=\"red\">The Hostname has changed!</font></b>"+"<br />"));
    				 tv.append(Html.fromHtml("<b><font color=\"yellow\">Pinned Host: </font></b>"+host+"<br />"));
    				 tv.append(Html.fromHtml("<b><font color=\"yellow\">Pinned key: </font></b>"+key+"<br />"));
    				 tv.append(Html.fromHtml("<b><font color=\"red\">New Hostname: </font></b>"+hostTmp+"<br />"));
				  }			    		  
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
     }
     
     public void updateHost(){
    	 try {
			uploadFilePath = Environment.getExternalStorageDirectory()
						.getPath() + "/";
				File resolveMeSDCard = new File(Environment
						.getExternalStorageDirectory().getPath(),
						uploadFileName);
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