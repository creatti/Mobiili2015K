package com.example.torton.myweatherapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class DetailActivity extends ActionBarActivity implements LocationListener, SensorEventListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
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
            return true;
        }
        if( id == R.id.action_start_positioning ){
            startPositioning();
            return true;
        }
        if( id == R.id.action_start_sensors ){
            startSensors();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startSensors() {
        SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for( int i=0; i<sensorList.size(); i++ ){
            Toast.makeText(this, sensorList.get( i ).getName(), Toast.LENGTH_SHORT).show();
        }
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener( this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL );


    }

    private void startPositioning() {
        // Haetaan nykyinen sijainti (jos verkko saatavilla)
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE );
        if( locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER)){
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            String myLocation = "Lat: " + lastKnownLocation.getLatitude() + " Long: " + lastKnownLocation.getLongitude();
            // Näytetään sijainti Toastina
            Toast.makeText(this, myLocation, Toast.LENGTH_LONG).show();
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this );

    }

    @Override
    public void onLocationChanged(Location location) {
        // Uusi location saatu
        String myLocation = "Lat: " + location.getLatitude() + " Long: " + location.getLongitude();
        // Näytetään uusi sijainti Toastina
        Toast.makeText(this, myLocation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("SENSOR EVENT: ", "Accelerator X-axis: " + event.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            Intent intent = getActivity().getIntent();
            if( intent != null && intent.hasExtra( Intent.EXTRA_TEXT )){
                String listItemText = intent.getStringExtra( Intent.EXTRA_TEXT );
                TextView weatherTextView = (TextView)rootView.findViewById(R.id.weatherTextView);
                weatherTextView.setText( listItemText );
            }

            return rootView;
        }


    }
}











