package com.diycircuits.gpsfake;

import java.util.HashSet;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.app.Activity;

public class AppChooser extends Activity {
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_chooser);
		
		refreshAppList();
	}

	private void refreshAppList() {
		Settings settings = new Settings(this);
		HashSet<String> Apps = settings.getApps();
		
		TextView tv = (TextView) findViewById(R.id.hookedApps);
		
		tv.setText("");
		
		for(String appname : Apps) {
			tv.append(appname + "\n");
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void addApp(View view) {
		EditText editAppName = (EditText) findViewById(R.id.AppName);
		String appName = editAppName.getText().toString();
		Settings settings = new Settings(this);
		
		HashSet<String> Apps = settings.getApps();
		Apps.add(appName);
		settings.updateApps(Apps);
		
		refreshAppList();
	}
	
}
