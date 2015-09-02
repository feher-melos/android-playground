package com.feketga.testapp;

import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by gabor on 11.6.2015.
 */
public class Common {
    public static void dumpFilePathStuff(String TAG, File file, String variableName) {
        try {
            Log.d(TAG, variableName + ".getAbsolutePath(): " + file.getAbsolutePath());
            Log.d(TAG, variableName + ".getCanonicalPath(): " + file.getCanonicalPath());
            Log.d(TAG, variableName + ".getPath(): " + file.getPath());
            Log.d(TAG, variableName + ".getName(): " + file.getName());
            Log.d(TAG, variableName + ".isAbsolute(): " + file.isAbsolute());
        } catch (IOException e) {
            Log.d(TAG, "IOException happened: " + e.getMessage());
        }
    }
}
