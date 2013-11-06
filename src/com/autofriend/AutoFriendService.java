package com.autofriend;

import java.util.HashMap;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;

public class AutoFriendService extends Service {

	private static final String TAG = "AutoFriendService";
	private static final String RECEIVED_ACTION
	= "android.provider.Telephony.SMS_RECEIVED";
	
	private static final String TELEPHONE_NUMBER_FIELD_NAME = "address";
	private static final String MESSAGE_BODY_FIELD_NAME = "body";
	private static final Uri SENT_MESSAGE_CONTENT_PROVIDER = Uri.parse("content://sms/sent");
	
	private final ChatterBotType type = ChatterBotType.CLEVERBOT;
	private static ChatterBotFactory botFactory;
	private static HashMap<String, ChatterBotSession> sessionMap;

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent in) {
			Log.v(TAG, "onReceive");

			if (in.getAction().equals(RECEIVED_ACTION)) {
				Bundle bundle = in.getExtras();

				if (bundle != null) {
					Object[] pdus = (Object[])bundle.get("pdus");
					SmsMessage[] messages = new SmsMessage[pdus.length];
					
					for (int i = 0; i < pdus.length; ++i) {
						messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
					}

					for (SmsMessage message: messages) {
						if(!sessionMap.containsKey(message.getOriginatingAddress())) {
							try {
								ChatterBot bot = botFactory.create(type);
								ChatterBotSession session = bot.createSession();
								sessionMap.put(message.getOriginatingAddress(), session);
							} catch (Exception e) {
								Log.e("onCreate", "error with bot creation", e);
							}
						}
						
						String text = message.getDisplayMessageBody();
						respond(message.getOriginatingAddress(), text, message.getOriginatingAddress());
					}
				}
			}
		}
	};

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		super.onCreate();

		sessionMap = new HashMap<String, ChatterBotSession>();

		IntentFilter receiverFilter = new IntentFilter(RECEIVED_ACTION);
		registerReceiver(receiver, receiverFilter);

		botFactory = new ChatterBotFactory();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		return null;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		
		unregisterReceiver(receiver);
	}
	
	private void respond(String requester, String message, String originatingAddress) {
		Log.v(TAG, "respond");
		
		try {
			String reply = "Ok";
			ChatterBotSession session = sessionMap.get(originatingAddress);
			
			reply = session.think(message);
			
			SmsManager sms = SmsManager.getDefault();
			sms.sendTextMessage(requester, null, reply, null, null);
			
			addMessageToSent(requester, reply);
		} catch (Exception e) {
			Log.e("respond", "error with respond", e);
		}
	}
	
	private void addMessageToSent(String requester, String message) {
		Log.v(TAG, "addMessageToSent");
		
		ContentValues sentSms = new ContentValues();
		
		sentSms.put(TELEPHONE_NUMBER_FIELD_NAME, requester);
		sentSms.put(MESSAGE_BODY_FIELD_NAME, "CleverBot>" + message);
		
		ContentResolver contentResolver = getContentResolver();
		contentResolver.insert(SENT_MESSAGE_CONTENT_PROVIDER, sentSms);
	}
}