package com.arejaysmith.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.arejaysmith.popularmovies.database.MovieBaseHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.arejaysmith.popularmovies.FavoritesHolder.*;


public class MovieDetailFragment extends Fragment {

    private ArrayList<MovieTrailer> mTrailerData;
    private ArrayList<MovieReview> mReviewData;
    private LinearLayout mMovieTrailerContainer;
    private Context mContext;
    private Button mFavorites;
    private FavoritesHolder mFavoritesHolder;
    private List<Movie> mMovieItems;
    private Movie mMovie;
    private Boolean mTrailersReturned = false;
    private Boolean mReviewsReturned = false;


    public MovieDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getActivity().getSharedPreferences("twoPane", getActivity().MODE_PRIVATE);
        if (savedInstanceState != null && prefs.getBoolean("twoPane", false)) {
            // Do nothing because the main activity will restart this fragment
        } else if (savedInstanceState != null && !prefs.getBoolean("twoPane", false)) {
            mMovie = savedInstanceState.getParcelable("movie");
            fetchRows();
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mMovie = arguments.getParcelable("movie");

            } else {

                mMovie = getActivity().getIntent().getParcelableExtra("movie");
            }

            fetchRows();
        }


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        if (mMovie == null) {

            return view;
        } else {

            mFavorites = (Button) view.findViewById(R.id.add_to_favorites);

            //check to see if already in database
            if (checkDb()) {

                mFavorites.setText("Added to Favorites");
            } else {

                mFavorites.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mContext = getActivity();
                        mFavoritesHolder = new FavoritesHolder(mContext);
                        mFavoritesHolder.addMovie(mMovie);
                        mFavorites.setText("Added to Favorites");
                        mFavorites.setOnClickListener(null);
                    }
                });
            }

            // Set title
            TextView titleView = (TextView) view.findViewById(R.id.movie_detail_title);
            titleView.setText(mMovie.getTitle());

            ImageView imageView = (ImageView) view.findViewById(R.id.movie_detail_poster_image);
            Picasso.with(getActivity()).load(mMovie.getPosterPath()).into(imageView);

            TextView releaseView = (TextView) view.findViewById(R.id.movie_detail_release_date);
            releaseView.setText(mMovie.getDate());

            TextView ratingView = (TextView) view.findViewById(R.id.movie_detail_rating);
            ratingView.setText(Double.toString(mMovie.getRating()) + " / 10");

            TextView synopsisView = (TextView) view.findViewById(R.id.movie_detail_synopsis);
            synopsisView.setText(mMovie.getDescription());

            mMovieTrailerContainer = (LinearLayout) view.findViewById(R.id.movie_trailer_container);

            return view;
        }

    }

    public void setTrailerData(ArrayList<MovieTrailer> trailers) {
        // Set data for trailers
        mTrailerData = trailers;
        // Inform that trailers returned
        mTrailersReturned = true;
        // Call to set up the adapter and make the changes
        addRowView();
    }

    public void setMovieReviewData(ArrayList<MovieReview> movieReviewData) {

        mReviewData = movieReviewData;
        mReviewsReturned = true;
        addRowView();
    }

    private void addRowView() {

        if (mTrailersReturned && mReviewsReturned) {

            if (mTrailerData.size() > 0) {
                for (int i = 0; i < mTrailerData.size(); i++) {

                    MovieTrailer currentTrailer = mTrailerData.get(i);

                    View mMovieTrailerItem = LayoutInflater.from(getActivity()).inflate(
                            R.layout.trailer_list_item, null);

                    final String curURL = currentTrailer.getTrailerUrl();

                    mMovieTrailerItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Log.v("The movie url is: ", curURL);
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(curURL)));
                        }
                    });

                    TextView mMovieTrailerTitle = (TextView) mMovieTrailerItem.findViewById(R.id.trailer_title_item);
                    mMovieTrailerTitle.setText(currentTrailer.getTrailerTitle());

                    // New data is back from the server.  Hooray!
                    mMovieTrailerContainer.addView(mMovieTrailerItem);
                }
            }

            if (mReviewData.size() > 0) {
                for (int x = 0; x < mReviewData.size(); x++) {

                    MovieReview currMovieReview = mReviewData.get(x);

                    View mMovieReviewItem = LayoutInflater.from(getActivity()).inflate(
                            R.layout.review_list_item, null);

                    TextView mMovieReview = (TextView) mMovieReviewItem.findViewById(R.id.reviews_name);
                    mMovieReview.setText("Review by: " + currMovieReview.getAuthor());

                    // Declare as final to pass into onlclick listenter
                    final MovieReview review = currMovieReview;

                    mMovieReviewItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Intent mReviewIntent = new Intent(getActivity(), ReviewActivity.class);
                            Bundle mBundle = new Bundle();
                            mBundle.putParcelable("review", review);
                            mReviewIntent.putExtras(mBundle);

                            startActivity(mReviewIntent);
                        }
                    });

                    mMovieTrailerContainer.addView(mMovieReviewItem);
                }
            }
        }

    }

    private void fetchRows() {

        if (isNetworkAvailable() && mMovie != null) {

            FetchMovieTrailersTask trailerTask = new FetchMovieTrailersTask(this);
            trailerTask.execute(Integer.toString(mMovie.getId()));

            FetchMovieReviewsTask reviewsTask = new FetchMovieReviewsTask(this);
            reviewsTask.execute(Integer.toString(mMovie.getId()));

        } else {

            // TODO: create a broadcast receiver for when a connection is available
            Toast.makeText(getActivity(), "No internet connection",
                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkDb() {

        mMovieItems = new ArrayList<>();
        mFavoritesHolder = new FavoritesHolder(getActivity());
        mMovieItems = mFavoritesHolder.getFavorites();
        for (int i = 0; i < mMovieItems.size(); i++) {

            Movie movie = mMovieItems.get(i);

            if (movie.getId() == mMovie.getId()) {

                return true;
            }
        }

        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (mMovie != null) {
            outState.putParcelable("movie", mMovie);
            super.onSaveInstanceState(outState);
        }
    }
}
