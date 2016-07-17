package com.arejaysmith.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MovieDetailFragment extends Fragment {

    private JSONArray mJason;
    private ArrayList<String> mTrailerUrls;
    private ListView mTrailerView;
    private ListView list;
    private CustomAdapter mTrailerAdapter;
    private LinearLayout mMovieTrailerContainer;


    public MovieDetailFragment() {
        // Required empty public constructor
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void createTrailerTitleArray(JSONArray trailers) throws JSONException {

        ArrayList<String> mTrailerTitles = new ArrayList<String>();
        ArrayList<String> mTrailerUrls = new ArrayList<String>();

        try {

            for (int i = 0; i < trailers.length(); i++) {

                JSONObject currentObject = trailers.getJSONObject(i);

                String title = currentObject.getString("title");
                String url = currentObject.getString("url");

                mTrailerTitles.add(title);
                mTrailerUrls.add(url);
            }
        }catch (JSONException e){

        }

        final String[] title = mTrailerTitles.toArray(new String[mTrailerTitles.size()]);

        // Call to set up the adapter and make the changes
        addRowView(title);

    }

    private void addRowView(String[] trailerTitles){

        if (trailerTitles != null) {

            for (String trailer : trailerTitles) {

                View mMovieTrailerItem = LayoutInflater.from(getActivity()).inflate(
                        R.layout.trailer_list_item, null);

                // New data is back from the server.  Hooray!
                mMovieTrailerContainer.addView(mMovieTrailerItem);
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

        ArrayList<String> hello = new ArrayList<String>();
        hello.add("Hello");
        hello.add("from");
        hello.add("the");
        hello.add("other");
        hello.add("side");

        // Create adapter
        mTrailerAdapter = new CustomAdapter(hello, getActivity());
        ListView listView = (ListView) view.findViewById(R.id.trailer_list);
        listView.setAdapter(mTrailerAdapter);


        return view;
    }

    public class CustomAdapter extends ArrayAdapter<String> {

        private ArrayList<String> dataSet;
        Activity mContext;

        public CustomAdapter(ArrayList<String> data, Activity context) {
            super(context, R.layout.trailer_list_item, data);
            this.dataSet = data;
            this.mContext=context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater layoutInflater = mContext.getLayoutInflater();
            View row = layoutInflater.inflate(R.layout.trailer_list_item, null, true);

            TextView trailerTitle = (TextView) row.findViewById(R.id.trailer_title_item);
//            ImageView trailerImg = (ImageView) row.findViewById(R.id.trailer_play_item);
//            trailerImg.setImageDrawable(R.drawable.trailer_play);

            trailerTitle.setText(dataSet.get(position));


            return row;
        }

    }



    private static class trailerHolder{

        public TextView trailerTitle;

        public  ImageView playTrailerImg;
    }
}
