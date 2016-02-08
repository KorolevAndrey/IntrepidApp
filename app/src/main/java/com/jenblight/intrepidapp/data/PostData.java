package com.jenblight.intrepidapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.provider.BaseColumns;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostData {
    public static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ";

    private Long id;
    public Integer trip;
    public String name;
    public Date date;
    public String text;
    public Double latitude;
    public Double longitude;
    public List<String> photos;

    public static abstract class PostEntry implements BaseColumns {
        public static final String TABLE_NAME = "posts";
        public static final String COLUMN_NAME_TRIP = "trip";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_TEXT = "text";
        public static final String COLUMN_NAME_LAT = "latitude";
        public static final String COLUMN_NAME_LON = "longitude";
        public static final String SQL_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_NAME_TRIP + " INTEGER," +
                        COLUMN_NAME_NAME + " TEXT," +
                        COLUMN_NAME_DATE + " TEXT," +
                        COLUMN_NAME_TEXT + " TEXT," +
                        COLUMN_NAME_LAT + " REAL," +
                        COLUMN_NAME_LON + " REAL" +
                " )";
        public static final String SQL_DELETE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

    }

    public static abstract class PhotoEntry implements BaseColumns {
        public static final String TABLE_NAME = "photos";
        public static final String COLUMN_NAME_POST_ID = "post_id";
        public static final String COLUMN_NAME_PATH = "path";
        public static final String SQL_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_NAME_POST_ID + " INTEGER," +
                        COLUMN_NAME_PATH + " TEXT" +
                        " )";
        public static final String SQL_DELETE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public PostData(Integer inTrip, String inName, Date inDate, String inText, Location inLocation, List<String> inPhotos) {
        id = null;
        trip = inTrip;
        name = inName;
        date = inDate;
        text = inText;
        latitude = inLocation.getLatitude();
        longitude = inLocation.getLongitude();
        photos = inPhotos;
    }

    public PostData(Integer inTrip, String inName, Date inDate, String inText, Double inLatitude, Double inLongitude, List<String> inPhotos) {
        id = null;
        trip = inTrip;
        name = inName;
        date = inDate;
        text = inText;
        latitude = inLatitude;
        longitude = inLongitude;
        photos = inPhotos;
    }

    public PostData(Long inId, Integer inTrip, String inName, String inDate, String inText, Double inLatitude, Double inLongitude) throws ParseException {
        id = inId;
        trip = inTrip;
        name = inName;
        date = new SimpleDateFormat(dateFormat).parse(inDate);
        text = inText;
        latitude = inLatitude;
        longitude = inLongitude;
        photos = new ArrayList<String>();
    }


    public void save(Context context){
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues postValues = new ContentValues();
        postValues.put(PostEntry.COLUMN_NAME_TRIP, trip);
        postValues.put(PostEntry.COLUMN_NAME_NAME, name);
        postValues.put(PostEntry.COLUMN_NAME_DATE, new SimpleDateFormat(dateFormat).format(date));
        postValues.put(PostEntry.COLUMN_NAME_TEXT, text);
        postValues.put(PostEntry.COLUMN_NAME_LAT, latitude);
        postValues.put(PostEntry.COLUMN_NAME_LON, longitude);

        long post_id = db.insert(PostEntry.TABLE_NAME, "null", postValues);
        id = post_id;

        ContentValues photoValues;
        for (String photo : photos) {
            photoValues = new ContentValues();
            photoValues.put(PhotoEntry.COLUMN_NAME_POST_ID, post_id);
            photoValues.put(PhotoEntry.COLUMN_NAME_PATH, photo);
            db.insert(PhotoEntry.TABLE_NAME, "null", photoValues);
        }
    }

    public void delete(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] post_id = new String[] {id.toString()};
        db.delete(PhotoEntry.TABLE_NAME, PhotoEntry.COLUMN_NAME_POST_ID + " == ?;", post_id);
        db.delete(PostEntry.TABLE_NAME, PostEntry._ID + " == ?;", post_id);
        id = null;

    }

    public static List<PostData> getAll(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor post_cursor = db.rawQuery("select * from " + PostEntry.TABLE_NAME + ";", null);

        List<PostData> posts = new ArrayList<PostData>();
        PostData post;
        Cursor photo_cursor;
        if (post_cursor.moveToFirst()) {
            do {
                try {
                    post = new PostData(
                            post_cursor.getLong(post_cursor.getColumnIndex(PostEntry._ID)),
                            post_cursor.getInt(post_cursor.getColumnIndex(PostEntry.COLUMN_NAME_TRIP)),
                            post_cursor.getString(post_cursor.getColumnIndex(PostEntry.COLUMN_NAME_NAME)),
                            post_cursor.getString(post_cursor.getColumnIndex(PostEntry.COLUMN_NAME_DATE)),
                            post_cursor.getString(post_cursor.getColumnIndex(PostEntry.COLUMN_NAME_TEXT)),
                            post_cursor.getDouble(post_cursor.getColumnIndex(PostEntry.COLUMN_NAME_LAT)),
                            post_cursor.getDouble(post_cursor.getColumnIndex(PostEntry.COLUMN_NAME_LON))
                    );
                    photo_cursor = db.rawQuery("select * from " + PhotoEntry.TABLE_NAME + " where " +
                            PhotoEntry.COLUMN_NAME_POST_ID + " == " + post.id.toString(), null);
                    if (photo_cursor.moveToFirst()) {
                        do {
                            post.photos.add(photo_cursor.getString(photo_cursor.getColumnIndex(PhotoEntry.COLUMN_NAME_PATH)));
                        } while (photo_cursor.moveToNext());
                    }
                    photo_cursor.close();
                    posts.add(post);
                } catch (ParseException e) {
                    Log.e("Intrepid", "Could not parse date from database");
                }
            } while (post_cursor.moveToNext());
        }
        post_cursor.close();

        return posts;
    }


}
