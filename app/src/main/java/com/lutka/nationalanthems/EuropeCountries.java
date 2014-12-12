package com.lutka.nationalanthems;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.lutka.nationalanthems.Country;

import java.util.ArrayList;

/**
 * Created by Paulina on 30/11/2014.
 */
public class EuropeCountries
{
    final Country [] europeanCountries;

    public Country[] getEuropeanCountries()
    {
        return europeanCountries;
    }

    public EuropeCountries()
    {
        europeanCountries = new Country[]{
                new Country("Poland", "PL", new LatLng(52.2329379, 21.061194111), true),
                new Country("Ireland", "IE", new LatLng(53.3243201,-6.25169511), true),
                new Country("United Kingdom", "UK", new LatLng(51.5286416,-0.101598711), true),
                new Country("Greece", "GR", new LatLng(37.9908372,23.738339413), true),
        };

    }
    public int getNumberOfCountries()
    {
        return  europeanCountries.length;
    }



}
