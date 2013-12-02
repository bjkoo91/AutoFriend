package com.autofriend;

import java.util.List;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Button;

public class Settings extends PreferenceActivity {

	private static String TAG = "AutoFriend_Settings";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preference_headers, target);
	}

	public static class Prefs1Fragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Make sure default values are applied.  In a real app, you would
			// want this in a shared function that is used to retrieve the
			// SharedPreferences wherever they are needed.
			//PreferenceManager.setDefaultValues(getActivity(), R.xml.advanced_preferences, false);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.fragmented_preferences);
		}
	}
}
