package com.arejaysmith.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MovieListFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private RecyclerView mMovieRecyclerView;
    private List<Movie> mMovieItems = new ArrayList<>();
    private FavoritesHolder mFavorites;
    private Context context = getActivity();
    private String settings;
    private MovieAdapter mAdapter;
    private TextView mEmptyList;
    private int mSelectedItem = 0;


    public MovieListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {

            // Grab the previous state and the adapter will be set up again in oncreateView
            mMovieItems = savedInstanceState.getParcelableArrayList("movies");
            mSelectedItem = savedInstanceState.getInt("selected");
        } else {
            // If no saved instance, then check for favorites, if not favorites then launch the async task
            SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            settings = mSharedPreferences.getString(getString(R.string.movie_key), getString(R.string.movie_list_popular));
            if (settings.equals("favorites")) {
                getActivity().setTitle("Your Favorites");
                mFavorites = new FavoritesHolder(getActivity());
                mMovieItems = mFavorites.getFavorites();
            } else {

                if (isNetworkAvailable()) {
                    FetchMovieTask movieTask = new FetchMovieTask();
                    movieTask.execute();
                } else {

                    // TODO: create a broadcast receiver for when a connection is available
                    Toast.makeText(getActivity(), "Try again Please. No internet connection",
                            Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    @Override
    public void onStart() {
        super.onStart();

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

        if (id == R.id.action_settings) {

            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }

        // Used for debuging purposes, currently hidden by xml line
        if (id == R.id.refresh) {

            FetchMovieTask movieTask = new FetchMovieTask();
            movieTask.execute();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // In case the screen is flipped, grab shared preferences again
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        settings = mSharedPreferences.getString(getString(R.string.movie_key), getString(R.string.movie_list_popular));
        View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);
        // Register a new listener to change when the preferences are update
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(this);

        // Wire up the recyclerView
        mMovieRecyclerView = (RecyclerView) rootView.findViewById(R.id.movie_recycler_view);

        // Set listener
        mMovieRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        // Pass movie object to detail activity using parcelable
                        Movie movieItem = mMovieItems.get(position);
                        mSelectedItem = position;
                        ((Callback) getActivity()).onItemSelected(movieItem);
                    }
                })
        );

        // Set up the layout for the grid in the recycler view
        mMovieRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        // Make sure the recyclerview isn't empty, this will most likely happen with favorites
        mEmptyList = (TextView) rootView.findViewById(R.id.empty_movie_list);
        if (mMovieItems == null && settings.equals("favorites")) {
            mMovieRecyclerView.setVisibility(View.GONE);
            mEmptyList.setVisibility(View.VISIBLE);
        } else if (mMovieItems != null) {
            setupAdapter();
        }

        // Inflate the layout for this fragment
        return rootView;
    }


    private class PosterHolder extends RecyclerView.ViewHolder {

        private ImageView mMovieView;


        public PosterHolder(View itemView) {
            super(itemView);

            mMovieView = (ImageView) itemView.findViewById(R.id.poster_img_holder);

        }


    }

    private class MovieAdapter extends RecyclerView.Adapter<PosterHolder> {

        private List<Movie> mMovieItems;

        public MovieAdapter(List<Movie> movieItems) {

            mMovieItems = movieItems;
        }


        @Override
        public PosterHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

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


    // Create Async task to fetch Movie data
    public class FetchMovieTask extends AsyncTask<String, Void, ArrayList<Movie>> {
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        private ArrayList<Movie> getMovieDataFromJson(String forecastJsonStr) throws JSONException {

            JSONObject movieObject = new JSONObject(forecastJsonStr);
            JSONArray movieResults = movieObject.getJSONArray("results");
            ArrayList<Movie> moviesArray = new ArrayList<Movie>();

            try {

                for (int i = 0; i < movieResults.length(); i++) {

                    // Get each object
                    JSONObject currentObject = movieResults.getJSONObject(i);
                    Movie movie = new Movie();

                    //grab needed variables
                    movie.setId(currentObject.getInt("id"));
                    movie.setTitle(currentObject.getString("original_title"));
                    movie.setDescription(currentObject.getString("overview"));
                    movie.setRating(currentObject.getDouble("vote_average"));

                    //ParseDate
                    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(currentObject.getString("release_date"));
                    String formattedDate = new SimpleDateFormat("MM/dd/yyyy").format(date);
                    movie.setDate(formattedDate);

                    // Make adjustments for image
                    String posterPath = "http://image.tmdb.org/t/p/w500" + currentObject.getString("poster_path");
                    movie.setPosterPath(posterPath);

                    Log.v(LOG_TAG, posterPath);

                    moviesArray.add(movie);

                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
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
                final String LIST_CHOICE = mSharedPreferences.getString(getString(R.string.movie_key), getString(R.string.movie_list_popular));
                final String API_KEY = "api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendEncodedPath(LIST_CHOICE)
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

                Log.v(LOG_TAG, "Movies JSON String: " + moviesJsonStr);


            } catch (IOException e) {
                Log.e(LOG_TAG, "Error: Couldn't get the data", e);
                e.printStackTrace();

                Log.v(LOG_TAG, "Forecast JSON String: " + moviesJsonStr);

                return null;
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

            // Set the created list to a local variable
            mMovieItems = movies;

            // If there's no data then don't start the adapter
            if (mMovieItems.size() > 0) {
                setupAdapter();
            }

            // Display the current filter
            switch (mSharedPreferences.getString(getString(R.string.movie_key), getString(R.string.movie_list_popular))) {

                case "popular":
                    getActivity().setTitle(getString(R.string.movie_list_label_popular) + " Movies");
                    break;

                case "top_rated":
                    getActivity().setTitle(getString(R.string.movie_list_label_top_rated) + " Movies");
                    break;
            }
        }
    }

    // Update the adapter after onpost execute or whenever called
    private void setupAdapter() {
        if (isAdded()) {

            //Make sure recycler view is visible
            if (mMovieItems.size() > 0) {
                mMovieRecyclerView.setVisibility(View.VISIBLE);
                mMovieRecyclerView.setAdapter(new MovieAdapter(mMovieItems));
                mEmptyList.setVisibility(View.GONE);
                SharedPreferences prefs = getActivity().getSharedPreferences("twoPane", getActivity().MODE_PRIVATE);
                if (prefs.getBoolean("twoPane", false)) {
                    ((Data) getActivity()).dataReceived(mMovieItems.get(mSelectedItem));
                }
            } else {
                mMovieRecyclerView.setVisibility(View.GONE);
                mEmptyList.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface Callback {

        //DetailFragmentCallback for when an item has been selected.
        public void onItemSelected();

        void onItemSelected(Movie movieSelected);
    }

    public interface Data {

        void dataReceived(Movie movieSelected);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        settings = mSharedPreferences.getString(getString(R.string.movie_key), getString(R.string.movie_list_popular));

        if (settings.equals("favorites")) {
            mFavorites = new FavoritesHolder(getActivity());
            mMovieItems = mFavorites.getFavorites();
            if (mMovieItems != null) {
                setupAdapter();
                getActivity().setTitle("Your Favorites");
            } else {
                mMovieRecyclerView.setVisibility(View.GONE);
                mEmptyList.setVisibility(View.VISIBLE);
            }
        } else {
            FetchMovieTask movieTask = new FetchMovieTask();
            movieTask.execute();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state

        ArrayList<Movie> movies = ((ArrayList<Movie>) mMovieItems);
        savedInstanceState.putParcelableArrayList("movies", movies);
        savedInstanceState.putInt("selected", mSelectedItem);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroy() {

        // Unregister listener
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }
}
