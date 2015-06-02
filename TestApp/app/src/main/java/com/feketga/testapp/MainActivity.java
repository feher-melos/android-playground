package com.feketga.testapp;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private Dummy d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testDirs();
            }
        });

        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MediaStoreTester().testGalleryProvider(MainActivity.this);
            }
        });

        d = new Dummy();
        d.data = 43;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void testDirs() {
        Context context = getApplicationContext();

        dumpDir(context.getCacheDir(), "context.getCacheDir()");
        dumpDir(context.getDir("privateTestFile", MODE_PRIVATE), "context.getDir(\"privateTestFile\", MODE_PRIVATE)");
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

    private void dumpDir(File file, String message) {
        if (file != null) {
            Log.d("ZIZI", message + ": " + file.getAbsolutePath());
        }
    }

}
