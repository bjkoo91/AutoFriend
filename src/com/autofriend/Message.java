package com.autofriend;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;

public class Message {

	private static final String TAG = "AutoFriendMessage";
	
	private String requester;
	private String message;
	private long deliveryTime;
	private Context context;
	
	public Message(String r, String m, long t, Context c) {
		requester = r;
		message = m;
		deliveryTime = t;
		context = c;
	}
	
	/*
	 * Send the queued message if its delivery time is marked before the current time
	 * Return false if its delivery time is not yet reached
	 */
	public boolean sendMessage() {
		Log.v(TAG, "sendMessage");
		
		long current = System.currentTimeMillis();
		
		if (current >= deliveryTime) {
			Log.v(TAG, "delivering a message");
			
			SmsManager.getDefault().sendTextMessage(requester, null, message, null, null);
			addMessageToSent();
			return true;
		}
		
		return false;
	}
	
	/*
	 * Add the CleverBot's respond to the Sent
	 * Each CleverBot's message has "CleverBot> " appended to front of it
	 */
	private void addMessageToSent() {
		Log.v(TAG, "addMessageToSent");
		
		ContentValues sentSms = new ContentValues();
		sentSms.put("address", requester);
		sentSms.put("body", "CleverBot> " + message);
		context.getContentResolver().insert(Uri.parse("content://sms/sent"), sentSms);
	}
	
	public long getDeliveryTime() {
		return deliveryTime;
	}
}