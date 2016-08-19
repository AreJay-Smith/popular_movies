package com.arejaysmith.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

public class MainActivity extends AppCompatActivity implements MovieListFragment.Callback, MovieListFragment.Data {

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences prefs = this.getSharedPreferences("twoPane", this.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        // Set default
        edit.putString("movie_list", "popular");
        edit.apply();
        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
            edit.putBoolean("twoPane", mTwoPane);
            edit.apply();
        } else {
            mTwoPane = false;
            edit.putBoolean("twoPane", mTwoPane);
            edit.apply();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.refresh) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        MovieDetailFragment mdf = (MovieDetailFragment) getSupportFragmentManager().findFragmentById(R.id.movie_detail_container);

    }

    @Override
    public void onItemSelected() {

    }

    @Override
    public void onItemSelected(Movie movieSelected) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable("movie", movieSelected);

            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent mIntent = new Intent(this, MovieDetailActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putParcelable("movie", movieSelected);
            mIntent.putExtras(mBundle);

            startActivity(mIntent);
        }

    }

    @Override
    public void dataReceived(Movie movieSelected) {

        Bundle args = new Bundle();
        args.putParcelable("movie", movieSelected);

        MovieDetailFragment fragment = new MovieDetailFragment();
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                .commitAllowingStateLoss();
    }
}
