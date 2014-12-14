package com.lutka.nationalanthems;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;

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


    public Country(String name, String code, LatLng location, boolean isEUMember)
    {
        this.name = name;
        this.code = code;
        this.location = location;
        this.isEUMember = isEUMember;
        this.anthem = code + ".ogg";
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

}