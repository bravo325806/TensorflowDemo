package com.example.cheng.tensorflowdemo.utils;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FileUtils {
 
    /**
     * 從 uri 取得檔案路徑 這將從 Storage Access Framework Documents 取得路徑
     * 也會使用從 _data 欄位取得 MediaStore 以及其他 file-based ContentProviders 的對應路徑
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {
 
        // DocumentProvider
        if ( DocumentsContract.isDocumentUri(context, uri) ) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
 
                if ("primary".equalsIgnoreCase(type)) {
                    String path = Environment.getExternalStorageDirectory().getPath();
 
                    if (split.length > 1) {
                        path += "/" + split[1];
                    }
 
                    return path;
                }
                else {
                    String path;
                    if (Environment.isExternalStorageRemovable()) {
                        path = System.getenv("EXTERNAL_STORAGE");
                    } else {
                        path = System.getenv("SECONDARY_STORAGE");
                        if (path == null || path.length() == 0) {
                            path = System.getenv("EXTERNAL_SDCARD_STORAGE");
                        }
                    }
 
                    if (split.length > 1) {
                        path += "/" + split[1];
                    }
 
                    return path;
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
 
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
 
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
 
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
 
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };
 
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri)) return uri.getLastPathSegment();
 
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
 
        return null;
    }

    public static Bitmap getBitmap(String filePath) {
        return BitmapFactory.decodeFile(filePath);
    }

    public static String encode(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();

        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public static BitmapDrawable decode(String imageBase64) {

        BitmapDrawable bitmapDrawable = null;

        if (imageBase64.length() > 50) {
            try {
                byte[] data = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                bitmapDrawable = new BitmapDrawable(bitmap);
            } catch (Exception e) {
                Log.e("bug", e.toString());
            }
        }

        return bitmapDrawable;
    }

    public static Bitmap resize(Bitmap bmp) {

        int oldwidth = bmp.getWidth();
        int oldheight = bmp.getHeight();
        Log.i("Alex","SettingManager====>bmp size is :"+oldwidth+","+oldheight);
        float scaleWidth = 128 / (float)oldwidth;
        float scaleHeight = 128 / (float)oldheight;
        Log.i("Alex","SettingManager====> set scale value : "+scaleWidth + ":" + scaleHeight);
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
// create the new Bitmap object
        Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, oldwidth,oldheight, matrix, true);


        return resizedBitmap;
    }
 
    /**
     * 從 data 欄位取得 uri 對應的實體路徑
     * 主要用以取得 MediaStore Uris 以及其他 file-based ContentProviders 的對應路徑
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @author paulburke
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {
 
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };
 
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
 
 
    /**
     * @param uri The Uri to check.
     * @return 是否為 ExternalStorageProvider 類型的 uri
     * @author paulburke
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
 
    /**
     * @param uri The Uri to check.
     * @return 是否為 DownloadsProvider 類型的 uri
     * @author paulburke
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
 
    /**
     * @param uri The Uri to check.
     * @return MediaProvider 類型的 uri
     * @author paulburke
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
 
    /**
     * @param uri The Uri to check.
     * @return 是否為 Google Photos 類型的 uri
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     *
     * @param file
     * @return 矯正照片的方向
     */
    public static Bitmap getBitmapRotation(String file) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file, bounds);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(file, opts);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;

        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        return Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
    }
    // 檢查檔案類型的小範例
 
    /*
     * 檢查取回的檔案副檔名
     * @param path  檔案路徑
     * @return
     *  若檔案類型正確則回傳檔案名稱, 反之則回傳空字串
     * @throws EmptyFilePathException 路徑是空字串或是 null 物件
     * @throws FileNotFoundException 檔案不存在
     * @throws IllegalTrueTypeFontException 檔案類型不正確
     */
}