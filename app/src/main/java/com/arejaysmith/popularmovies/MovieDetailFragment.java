package com.arejaysmith.popularmovies;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.util.ArrayList;


public class MovieDetailFragment extends Fragment {

    private JSONArray mJason;
    private ArrayList<MovieTrailer> mTrailerData;
    private ArrayList<MovieReview> mReviewData;
    private ListView mTrailerView;
    private LinearLayout mMovieTrailerContainer;



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

    }

    public void setMovieReviewData(ArrayList<MovieReview> movieReviewData) {

        mReviewData = movieReviewData;

        addRowView();
    }

    private void addRowView(){

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

            for (int x =0; x < mReviewData.size(); x++) {

                MovieReview currMovieReview = new MovieReview();
                currMovieReview = mReviewData.get(x);

                View mMovieReviewItem = LayoutInflater.from(getActivity()).inflate(
                        R.layout.review_list_item, null);

                TextView mMovieReview = (TextView) mMovieReviewItem.findViewById(R.id.reviews_name);
                mMovieReview.setText("Review by: " + currMovieReview.getAuthor());

                mMovieTrailerContainer.addView(mMovieReviewItem);
            }
        }

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Movie mMovie = getActivity().getIntent().getParcelableExtra("test");

        if (isNetworkAvailable()) {

            FetchMovieTrailersTask trailerTask = new FetchMovieTrailersTask(this);
            trailerTask.execute(Integer.toString(mMovie.getId()));

            FetchMovieReviewsTask reviewsTask = new FetchMovieReviewsTask(this);
            reviewsTask.execute(Integer.toString(mMovie.getId()));
        }else {

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

        Movie mMovie = getActivity().getIntent().getParcelableExtra("test");

        // Set title
        TextView titleView = (TextView) view.findViewById(R.id.movie_detail_title);
        titleView.setText(mMovie.getTitle());

        ImageView imageView = (ImageView) view.findViewById(R.id.movie_detail_poster_image);
        Picasso.with(getActivity()).load(mMovie.getPosterPath()).into(imageView);

        TextView releaseView = (TextView) view.findViewById(R.id.movie_detail_release_date);
        releaseView.setText(mMovie.getDate());

        TextView ratingView = (TextView) view.findViewById(R.id.movie_detail_rating);
        ratingView.setText(Double.toString(mMovie.getRating()));

        TextView synopsisView = (TextView) view.findViewById(R.id.movie_detail_synopsis);
        synopsisView.setText(mMovie.getDescription());

        mMovieTrailerContainer = (LinearLayout) view.findViewById(R.id.movie_trailer_container);

        return view;
    }


}
