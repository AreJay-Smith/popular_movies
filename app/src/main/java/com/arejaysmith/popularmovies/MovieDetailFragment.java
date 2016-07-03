package com.arejaysmith.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


public class MovieDetailFragment extends Fragment {


    public MovieDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        return view;
    }
}
