package com.autofriend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class AutoFriend extends Activity {

	private static final String TAG = "AutoFriend";
	private static final String serviceName = "com.autofriend.AutoFriendService";
	private static boolean mServiceOn;
	private static Switch serviceSwitch;
	private ListView mContactsListView;
	private ContactsListAdapter mContactsListAdapter = null;
	private static ArrayList<Contact> mContacts;
	private SharedPreferences mPrefs;
	private static Set<String> numbers;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mContacts = new ArrayList<Contact>();
		mServiceOn = isAutoFriendServiceRunning();
		mContactsListView = (ListView) findViewById(R.id.contact_list);
		mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);
		
		numbers = mPrefs.getStringSet("numbers", null);
		if (numbers == null)
			numbers = new HashSet<String>();
		
		new LoadContacts().execute();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_bar_menu, menu);
		serviceSwitch = (Switch) menu.findItem(R.id.serviceSwitch).getActionView();
		serviceSwitch.setChecked(mServiceOn);
		serviceSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Log.v(TAG, "onServiceToggleClicked");

				boolean on = ((Switch) buttonView).isChecked();
				if (on) {
					Log.d(TAG, "button toggled on");
					startAutoFriend(buttonView);
				} else {
					Log.d(TAG, "button toggled off");
					stopAutoFriend(buttonView);
				}
			}
		});
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.settings:
				Intent settings = new Intent(this, Settings.class);
				startActivity(settings);
				return true;
			case R.id.about:
				Intent about = new Intent(this, About.class);
				startActivity(about);
				return true;
			default: return super.onOptionsItemSelected(item);
		}
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
		SharedPreferences mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);  
		SharedPreferences.Editor ed = mPrefs.edit();
		ed.putStringSet("numbers", numbers);
		ed.commit();
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

	/*
	 * Start the AutoFriend Service if the service is not already running
	 */
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

	/*
	 * Check if AutoFriendService is already running or not
	 */
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

	/*
	 * Populate the contact list
	 */
	private void getContactsList() {
		Log.v(TAG, "getContactsList");
		// Build adapter with contact entries
		String [] projection = new String[] {
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.NUMBER
		};
		Cursor cursor = getContentResolver().query(	
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				projection,
				null,
				null,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
		while(cursor.moveToNext()) {
			String contact_name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			number = number.replaceAll(" ", "");
			number = number.replaceAll("-", "");
			boolean checked = numbers.contains(number);
			Contact contact = new Contact(contact_name, number, checked);
			mContacts.add(contact);
			Log.d("TEST", ""+ mContacts.size());
		}
		cursor.close();
		Log.d(TAG,"mContacts: "+Arrays.toString(mContacts.toArray()));
	}

	private ContactsListAdapter getContactsListAdapter() {
		Log.v(TAG, "getContactsListAdapter");
		return new ContactsListAdapter(this, R.layout.contact_entry, mContacts);
	}

	private static class ContactsListAdapter extends ArrayAdapter<Contact> {
		ArrayList<Contact> contacts;
		Context c;

		private ContactsListAdapter(Context c, int layout, ArrayList<Contact> contacts) {
			super(c, layout, contacts);
			this.c = c;
			this.contacts = contacts;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.v(TAG,"ContactsListAdapter.getView");
			View view = convertView;
			if (view == null) {
				LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = vi.inflate(R.layout.contact_entry, null);
			}
			Contact contact = contacts.get(position);
			Log.d(TAG,"got contact?");
			if (contact != null) {
				CheckBox nameCheckBox = (CheckBox) view.findViewById(R.id.set_service);
				nameCheckBox.setChecked(contacts.get(position).getChecked());
				if (nameCheckBox != null) {
					nameCheckBox.setText(contact.getName());
				}
				nameCheckBox.setOnClickListener(new OnItemClickListener(position, nameCheckBox.getText(), nameCheckBox));
			}
			return view;
		}

		class OnItemClickListener implements OnClickListener{          
			private int position;
			private CharSequence text;
			private CheckBox checkBox;
			
			OnItemClickListener(int position, CharSequence text, CheckBox checkBox){
				this.position = position;
				this.text = text;
				this.checkBox = checkBox;
				Log.v(TAG,"onItemClickListener "+ position + " and text " + text);
			}

			@Override
			public void onClick(View arg0) {
				Contact clicked = mContacts.get(position);
				if(clicked.getChecked()) {
					numbers.remove(clicked.getNumber());
					if(mServiceOn) {
						AutoFriendService.removeNumber(clicked.getNumber());
					}
				} else {
					numbers.add(clicked.getNumber());
					if(mServiceOn) {
						AutoFriendService.addNumber(clicked.getNumber());
					}
				}
				clicked.toggle();
				Log.d(TAG, "Toggled " + clicked.getChecked());
			}              
		}
	}

	public static class ViewHolder {
		TextView displayName;
		CheckBox setService;
	}

	public class LoadContacts extends AsyncTask<Void, Void, Void> {
		// private ViewHolder viewHolder;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Do Dialog stuff here
			Log.v(TAG,"LoadContacts");
		}

		protected Void doInBackground(Void... args) {
			// Put your implementation to retrieve contacts here
			Log.v(TAG,"doInBackground");
			getContactsList();
			return null;
		}

		protected void onPostExecute(Void result) {
			Log.v(TAG,"onPostExecute");
			// Dismiss Dialog
			runOnUiThread(new Runnable() {
				public void run() {
					Log.d(TAG,"onPostExecute.run");
					// Create list adapter and notify it
					mContactsListAdapter = getContactsListAdapter();
					mContactsListAdapter.notifyDataSetChanged();
					mContactsListView.setAdapter(mContactsListAdapter);
				}
			});
			return;
		}
	}

	public class Contact {
		private String name;
		private boolean checked;
		private String number;

		public Contact(String name, String number, boolean checked) {
			this.name = name;
			this.number = number;
			this.checked = checked;
		}
		public String getName() {
			return name;
		}
		public boolean getChecked() {
			return checked;
		}
		public String getNumber() {
			return number;
		}
		
		public void toggle() {
			checked = !checked;
		}
	}
	
	public static Set<String> getNumbers() {
		return numbers;
	}
}
