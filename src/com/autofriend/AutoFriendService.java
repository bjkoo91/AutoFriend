package com.autofriend;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;

public class AutoFriendService extends Service {

	private static final String TAG = "AutoFriendService";

	private static final String SMS_RECEIVED
	= "android.provider.Telephony.SMS_RECEIVED";

	private static final ChatterBotType type = ChatterBotType.CLEVERBOT;
	private static ChatterBotFactory botFactory;
	private static HashMap<String, ChatterBotSession> sessionMap;
	private static Set<String> numbers;

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		super.onCreate();

		botFactory = new ChatterBotFactory();
		sessionMap = new HashMap<String, ChatterBotSession>();

		IntentFilter receiverFilter = new IntentFilter(SMS_RECEIVED);
		registerReceiver(receiver, receiverFilter);

		numbers = new HashSet<String>();
		Set<String> temp = AutoFriend.getNumbers();
		for (String number : temp) {
			numbers.add(number);
		}

		MessageTimer.start();
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

		MessageTimer.stop();
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent in) {
			Log.v(TAG, "receiver/onReceive");

			if (in.getAction().equals(SMS_RECEIVED)) {
				Bundle bundle = in.getExtras();

				if (bundle != null) {
					Object[] pdus = (Object[])bundle.get("pdus");
					SmsMessage[] messages = new SmsMessage[pdus.length];

					for (int i = 0; i < pdus.length; ++i) {
						messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
					}

					for (SmsMessage message: messages) {
						/* 
						 * Creating a unique ChatterBot session for each person and store them in the HashMap
						 */
						if(isChecked(message.getOriginatingAddress())) {
							if(!sessionMap.containsKey(message.getOriginatingAddress())) {
								try {
									ChatterBot bot = botFactory.create(type);
									ChatterBotSession session = bot.createSession();
									sessionMap.put(message.getOriginatingAddress(), session);
								} catch (Exception e) {
									Log.e("onCreate", "error with bot creation", e);
								}
							}
							respond(message.getOriginatingAddress(), message.getDisplayMessageBody(), c);
						}
					}
				}
			}
		}
	};

	/*
	 * Get CleverBot's respond to the received message
	 * Add that respond to the message queue
	 */
	private void respond(String requester, String message, Context context) {
		Log.v(TAG, "respond");

		try {
			String reply = "Ok";
			ChatterBotSession session = sessionMap.get(requester);

			reply = session.think(message);

			MessageTimer.addNewMessage(requester, reply, context);	
		} catch (Exception e) {
			Log.e("respond", "error with respond", e);
		}
	}

	private boolean isChecked(String number) {
		return numbers.contains(number);
	}

	public static void addNumber(String number) {
		numbers.add(number);
	}

	public static void removeNumber(String number) {
		numbers.remove(number);
	}
}