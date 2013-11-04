package com.example.autofriend;

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
	private static final String serviceName = "AUTO_FRIEND";
	private static Intent service;
	private static boolean running = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	@Override
	public void onResume() {
		Log.v(TAG, "onResume");
		super.onResume();
	}
	
	private void startAutoFriend(View v) {
		Log.v(TAG, "startAutoFriend");
		startAutoFriendService();
	}
	
	private void stopAutoFriend(View v) {
		Log.v(TAG, "stopAutoFriend");
		//Intent 
		service = new Intent(this, AutoFriendService.class);
		boolean stopped = stopService(service);
		Log.d(TAG, "service stopped: " + stopped);
	}
	
	private void startAutoFriendService() {
		Log.v(TAG, "startAutoFriendService");
		running = isAutoFriendServiceRunning();
		Log.d(TAG, "service running: " + running);
		
		if (!running) {
			try {
				//Intent 
				service = new Intent(this, AutoFriendService.class);
				startService(service);
			} catch (Exception e) {
				Log.e("onCreate", "service creation problem", e);
			}
		}
	}
	
	private boolean isAutoFriendServiceRunning() {
		Log.v(TAG, "isServiceRunning");
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		boolean result = false;
		
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceName.equals(service.service.getClassName())) {
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	// serviceToggle click event handler
	public void onServiceToggleClicked(View view) {
		Log.v(TAG, "onServiceToggle");

		boolean on = ((ToggleButton) view).isChecked();
		if (on) {
			startAutoFriend(view);
		} else {
			stopAutoFriend(view);
		}
	}

}