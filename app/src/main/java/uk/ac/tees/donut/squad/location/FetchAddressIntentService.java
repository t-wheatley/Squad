package uk.ac.tees.donut.squad.location;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import uk.ac.tees.donut.squad.R;

import static android.R.id.list;
import static com.google.android.gms.wearable.DataMap.TAG;

/**
 * Created by Anthony Ward on 17/03/2017.
 */

public class FetchAddressIntentService extends IntentService {
    protected ResultReceiver mReceiver;
    private static final String TAG = "FetchAddyIntentService";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public FetchAddressIntentService(String name)
    {
        super(name);
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        list<Address> address = null;

        mReceiver = intent.getParcelableArrayExtra(Constants.RECEIVER);
        int fetchType = intent.getIntExtra(Contsants.FETCH_TYPE_EXTRA, 0);
        
    }
}

