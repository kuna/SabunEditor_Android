package com.kuna.sabuneditor_android;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class OpenActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_open);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.open, menu);
		return true;
	}

}
