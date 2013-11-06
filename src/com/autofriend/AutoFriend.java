package com.autofriend;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ToggleButton;

public class AutoFriend extends Activity {
	
	private static final String TAG = "AutoFriend";
	private static final String serviceName = "com.autofriend.AutoFriendService";
	private static boolean mServiceOn;
	private static ToggleButton mServiceToggleButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mServiceOn = isAutoFriendServiceRunning();
		mServiceToggleButton = (ToggleButton) findViewById(R.id.serviceToggleButton);
		mServiceToggleButton.setChecked(mServiceOn);
	}
	
	@Override
	public void onResume() {
		Log.v(TAG, "onResume");
		super.onResume();
	}
	
	@Override
	public void onStop() {
		Log.v(TAG, "onStop");
		super.onStop();
	}
	
	private void startAutoFriend(View v) {
		Log.v(TAG, "startAutoFriend");
		startAutoFriendService();
	}
	
	private void stopAutoFriend(View v) {
		Log.v(TAG, "stopAutoFriend");
		Intent service = new Intent(this, AutoFriendService.class);
		boolean stopped = stopService(service);
		Log.d(TAG, "service stopped: " + stopped);
		mServiceOn = !stopped;
	}
	
	private void startAutoFriendService() {
		Log.v(TAG, "startAutoFriendService");
		boolean running = isAutoFriendServiceRunning();
		Log.d(TAG, "service running: " + running);
		
		if (!running) {
			try {
				Intent service = new Intent(this, AutoFriendService.class);
				startService(service);
				running = isAutoFriendServiceRunning();
				Log.d(TAG, "started service: " + running);
			} catch (Exception e) {
				Log.e("onCreate", "error with service creation", e);
			}
		}
		mServiceOn = running;
	}
	
	private boolean isAutoFriendServiceRunning() {
		Log.v(TAG, "isServiceRunning");
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		boolean result = false;
		
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceName.equals(service.service.getClassName())) {
				result = true;
			}
		}
		
		return result;
	}
	
	public void onServiceToggleClicked(View view) {
		Log.v(TAG, "onServiceToggleClicked");

		boolean on = ((ToggleButton) view).isChecked();
		if (on) {
			Log.d(TAG, "button toggled on");
			startAutoFriend(view);
		} else {
			Log.d(TAG, "button toggled off");
			stopAutoFriend(view);
		}
	}

}
