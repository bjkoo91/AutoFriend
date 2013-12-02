package com.autofriend;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class About extends Activity {
	
	TextView text;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		text = (TextView) this.findViewById(R.id.about);
	}
}
