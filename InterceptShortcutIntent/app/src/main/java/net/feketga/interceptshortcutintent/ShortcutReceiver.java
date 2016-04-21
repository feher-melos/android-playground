package net.feketga.interceptshortcutintent;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;

public class ShortcutReceiver extends BroadcastReceiver {
    private static final String TAG = ShortcutReceiver.class.getSimpleName();

    public ShortcutReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "ZIZI RECEIVED");
        dumpIntent(intent, "");
    }

    private void dumpIntent(@Nullable Intent i, String prefix) {
        Log.e(TAG, prefix + "Dumping Intent start");

        if (i == null) {
            Log.e(TAG, prefix + "Intent is null");
        } else {
            Log.e(TAG, prefix + "Action : " + i.getAction());

            Set<String> categories = i.getCategories();
            if (categories != null) {
                for (String category : categories) {
                    Log.e(TAG, prefix + "Category: " + category);
                }
            } else {
                Log.e(TAG, prefix + "Categories: null");
            }

            Log.e(TAG, prefix + "Type: " + i.getType());

            ComponentName componentName = i.getComponent();
            if (componentName != null) {
                Log.e(TAG, prefix + "Component: Package: " + componentName.getPackageName());
                Log.e(TAG, prefix + "Component: Class: " + componentName.getClassName());
            } else {
                Log.e(TAG, prefix + "Component: null");
            }

            Bundle extrasBundle = i.getExtras();
            if (extrasBundle != null) {
                Set<String> keys = extrasBundle.keySet();
                for (String key : keys) {
                    Object extraObject = extrasBundle.get(key);
                    Log.e(TAG, prefix + "Extra: [" + key + "=" + extraObject + "]");
                    if (key.equals(Intent.EXTRA_SHORTCUT_INTENT)) {
                        if (extraObject instanceof Parcelable) {
                            Intent shortcutIntent = extrasBundle.getParcelable(key);
                            dumpIntent(shortcutIntent, prefix + "    ");
                        } else if (extraObject instanceof String) {
                            try {
                                Intent intent = Intent.parseUri((String) extraObject, 0);
                                dumpIntent(intent, prefix + "    ");
                            } catch (URISyntaxException e) {
                                Log.e(TAG, "Syntax error. Cannot parse Intent from String.");
                            }
                        }
                    }
                }
            }
        }

        Log.e(TAG, prefix + "Dumping Intent end");
    }
}
