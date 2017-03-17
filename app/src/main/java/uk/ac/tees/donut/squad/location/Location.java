package uk.ac.tees.donut.squad.location;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import uk.ac.tees.donut.squad.R;

import static com.google.android.gms.wearable.DataMap.TAG;

/**
 * Created by Anthony Ward on 16/03/2017.
 */

public class Location extends IntentService implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private ResultReceiver mReceiver;
    private String Address;
    private ResultReceiver mResultReceiver;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public Location(String name) {
        super(name);

    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }




    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }

   // protected void startIntentService() {
     //   Intent intent = new Intent(this, FetchAddressIntentService.class);
       // intent.putExtra(Constants.RECEIVER, mResultReceiver);
        //intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        //startService(intent);
   // }
}
