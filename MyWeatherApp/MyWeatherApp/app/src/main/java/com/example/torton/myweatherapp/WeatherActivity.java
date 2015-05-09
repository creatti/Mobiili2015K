package com.example.torton.myweatherapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class WeatherActivity extends ActionBarActivity {
    public static ArrayAdapter<String> forecastArrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        if (savedInstanceState == null) {  // Ei palautettavaa dataa
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        else { // savedInstanceState != null
            // Haetaan merkkijonolista
            ArrayList<String> forecasts = savedInstanceState.getStringArrayList("FORECAST_STRING");
            // Luodaan array adapter listan pohjalta
            forecastArrayAdapter =
                    new ArrayAdapter<String>( this, R.layout.forecast_list_item_layout,
                            R.id.forecastTextView, forecasts);

        }
        if( forecastArrayAdapter == null ){
            forecastArrayAdapter =
                    new ArrayAdapter<String>( this, R.layout.forecast_list_item_layout,
                            R.id.forecastTextView, new ArrayList<String>());
        }

        
    }

    public void onSaveInstanceState( Bundle savedInstanceState ){
        // Talleta aktiviteetin tiedot tässä (list array adapterin string -taulukko)
        ArrayList<String> forecastStrings = new ArrayList<String>();
        for( int i=0; i<forecastArrayAdapter.getCount(); i++ ){
            forecastStrings.add( forecastArrayAdapter.getItem( i ));
        }
        // Talletetaan forecastStrings bundleen
        savedInstanceState.putStringArrayList("FORECAST_STRING", forecastStrings);
        super.onSaveInstanceState( savedInstanceState );
    }

    public void onRestoreInstanceState( Bundle savedInstanceState ){

        ArrayList<String> forecastStrings = savedInstanceState.getStringArrayList("FORECAST_STRING");
        forecastArrayAdapter =
                new ArrayAdapter<String>( this, R.layout.forecast_list_item_layout, R.id.forecastTextView, forecastStrings);
        super.onSaveInstanceState(savedInstanceState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weather, menu);
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
            Intent intent = new Intent( this, DetailActivity.class ).putExtra( Intent.EXTRA_TEXT, "Settings" );
            startActivity( intent );
            return true;
        }
        if( id == R.id.action_refresh ){
            new GetWeatherTask().execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */


    public String doWebRequest() {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=tampere");

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return forecastJsonStr;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
                Log.d("Line:", line );
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return forecastJsonStr;
            }
            forecastJsonStr = buffer.toString();
            Log.d("JSON response", forecastJsonStr );
        } catch (IOException e) {
            Log.e("PlaceholderFragment", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return forecastJsonStr;
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }

        return forecastJsonStr;
    }

    private class GetWeatherTask extends AsyncTask<Object, Void, String > {
        protected String doInBackground(Object[] params) {

            return doWebRequest();
        }

        protected void onPostExecute( String jsonResponse ){
            // Handle JSON and update UI
            Log.d( "JSON response", jsonResponse );
            parseWeatherDataFromJsonResponse(jsonResponse);
        }

        protected void onProgressUpdate(){

        }
    }

    private void parseWeatherDataFromJsonResponse(String jsonResponse) {
        JSONObject weatherForecastJSON = null;
        try {
            // Tyhjennetään ArrayAdapter (näytöllä oleva lista)
            forecastArrayAdapter.clear();
            weatherForecastJSON = new JSONObject( jsonResponse );
            JSONArray forecastArray = weatherForecastJSON.getJSONArray("list");
            for( int i=0; i<forecastArray.length(); i++ ){
                JSONObject weatherItem = forecastArray.getJSONObject(i);
                JSONArray weatherArray = weatherItem.getJSONArray("weather");
                for( int j=0; j<weatherArray.length(); j++ ){
                    String description = weatherArray.getJSONObject(j).getString("description");
                    // Lisätään description adapteriin
                    forecastArrayAdapter.add( description );
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static class PlaceholderFragment extends Fragment {



        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
            ListView listView = (ListView)rootView.findViewById(R.id.listView);
            listView.setAdapter(forecastArrayAdapter);
            listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Näytetään valitun listItemin teksti näytöllä käyttämällä Toastia
                    String listItemText = forecastArrayAdapter.getItem( position );
                    Toast.makeText( getActivity(), listItemText, Toast.LENGTH_LONG ).show();
                    // Avataan Detail -aktiviteetti ja välitetään list itemin teksti parametrina
                    Intent intent = new Intent( getActivity(), DetailActivity.class ).putExtra( Intent.EXTRA_TEXT, listItemText );
                    startActivity( intent );
                }
            });


            return rootView;
        }
    }

}
















