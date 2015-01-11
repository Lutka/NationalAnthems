/**
 * Created by Paulina on 15/11/2014.
 */
package com.lutka.nationalanthems;
/*
having a screen which have lyrics there and allow playing anthem again, required number of times - user input
 */

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

public class MapActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMapClickListener
{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    EuropeCountries europeCountries = new EuropeCountries();
    HashMap<Marker, Country> markerCountryHashMap = new HashMap<Marker, Country>();
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
                for (Country country : europeCountries.getEuropeanCountries())
                    if (country.anthemExists(getAssets()) == false)
                        Log.e("Anthem file missing", "No anthem file for "+country+". Expected file "+country.getAnthem());
                return null;
            }

            @Override
            protected void onProgressUpdate(Country... values)
            {
                super.onProgressUpdate(values);
                addCountryPin(values[0]);
            }
        }.execute();

    }

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

    private void setUpMap()
    {
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMapClickListener(this);
        // hide option to navigate to country and open external map
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.2139194, 15.8411428), 3.0f));

    }

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

    private void addPins()
    {
        for (Country country : europeCountries.getEuropeanCountries())
        {
            addCountryPin(country);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        stopMediaPlayer();
       // Toast.makeText(this, marker.getTitle(), Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker)
    {
        playAnthem(marker);
        showCountryDialog(markerCountryHashMap.get(marker));
    }

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


    void updateMediaControl(final MediaPlayer mediaPlayer)
    {
        if (this.mediaControlLayout != null)
        {
            ImageButton imageButton = (ImageButton) mediaControlLayout.findViewById(R.id.btnStartStop);
            SeekBar seekBar = (SeekBar) mediaControlLayout.findViewById(R.id.seekBar);

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
        if (country.anthemExists(getAssets()) == false)
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

        ImageView flag = (ImageView) dialogView.findViewById(R.id.flag);
        flag.setImageResource(country.getFlagResourceId(getResources(), getPackageName()));
        final Dialog dialog = builder.create();
        dialog.show();
    }
}
