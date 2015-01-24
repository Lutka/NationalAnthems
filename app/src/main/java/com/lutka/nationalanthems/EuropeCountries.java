package com.lutka.nationalanthems;

import android.content.res.Resources;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Paulina on 30/11/2014.
 */
public class EuropeCountries
{
    public static interface CountryColorPaletteListener
    {
        void onPaletteLoaded(Country country);
    }

    final Country [] europeanCountries;

    public Country[] getEuropeanCountries()
    {
        return europeanCountries;
    }

    //Array of European Countries and all necessary information related to them
    //I have used country codes in order to make life easier when using assets such: flags and audio files
    //LatLng coordinates are coordinates of the capital
    // value isEUMember  - not used currently but might be used in the future
    public EuropeCountries()
    {
        europeanCountries = new Country[]{
                new Country("Greece", "gr", new LatLng(37.9908372,23.738339413), true),
                new Country("Poland", "pl", new LatLng(52.2329379, 21.061194111), true),
                new Country("Ireland", "ie", new LatLng(53.3243201,-6.25169511), true),
                new Country("United Kingdom", "uk", new LatLng(51.5286416,-0.101598711), true),
                new Country("Germany", "de", new LatLng(52.5075419,13.4251364), true),
                new Country("Netherlands", "nl", new LatLng(52.3747158, 4.8986142), true),
                new Country("Belgium", "be", new LatLng(50.8387,4.363405), true),
                new Country("France", "fr", new LatLng(48.8588589,2.3470599), true),
                new Country("Spain", "es", new LatLng(40.4378271,-3.6795366), true),
                new Country("Italy", "it", new LatLng(41.9100711,12.5359979), true),
                new Country("Portugal", "pt", new LatLng(38.7436266,-9.1602037), true),
                new Country("Romania", "ro", new LatLng(44.4378258,26.0946376), true),
                new Country("Hungary", "hu", new LatLng(47.4805856,19.1303031), true),
                new Country("Bulgaria", "bg", new LatLng(42.6954322,23.3239467), true),
                new Country("Czech Republic", "cz", new LatLng(50.0596696,14.4656239), true),
                new Country("Lithuania", "lt", new LatLng(54.700171,25.2529321), true),
                new Country("Norway", "no", new LatLng(59.893855,10.7851166), true),
                new Country("Sweden", "se", new LatLng(59.326142,17.9875456), true),
                new Country("Finland", "fi", new LatLng(60.1733239,24.9410248), true),
                new Country("Croatia", "hr", new LatLng(45.840196,15.9643316), true),
                new Country("Serbia", "rs", new LatLng(44.1256196,20.334852), true),
                new Country("Latvia", "lv", new LatLng(56.9714745,24.1291625), true),
                new Country("Estonia", "ee", new LatLng(59.424959,24.7382414), true),
                new Country("Belarus", "by", new LatLng(53.8838884,27.594974), true),
                new Country("Ukraine", "ua", new LatLng(50.6027163,30.7846895), true),
                new Country("Montenegro", "me", new LatLng(42.7044223,19.3957785), false),
                new Country("Austria", "at", new LatLng(48.2206849,16.3800599), true),
                new Country("Switzerland", "ch", new LatLng(47.377455,8.536715), false),
                new Country("Liechtenstein", "li", new LatLng(47.1594184,9.553635), true),
                new Country("Luxemburg", "lu", new LatLng(49.6076049,6.1358701), true),
                new Country("Denmark", "dk", new LatLng(55.6712673,12.5608388), true),
                new Country("Malta", "mt", new LatLng(35.9440174,14.3795242), true),
                new Country("Albania", "al", new LatLng(41.1529058,20.1605717), true),
                new Country("Macedonia", "mk", new LatLng(41.9990903,21.4248903), true),
                new Country("Russia", "ru", new LatLng(55.749792,37.632495), true),
                new Country("Vatican", "va", new LatLng(41.9038795,12.4520834), true),
                new Country("Andorra", "ad", new LatLng(42.5422699,1.5976721), true),
                new Country("Slovenia", "si", new LatLng(46.0661174,14.5320991), true),
                new Country("Moldova", "md", new LatLng(46.9998691,28.8581765), true),
                new Country("Slovakia", "sk", new LatLng(48.1357804,17.1159171), true),
                new Country("Bosnia and Herzegovina", "ba", new LatLng(43.851882,18.383925), true),
        };

    }
    public int getNumberOfCountries()
    {
        return  europeanCountries.length;
    }

    //use to generate color palette necessary for assigning colors to the markers
    public void generateColorPalettes(Resources resources, String packageName, CountryColorPaletteListener countryColorPaletteListener)
    {
        for (Country country : europeanCountries)
        {
            country.generateColorPalette(resources, packageName);
            if (countryColorPaletteListener != null)
                countryColorPaletteListener.onPaletteLoaded(country);
        }
    }
}
