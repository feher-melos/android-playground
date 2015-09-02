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

    public static void testSdCardAppDir(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            File[] externalFilesDirs = context.getExternalFilesDirs(null);
            for (File file : externalFilesDirs) {
                if (file.getAbsolutePath().startsWith("/storage/sdcard")) {
                    createTestFile(new File(file.getAbsolutePath() + "/.uuid"));
                }
            }
        }
    }

    private static void createTestFile(File f) {
        try {
            f.createNewFile();
            FileWriter fstream = new FileWriter(f, true);
            BufferedWriter bw = new BufferedWriter(fstream);
            try {
                bw.write("Hello world!\n");
                Log.d(TAG, "Created test file: " + f.getAbsolutePath());
            } finally {
                bw.close();
            }
        } catch (IOException e) {
            Log.d(TAG, "Cannot create: " + f.getAbsolutePath());
        }
    }

    public static String testDirPaths(Context context) {
        StringBuilder b = new StringBuilder();

        String envSecondaryStorage = System.getenv("SECONDARY_STORAGE");
        if (envSecondaryStorage != null) {
            myLog(b, "System.getenv(\"SECONDARY_STORAGE\"): " + envSecondaryStorage);
        } else {
            myLog(b, "System.getenv(\"SECONDARY_STORAGE\"): NOT AVAILABLE");
        }
        dumpDir(b, Environment.getExternalStorageDirectory(), "Environment.getExternalStorageDirectory()");
        dumpDir(b, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)");

        dumpDir(b, context.getDir("privateTestFile", Context.MODE_PRIVATE), "context.getDir(\"privateTestFile\", MODE_PRIVATE)");
        dumpDir(b, context.getFilesDir(), "context.getFilesDir()");
        dumpDir(b, context.getCacheDir(), "context.getCacheDir()");
        dumpDir(b, context.getDatabasePath("testDbase"), "context.getDatabasePath(\"testDbase\")");
        dumpDir(b, context.getExternalFilesDir(null), "context.getExternalFilesDir(null)");
        dumpDir(b, context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            File[] externalFilesDirs = context.getExternalFilesDirs(null);
            for (File file : externalFilesDirs) {
                dumpDir(b, file, "context.getExternalFilesDirs(null)");
            }
        } else {
            myLog(b, "context.getExternalFilesDirs(null): NOT SUPPORTED");
        }

        return b.toString();
    }

    private static void dumpDir(StringBuilder b, File file, String message) {
        if (file != null) {
            myLog(b, message + ": " + file.getAbsolutePath());
        } else {
            myLog(b, message + ": NOT AVAILABLE");
        }
    }

    private static void myLog(StringBuilder b, String message) {
        Log.d(TAG, message);
        if (b != null) {
            b.append(message).append("\n\n");
        }
    }

}
