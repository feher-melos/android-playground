package com.feketga.testapp;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SecondaryStorageTester {
    private static final String TAG = SecondaryStorageTester.class.getSimpleName();

    public static void testRootOfSdCard(Context context) {
        System.getenv("SECONDARY_STORAGE");
        File f = new File("/mnt/extSdCard/test.txt");
        try {
            f.createNewFile();
            FileWriter fstream = new FileWriter(f, true);
            BufferedWriter bw = new BufferedWriter(fstream);
            try {
                bw.write("Hello world!\n");
            } finally {
                bw.close();
            }
            //bw.close();
        } catch (IOException e) {
            Log.d(TAG, "Cannot create: " + f.getAbsolutePath());
        }
    }

    public static void testDirPaths(Context context) {
        dumpDir(context.getCacheDir(), "context.getCacheDir()");
        dumpDir(context.getDir("privateTestFile", Context.MODE_PRIVATE), "context.getDir(\"privateTestFile\", MODE_PRIVATE)");
        dumpDir(context.getFilesDir(), "context.getFilesDir()");
        dumpDir(context.getDatabasePath("testDbase"), "context.getDatabasePath(\"testDbase\")");
        dumpDir(Environment.getExternalStorageDirectory(), "Environment.getExternalStorageDirectory()");
        dumpDir(context.getExternalFilesDir(null), "context.getExternalFilesDir(null)");
        dumpDir(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)");
        dumpDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            File[] externalFilesDirs = context.getExternalFilesDirs(null);
            for (File file : externalFilesDirs) {
                dumpDir(file, "context.getExternalFilesDirs(null)");
            }
        }
    }

    private static void dumpDir(File file, String message) {
        if (file != null) {
            Log.d("ZIZI", message + ": " + file.getAbsolutePath());
        }
    }

}
