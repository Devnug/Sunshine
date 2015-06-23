package com.example.admin.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.admin.sunshine.app.sync.SunshineSyncAdapter;


public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback{

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private String mLocation;
    public boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        mLocation = Utility.getPreferredLocation(this);
        if(findViewById(R.id.weather_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.weather_detail_container, new DetailActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        }
        else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
        ForecastFragment ff = ((ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast));
        ff.setUseTodayLayout(!mTwoPane);
        SunshineSyncAdapter.initializeSyncAdapter(this);
    }

        @Override
        protected void onResume() {
                super.onResume();
                String location = Utility.getPreferredLocation( this );
                // update the location in our second pane using the fragment manager
                if (location != null && !location.equals(mLocation)) {
                        ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
                        if ( null != ff ) {
                                ff.onLocationChanged();
                            }
                    DetailActivityFragment df = (DetailActivityFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
                    if ( null != df ) {
                        df.onLocationChanged(location);
                    }
                        mLocation = location;
                }
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            // Launch the Settings Activity
            Intent settingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(settingsActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailActivityFragment.DETAIL_URI, contentUri);

            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }
}
