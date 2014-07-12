package com.diycircuits.gpsfake;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.app.Activity;

public class AppChooser extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_chooser);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void addApp(View view) {
		EditText editAppName = (EditText) findViewById(R.id.AppName);
		String appName = editAppName.getText().toString();
		
		
	}
	
}
