package uk.ac.tees.donut.squad.location;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

/**
 * Created by Anthony Ward on 17/03/2017.
 */

public class FetchAddressIntentService extends IntentService {
    protected ResultReceiver mReceiver;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public FetchAddressIntentService(String name) {
        super(name);
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
