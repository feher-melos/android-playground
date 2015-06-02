package com.feketga.testapp;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MediaStoreTester {

    private final static String TAG = MainActivity.class.getSimpleName();

    public void testGalleryProvider(Context context) {
        dumpContent(context, MediaStore.Images.Media.INTERNAL_CONTENT_URI, true);
        dumpContent(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true);

        dumpImageInfo(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "Grip ring.jpg", true);
    }


    private void dumpContent(Context context, Uri uri, boolean isVerbose) {
        Log.d(TAG, "Content of " + uri.toString());
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(uri, null, null, null, null);
        if (cursor.getCount() > 0) {
            Log.d(TAG, "Items count = " + cursor.getCount());

            String[] columnNames = cursor.getColumnNames();
            for (int i = 0; i < columnNames.length; ++i) {
                Log.d(TAG, "Column name: " + columnNames[i]);
            }

            while (cursor.moveToNext()) {
                dumpItemInfo(cursor, isVerbose);
            }
        }
    }

    private void dumpImageInfo(Context context, Uri uri, String fileName, boolean isVerbose) {
        Log.d(TAG, "Info of " + fileName);
        ContentResolver cr = context.getContentResolver();

        // SELECT _id, _display_name FROM authority WHERE _display_name LIKE ?
        Cursor cursor = cr.query(uri, new String[]{"_id","_display_name"}, "_display_name LIKE ?", new String[]{fileName}, null);
        Log.d(TAG, "Matching image count: " + cursor.getCount());
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                dumpItemInfo(cursor, isVerbose);
            }
        }
    }

    private void dumpItemInfo(Cursor cursor, boolean isVerbose) {
        if (isVerbose) {
            dumpVerboseItemInfo(cursor);
        } else {
            dumpBriefItemInfo(cursor);
        }
    }

    private void dumpBriefItemInfo(Cursor cursor) {
        int id = -1;
        String displayName = "";
        int idColumnIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID);
        int displayNameColumnIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME);
        if (displayNameColumnIndex != -1) {
            displayName = cursor.getString(displayNameColumnIndex);
        }
        if (idColumnIndex != -1) {
            id = cursor.getInt(idColumnIndex);
        }
        Log.d(TAG, "ID: " + id + ", Display name: " + displayName);
    }

    private void dumpVerboseItemInfo(Cursor cursor) {
        String[] columnNames = cursor.getColumnNames();
        for (int i = 0; i < columnNames.length; ++i) {
            Log.d(TAG, "Column name: " + columnNames[i]);
            int columnIndex = cursor.getColumnIndex(columnNames[i]);
            int columnType = cursor.getType(columnIndex);

            String value = "";
            switch (columnType) {
                case Cursor.FIELD_TYPE_INTEGER:
                    value = String.valueOf(cursor.getInt(columnIndex));
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    value = String.valueOf(cursor.getFloat(columnIndex));
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    value = cursor.getString(columnIndex);
                    break;
                case Cursor.FIELD_TYPE_BLOB:
                    value = "<BLOB>";
                    break;
                case Cursor.FIELD_TYPE_NULL:
                    value = "<NULL>";
                    break;
                default:
                    value = "<n/a>";
                    break;
            }
            Log.d(TAG, "       value: " + value);
        }
    }

}
