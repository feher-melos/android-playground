package com.feketga.testapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private Dummy d;
    private FileObserverTester mFileObserverTester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final TextView t = (TextView) findViewById(R.id.output_text);

        Button button1 = (Button) findViewById(R.id.button1);
        button1.setText("Dir Paths");
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s1 = SecondaryStorageTester.testDirPaths(getApplicationContext());
                String s2 = SecondaryStorageTester.testDirPaths(MainActivity.this);
                t.setText(s1 + s2);
//                t.setText(DateAndTime.testCalendarMonth());
            }
        });

//        Button button2 = (Button) findViewById(R.id.button2);
//        button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new MediaStoreTester().testGalleryProvider(MainActivity.this);
//            }
//        });

        Button button2 = (Button) findViewById(R.id.button2);
        button2.setText("Dir Observer");
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFileObserverTester == null) {
                    mFileObserverTester = new FileObserverTester();
                    mFileObserverTester.startDirObserver();
                    mFileObserverTester.startDirObserver();
                } else {
                    mFileObserverTester.createSomeFile();
                }
            }
        });

        Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MediaStoreTester().testFileAbsolutePath();
            }
        });

        Button button4 = (Button) findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SecondaryStorageTester.testRootOfSdCard(MainActivity.this);
            }
        });

        Button button5 = (Button) findViewById(R.id.button5);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SecondaryStorageTester.testSdCardAppDir(MainActivity.this);
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

}
