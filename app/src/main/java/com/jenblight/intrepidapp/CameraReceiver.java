package com.jenblight.intrepidapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.jenblight.intrepidapp.data.CollectedPhoto;

import java.io.IOException;
import java.util.Calendar;

public class CameraReceiver extends BroadcastReceiver {
    public CameraReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        Uri uri = intent.getData();
        String realPath = getRealPathFromURI(uri, context);
        Log.d("Intrepid", realPath);
        float[] latlng = new float[2];

        try {

            ExifInterface exifInterface = new ExifInterface(realPath);
            exifInterface.getLatLong(latlng);
            Log.d("Intrepid", String.valueOf(latlng[0]));
            Log.d("Intrepid", String.valueOf(latlng[1]));
            if (latlng[0] == 0.0 || latlng[1] == 0.0){
                Log.d("Intrepid", "Location is off");
            } else {
                CollectedPhoto photo = new CollectedPhoto(Calendar.getInstance().getTime(), realPath, (double)latlng[0], (double)latlng[1]);
                photo.save(context);
            }

        } catch (IOException e) {

        }

    }

    private String getRealPathFromURI(Uri contentUri, Context context) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(context, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}
