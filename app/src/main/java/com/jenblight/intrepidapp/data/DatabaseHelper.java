package com.jenblight.intrepidapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jenblight.intrepidapp.data.CollectedPhoto;
import com.jenblight.intrepidapp.data.PostData;

/**
 * Created by jgblight on 15-12-07.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "PostData.db";

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(PostData.PostEntry.SQL_CREATE);
            db.execSQL(PostData.PhotoEntry.SQL_CREATE);
            db.execSQL(CollectedPhoto.CollectedPhotoEntry.SQL_CREATE);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO: something less dumb
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO: something less dumb
        }
}
