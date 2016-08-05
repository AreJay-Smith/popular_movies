package com.arejaysmith.popularmovies;

import android.content.Context;
import android.content.Intent;
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

    private JSONArray mJason;
    private ArrayList<MovieTrailer> mTrailerData;
    private ArrayList<MovieReview> mReviewData;
    private ListView mTrailerView;
    private LinearLayout mMovieTrailerContainer;
    private Context mContext;
    private Button mFavorites;
    private JSONObject mJSONFavorites = new JSONObject();
    private FavoritesHolder mFavoritesHolder;
    private List<Movie> mMovieItems;
    private Movie mMovie;


    public MovieDetailFragment() {
        // Required empty public constructor
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void setTrailerData(ArrayList<MovieTrailer> trailers) {

        mTrailerData = trailers;

        // Call to set up the adapter and make the changes
        addRowView();

        //add movie reviews to mJSONFavorites
        try {
            JSONArray movieTrailers = new JSONArray();

            for (int i = 0; i < trailers.size(); i++) {

                MovieTrailer currentMovie = trailers.get(i);

                JSONObject object = new JSONObject();
                object.put(currentMovie.getTrailerTitle(), currentMovie.getTrailerUrl());
                movieTrailers.put(object);
            }

            mJSONFavorites.put("trailers", movieTrailers.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void setMovieReviewData(ArrayList<MovieReview> movieReviewData) {

        mReviewData = movieReviewData;

        addRowView();

        //add movie reviews to mJSONFavorites
        try {
            JSONArray movieReviews = new JSONArray();

            for (int i = 0; i < movieReviewData.size(); i++) {

                MovieReview currentMovieReview = movieReviewData.get(i);

                JSONObject object = new JSONObject();
                object.put(currentMovieReview.getAuthor(), currentMovieReview.getContent());
                movieReviews.put(object);
            }

            mJSONFavorites.put("reviews", movieReviews.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addRowView() {

        if (mTrailerData != null && mReviewData != null) {

            for (int i = 0; i < mTrailerData.size(); i++) {

                MovieTrailer currentTrailer = new MovieTrailer();
                currentTrailer = mTrailerData.get(i);

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

            for (int x = 0; x < mReviewData.size(); x++) {

                MovieReview currMovieReview;
                currMovieReview = mReviewData.get(x);

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMovie = getActivity().getIntent().getParcelableExtra("movie");

        if (isNetworkAvailable() && mMovie != null) {

            FetchMovieTrailersTask trailerTask = new FetchMovieTrailersTask(this);
            trailerTask.execute(Integer.toString(mMovie.getId()));

            FetchMovieReviewsTask reviewsTask = new FetchMovieReviewsTask(this);
            reviewsTask.execute(Integer.toString(mMovie.getId()));

            try {

                mJSONFavorites.put("movie_id", mMovie.getId());
                mJSONFavorites.put("title", mMovie.getTitle());
                mJSONFavorites.put("release_date", mMovie.getDate());
                mJSONFavorites.put("description", mMovie.getDescription());
                mJSONFavorites.put("poster", mMovie.getPosterPath());
                mJSONFavorites.put("score", mMovie.getRating());


            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {

            // TODO: create a broadcast receiver for when a connection is available
            Toast.makeText(getActivity(), "No internet connection",
                    Toast.LENGTH_LONG).show();
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        final Movie mMovie = getActivity().getIntent().getParcelableExtra("movie");
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


}
