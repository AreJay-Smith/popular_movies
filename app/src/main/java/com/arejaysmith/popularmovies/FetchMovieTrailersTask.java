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

import com.arejaysmith.popularmovies.MovieDetailFragment;

/**
 * Created by Urge_Smith on 7/9/16.
 */
public class FetchMovieTrailersTask extends AsyncTask<String, Void, ArrayList<MovieTrailer>> {

    final MovieDetailFragment outer;
    private final String LOG_TAG = FetchMovieTrailersTask.class.getSimpleName();

    FetchMovieTrailersTask(MovieDetailFragment outer) {
        this.outer = outer;
    }

    @Override
    protected ArrayList<MovieTrailer> doInBackground(String... params) {

        //Holds the connection and buffer
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String trailersJsonStr = null;

        try {

            final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String MOVIE_ID = params[0];
            final String VIDEOS = "videos";
            final String API_KEY = "api_key";

            Uri builtUri = Uri.parse(MOVIE_BASE_URL). buildUpon()
                    .appendEncodedPath(MOVIE_ID)
                    .appendEncodedPath(VIDEOS)
                    .appendQueryParameter(API_KEY, BuildConfig.MOVIE_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());
            Log.v(LOG_TAG + " my trailer's url is: ", url.toString());

            // Create the request to MovieDatabase.org and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null){

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
                trailersJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Trailers JSON String: " + trailersJsonStr);
            }

        }catch (Exception e){

            Log.e(LOG_TAG, "Trailers JSON String: " + trailersJsonStr);

        }finally {

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

        }

        try{

            return parseTrailerDataFromJson(trailersJsonStr);
        }catch (JSONException e) {

            Log.e(LOG_TAG, e.getMessage());
        }

        return null;
    }

    public ArrayList<MovieTrailer> parseTrailerDataFromJson(String trailers) throws JSONException{

        JSONObject trailersObject = new JSONObject(trailers);
        JSONArray  arrayOfObjects = trailersObject.getJSONArray("results");
        ArrayList<MovieTrailer> movieTrailerArrayList = new ArrayList<>();

        try{

            for (int i = 0; i < arrayOfObjects.length(); i++) {

                JSONObject currentObject = arrayOfObjects.getJSONObject(i);
                MovieTrailer movieTrailer = new MovieTrailer();

                movieTrailer.setTrailerTitle(currentObject.getString("name"));
                movieTrailer.setTrailerUrl("https://www.youtube.com/watch?v=" + currentObject.getString("key"));

                movieTrailerArrayList.add(movieTrailer);
            }

        }catch (JSONException e){

            Log.e(LOG_TAG, "Trailer JSON: " + movieTrailerArrayList.toString());
        }

        return movieTrailerArrayList;
    }

    @Override
    protected void onPostExecute(ArrayList<MovieTrailer> trailers) {

        Log.v("My JSON: ", trailers.toString());
        try {
            outer.setTrailerData(trailers);
        }catch (Exception e){

        }

    }

}
