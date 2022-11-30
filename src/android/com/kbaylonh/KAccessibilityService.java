package com.kbaylonh;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.content.res.Configuration;

public class AccessibilityPlugin extends CordovaPlugin {
  private static final String TAG = "AccessibilityPlugin";
  protected Context context = null;
  public static AccessibilityPlugin instance = null;

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      Log.v(TAG, "***** onMultiWindowModeChanged: " + cordova.getActivity().isInMultiWindowMode());
      final Intent intent = new Intent("layoutChannel");
      Bundle b = new Bundle();
      b.putString("action", "split_status");
      b.putBoolean("status", cordova.getActivity().isInMultiWindowMode());
      intent.putExtras(b);
      LocalBroadcastManager.getInstance(cordova.getActivity()).sendBroadcastSync(intent);
  }

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    context = super.cordova.getActivity().getApplicationContext();

    Log.d(TAG, "Inicializando AccessibilityPlugin");
    instance = this;
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException{
    switch(action){
      case "checkAccessibility":
        String packageSource = args.getString(0);
        if (isAccessibilityEnabled(packageSource)){
          callbackContext.sendPluginResult(new PluginResult(Status.OK));
        } else {
          callbackContext.sendPluginResult(new PluginResult(Status.ERROR));
        }
        break;
      case "startAccessibility":
          KAccessibilityService.activated = true;
        break;
      case "actionAccessibility":
        String actionName = args.getString(0);
        KAccessibilityService service = new KAccessibilityService();
        service.doAction(actionName);
        break;
      case "openAccessibility":
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        super.cordova.getActivity().startActivity(intent);
        callbackContext.sendPluginResult(new PluginResult(Status.OK));
        break;
      case "checkService":
        if(!KAccessibilityService.activated){
          callbackContext.sendPluginResult(new PluginResult(Status.OK));
        } else {
          callbackContext.sendPluginResult(new PluginResult(Status.ERROR));
        }
        break;
      case "stopService":
        try {
          KAccessibilityService.activated = false;
          callbackContext.sendPluginResult(new PluginResult(Status.OK));
        } catch (Exception e) {
          e.printStackTrace();
          callbackContext.sendPluginResult(new PluginResult(Status.ERROR));
        }
        break;
      case "checkMultiWindowMode":
        try {
          boolean isInMultiWindowMode = false;
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            isInMultiWindowMode = cordova.getActivity().isInMultiWindowMode();
          }
          callbackContext.sendPluginResult(new PluginResult(Status.OK, isInMultiWindowMode));
        } catch (Exception e) {
          e.printStackTrace();
          callbackContext.sendPluginResult(new PluginResult(Status.ERROR));
        }
        break;
      default:
        callbackContext.sendPluginResult(new PluginResult(Status.ERROR));
        break;
    }

    return true;
  }

  private boolean isAccessibilityEnabled(String packageSource){
    int accessibilityEnabled = 0;

    try {
      accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(),android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
      Log.d(TAG, "ACCESSIBILITY: " + accessibilityEnabled);
    } catch (Settings.SettingNotFoundException e) {
      Log.d(TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
    }

    TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

    if (accessibilityEnabled==1){
      Log.d(TAG, "***ACCESSIBILIY IS ENABLED***: ");


      String settingValue = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
      Log.d(TAG, "Setting: " + settingValue);
      if (settingValue != null) {
        TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
        mStringColonSplitter.setString(settingValue);
        while (splitter.hasNext()) {
          String accessabilityService = splitter.next();
          Log.d(TAG, "Setting: " + accessabilityService);
          if (accessabilityService.equalsIgnoreCase(packageSource + "/com.kbaylonh.KAccessibilityService")){
            Log.d(TAG, "We've found the correct setting - accessibility is switched on!");
            return true;
          }
        }
      }

      Log.d(TAG, "***END***");
    }
    else{
      Log.d(TAG, "***ACCESSIBILIY IS DISABLED***");
    }

    return false;
  }
}
