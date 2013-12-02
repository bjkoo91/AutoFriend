package com.autofriend;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.util.Log;

public class MessageTimer {
	
	private static final String TAG = "AutoFriendTimer";
	
	private static Timer timer = new Timer();
	private static int repeatTime = 5000;
	
	private static LinkedList<Message> messages;
	private static int defaultDelay = 3000;
	private static int delayPerChar = 200;
	private static Lock messageLock;
	
	/*
	 * Initialize the message queue and start the timer
	 */
	public static void start() {
		Log.v(TAG, "start");
		messages = new LinkedList<Message>();
		messageLock = new ReentrantLock();
		timer.schedule(new MessageSender(), 0, repeatTime);
	}
	
	/*
	 * Stop the timer and send all messages in the message queue
	 * Will take a while if there is a lot of messages in the queue
	 * Require the lock to send queued messages
	 */
	public static void stop() {
		Log.v(TAG, "stop");
		timer.cancel();
		
		try {
			messageLock.lock();
			Message msg = messages.poll();
			while(msg != null) {
				if (msg.sendMessage()) {
					msg = messages.poll();
				} else {
					try {
						Thread.sleep(Math.abs(msg.getDeliveryTime() - System.currentTimeMillis()));
					} catch (InterruptedException ie) {
						Log.e("stop", "error with thread sleep", ie);
					}
				}
			}
		} finally {
			messageLock.unlock();
		}
		
		Log.v(TAG, "stop/completed");
	}
	
	/*
	 * Add new message to the queue
	 * Require the lock to add new message to the queue
	 */
	public static void addNewMessage(String requester, String message, Context context) {
		Log.v(TAG, "addNewMessage");
		
		try {
			messageLock.lock();
			long deliveryTime = messages.peekLast() == null ? System.currentTimeMillis() : messages.peekLast().getDeliveryTime();
			deliveryTime += defaultDelay + (delayPerChar * message.length());
			Message new_msg = new Message(requester, message, deliveryTime, context);
			messages.addLast(new_msg);
		} finally {
			messageLock.unlock();
		}
	}
	
	static class MessageSender extends TimerTask {
		/*
		 * Send all messages in the queue that reached their delivery times
		 * Require the lock to send queued messages
		 */
		public void run() {
			Log.v(TAG, "run");
			
			try {
				messageLock.lock();	
				Message msg = messages.peek();
				while (msg != null) {
					if (msg.sendMessage()) {
						messages.poll();
						msg = messages.peek();
					} else {
						break;
					}
				}
			} finally {
				messageLock.unlock();
			}
		}
	}
}
