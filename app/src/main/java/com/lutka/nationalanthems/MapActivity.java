/**
 * Created by Paulina on 15/11/2014.
 */
package com.lutka.nationalanthems;

import android.app.AlertDialog;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MapActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMapClickListener
{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    EuropeCountries europeCountries = new EuropeCountries();
    HashMap<Marker, Country> markerCountryHashMap = new HashMap<Marker, Country>();
    private MediaPlayer mediaPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setUpMapIfNeeded();

    }

    public void playAnthem(Marker marker){
        if(mediaPlayer == null)
        {
            try
            {
                String anthemId = markerCountryHashMap.get(marker).getAnthem();
                AssetFileDescriptor anthemFile = getAssets().openFd(anthemId);
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener()
                {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra)
                    {
                        new AlertDialog.Builder(MapActivity.this)
                                .setMessage("an error, what" + what + "extra " + extra)
                                .show();
                        return false;
                    }
                });
                mediaPlayer.setDataSource(anthemFile.getFileDescriptor(), anthemFile.getStartOffset(), anthemFile.getLength());
                // Log.i("Anthem "," Len "+ anthemFile.getLength());
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e)
            {
                e.printStackTrace();

                new AlertDialog.Builder(MapActivity.this)
                        .setMessage(e.getMessage())
                        .show();
            }
        }
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded()
    {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null)
        {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null)
            {
                setUpMap();
            }
        }
    }

    private void setUpMap()
    {
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.2139194, 15.8411428), 3.0f));

        addPins();
    }

    private void addPins()
    {
        MarkerOptions markerOptions;
        Marker marker;
        for (int i = 0; i < europeCountries.getNumberOfCountries(); i++)
        {
            markerOptions = new MarkerOptions()
                    .position(europeCountries.getEuropeanCountries()[i].getLocation())
                    .title(europeCountries.getEuropeanCountries()[i].getName());
            marker = mMap.addMarker(markerOptions);

            markerCountryHashMap.put(marker, europeCountries.getEuropeanCountries()[i]);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
       // Toast.makeText(this, marker.getTitle(), Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker)
    {
        playAnthem(marker);
    }

    @Override
    public void onMapClick(LatLng latLng)
    {
        if(mediaPlayer.isPlaying())
        {
            mediaPlayer.stop();
            Toast.makeText(this, "mediaPlayer should stop", Toast.LENGTH_SHORT).show();
        }
    }
}
