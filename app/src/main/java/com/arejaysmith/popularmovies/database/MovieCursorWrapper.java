package com.arejaysmith.popularmovies.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.arejaysmith.popularmovies.Favorite;
import com.arejaysmith.popularmovies.Movie;
import com.arejaysmith.popularmovies.database.MovieDbSchema.FavoritesTable;

/**
 * Created by Urge_Smith on 8/4/16.
 */
public class MovieCursorWrapper extends CursorWrapper {

    public MovieCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Movie getFavorite() {

        String movie_id = getString(getColumnIndex(FavoritesTable.Cols.MOVIE_ID));
        String description = getString(getColumnIndex(FavoritesTable.Cols.DESCRIPTION));
        String poster = getString(getColumnIndex(FavoritesTable.Cols.POSTER));
        String release_date = getString(getColumnIndex(FavoritesTable.Cols.RELEASE_DATE));
        String score = getString(getColumnIndex(FavoritesTable.Cols.SCORE));
        String title = getString(getColumnIndex(FavoritesTable.Cols.TITLE));

        Movie movie = new Movie();
        movie.setDescription(description);
        movie.setId(Integer.parseInt(movie_id));
        movie.setPosterPath(poster);
        movie.setDate(release_date);
        movie.setRating(Double.parseDouble(score));
        movie.setTitle(title);

        return movie;
    }
}
