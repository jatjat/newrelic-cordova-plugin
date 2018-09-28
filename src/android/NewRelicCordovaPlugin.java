//  New Relic for Mobile -- Android edition
//
//  See:
//    https://docs.newrelic.com/docs/releases/android for release notes
//
//  Copyright (c) 2017 New Relic. All rights reserved.
//  See https://docs.newrelic.com/docs/licenses/android-agent-licenses for license details
//

package com.newrelic.cordova.plugin;

import android.util.Log;

import com.newrelic.agent.android.Agent;
import com.newrelic.agent.android.ApplicationPlatform;
import com.newrelic.agent.android.NewRelic;
import com.newrelic.agent.android.analytics.AnalyticAttribute;
import com.newrelic.agent.android.harvest.DeviceInformation;
import com.newrelic.agent.android.logging.AgentLog;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import org.json.JSONObject;
import java.util.*;

public class NewRelicCordovaPlugin extends CordovaPlugin {
    private final static String TAG = NewRelicCordovaPlugin.class.getSimpleName();

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        String appToken = preferences.getString("ANDROID_APP_TOKEN", null);

        if (appToken == null || appToken.isEmpty() || "x".equals(appToken)) {
            Log.e(TAG, "Failed to load application token! The Android agent is not configured for Cordova.");

        } else {
            NewRelic.withApplicationToken(appToken)
                    .start(this.cordova.getActivity().getApplication());

            final String pluginVersion = preferences.getString("PLUGIN_VERSION", "undefined");
            final DeviceInformation devInfo = Agent.getDeviceInformation();

            devInfo.setApplicationPlatform(ApplicationPlatform.Cordova);
            devInfo.setApplicationPlatformVersion(pluginVersion);

            NewRelic.setAttribute(AnalyticAttribute.APPLICATION_PLATFORM_VERSION_ATTRIBUTE, pluginVersion);            
        }

    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        if (action.equals("recordCustomEvent")) {
            String eventType = data.getString(0);
            String eventName = data.getString(1);
            JSONObject eventAttributes = data.getJSONObject(2);
            Map<String, Object> eventAttributesMap = toMap(eventAttributes);

            Boolean success = NewRelic.recordCustomEvent(eventType, eventName, eventAttributesMap);

            if (success == true) {
                callbackContext.success("Custom event was succesfully sent to New Relic");
            } else {
                callbackContext.error("Custom event failed to be sent to New Relic");
            }

            return true;
        } else {
            return false;
        }
    }

    private static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();
    
        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);
    
            if(value instanceof JSONArray) {
                throw new JSONException("Cannot send a list to New Relic as a custom event attribute");
            }
    
            else if(value instanceof JSONObject) {
                throw new JSONException("Cannot send a map to New Relic as a value in a custom event attribute");
            }
            map.put(key, value);
        }
        return map;
    }
}
