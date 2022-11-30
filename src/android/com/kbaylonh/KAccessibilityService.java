package com.kbaylonh;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class KAccessibilityService extends AccessibilityService {

    private final String TAG = this.getClass().getName();
    private static KAccessibilityService instance = null;
    public static boolean activated = false;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "servicio creado");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY;
        info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;

        info.notificationTimeout = 0;
        this.setServiceInfo(info);
        Log.v(TAG, "***** connectedAccessibility *****");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.v(TAG, "***** onAccessibilityEvent");
    }

    @Override
    public void onInterrupt() {
    }

    public void doAction(String actionName) {
      Log.v(TAG, "***** actionAccessibilityEvent ****** :" + actionName);
      boolean result = true;
      switch(actionName) {
        case "split":
          result = KAccessibilityService.instance.performGlobalAction(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN);
        break;
        case "home":
          result = KAccessibilityService.instance.performGlobalAction(GLOBAL_ACTION_HOME);
        break;
      }
      Log.v(TAG, "AccessibilityCall" + result);
    }
}
