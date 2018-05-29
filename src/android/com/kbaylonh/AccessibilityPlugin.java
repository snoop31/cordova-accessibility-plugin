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
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.content.Context;
import android.content.Intent;

public class AccessibilityPlugin extends CordovaPlugin {
  private static final String TAG = "AccessibilityPlugin";
  protected Context context = null;
  public static JSONArray _numeros = null;
  public static AccessibilityPlugin instance = null;
  private Intent whatsappIntent = null;

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    context = super.cordova.getActivity().getApplicationContext();

    Log.d(TAG, "Inicializando AccessibilityPlugin");
    instance = this;
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

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

        // clear contacts
        _numeros = new JSONArray();
        JSONArray rawContacts = args.getJSONObject(0).getJSONArray("contacto");

        for(int i=0; i<rawContacts.length();i++){
          // get contact
          JSONObject contact = rawContacts.getJSONObject(i);

          // flag to add _numeros variable...
          boolean process = false;

          // check if exists in cellphone
          if(!ContactHelper.contactExists(context.getApplicationContext(), contact.getString("numero")) ){
            // try to create it
            if( ContactHelper.insertContact(context.getContentResolver(), contact.getString("nombre"), contact.getString("numero")) ){
              process = true;
            }
          } else {
            process = true;
          }

          if(process)
            _numeros.put(contact);
        }

        // send whatsapp intent
        whatsappIntent = new Intent();
        whatsappIntent.setAction(Intent.ACTION_SEND);
        whatsappIntent.setPackage("com.whatsapp");
        whatsappIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        whatsappIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // attach messahe
        if(args.getJSONObject(0).getString("mensaje") != null){
          try {
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, args.getJSONObject(0).getString("mensaje"));
            whatsappIntent.setType("text/plain");
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        // attach file
        if(args.getJSONObject(0).getString("attachment") != null){
          try {
            whatsappIntent.putExtra("android.intent.extra.STREAM", Uri.parse( args.getJSONObject(0).getJSONObject("attachment").getString("uri") ));
            whatsappIntent.setType(args.getJSONObject(0).getJSONObject("attachment").getString("type"));
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        // open whatsapp
        this.openWhatsapp();

        // active AccessibilityService

        KAccessibilityService.activated = true;

        super.cordova.getThreadPool().execute(new Runnable() {
          @Override
          public void run() {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, _numeros.length()));
          }
        });
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

  public void openWhatsapp(){
    super.cordova.getActivity().startActivity(whatsappIntent);
  }
}
