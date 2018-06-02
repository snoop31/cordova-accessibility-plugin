package com.kbaylonh;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
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
import com.kbaylonh.AccessibilityPlugin;

public class KAccessibilityService extends AccessibilityService {

    private final String TAG = this.getClass().getName();
    private static KAccessibilityService instance = null;
    private String numeroWhatsapp;
    public static boolean activated = false;
    public static int sent = 0;
    private int totalCount = 0;
    final private int limiter = 10;

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
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY;
        info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;

        info.notificationTimeout = 0;
        this.setServiceInfo(info);
        Log.d(TAG, "servicio conectado");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.v(TAG, "***** onAccessibilityEvent");
        if(KAccessibilityService.activated){
            Log.v(TAG, "Empezamos a enviar...");

            try {
                // Obtenemos los numeros a enviar
                JSONArray numeros = AccessibilityPlugin._numeros;
                // recorremos los numeros
                sent=0;
                for (int i = 0; i < numeros.length(); i++) {

                    if(i%limiter == 0){
                        if(!openSearchMenuItem()){
                            break;
                        }
                    }

                    JSONObject obj = numeros.getJSONObject(i);
                    String nombre = obj.getString("nombre");
                    numeroWhatsapp = obj.getString("numero");

                    Log.v(TAG, "Nombre:" + nombre + ", Numero: " + numeroWhatsapp);

                    // al pasar todas las validaciones, procedemos a preparar el envio de mensaje
                    searchContact();

                    Log.v(TAG, "Indice: " + i);

                    if((i+1)%limiter==0 || (i+1)==numeros.length()){

                        Log.v(TAG, "Total a enviar: " + totalCount);

                        // Enviamos el mensaje
                        AccessibilityNodeInfo h = sendButton();

                        sleep(2000);

                        while (h != null && totalCount > 0) {
                            sendClick(h);
                            h.recycle();
                            sleep(2000);

                            //if(totalCount == 1){
                                h = sendButton();
                            /*} else {
                                h = null;
                            }*/
                        }

                        sleep(1000);
                        totalCount = 0;


                        if((i+1) != numeros.length()){
                            AccessibilityPlugin.instance.openWhatsapp();
                        }

                    }
                }

                Log.v(TAG, "Proceso terminado.");

                //performGlobalAction(1);
                ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
                am.killBackgroundProcesses("com.whatsapp");
                activated = false;
                //MiPlugin.started = false;

                //sleep(1000L);
                //MainActivity.instance.openWhatsapp();

                // Minimizamos whatsapp
                /*
                // descansamos un momento
                sleep(1000L);

                // Volvemos a empezar
                MainActivity.instance.init();*/

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        } else {
            //Log.v(TAG, "Esperando respuesta de MainActivity....");
        }
    }

    @Override
    public void onInterrupt() {
    }

    private void searchContact(){
        // Paso la validacion, si lo encontro
        AccessibilityNodeInfo a = findNode("com.whatsapp:id/search_src_text");

        pasteInSearch(numeroWhatsapp, a);

        sleep(1500);

        if (a != null) {
            a.recycle();
        }

        a = findNode("android:id/list");

        // Pregunta si encuentra el elemento lista
        if(a == null){
            Log.e(TAG, "Imposible en encontrar ListView...");
            return;
        }

        Log.v(TAG, "findContact and click");

        boolean booleanValue = searchContactInList(a);
        Log.v(TAG, "First try: is contact found=" + booleanValue);

        //
        sleep(1500);

        while (!booleanValue && a.performAction(4096)) {
            // descansar
            sleep(1000);

            // intentar de nuevo
            Log.d(TAG, "retry to find contact");

            booleanValue = searchContactInList(a);

            Log.v(TAG, "is contact found=" + booleanValue);
        }

        if(booleanValue){
            Log.v(TAG, numeroWhatsapp + " encontrado en la lista");
            sent++;
            totalCount++;
            AccessibilityNodeInfo a2 = findNode("com.whatsapp:id/search_close_btn");
            if (a2 == null) {
                Log.e(TAG, "Search for CloseButtonNode failed");
            }

            sendClick(a2);
            if (a2 != null) {
                a2.recycle();
            }
            a.recycle();

            //MiPlugin.started = false;
            //activated = false;
        } else {
            Log.e(TAG, numeroWhatsapp + " No se pudo encontrar el numero en la lista");
        }
    }

    private void sleep(long j){
        try {
            Thread.sleep(j);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private AccessibilityNodeInfo findNode(String str) {
        List findAccessibilityNodeInfosByViewId;
        int i = 0;
        do {
            AccessibilityNodeInfo rootInActiveWindow = instance.getRootInActiveWindow();
            if (rootInActiveWindow != null) {
                findAccessibilityNodeInfosByViewId = rootInActiveWindow.findAccessibilityNodeInfosByViewId(str);
                i++;
                sleep(500);
                if (findAccessibilityNodeInfosByViewId != null && findAccessibilityNodeInfosByViewId.size() != 0) {
                    break;
                }
            } else {
                return null;
            }
        } while (i < 10);
        return (findAccessibilityNodeInfosByViewId == null || findAccessibilityNodeInfosByViewId.size() == 0) ? null : (AccessibilityNodeInfo) findAccessibilityNodeInfosByViewId.get(0);
    }

    private boolean sendClick(AccessibilityNodeInfo accessibilityNodeInfo) {
        return accessibilityNodeInfo != null && accessibilityNodeInfo.performAction(16);
    }

    private void pasteInSearch(String str, AccessibilityNodeInfo accessibilityNodeInfo) {
        if (accessibilityNodeInfo == null) {
            Log.e(TAG,"cant find SearchTextBox");
        } else if (Build.VERSION.SDK_INT >= 21) {
            m10480b(str, accessibilityNodeInfo);
        } else {
            m10481c(str, accessibilityNodeInfo);
        }
    }
    private void m10480b(String str, AccessibilityNodeInfo accessibilityNodeInfo) {
        Bundle bundle = new Bundle();
        bundle.putCharSequence("ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE", str);
        accessibilityNodeInfo.performAction(2097152, bundle);
        Log.v(TAG, "Numero " + numeroWhatsapp + " pegado en la searchbox");
    }

    @TargetApi(18)
    private void m10481c(String str, AccessibilityNodeInfo accessibilityNodeInfo) {
        Log.d(TAG, "API level below 21, using clipboard to send data");
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String str2 = "";
        try {
            str2 = clipboardManager.getPrimaryClip().getItemAt(0).coerceToText(this).toString();
        } catch (Throwable e) {
            Throwable th = e;
            str2 = "";
            e.printStackTrace();
        }
        Log.d(TAG, "Last clip on clipboard=" + str2);
        Log.d(TAG, "The WhatsApp search editText node info" + accessibilityNodeInfo.toString());
        Log.d(TAG, "setting our search text as primary clipboard text=" + str);
        str2 = "SKEDit_input";
        clipboardManager.setPrimaryClip(ClipData.newPlainText("SKEDit_input", str));
        Log.d(TAG, "CHECK: new primary clip data=" + clipboardManager.getPrimaryClip().getItemAt(0).coerceToText(this));
        Log.d(TAG, "CHECK, is new primary clip ours=" + clipboardManager.getPrimaryClip().getItemAt(0).coerceToText(this).toString().equals(str));
        Log.d(TAG, "Refreshing edit search edit text=" + accessibilityNodeInfo.refresh());
        sleep(500);
        Log.d(TAG, "Pasting text to search edit text=" + accessibilityNodeInfo.performAction(32768));
    }

    private Boolean searchContactInList(AccessibilityNodeInfo accessibilityNodeInfo) {
        boolean z2 = false;
        if (accessibilityNodeInfo != null) {
            accessibilityNodeInfo.refresh();
            for (int i = 0; i < accessibilityNodeInfo.getChildCount(); i++) {
                AccessibilityNodeInfo child = accessibilityNodeInfo.getChild(i);
                if (child != null) {
                    if(child.getChildCount()>1){
                        sendClick(child);
                        child.recycle();
                        z2 = true;
                        break;
                    }
                    child.recycle();
                }
            }
        }
        return z2;
    }

    private AccessibilityNodeInfo sendButton() {
        return findNode("com.whatsapp:id/send");
    }

    private boolean openSearchMenuItem(){

        sleep(1000);

        AccessibilityNodeInfo node = findNode("com.whatsapp:id/menuitem_search");

        sleep(1000);

        return node != null && sendClick(node);
    }
}
