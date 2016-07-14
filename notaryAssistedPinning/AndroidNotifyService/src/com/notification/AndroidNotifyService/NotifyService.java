package com.notification.AndroidNotifyService;

import java.security.Certificate;
import java.security.cert.X509Certificate;

import com.notification.AndroidNotifyService.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class NotifyService extends Service {
	
	private static final String TAG = "NotifyService";
	final static String ACTION = "NotifyServiceAction";
	final static String STOP_SERVICE = "";
	final static int RQS_STOP_SERVICE = 1;
	
	NotifyServiceReceiver notifyServiceReceiver;
	
	private static final int MY_NOTIFICATION_ID=1;
	private NotificationManager notificationManager;
	private Notification myNotification;
	String h;
	String k;
	String sigAlg;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		notifyServiceReceiver = new NotifyServiceReceiver();
		super.onCreate();
	}

	@SuppressWarnings("deprecation")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION);
		registerReceiver(notifyServiceReceiver, intentFilter);
		h = intent.getStringExtra("host");
		k = intent.getStringExtra("newkey");
		sigAlg = intent.getStringExtra("sigAlg");
		String issuer = intent.getStringExtra("issuer");
		String created = intent.getStringExtra("created");
		String expires = intent.getStringExtra("expires");
//		Log.i(TAG, "Host: "+h+" key: "+k);
		
		// Send Notification
		Log.i(TAG, "Send Notification ");
		notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		myNotification = new Notification(R.drawable.forbidden, "Alert!", System.currentTimeMillis());
		Context context = getApplicationContext();
		String notificationTitle = "Unknown Certificate!";
		String notificationText = "Click for more details";
		Intent myIntent = new Intent(NotifyService.this, NotificationReceiver.class);
		myIntent.putExtra("host", h);
		myIntent.putExtra("newkey", k);
		myIntent.putExtra("sigAlg", sigAlg);
		myIntent.putExtra("issuer", issuer);
		myIntent.putExtra("created", created);
		myIntent.putExtra("expires", expires);
		PendingIntent pendingIntent = PendingIntent.getActivity(NotifyService.this, 0, myIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		myNotification.defaults |= Notification.DEFAULT_SOUND;
		myNotification.flags |= Notification.FLAG_AUTO_CANCEL;
		myNotification.setLatestEventInfo(context, 
					notificationTitle, 
					notificationText, 
					pendingIntent);
		notificationManager.notify(MY_NOTIFICATION_ID, myNotification);
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		this.unregisterReceiver(notifyServiceReceiver);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public class NotifyServiceReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			int rqs = arg1.getIntExtra("RQS", 0);
			if (rqs == RQS_STOP_SERVICE){
				stopSelf();
			}
		}
	}	
}
