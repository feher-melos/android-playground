package com.feketga.testapp;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaStoreTester {

    private final static String TAG = MainActivity.class.getSimpleName();

    public void testGalleryProvider(Context context) {
        dumpContent(context, MediaStore.Images.Media.INTERNAL_CONTENT_URI, true);
        dumpContent(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true);

        dumpImageInfo(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "Grip ring.jpg", true);
    }

    public void testFileAbsolutePath() {
        String workDir = System.getProperty("user.dir");

        Log.d(TAG, "Current working directory: " + workDir);
        File f1 = new File("TestFile.txt");
        File f2 = new File("TestDir/TestFile.txt");
        File f3 = new File("./TestDir/TestFile.txt");
        File f4 = new File("/TestDir/TestFile.txt");
        Common.dumpFilePathStuff(TAG, f1, "f1");
        Common.dumpFilePathStuff(TAG, f2, "f2");
        Common.dumpFilePathStuff(TAG, f3, "f3");
        Common.dumpFilePathStuff(TAG, f4, "f4");

        System.setProperty("user.dir", "/mnt/sdcard");
        Log.d(TAG, "New current working directory: " + System.getProperty("user.dir"));
        Common.dumpFilePathStuff(TAG, f1, "f1");
        Common.dumpFilePathStuff(TAG, f2, "f2");
        Common.dumpFilePathStuff(TAG, f3, "f3");
        Common.dumpFilePathStuff(TAG, f4, "f4");

        File f5 = new File("/geza/", "/bela");
        Common.dumpFilePathStuff(TAG, f5, "f5");

        System.setProperty("user.dir", workDir);
    }

    private void dumpContent(Context context, Uri uri, boolean isVerbose) {
        Log.d(TAG, "---");
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
        Log.d(TAG, "---");
        Log.d(TAG, "Info of " + fileName);
        ContentResolver cr = context.getContentResolver();

        // SELECT _id, _display_name FROM authority WHERE _display_name LIKE ?
        Cursor cursor =
                cr.query(
                        uri,
                        new String[]{MediaStore.Images.ImageColumns._ID,
                                MediaStore.Images.ImageColumns.DISPLAY_NAME},
                        MediaStore.Images.ImageColumns.DISPLAY_NAME + " LIKE ?",
                        new String[]{fileName},
                        null);
        Log.d(TAG, "Matching image count: " + cursor.getCount());
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                dumpItemInfo(cursor, isVerbose);
            }
        }
    }

    private void dumpItemInfo(Cursor cursor, boolean isVerbose) {
        Log.d(TAG, "--- ---");
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
        String filePath = "";
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

            if (columnNames[i].equals(MediaStore.Images.Media.DATA)) {
                filePath = value;
            }
        }

        dumpImageExifInfo(filePath);
    }

    private void dumpImageExifInfo(String imageFilePath) {
        Log.d(TAG, "--- --- ---");
        try {
            ExifInterface exif = new ExifInterface(imageFilePath);
            dumpImageExifTag(exif, ExifInterface.TAG_APERTURE, "");
            dumpImageExifTag(exif, ExifInterface.TAG_DATETIME, "");
            dumpImageExifTag(exif, ExifInterface.TAG_EXPOSURE_TIME, "");
            dumpImageExifTag(exif, ExifInterface.TAG_FLASH, 0);
            dumpImageExifTag(exif, ExifInterface.TAG_FOCAL_LENGTH, 0.0);
            dumpImageExifTag(exif, ExifInterface.TAG_GPS_ALTITUDE, 0.0);
            dumpImageExifTag(exif, ExifInterface.TAG_GPS_ALTITUDE_REF, 0);
            dumpImageExifTag(exif, ExifInterface.TAG_GPS_DATESTAMP, "");
            dumpImageExifTag(exif, ExifInterface.TAG_GPS_LATITUDE, "");
            dumpImageExifTag(exif, ExifInterface.TAG_GPS_LATITUDE_REF, "");
            dumpImageExifTag(exif, ExifInterface.TAG_GPS_LONGITUDE, "");
            dumpImageExifTag(exif, ExifInterface.TAG_GPS_LONGITUDE_REF, "");
            dumpImageExifTag(exif, ExifInterface.TAG_GPS_PROCESSING_METHOD, "");
            dumpImageExifTag(exif, ExifInterface.TAG_GPS_TIMESTAMP, "");
            dumpImageExifTag(exif, ExifInterface.TAG_IMAGE_LENGTH, 0);
            dumpImageExifTag(exif, ExifInterface.TAG_IMAGE_WIDTH, 0);
            dumpImageExifTag(exif, ExifInterface.TAG_ISO, "");
            dumpImageExifTag(exif, ExifInterface.TAG_MAKE, "");
            dumpImageExifTag(exif, ExifInterface.TAG_MODEL, "");
            dumpImageExifTag(exif, ExifInterface.TAG_ORIENTATION, 0);
            dumpImageExifTag(exif, ExifInterface.TAG_WHITE_BALANCE, 0);
        } catch (IOException e) {
            Log.d(TAG, "Cannot read EXIF from " + imageFilePath);
        }
    }

    private void dumpImageExifTag(ExifInterface exif, String tag, String typeTag) {
        String value = exif.getAttribute(tag);
        if (value == null) {
            value = "<n/a>";
        }
        Log.d(TAG, tag + ": " + value);
    }

    private void dumpImageExifTag(ExifInterface exif, String tag, int typeTag) {
        String value = String.valueOf(exif.getAttributeInt(tag, -1));
        if (value.equals("-1")) {
            value = "<n/a>";
        }
        Log.d(TAG, tag + ": " + value);
    }

    private void dumpImageExifTag(ExifInterface exif, String tag, double typeTag) {
        String value = String.valueOf(exif.getAttributeDouble(tag, -1.0));
        if (value.equals("-1.0")) {
            value = "<n/a>";
        }
        Log.d(TAG, tag + ": " + value);
    }

//    private <T> void dumpImageExifTag(ExifInterface exif, String tag, T typeTag) {
//        Log.d(TAG, tag + ": " + getImageExifTag(exif, tag, typeTag));
//    }
//
//    private String getImageExifTag(ExifInterface exif, String tag, String _typeTag) {
//        return exif.getAttribute(tag);
//    }
//    private String getImageExifTag(ExifInterface exif, String tag, Integer _typeTag) {
//        return String.valueOf(exif.getAttributeInt(tag, -1));
//    }
//    private String getImageExifTag(ExifInterface exif, String tag, Double _typeTag) {
//        return String.valueOf(exif.getAttributeDouble(tag, -1.0));
//    }

}
