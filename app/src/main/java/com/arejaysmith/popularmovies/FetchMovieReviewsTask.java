package com.arejaysmith.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Urge_Smith on 7/19/16.
 */
public class FetchMovieReviewsTask extends AsyncTask<String, Void, ArrayList<MovieReview>> {

    private String LOG_TAG = getClass().getSimpleName();
    final MovieDetailFragment outer;

    FetchMovieReviewsTask(MovieDetailFragment outer) {
        this.outer = outer;
    }

    @Override
    protected ArrayList<MovieReview> doInBackground(String... params) {

        //Holds the connection and buffer
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String reviewJsonStr = null;

        try {

            final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String MOVIE_ID = params[0];
            final String VIDEOS = "reviews";
            final String API_KEY = "api_key";

            Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendEncodedPath(MOVIE_ID)
                    .appendEncodedPath(VIDEOS)
                    .appendQueryParameter(API_KEY, BuildConfig.MOVIE_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());
            Log.v(LOG_TAG + " my review's url is: ", url.toString());

            // Create the request to MovieDatabase.org and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {

                Log.e(LOG_TAG, "Input Stream is empty");
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                reviewJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Review's JSON String: " + reviewJsonStr);
            }

        } catch (Exception e) {

            Log.e(LOG_TAG, "Reviewss JSON String: " + reviewJsonStr);

        } finally {

            if (urlConnection != null) {
                urlConnection.disconnect();
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }

            try {

                return parseReviewDataFromJson(reviewJsonStr);
            } catch (JSONException e) {

                Log.e(LOG_TAG, e.toString());
            }

        }

        return null;
    }

    public ArrayList<MovieReview> parseReviewDataFromJson(String reviews) throws JSONException {

        try {
            ArrayList<MovieReview> movieReviewArrayList = new ArrayList<MovieReview>();
            MovieReview movieReview = new MovieReview();
            JSONObject reviewObject = new JSONObject(reviews);
            JSONArray jsonReviewArray = reviewObject.getJSONArray("results");

            for ( int i = 0; i < jsonReviewArray.length(); i++) {

                JSONObject currentReview = jsonReviewArray.getJSONObject(i);

                movieReview.setAuthor(currentReview.getString("author"));
                movieReview.setContent(currentReview.getString("content"));

                movieReviewArrayList.add(movieReview);
            }

            return movieReviewArrayList;
        } catch (JSONException e) {

            Log.e(LOG_TAG, "Review JSON: didn't parse correctly");
        }

        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<MovieReview> movieReviews) {
        super.onPostExecute(movieReviews);

        outer.setMovieReviewData(movieReviews);
    }
}

