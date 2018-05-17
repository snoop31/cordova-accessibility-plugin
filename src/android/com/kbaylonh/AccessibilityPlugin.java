/**
 */
package com.example;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.content.Context;
import android.content.Intent;

public class AccessibilityPlugin extends CordovaPlugin {
  private static final String TAG = "AccessibilityPlugin";
  protected static Context context = null;
  public static JSONArray _numeros = null;
  public CordovaInterface cordova           = null;

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    AccessibilityPlugin.context = super.cordova.getActivity().getApplicationContext();
    Log.d(TAG, "Inicializando AccessibilityPlugin");
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    if(action.equals("saludar")) {
      // An example of returning data back to the web layer
       String phrase = args.getString(0);
      // Echo back the first argument
      final PluginResult result = new PluginResult(PluginResult.Status.OK, "Hola todo el... "+phrase);
      callbackContext.sendPluginResult(result);
    } else if(action.equals("startAccessibility")){

      if (!isAccessibilityEnabled()) {
        callbackContext.sendPluginResult(new PluginResult(Status.ERROR));
      } else {
        Log.d(TAG, args.toString());

        _numeros = args.getJSONObject(0).getJSONArray("contacto");
        // init whatsapp
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, args.getJSONObject(0).getString("mensaje"));
        sendIntent.setPackage("com.whatsapp");
        sendIntent.setType("text/plain");
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        AccessibilityPlugin.context.startActivity(sendIntent);
        V2contactService.activated = true;
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
      }

    }
    return true;
  }

  public boolean isAccessibilityEnabled(){
    int accessibilityEnabled = 0;
    boolean accessibilityFound = false;

    try {
      accessibilityEnabled = Settings.Secure.getInt(AccessibilityPlugin.context.getContentResolver(),android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
      Log.d(TAG, "ACCESSIBILITY: " + accessibilityEnabled);
    } catch (Settings.SettingNotFoundException e) {
      Log.d(TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
    }

    TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

    if (accessibilityEnabled==1){
      Log.d(TAG, "***ACCESSIBILIY IS ENABLED***: ");


      String settingValue = Settings.Secure.getString(AccessibilityPlugin.context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
      Log.d(TAG, "Setting: " + settingValue);
      if (settingValue != null) {
        TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
        mStringColonSplitter.setString(settingValue);
        while (splitter.hasNext()) {
          String accessabilityService = splitter.next();
          Log.d(TAG, "Setting: " + accessabilityService);
          if (accessabilityService.equalsIgnoreCase("io.ionic.starter/com.example.V2contactService")){
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
    return accessibilityFound;
  }
}
