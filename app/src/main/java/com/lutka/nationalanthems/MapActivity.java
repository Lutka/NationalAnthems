/**
 * Created by Paulina on 15/11/2014.
 *
 * Class where all the android action logic is implemented
 *
 */
package com.lutka.nationalanthems;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity extends FragmentActivity implements GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMapClickListener
{
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    EuropeCountries europeCountries = new EuropeCountries();
    //hash map hashing markers with countries
    HashMap<Marker, Country> markerCountryHashMap = new HashMap<Marker, Country>();
    //media player to play anthems
    private MediaPlayer mediaPlayer = null;
    View mediaControlLayout = null;
    Timer mediaControlUpdateTimer = null;
    // ui handler handles task from timer thread and executes is in the ui thread to update media controls
    Handler uiHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        setUpMapIfNeeded();
        //asyncTask for multithreading: getting marker colours and checking for missing resources: lyrics and national anthems .ogg files
        new AsyncTask<Void, Country, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                europeCountries.generateColorPalettes(getResources(), getPackageName(), new EuropeCountries.CountryColorPaletteListener()
                {
                    @Override
                    public void onPaletteLoaded(Country country)
                    {
                        publishProgress(country);
                    }
                });
                //log error to check what resources are missing
                for (Country country : europeCountries.getEuropeanCountries())
                {
                    if (!country.anthemExists(getAssets()))
                        Log.e("Anthem file missing", "No anthem file for " + country + ". Expected file " + country.getAnthem());

                    if (!country.lyricExists(getAssets()))
                        Log.e("Anthem file missing", "No lyric file for " + country + ". Expected file " + country.getLyricName());
                }

                return null;
            }

            //add country pins to the map
            @Override
            protected void onProgressUpdate(Country... values)
            {
                super.onProgressUpdate(values);
                addCountryPin(values[0]);
            }
        }.execute();

    }

    //method to play anthem, done using media player
    public void playAnthem(Marker marker)
    {
        if(mediaPlayer == null)
        {
            try
            {
                AssetFileDescriptor anthemFile = markerCountryHashMap.get(marker).getAnthem(getAssets());
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
                mediaControlUpdateTimer = new Timer();
                mediaControlUpdateTimer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        // execute in main thread
                        uiHandler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                updateMediaControl(mediaPlayer);
                            }
                        });
                    }
                }, 0, 200);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onResume()
    {
        super.onResume();
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

    //this method is used to set up map on the start, all the listeners are here as well as
    // zooming the map onto chosen location (in this case Europe) with chosen zoom factor.
    private void setUpMap()
    {
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMapClickListener(this);
        // hide option to navigate to country and open external map
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.2139194, 15.8411428), 3.0f));

    }

    //create marker and add it to marker-country hash map
    void addCountryPin(Country country)
    {
        float hue = country.getHue();
        Log.i("Marker", country.toString() + " hue: " + hue);
        Log.i("Marker", country.toString()+" color: "+country.getColor());
        // normalize colors as google maps only accepts values between 0 and 360
        while (hue < 0f) hue += 360f;
        while (hue > 360f) hue -= 360f;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(country.getLocation())
                .icon(BitmapDescriptorFactory.defaultMarker(hue))
                .title(country.getName());
        Marker marker = mMap.addMarker(markerOptions);

        markerCountryHashMap.put(marker, country);
    }

    //action when info window has been clicked: play anthem, show country dialog
    @Override
    public void onInfoWindowClick(Marker marker)
    {
        playAnthem(marker);
        showCountryDialog(markerCountryHashMap.get(marker));
    }

    //mainly used for landscape orientation
    @Override
    public void onMapClick(LatLng latLng)
    {
        stopMediaPlayer();
    }

    public void stopMediaPlayer()
    {
        if(mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer = null;
            mediaControlUpdateTimer.cancel();
            mediaControlUpdateTimer = null;
        }
    }

    //use for updating state of media control relatively to any action made, eg. stop button being pressed
    void updateMediaControl(final MediaPlayer mediaPlayer)
    {
        if (this.mediaControlLayout != null)
        {
            ImageButton imageButton = (ImageButton) mediaControlLayout.findViewById(R.id.btnStartStop);
            SeekBar seekBar = (SeekBar) mediaControlLayout.findViewById(R.id.seekBar);

            //used seekBar to display how much of the anthem was played
            seekBar.setMax(mediaPlayer.getDuration());
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar)
                {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar)
                {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            });
            //setting pause, play image depending on the state of the mediaPlayer
            if (mediaPlayer.isPlaying())
            {
                imageButton.setImageResource(android.R.drawable.ic_media_pause);
                imageButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        mediaPlayer.pause();
                        updateMediaControl(mediaPlayer);
                    }
                });
            }
            else
            {
                imageButton.setImageResource(android.R.drawable.ic_media_play);
                imageButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        mediaPlayer.start();
                        updateMediaControl(mediaPlayer);
                    }
                });
            }
        }
    }

    //country dialog displayed when a particular country pin has been clicked
    void showCountryDialog(Country country)
    {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.country_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView).setTitle(country.getName())
                .setNegativeButton(R.string.close, null)
                .setOnDismissListener(new DialogInterface.OnDismissListener()
                {
                    @Override
                    public void onDismiss(DialogInterface dialog)
                    {
                        mediaControlLayout = null;
                        dialog.cancel();
                        stopMediaPlayer();
                    }
                });

        this.mediaControlLayout = dialogView.findViewById(R.id.layoutMediaControl);
        if (!country.anthemExists(getAssets()))
            mediaControlLayout.setVisibility(View.GONE);
        // lyrics
        TextView tvLyrics = (TextView) dialogView.findViewById(R.id.tvLyrics);
        try
        {
            tvLyrics.setText(country.getLyrics(getAssets()));
        } catch (IOException e)
        {
            tvLyrics.setText("");
        }
        // to display particular countries flag
        ImageView flag = (ImageView) dialogView.findViewById(R.id.flag);
        flag.setImageResource(country.getFlagResourceId(getResources(), getPackageName()));
        final Dialog dialog = builder.create();
        dialog.show();
    }
}
