package com.diycircuits.gpsfake;

import java.util.HashSet;

import android.content.Context;
import android.content.SharedPreferences;
import de.robv.android.xposed.XSharedPreferences;

public class Settings {

	private static double lat = 0.0;
	private static double lng = 0.0;
	private static boolean start = false;
	@SuppressWarnings("unused")
	private Context context = null;
	private XSharedPreferences xSharedPreferences = null;
    private SharedPreferences sharedPreferences = null;

	public Settings() {
		xSharedPreferences = new XSharedPreferences("com.diycircuits.gpsfake", "gps");
		// xSharedPreferences.makeWorldReadable();
	}

	public Settings(Context context) {
        sharedPreferences = context.getSharedPreferences("gps", Context.MODE_WORLD_READABLE);
        this.context = context;
    }

	public double getLat() {
		if (sharedPreferences != null)
			return sharedPreferences.getFloat("latitude", (float) 0.0);
		else if (xSharedPreferences != null)
			return xSharedPreferences.getFloat("latitude", (float) 0.0);
		return lat;
    }

	public double getLng() {
		if (sharedPreferences != null)
			return sharedPreferences.getFloat("longitude", (float) 0.0);
		else if (xSharedPreferences != null)
			return xSharedPreferences.getFloat("longitude", (float) 0.0);
		return lng;
    }

	public HashSet<String> getApps() {
		HashSet<String> defaultApps = new HashSet<String>();
		defaultApps.add("com.nianticproject.ingress");
		defaultApps.add("com.android.apps.maps");
		
		if (sharedPreferences != null) {
			HashSet<String> Apps = (HashSet<String>) sharedPreferences.getStringSet("apps", defaultApps);
			return Apps;
		}
		else if (xSharedPreferences != null) {
			HashSet<String> Apps = (HashSet<String>) xSharedPreferences.getStringSet("apps", defaultApps);
			return Apps;
		}
		
		return defaultApps;
	}
	
	public boolean isStarted() {
		if (sharedPreferences != null)
			return sharedPreferences.getBoolean("start", false);
		else if (xSharedPreferences != null)
			return xSharedPreferences.getBoolean("start", false);
		return start;
    }

	public void update(double la, double ln, boolean start) {
		SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        prefEditor.putFloat("latitude",  (float) la);
        prefEditor.putFloat("longitude", (float) ln);
        prefEditor.putBoolean("start",   start);
        prefEditor.apply();		
	}

	public void updateApps(HashSet<String> Apps) {
		SharedPreferences.Editor prefEditor = sharedPreferences.edit();
		prefEditor.putStringSet("apps", Apps);
		prefEditor.apply();
	}
	
	public void reload() {
        xSharedPreferences.reload();
    }
}
