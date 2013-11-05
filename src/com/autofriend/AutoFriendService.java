package com.autofriend;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class AutoFriendService extends Service {

	private static final String TAG = "AutoFriendService";
	private static final String RECEIVED_ACTION
	= "android.provider.Telephony.SMS_RECEIVED";
	private static final String SENT_ACTION = "SENT_SMS";
	private static final String DELIVERED_ACTION = "DELIVERED_SMS";
	private final ChatterBotType type = ChatterBotType.CLEVERBOT;
	private static ChatterBotFactory botFactory;
	private static ChatterBot bot;
	private static ChatterBotSession session;

	private String requester;

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent in) {
			Log.v(TAG, "receiver:onReceive");

			if (in.getAction().equals(RECEIVED_ACTION)) {
				Bundle bundle = in.getExtras();

				if (bundle != null) {
					Object[] pdus = (Object[])bundle.get("pdus");
					SmsMessage[] messages = new SmsMessage[pdus.length];
					
					for (int i = 0; i < pdus.length; ++i) {
						messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
					}

					for (SmsMessage message: messages) {
						requestReceived(message.getOriginatingAddress());
						String text = message.getDisplayMessageBody();
						respond(text);
					}
				}
			}
		}
	};

	private BroadcastReceiver sender = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent i) {
			Log.v(TAG, "sender:onReceive");

			if(i.getAction().equals(SENT_ACTION)) {    		
				if(getResultCode() != Activity.RESULT_OK) {
					String reciptent = i.getStringExtra("recipient");
					requestReceived(reciptent);
				}
			}
		}
	};

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		super.onCreate();

		IntentFilter receiverFilter = new IntentFilter(RECEIVED_ACTION);
		registerReceiver(receiver, receiverFilter);

		IntentFilter senderFilter = new IntentFilter(SENT_ACTION);
		registerReceiver(sender, senderFilter);

		botFactory = new ChatterBotFactory();
		try {
			bot = botFactory.create(type);
		} catch (Exception e) {
		}
		session = bot.createSession();
	}

	@Override
	public int onStartCommand(Intent inten, int flags, int startId) {
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		unregisterReceiver(receiver);
		unregisterReceiver(sender);
	}

	public void requestReceived(String f) {
		Log.v(TAG, "requestReceived");
		requester = f;
	}

	private void respond(String message) {
		Log.v(TAG, "respond");
		/* set reply to CleverBot's respond */
		String reply = "";
		try {
			reply = session.think(message);
		} catch (Exception e) {
		}

		SmsManager sms = SmsManager.getDefault();
		Intent sentIn = new Intent(SENT_ACTION);
		PendingIntent sentPIn = PendingIntent.getBroadcast(this, 0, sentIn, 0);

		Intent deliverIn = new Intent(DELIVERED_ACTION);
		PendingIntent deliverPIn = PendingIntent.getBroadcast(this, 0, deliverIn, 0);

		sms.sendTextMessage(requester, null, reply, sentPIn, deliverPIn);

	}
}