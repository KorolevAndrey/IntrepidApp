package com.jenblight.intrepidapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.util.Log;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by jgblight on 15-12-07.
 */
public class CollectedPhoto implements Parcelable {
    public static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ";

    private Long id;
    public Date date;
    public String photo;
    public Double latitude;
    public Double longitude;

    public static abstract class CollectedPhotoEntry implements BaseColumns {
        public static final String TABLE_NAME = "collected_photos";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_PHOTO = "photo";
        public static final String COLUMN_NAME_LAT = "latitude";
        public static final String COLUMN_NAME_LON = "longitude";
        public static final String SQL_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_NAME_DATE + " TEXT," +
                        COLUMN_NAME_PHOTO + " TEXT," +
                        COLUMN_NAME_LAT + " REAL," +
                        COLUMN_NAME_LON + " REAL" +
                        " )";
        public static final String SQL_DELETE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

    }
    public CollectedPhoto(Date inDate, String inPhoto, Double inLatitude, Double inLongitude) {
        id = null;
        date = inDate;
        photo = inPhoto;
        latitude = inLatitude;
        longitude = inLongitude;
    }

    public CollectedPhoto(Parcel in) {
        id = in.readLong();
        try {
            date = new SimpleDateFormat(dateFormat).parse(in.readString());
        } catch (ParseException e) {
            date = Calendar.getInstance().getTime();
        }
        photo = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public CollectedPhoto(Long inId, String inDate, String inPhoto, Double inLatitude, Double inLongitude) throws ParseException{
        id = inId;
        date = new SimpleDateFormat(dateFormat).parse(inDate);
        photo = inPhoto;
        latitude = inLatitude;
        longitude = inLongitude;
    }


    public void save(Context context){
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues postValues = new ContentValues();
        postValues.put(CollectedPhotoEntry.COLUMN_NAME_DATE, new SimpleDateFormat(dateFormat).format(date));
        postValues.put(CollectedPhotoEntry.COLUMN_NAME_PHOTO, photo);
        postValues.put(CollectedPhotoEntry.COLUMN_NAME_LAT, latitude);
        postValues.put(CollectedPhotoEntry.COLUMN_NAME_LON, longitude);

        long entry_id = db.insert(CollectedPhotoEntry.TABLE_NAME, "null", postValues);
        id = entry_id;
    }

    public void delete(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] post_id = new String[] {id.toString()};
        db.delete(CollectedPhotoEntry.TABLE_NAME, CollectedPhotoEntry._ID + " == ?;", post_id);
        id = null;

    }

    public static List<CollectedPhoto> getAll(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor photo_cursor = db.rawQuery("select * from " + CollectedPhotoEntry.TABLE_NAME + ";", null);

        List<CollectedPhoto> photos = new ArrayList<CollectedPhoto>();
        CollectedPhoto photo;
        if (photo_cursor.moveToFirst()) {
            do {
                try {
                    photo = new CollectedPhoto(
                            photo_cursor.getLong(photo_cursor.getColumnIndex(CollectedPhotoEntry._ID)),
                            photo_cursor.getString(photo_cursor.getColumnIndex(CollectedPhotoEntry.COLUMN_NAME_DATE)),
                            photo_cursor.getString(photo_cursor.getColumnIndex(CollectedPhotoEntry.COLUMN_NAME_PHOTO)),
                            photo_cursor.getDouble(photo_cursor.getColumnIndex(CollectedPhotoEntry.COLUMN_NAME_LAT)),
                            photo_cursor.getDouble(photo_cursor.getColumnIndex(CollectedPhotoEntry.COLUMN_NAME_LON))
                    );

                    File imgFile = new File(photo.photo);
                    if (imgFile.exists()) {
                        photos.add(photo);
                    } else {
                        photo.delete(context);
                    }
                } catch (ParseException e) {
                    Log.e("Intrepid", "Could not parse date from database");
                }
            } while (photo_cursor.moveToNext());
        }
        photo_cursor.close();

        return photos;
    }

    public int describeContents() {
        return 0;
    }

    /** save object in parcel */
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
        out.writeString(new SimpleDateFormat(dateFormat).format(date));
        out.writeString(photo);
        out.writeDouble(latitude);
        out.writeDouble(longitude);
    }

    public static final Parcelable.Creator<CollectedPhoto> CREATOR
            = new Parcelable.Creator<CollectedPhoto>() {
        public CollectedPhoto createFromParcel(Parcel in) {
                return new CollectedPhoto(in);
        }

        public CollectedPhoto[] newArray(int size) {
            return new CollectedPhoto[size];
        }
    };

}
