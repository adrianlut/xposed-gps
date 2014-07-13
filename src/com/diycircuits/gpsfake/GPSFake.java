package com.diycircuits.gpsfake;

import static de.robv.android.xposed.XposedHelpers.findClass;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.AndroidAppHelper;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

@SuppressLint("UseValueOf")
public class GPSFake implements IXposedHookLoadPackage, IXposedHookZygoteInit {

	private boolean mLocationManagerHooked = false;
	private HashMap<Method, XC_MethodHook> mHook = new HashMap<Method, XC_MethodHook>();
	private HashSet<String> appsToHook;
	private double newlat;
	private double newlng;
	private float newAcc;
	private static Settings settings = new Settings();

    private void updateLocation() {
		double earth = (double) 6378137.0;
		Random rand = new Random();
		
		double x = (double) (rand.nextInt(10) - 5);
		double y = (double) (rand.nextInt(10) - 5);
		
		double dlat = x / earth;
		double dlng = y / ( earth * Math.cos(Math.PI * settings.getLat() / 180.0));
		
		newlat = settings.getLat() + (dlat * 180.0 / Math.PI);
		newlng = settings.getLng() + (dlng * 180.0 / Math.PI);
		newAcc = (float) rand.nextInt(5);
	}

    private void hookGpsMethods(String name, Object instance) {
		if (name.equals(Context.LOCATION_SERVICE)) {
			if (!mLocationManagerHooked) {
				String packageName = AndroidAppHelper.currentPackageName();
				
				settings.reload();
				appsToHook = settings.getApps();
				
				if (appsToHook.contains(packageName)) {
					XposedBridge.log("Hooking Location Manager of " + packageName);
					try {
						XC_MethodHook methodHook = new XGpsMethodHook();
						
						Class<?> hookClass = null;
						hookClass = findClass(instance.getClass().getName(), null);
						if (hookClass == null)
							throw new ClassNotFoundException(instance.getClass().getName());
	
						Class<?> clazz = hookClass;
						for (Method method : clazz.getDeclaredMethods()) {
							int m = method.getModifiers();
							if (method != null && Modifier.isPublic(m) && !Modifier.isStatic(m)) {
								XposedBridge.hookMethod(method, methodHook);
							}
						}
						
						XposedBridge.log(packageName + " successfully hooked!");
						
					} catch (Exception ex) {
						
					}
				} else {
				}
			}
			mLocationManagerHooked = true;
		}
	}
    
    private void hookSystemService(String context) {
    	XposedBridge.log("GPSFake now trying to hook " + context);
		try {
			XC_MethodHook methodHook = new XSystemServiceHook();

			Set<XC_MethodHook.Unhook> hookSet = new HashSet<XC_MethodHook.Unhook>();
	    
			Class<?> hookClass = null;
			try {
				hookClass = findClass(context, null);
				if (hookClass == null)
					throw new ClassNotFoundException(context);
				XposedBridge.log("Zygote Context Find Class Done");
			} catch (Exception ex) {
				XposedBridge.log("Zygote Context Impl Exception " + ex);
			}

			XposedBridge.log("Zygote Context Find Class " + hookClass);
			Class<?> clazz = hookClass;
			while (clazz != null) {
				for (Method method : clazz.getDeclaredMethods()) {
					if (method != null && method.getName().equals("getSystemService")) {
						hookSet.add(XposedBridge.hookMethod(method, methodHook));
					}
				}
				clazz = (hookSet.isEmpty() ? clazz.getSuperclass() : null);
			}
		} catch (Exception ex) {
			XposedBridge.log("Zygote Context Hook Exception " + ex);
		}
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
		XposedBridge.log("<<<<<<<<<< GPSFake module started >>>>>>>>>>");
		
		appsToHook = settings.getApps();
	
		hookSystemService("android.app.ContextImpl");
		hookSystemService("android.app.Activity");
    }

    @Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		settings.reload();
		appsToHook = settings.getApps();
	}

	private final class XSystemServiceHook extends XC_MethodHook {
		@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {}

		@Override
		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
			if (!param.hasThrowable())
				try {
					if (param.args.length > 0 && param.args[0] != null) {
						// XposedBridge.log("Hook Method : " + mInstance + " " + mApp + " " + packageName);
						String name = (String) param.args[0];
						Object instance = param.getResult();
						if (name != null && instance != null) {
							hookGpsMethods(name, instance);
						}
					}
				} catch (Throwable ex) {
					throw ex;
				}
		}
	}

	private final class XGpsMethodHook extends XC_MethodHook {
		@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
		    settings.reload();
			if (param.method.getName().equals("onProviderDisabled")) {
				if (settings.isStarted()) param.setResult(null);
			}
		}

		@Override
		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
		    settings.reload();
			if (!param.hasThrowable())
				try {
					
					/* Injection of the faked gps data */
					if (settings.isStarted()) {
						updateLocation();
						
						if (param.method.getName().equals("getLatitude")) {
							// XposedBridge.log("Location Manager getLatitude " + newlat);
							param.setResult(new Double(newlat));
						}
						if (param.method.getName().equals("getLongitude")) {
							// XposedBridge.log("Location Manager getLongitude " + newlng);
							param.setResult(new Double(newlng));
						}
						if (param.method.getName().equals("getAccuracy")) {
							// XposedBridge.log("Location Manager getAccuracy " + param.getResult());
							param.setResult(new Float(newAcc));
						}
						
					}
					
					if (param.args.length > 0 && param.args[0] != null) {
						
						/* If gps fake is activated, answer all "isProviderEnabled"-requests with true */
						if (param.args != null && param.args.length == 1 && param.method.getName().equals("isProviderEnabled")) {
							// XposedBridge.log("Location Manager isProviderEnabled : " + param.args[0] + " " + param.getResult());
							if (settings.isStarted()) param.setResult(new Boolean(true));
						}
						
						
						/* Answer to "requestLocationUpdates" */
						if (param.args != null && param.args.length >= 1 && param.method.getName().equals("requestLocationUpdates")) {
							for (int count = 0; count < param.args.length; count++) {
								// XposedBridge.log("Location Manager requestLocationUpdates : " + param.args[count]);
								if (param.args[count] instanceof LocationListener) {
									LocationListener ll = (LocationListener) param.args[count];

									try {
										Class<?> clazz = ll.getClass();
										for (Method method : clazz.getDeclaredMethods()) {
											int m = method.getModifiers();
											if (method != null && Modifier.isPublic(m) && !Modifier.isStatic(m)) {
												if (!mHook.containsKey(method)) {
													mHook.put(method, this);
													// XposedBridge.log("Location Manager Listener " + method.getName());
													XposedBridge.hookMethod(method, this);
													if (settings.isStarted()) {
														Location l = new Location("network");
														updateLocation();
														// XposedBridge.log("Location Manager New Loc : " + newlat + " " + newlng);
														l.setTime(new Long(System.currentTimeMillis()));
														l.setLatitude(newlat);
														l.setLongitude(newlng);
														l.setAccuracy(newAcc);
														XposedHelpers.callMethod(ll, "onLocationChanged", l);
													}
												}
											}
										}
									} catch (Exception ex) {
									}
								}
							}
						}
						
						
						if (param.args != null && param.args.length == 1 && param.method.getName().equals("onLocationChanged")) {
							if (param.args[0] instanceof Location) {
								Location ll = (Location) param.args[0];
								try {
									Class<?> clazz = ll.getClass();
									for (Method method : clazz.getDeclaredMethods()) {
										int m = method.getModifiers();
										if (method != null && Modifier.isPublic(m) && !Modifier.isStatic(m)) {
											if (!mHook.containsKey(method)) {
												mHook.put(method, this);
												// XposedBridge.log("Location Manager Listener " + method.getName());
												XposedBridge.hookMethod(method, this);
											}
										}
									}
								} catch (Exception ex) {
								}
							}
						}
						
						
						if (param.method.getName().equals("removeUpdates")) {
							Set<Method> mMethod = mHook.keySet();
							for (Method m : mMethod) {
								XposedBridge.unhookMethod(m, mHook.get(m));
							}
							mHook.clear();
						}
					}
				} catch (Throwable ex) {
					throw ex;
				}
		}
	}
    
}
