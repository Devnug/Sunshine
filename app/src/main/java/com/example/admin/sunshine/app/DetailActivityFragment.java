package com.example.admin.sunshine.app;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.admin.sunshine.app.data.WeatherContract;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    TextView forecastDay;
    TextView forecastDate;
    TextView forecastHigh;
    TextView forecastLow;
    ImageView forecastImage;
    TextView forecastDesc;
    TextView forecastHumidity;
    TextView forecastWind;
    TextView forecastPressure;
    Uri mUri;


    private ShareActionProvider mShareActionProvider;

    private final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    private static final int FORECAST_LOADER = 0;

    static final String DETAIL_URI = "URI";

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    static final int COL_HUMIDITY = 9;
    static final int COL_PRESSURE = 10;
    static final int COL_WIND_SPEED = 11;
    static final int COL_DEGREES = 12;

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private String mForecastStr;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    public static DetailActivityFragment newInstance(Uri uri) {
        DetailActivityFragment df = new DetailActivityFragment();
        Bundle args = new Bundle();
        args.putString("Uri", uri.toString());
        df.setArguments(args);
        return df;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
        }

        forecastDay = (TextView) rootView.findViewById(R.id.detail_day_text);
        forecastDate = (TextView) rootView.findViewById(R.id.detail_date_text);
        forecastHigh = (TextView) rootView.findViewById(R.id.detail_high_temp);
        forecastLow = (TextView) rootView.findViewById(R.id.detail_low_temp);
        forecastImage = (ImageView) rootView.findViewById(R.id.detail_icon);
        forecastDesc = (TextView) rootView.findViewById(R.id.detail_desc_text);
        forecastHumidity = (TextView) rootView.findViewById(R.id.detail_humidity);
        forecastWind = (TextView) rootView.findViewById(R.id.detail_wind_text);
        forecastPressure = (TextView) rootView.findViewById(R.id.detail_pressure_text);

        return rootView;
    }

    private static final int DETAIL_ID = 0;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_ID, getArguments(), this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = new ShareActionProvider(getActivity()); //ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (mForecastStr != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
            MenuItemCompat.setActionProvider(item, mShareActionProvider);
            Log.e(LOG_TAG, "Share Action Provider is not null?");
        } else {
            Log.e(LOG_TAG, "Share Action Provider is null?");
        }
        super.onCreateOptionsMenu(menu, inflater);
        //return true;
    }

    void onLocationChanged(String newLocation) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_ID, null, this);
        }
    }

    public Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");
        switch (id) {
            case DETAIL_ID:
                if(null == mUri)
                    return null;
//                mUri = intent.getData();
                Log.v(LOG_TAG, "intent is not null");
                return new CursorLoader(getActivity(),
                        mUri,
                        FORECAST_COLUMNS,
                        null,
                        null,
                        null
                );
            default:
                // An invalid id was passed in
                return null;

        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) {
            return;
        }

        int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
        // Add images later on
        forecastImage.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

        String dayString = Utility.getFriendlyDayString(this.getActivity(), Long.parseLong(data.getString(COL_WEATHER_DATE)));

        String dateString = Utility.getFormattedMonthDay(this.getActivity(), Long.parseLong(data.getString(COL_WEATHER_DATE)));

        String weatherDescription = data.getString(COL_WEATHER_DESC);

        String humidityString = Utility.getFormattedHumdity(this.getActivity(), data.getString(COL_HUMIDITY));

        String pressureString = Utility.getFormattedPressure(this.getActivity(), data.getString(COL_PRESSURE));

        String windSpeedString = Utility.getFormattedWind(this.getActivity(), Float.parseFloat(data.getString(COL_WIND_SPEED)), Float.parseFloat(data.getString(COL_DEGREES)));

        boolean isMetric = Utility.isMetric(getActivity());

        String high = Utility.formatTemperature(this.getActivity(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);

        String low = Utility.formatTemperature(this.getActivity(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

        forecastDay.setText(dayString);
        forecastDate.setText(dateString);
        forecastHigh.setText(high);
        forecastLow.setText(low);
        forecastDesc.setText(weatherDescription);
        forecastHumidity.setText(humidityString);
        forecastWind.setText(windSpeedString);
        forecastPressure.setText(pressureString);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
