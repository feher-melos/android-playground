package com.feketga.concurrentdatabaseaccess;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Random;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    DbHelper mDbHelper;
    @Nullable private Thread mThread1;
    @Nullable private Thread mThread2;

    private boolean mAlwaysCloseDb;

    private static final int ITERATION_COUNT = 1000;
    final Runnable mWriterRunnable1 = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < ITERATION_COUNT; ++i) {
                SQLiteDatabase wdb = mDbHelper.getWritableDatabase();
                boolean isInterrupted = writeToDb(wdb, String.valueOf(i), 2000, mAlwaysCloseDb);
                if (Thread.interrupted() || isInterrupted) {
                    return;
                }
            }
        }
    };

    final Runnable mWriterRunnable2 = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < ITERATION_COUNT; ++i) {
                SQLiteDatabase wdb = mDbHelper.getWritableDatabase();
                boolean isInterrupted = writeToDb(wdb, String.valueOf(i), 4000, mAlwaysCloseDb);
                if (Thread.interrupted() || isInterrupted) {
                    return;
                }
            }
        }
    };

    final Runnable mReaderRunnable = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < ITERATION_COUNT; ++i) {
                SQLiteDatabase rdb = mDbHelper.getReadableDatabase();
                boolean isInterrupted = readFromDb(rdb, String.valueOf(i), 0, mAlwaysCloseDb);
                if (Thread.interrupted() || isInterrupted) {
                    return;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);


        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectAll()
                .penaltyLog()
                .penaltyFlashScreen()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());



        mDbHelper = new DbHelper(this);

        Button b = (Button) findViewById(R.id.button);
        b.setText("(Bad) 2 writers, always close");
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopThreads();
                mAlwaysCloseDb = false;
                testConcurrentWriters();
            }
        });

        b = (Button) findViewById(R.id.button2);
        b.setText("(Bad) Reader and writer, always close");
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopThreads();
                mAlwaysCloseDb = false;
                testConcurrentReaderAndWriter();
            }
        });

        b = (Button) findViewById(R.id.button3);
        b.setText("(Bad) Reader and writer, close writer before reader");
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopThreads();
                mAlwaysCloseDb = false;
                testConcurrentReaderAndWriter_closeBeforeReader();
            }
        });

        b = (Button) findViewById(R.id.button4);
        b.setText("(Bad) Reader and writer, loop, always close");
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopThreads();
                mAlwaysCloseDb = true;
                testConcurrentReaderWriterLoop();
            }
        });

        b = (Button) findViewById(R.id.button5);
        b.setText("(OK) Reader and writer loop, close once");
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopThreads();
                mAlwaysCloseDb = false;
                testConcurrentReaderWriterLoop();
            }
        });

//        b = (Button) findViewById(R.id.button6);
//        b.setText("(OK) 2 writers loop, close once");
//        b.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                stopThreads();
//                mAlwaysCloseDb = false;
//                testConcurrentWritersLoop();
//            }
//        });
        b = (Button) findViewById(R.id.button6);
        b.setText("Query test");
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopThreads();
                testQuery();
            }
        });
    }

    @Override
    protected void onDestroy() {
        stopThreads();
        super.onDestroy();
    }

    private static final String DB_NAME = "MyDbase";
    private static final int DB_VERSION = 1;

    public static final String TABLE_NAMES = "NAMES";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "NAME";

    private class DbHelper extends SQLiteOpenHelper {
        public DbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createFileTable = "CREATE TABLE " + TABLE_NAMES + " ("
                    + COLUMN_ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_NAME + " TEXT )";
            db.execSQL(createFileTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        }
    }

    private void testConcurrentWriters() {
        purgeDB(mDbHelper.getWritableDatabase());

        final SQLiteDatabase wdb1 = mDbHelper.getWritableDatabase();
        final SQLiteDatabase wdb2 = mDbHelper.getWritableDatabase();

        final Random random = new Random();
        final String name1 = String.valueOf(random.nextInt(1000));
        final String name2 = String.valueOf(random.nextInt(1000));

        Log.d(TAG, "START write: " + name1);
        wdb1.beginTransaction();
        try {
            final ContentValues cv1 = new ContentValues();
            cv1.put(COLUMN_NAME, name1);
            final ContentValues cv2 = new ContentValues();
            cv2.put(COLUMN_NAME, name2);

            Log.d(TAG, "START write: " + name2);
            wdb2.beginTransaction();
            wdb2.update(TABLE_NAMES, cv2, null, null);
            wdb2.setTransactionSuccessful();
            wdb2.endTransaction();
            wdb2.close(); // This closes also wdb1 because wdb1 equals wdb2.
            Log.d(TAG, "END write: " + name2);

            wdb1.update(TABLE_NAMES, cv1, null, null);
            wdb1.setTransactionSuccessful();
        } finally {
            wdb1.endTransaction();
        }
        wdb1.close();
        Log.d(TAG, "END write: " + name1);
    }

    private void testConcurrentReaderAndWriter() {
        purgeDB(mDbHelper.getWritableDatabase());

        final SQLiteDatabase wdb = mDbHelper.getWritableDatabase();
        final SQLiteDatabase rdb = mDbHelper.getReadableDatabase();

        final Random random = new Random();
        final String name1 = String.valueOf(random.nextInt(1000));
        final String name2 = String.valueOf(random.nextInt(1000));

        Log.d(TAG, "START write: " + name1);
        wdb.beginTransaction();
        try {
            final ContentValues cv1 = new ContentValues();
            cv1.put(COLUMN_NAME, name1);
            final ContentValues cv2 = new ContentValues();
            cv2.put(COLUMN_NAME, name2);


            Log.d(TAG, "START read: " + name2);
            final String sql =
                    String.format(
                            "SELECT * FROM %s WHERE %s = ?",
                            TABLE_NAMES, COLUMN_NAME);
            Cursor cursor = rdb.rawQuery(sql, new String[]{name2});
            try {
                while (cursor.moveToNext()) {
                    Log.d(TAG, cursor.getString(0));
                }
            } finally {
                cursor.close();
            }
            rdb.close(); // This also closes wdb because they are the same object.
            Log.d(TAG, "END read: " + name2);

            wdb.update(TABLE_NAMES, cv1, null, null);
            wdb.setTransactionSuccessful();
        } finally {
            wdb.endTransaction();
        }
        wdb.close();
        Log.d(TAG, "END write: " + name1);

    }

    private void testConcurrentReaderAndWriter_closeBeforeReader() {
        purgeDB(mDbHelper.getWritableDatabase());

        final SQLiteDatabase wdb = mDbHelper.getWritableDatabase();
        final SQLiteDatabase rdb = mDbHelper.getReadableDatabase();

        final Random random = new Random();
        final String name1 = String.valueOf(random.nextInt(1000));
        final String name2 = String.valueOf(random.nextInt(1000));

        // WRITER

        Log.d(TAG, "START write: " + name1);
        wdb.beginTransaction();
        try {
            final ContentValues cv1 = new ContentValues();
            cv1.put(COLUMN_NAME, name1);
            wdb.update(TABLE_NAMES, cv1, null, null);
            wdb.setTransactionSuccessful();
        } finally {
            wdb.endTransaction();
        }
        //wdb.close(); // We close the database.
        Log.d(TAG, "END write: " + name1);

        // READER

        final ContentValues cv2 = new ContentValues();
        cv2.put(COLUMN_NAME, name2);
        Log.d(TAG, "START read: " + name2);
        final String sql =
                String.format(
                        "SELECT * FROM %s WHERE %s = ?",
                        TABLE_NAMES, COLUMN_NAME);
        Cursor cursor = rdb.rawQuery(sql, new String[]{name2});
        rdb.close(); // This also closes wdb because they are the same object.
        try {
            while (cursor.moveToNext()) {
                Log.d(TAG, cursor.getString(0));
            }
        } finally {
            cursor.close();
        }
        Log.d(TAG, "END read: " + name2);
    }

    private void testConcurrentReaderWriterLoop() {
        purgeDB(mDbHelper.getWritableDatabase());

        mThread1 = new Thread(mWriterRunnable1);
        mThread1.start();
        mThread2 = new Thread(mReaderRunnable);
        mThread2.start();
    }

    private void testConcurrentWritersLoop() {
        purgeDB(mDbHelper.getWritableDatabase());

        mThread1 = new Thread(mWriterRunnable1);
        mThread1.start();
        mThread2 = new Thread(mWriterRunnable2);
        mThread2.start();
    }

    private void testQuery() {
        purgeDB(mDbHelper.getWritableDatabase());

        final SQLiteDatabase wdb = mDbHelper.getWritableDatabase();
        final SQLiteDatabase rdb = mDbHelper.getReadableDatabase();

        final Random random = new Random();
        final String name1 = String.valueOf(random.nextInt(1000));
        final String name2 = String.valueOf(random.nextInt(1000));

        Log.d(TAG, "START write");
        wdb.beginTransaction();
        try {
            final ContentValues cv1 = new ContentValues();
            cv1.put(COLUMN_NAME, name1);
            final ContentValues cv2 = new ContentValues();
            cv2.put(COLUMN_NAME, name2);
            wdb.insert(TABLE_NAMES, null, cv1);
            wdb.insert(TABLE_NAMES, null, cv2);
            wdb.setTransactionSuccessful();
        } finally {
            wdb.endTransaction();
        }
        Log.d(TAG, "END write");

        Log.d(TAG, "START read");
        final String sql = String.format("SELECT * FROM %s", TABLE_NAMES);
        Cursor cursor1 = rdb.rawQuery(sql, null);
        cursor1.close();
        Cursor cursor2 = rdb.rawQuery(sql, null);
        while (cursor1.moveToNext()) {
            Log.d(TAG, "DATA 1: " + cursor1.getString(cursor1.getColumnIndex(COLUMN_NAME)));
        }
        while (cursor2.moveToNext()) {
            Log.d(TAG, "DATA 2: " + cursor2.getString(cursor2.getColumnIndex(COLUMN_NAME)));
        }
        cursor1.close();
        cursor2.close();
        Log.d(TAG, "END read");

        wdb.close();
        Log.d(TAG, "END write: " + name1);
    }

    private void stopThreads() {
        Log.d(TAG, "Stopping threads...");
        try {
            if (mThread1 != null) {
                mThread1.interrupt();
                mThread1.join();
                mThread1 = null;
            }
            if (mThread2 != null) {
                mThread2.interrupt();
                mThread2.join();
                mThread2 = null;
            }
            Log.d(TAG, "Threads stopped.");
        } catch (InterruptedException e) {
            Log.d(TAG, e.getMessage());
        }

        mDbHelper.getWritableDatabase().close();
        mDbHelper.getReadableDatabase().close(); // This does the same as the previous line.
    }

    private void purgeDB(SQLiteDatabase db) {
        String sql = String.format("DELETE FROM %s", TABLE_NAMES);
        db.beginTransaction();
        try {
            db.execSQL(sql);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    private boolean writeToDb(SQLiteDatabase db, String name, int workTime, boolean closeDb) {
        boolean isInterrupted = false;
        long startTime = System.currentTimeMillis();
        Log.d(TAG, "Thread " + Thread.currentThread().getId() + " " + name + " START write, estimated " + workTime/1000.0 + " seconds");
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_NAME, name);
            try {
                Thread.sleep(workTime); // This blocks the whole transaction.
            } catch (InterruptedException e) {
                isInterrupted = true;
            }
            // Just for delaying, we don't have to do anything.
            // The sleep will delay the transaction anyway.
            // db.update(TABLE_NAMES, cv, null, null);
            Log.d(TAG, "Thread " + Thread.currentThread().getId() + " " + name + " Transaction done");
            db.setTransactionSuccessful();
        } finally {
            Log.d(TAG, "Thread " + Thread.currentThread().getId() + " " + name + " Transaction end");
            db.endTransaction();
        }
        if (closeDb) {
            db.close();
        }
        long realWorkTime = System.currentTimeMillis() - startTime;
        Log.d(TAG, "Thread " + Thread.currentThread().getId() + " " + name + " END write, took " + realWorkTime/1000.0 + " seconds");
        return isInterrupted;
    }

    private boolean readFromDb(SQLiteDatabase db, String name, int workTime, boolean closeDb) {
        boolean isInterrupted = false;
        long startTime = System.currentTimeMillis();
        Log.d(TAG, "Thread " + Thread.currentThread().getId() + " " + name + " START read, estimated " + workTime/1000.0 + " seconds");
        String sql =
                String.format(
                        "SELECT * FROM %s WHERE %s = ?",
                        TABLE_NAMES, COLUMN_NAME);
        Log.d(TAG, "Thread " + Thread.currentThread().getId() + " " + name + " Query start");
        Cursor cursor = db.rawQuery(sql, new String[]{name});
        Log.d(TAG, "Thread " + Thread.currentThread().getId() + " " + name + " Query done");
        try {
            while (cursor.moveToNext()) {
                try {
                    Thread.sleep(workTime);
                } catch (InterruptedException e) {
                    isInterrupted = true;
                }
                Log.d(TAG, cursor.getString(0));
            }
        } finally {
            cursor.close();
        }
        if (closeDb) {
            db.close();
        }
        long realWorkTime = System.currentTimeMillis() - startTime;
        Log.d(TAG, "Thread " + Thread.currentThread().getId() + " " + name + " END read, took " + realWorkTime/1000.0 + " seconds");
        return isInterrupted;
    }

}
