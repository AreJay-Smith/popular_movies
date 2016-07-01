package com.arejaysmith.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MovieListFragment extends Fragment {

    private RecyclerView mMovieRecyclerView;
    private ArrayList<Movie> mMovieItems = new ArrayList<>();


    public MovieListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        FetchMovieTask movieTask = new FetchMovieTask();
        movieTask.execute();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Handle action bar item clicks here. The action bar wil
        inflater.inflate(R.menu.menu_main, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.refresh) {

            FetchMovieTask movieTask = new FetchMovieTask();
            movieTask.execute("Hello");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);

        // Wire up the recyclerView
        mMovieRecyclerView = (RecyclerView) rootView.findViewById(R.id.movie_recycler_view);

        // Set up the layout for the grid in the recycler view
        mMovieRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));

        // Inflate the layout for this fragment
        return rootView;
    }


    private class PosterHolder extends RecyclerView.ViewHolder {

        private ImageView mMovieView;

        public PosterHolder(View itemView) {
            super(itemView);

            mMovieView = (ImageView) itemView.findViewById(R.id.poster_img);

        }

        public void bindMovieImage(Movie movie) {
            Picasso.with(getActivity()).load(movie.getPosterPath()).resize(240, 120).into(mMovieView);
        }
    }

    private class MovieAdapter extends RecyclerView.Adapter<PosterHolder> {

        private List<Movie> mMovieItems;

        public MovieAdapter(List<Movie> movieItems) {

            mMovieItems = movieItems;
        }

        @Override
        public PosterHolder onCreateViewHolder (ViewGroup viewGroup, int viewType) {

//            ImageView imageView = new ImageView(getActivity());
            View imageView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.poster_layout, viewGroup, false);

            return new PosterHolder(imageView);
        }

        @Override
        public void onBindViewHolder(PosterHolder posterHolder, int position) {

            Movie movie = mMovieItems.get(position);

            Picasso.with(getActivity()).load(movie.getPosterPath()).into(posterHolder.mMovieView);

        }

        @Override
        public int getItemCount() {

            return mMovieItems.size();
        }

    }



    public class FetchMovieTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
        JSONObject mMovieObject = new JSONObject();
        JSONArray mMoviesArray = new JSONArray();

        private ArrayList<Movie> getMovieDataFromJson(String forecastJsonStr) throws JSONException {

            JSONObject movieObject = new JSONObject(forecastJsonStr);
            JSONArray movieResults = movieObject.getJSONArray("results");
            ArrayList<Movie> moviesArray = new ArrayList<Movie>();

            try {

            for (int i = 0; i <movieResults.length(); i++) {

                // Get each object
                JSONObject currentObject = movieResults.getJSONObject(i);
                Movie movie = new Movie();

                // Create new local JSON
                mMovieObject = new JSONObject();

                //grab needed variables
                mMovieObject.put("id", currentObject.getInt("id"));
                movie.setId(currentObject.getInt("id"));
                mMovieObject.put("title", currentObject.getString("original_title"));
                movie.setTitle(currentObject.getString("original_title"));
                mMovieObject.put("description", currentObject.getString("overview"));
                movie.setDescription(currentObject.getString("overview"));
                mMovieObject.put("rating", currentObject.getDouble("vote_average"));
                movie.setRating(currentObject.getDouble("vote_average"));

                // Make adjustments for image
                String posterPath = "http://image.tmdb.org/t/p/w500" + currentObject.getString("poster_path");
                mMovieObject.put("poster_path", posterPath);
                movie.setPosterPath(posterPath);

                Log.v(LOG_TAG, posterPath);

                mMoviesArray.put(movieObject);
                moviesArray.add(movie);

            }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return moviesArray;
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            try {

                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
                final String TOP_RATED = "top_rated";
                final String POPULAR = "popular";
                final String API_KEY = "api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendEncodedPath(POPULAR)
                        .appendQueryParameter(API_KEY, BuildConfig.MOVIE_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, url.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Forecast JSON String: " + moviesJsonStr);


            } catch (IOException e) {
                Log.e(LOG_TAG, "Error: Couldn't get the data", e);

                Log.v(LOG_TAG, "Forecast JSON String: " + moviesJsonStr);

                return null;
            }finally{
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

            try {
                return getMovieDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {

            mMovieItems = movies;
            setupAdapter();
        }
    }

    // Update the adapter after onpost execute
    private void setupAdapter() {
        if (isAdded()){
            mMovieRecyclerView.setAdapter(new MovieAdapter(mMovieItems));
        }
    }

}
