package com.arejaysmith.popularmovies.database;

/**
 * Created by Urge_Smith on 8/3/16.
 */
public class MovieDbSchema {

    public static final class FavoritesTable {

        public static final String NAME = "favorites";

        public static final class Cols {

            public static final String MOVIE_ID = "movie_id";
            public static final String TITLE = "title";
            public static final String DESCRIPTION = "description";
            public static final String RELEASE_DATE = "release_date";
            public static final String SCORE = "score";
            public static final String POSTER = "poster";

        }
    }
}
