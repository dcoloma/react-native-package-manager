package com.rnlib.packagemanager;

import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;

public class Module extends ReactContextBaseJavaModule {

  private static final String DURATION_SHORT_KEY = "SHORT";
  private static final String DURATION_LONG_KEY = "LONG";

  private ReactContext nReactContext;

  public Module(ReactApplicationContext reactContext) {
    super(reactContext);
    nReactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNPackageManager";
  }

  @ReactMethod
  public void getPackageList(Promise promise) {
    try {
      List<PackageInfo> packageInfoList = nReactContext.getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA | PackageManager.GET_PERMISSIONS);
      WritableArray appMaps = Arguments.createArray();

      for (PackageInfo packageInfo : packageInfoList) {
        if (!isSystemPackage(packageInfo)) {
          WritableMap appMap = Arguments.createMap();

          appMap.putString("name", packageInfo.packageName);
          appMap.putString("version", packageInfo.versionName);
          appMap.putInt("versionCode", packageInfo.versionCode);
          
          Date installTime = new Date(packageInfo.firstInstallTime);
          String formattedInstallTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(installTime);
          appMap.putString("firstInstallTime", formattedInstallTime);
          Date updateTime = new Date(packageInfo.lastUpdateTime);
          String formattedUpdateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(updateTime);
          appMap.putString("lastUpdateTime", formattedUpdateTime);

          appMap.putInt("targetSdkVersion", packageInfo.applicationInfo.targetSdkVersion);
          appMap.putString("appinfoName", packageInfo.applicationInfo.name);
          appMap.putString("appinfoPackageName", packageInfo.applicationInfo.packageName);
          appMap.putString("label", packageInfo.applicationInfo.loadLabel(nReactContext.getPackageManager()).toString());

          if (packageInfo.requestedPermissions != null) {
            WritableMap permissionMap = Arguments.createMap();
            for(int i=0; i < packageInfo.requestedPermissions.length; i++) {
              String permissionName = packageInfo.requestedPermissions[i];
              if ((packageInfo.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                permissionMap.putBoolean(permissionName, true);
              }
              else {
                permissionMap.putBoolean(permissionName, false);
              }
            }
            appMap.putMap("permissionMap", permissionMap);
          }
          appMaps.pushMap(appMap);
        }
      }
      promise.resolve(appMaps);
     }
     catch (Exception e) {
       promise.reject(e.getMessage());
     }
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put(DURATION_SHORT_KEY, Toast.LENGTH_SHORT);
    constants.put(DURATION_LONG_KEY, Toast.LENGTH_LONG);
    return constants;
  }

  @ReactMethod
  public void show(String message, int duration) {
    Toast.makeText(getReactApplicationContext(), message, duration).show();
  }

  private boolean isSystemPackage(PackageInfo packageInfo) {
    return ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
  }

}
