package uk.ac.tees.donut.squad.posts;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.google.android.gms.maps.model.LatLng;

import uk.ac.tees.donut.squad.location.FetchAddressIntentService;
import uk.ac.tees.donut.squad.location.LocContants;


/**
 * Created by q5273202 on 11/04/2017.
 */

public class Place
{
    public String placeId;

    protected AddressResultReceiver mResultReceiver;
    protected int fetchType;

    public String name;
    public String interest;
    public String description;

    public Double locLat;
    public Double locLong;
    public LatLng latLng;

    public String address1;
    public String address2;
    public String townCity;
    public String county;
    public String postCode;
    public String addressFull;

    public String userId;


    public Place()
    {
        //empty constructor
    }
    public Place(String pi, String n, String i, String d, double la, double lo, String u)
    {
        placeId = pi;

        name = n;
        interest = i;
        description = d;

        locLat = la;
        locLong = lo;

        userId = u;

        updateLatLng();
        updateAddress();
    }
    public Place(String pi, String n, String i, String d, LatLng ll, String u)
    {
        placeId = pi;

        name = n;
        interest = i;
        description = d;

        latLng = ll;

        userId = u;

        updateLoc();
        updateAddress();
    }
    public Place(String pi, String n, String i, String d, String a1, String a2, String tc, String c, String pc, String u)
    {
        placeId = pi;

        name = n;
        interest = i;
        description = d;

        address1 = a1;
        address2 = a2;
        townCity = tc;
        county = c;
        postCode = pc;

        userId = u;

        updateLatLng();
        updateLoc();
    }

    //GETTERS
    public String getPlaceId()
    {
        return placeId;
    }

    public String getName()
    {
        return name;
    }
    public String getInterest()
    {
        return interest;
    }
    public String getDescription()
    {
        return description;
    }

    public double getLocLat()
    {
        return locLat;
    }
    public double getLocLong()
    {
        return locLong;
    }
    public LatLng getLatLng()
    {
        return latLng;
    }

    public String getAddress()
    {
        String a = "";
        if(address1 != null)
            a = a + address1;
        if(address2 != null)
            a = a + ", " + address2;
        if(townCity != null)
            a = a + ", " + townCity;
        if(county != null)
            a = a + ", " + county;
        if(postCode != null)
            a = a + ", " + postCode;

        return a;
    }

    public String getAddress1()
    {
        return address1;
    }
    public String getAddress2()
    {
        return address2;
    }
    public String getTownCity()
    {
        return townCity;
    }
    public String getCounty()
    {
        return county;
    }
    public String getPostCode()
    {
        return postCode;
    }

    public String getUserId()
    {
        return userId;
    }

    //SETTERS
    public void setPlaceId(String pi)
    {
        placeId = pi;
    }

    public void setName(String n)
    {
        name = n;
    }
    public void setInterest(String i)
    {
        interest = i;
    }
    public void setDescription(String d)
    {
        description = d;
    }

    public void setLocLat(double la)
    {
        locLat = la;
    }
    public void setLocLong(double lo)
    {
        locLong = lo;
    }
    public void setLatLng(LatLng ll)
    {
        latLng = ll;
    }

    public void setAddress1(String a1)
    {
        address1 = a1;
    }
    public void setAddress2(String a2)
    {
        address2 = a2;
    }
    public void setTownCity(String tc)
    {
        townCity = tc;
    }
    public void setCounty(String c)
    {
        county = c;
    }
    public void setPostCode(String pc)
    {
        postCode = pc;
    }

    public void setUserId(String u)
    {
        userId = u;
    }

    //ADDRESS/LOCATION MANAGEMENT METHODS
    public void updateLatLng()
    {
        //update LatLng
        if(locLong != null && locLat != null)
        {
            //update using locLong and locLat
        }
        else if(getAddress().length() > 0)
        {
            //update using address fields
        }
        else
        {
            //nothing happens, unable to update LatLng
            //error handling?
        }

    }
    public void updateLoc()
    {
        //update locLong and locLat using the LatLng variable
    }
    public void updateAddress()
    {
        //update the address using
    }

    private void geocode(){
        addressFull = address1 + " " + address2 + " " + townCity + " " + county + " " + postCode;
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(LocContants.RECEIVER, mResultReceiver);
        intent.putExtra(LocContants.FETCH_TYPE_EXTRA, fetchType);
        if(fetchType == LocContants.USE_ADDRESS_NAME) {
            if(addressFull.length() == 0) {
                return;
            }
            intent.putExtra(LocContants.LOCATION_NAME_DATA_EXTRA, addressFull);
        }

        startService(intent);
    }

    //Inner Class to recieve address for Geocoder
    public class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, final Bundle resultData) {
            if (resultCode == LocContants.SUCCESS_RESULT) {
                final Address address = resultData.getParcelable(LocContants.RESULT_ADDRESS);

            }
        }
    }
}
