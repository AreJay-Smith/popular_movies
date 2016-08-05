package com.arejaysmith.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.arejaysmith.popularmovies.database.MovieBaseHelper;
import com.arejaysmith.popularmovies.database.MovieCursorWrapper;
import com.arejaysmith.popularmovies.database.MovieDbSchema;
import com.arejaysmith.popularmovies.database.MovieDbSchema.FavoritesTable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Urge_Smith on 8/4/16.
 */
public class FavoritesHolder {

    private Context mContext;
    private SQLiteDatabase mSQLiteDatabase;

    public FavoritesHolder(Context context) {

        mContext = context.getApplicationContext();
        mSQLiteDatabase = new MovieBaseHelper(mContext).getWritableDatabase();
    }

    private static ContentValues getContentValues(Movie movie) {

        ContentValues values = new ContentValues();

        values.put(FavoritesTable.Cols.MOVIE_ID, movie.getId());
        values.put(FavoritesTable.Cols.POSTER, movie.getPosterPath());
        values.put(FavoritesTable.Cols.RELEASE_DATE, movie.getDate());
        values.put(FavoritesTable.Cols.SCORE, movie.getRating());
        values.put(FavoritesTable.Cols.TITLE, movie.getTitle());
        values.put(FavoritesTable.Cols.DESCRIPTION, movie.getDescription());

        return values;
    }

    public void addMovie(Movie movie) {

        ContentValues values = getContentValues(movie);

        mSQLiteDatabase.insert(FavoritesTable.NAME, null, values);
    }

    public void updateMovie(Movie movie) {

            String uuidString = Integer.toString(movie.getId());
            ContentValues values = getContentValues(movie);

            mSQLiteDatabase.update(FavoritesTable.NAME, values, FavoritesTable.Cols.MOVIE_ID + " = ?", new String[]{uuidString});
    }

    private MovieCursorWrapper queryFavorites() {

        Cursor cursor = mSQLiteDatabase.query(

                FavoritesTable.NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        return new MovieCursorWrapper(cursor);
    }

    public List<Movie> getFavorites() {

        List<Movie> mFavorites = new ArrayList<>();
        MovieCursorWrapper cursor = queryFavorites();

        {

            try {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {

                    mFavorites.add(cursor.getFavorite());
                    cursor.moveToNext();
                }
            } finally {
                cursor.close();
            }
        }

        return mFavorites;
    }
}
