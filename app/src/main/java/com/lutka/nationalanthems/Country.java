package com.lutka.nationalanthems;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.graphics.Palette;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * Created by Paulina on 30/11/2014.
 */
public class Country
{
    String name;
    String code;
    LatLng location;
    boolean isEUMember;
    String anthem;
    Palette palette = null;

    public Country(String name, String code, LatLng location, boolean isEUMember)
    {
        this.name = name;
        this.code = code;
        this.location = location;
        this.isEUMember = isEUMember;
        this.anthem = "ogg/" +code + ".ogg";
    }

    public void generateColorPalette(Resources resources, String packageName)
    {
        BitmapDrawable drawable = (BitmapDrawable) resources.getDrawable(getFlagResourceId(resources, packageName));
        Bitmap bitmap = drawable.getBitmap();
        this.palette = Palette.generate(bitmap);
        Log.i("Color palette", toString()+" palette: "+palette);
    }

    public String getAnthem()
    {
        return anthem;
    }

    public String getCode()
    {
        return code;
    }

    public String getName()
    {
        return name;
    }

    public LatLng getLocation()
    {
        return location;
    }

    public boolean isEUMember()
    {
        return isEUMember;
    }

    public String getLyricsFile()
    {
        return String.format(Locale.ENGLISH, "lyrics/%s.txt", this.code);
    }

    public CharSequence getLyrics(AssetManager assetManager) throws IOException
    {
        final int BUFFER_SIZE = 100;
        InputStream inputStream = assetManager.open(getLyricsFile());
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        char[] buffer = new char[BUFFER_SIZE];
        int len;
        StringBuilder stringBuilder = new StringBuilder();
        while ((len = inputStreamReader.read(buffer, 0, BUFFER_SIZE)) > 0)
        {
            stringBuilder.append(buffer, 0, len);
        }
        inputStream.close();

        String string = stringBuilder.toString();
        int newLine = string.indexOf('\n');
        // make first line (title) bold
        if (newLine > 0)
        {
            SpannableString spannableString = new SpannableString(string);
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, newLine, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

            return spannableString;
        }
        else return string;
    }

    public int getFlagResourceId(Resources resources, String packageName)
    {
        return resources.getIdentifier(this.code, "drawable", packageName);
    }

    public int getColor()
    {
        if (palette != null)
            return Color.RED;
        else
        {
            return palette.getVibrantColor(Color.RED);
        }
    }

    public float getHue()
    {
        try
        {
            return palette.getLightVibrantSwatch().getHsl()[0];
        }
        catch (NullPointerException e)
        {
            return 0f;
        }
    }

    public Palette getPalette()
    {
        return palette;
    }

    @Override
    public String toString()
    {
        return name;
    }
}