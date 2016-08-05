package com.arejaysmith.popularmovies.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.arejaysmith.popularmovies.Movie;
import com.arejaysmith.popularmovies.database.MovieDbSchema.FavoritesTable;

/**
 * Created by Urge_Smith on 8/4/16.
 */
public class MovieBaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "movieBase.db";

    public MovieBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + FavoritesTable.NAME + "(" + " _id integer primary key autoincrement, " +
                FavoritesTable.Cols.MOVIE_ID + ", " +
                FavoritesTable.Cols.TITLE + ", " +
                FavoritesTable.Cols.DESCRIPTION + ", " +
                FavoritesTable.Cols.POSTER + ", " +
                FavoritesTable.Cols.RELEASE_DATE + ", " +
                FavoritesTable.Cols.SCORE + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
