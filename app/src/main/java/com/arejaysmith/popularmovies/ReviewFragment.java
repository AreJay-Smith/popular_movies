package com.arejaysmith.popularmovies;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReviewFragment extends Fragment {

    private TextView mMovieReviewAuthor;
    private TextView mMovieReviewContent;


    public ReviewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);
        MovieReview currentReview = getActivity().getIntent().getParcelableExtra("review");

        mMovieReviewAuthor = (TextView) view.findViewById(R.id.movie_review_author);
        mMovieReviewAuthor.setText("Review by: " + currentReview.getAuthor());

        mMovieReviewContent = (TextView) view.findViewById(R.id.movie_review_content);
        mMovieReviewContent.setText(currentReview.getContent());

        // Inflate the layout for this fragment
        return view;
    }

}
