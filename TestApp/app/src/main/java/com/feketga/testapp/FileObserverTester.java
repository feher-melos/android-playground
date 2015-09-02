package com.feketga.testapp;

import android.os.FileObserver;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by gabor on 11.6.2015.
 */
public class FileObserverTester {
    private final static String TAG = MainActivity.class.getSimpleName();

    private FileObserver mDirObserver;
    private int mCounter = 0;

    public void startDirObserver() {
        File dir = new File("/mnt/extSdCard");
        Common.dumpFilePathStuff(TAG, dir, "dir");

        FileObserver fo = new FileObserver("/mnt/extSdCard") {
            @Override
            public void onEvent(int event, String path) {
                Log.d(TAG, "Event : " + event + ", path: " + path);
            }
        };
        mDirObserver = fo;
        mDirObserver.startWatching();
    }

    public void createSomeFile() {
        try {
            switch (mCounter) {
                case 0: {
                    File f = new File("/mnt/extSdCard/test1.txt");
                    f.createNewFile();
                    break;
                }
                case 1: {
                    File f = new File("/mnt/extSdCard/test_dir");
                    f.mkdirs();
                    break;
                }
                case 2: {
                    File f = new File("/mnt/extSdCard/test_dir/test2.txt");
                    f.createNewFile();
                    break;
                }
                default: {
                    new File("/mnt/extSdCard/test1.txt").delete();
                    new File("/mnt/extSdCard/test_dir/test2.txt").delete();
                    new File("/mnt/extSdCard/test_dir").delete();
                    mCounter = -1;
                    break;
                }
            }
            ++mCounter;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }
    }
}
