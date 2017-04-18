package uk.ac.tees.donut.squad.location;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;




import uk.ac.tees.donut.squad.R;

/**
 * Created by Anthony Ward
 */
public class GeocoderActivity extends AppCompatActivity {

    AddressResultReceiver mResultReceiver;

    EditText latitudeEdit, longitudeEdit, addressEdit;
    ProgressBar progressBar;
    TextView infoText;
    CheckBox checkBox;
    MapActivity m =new MapActivity();

    boolean fetchAddress;
    int fetchType = LocContants.USE_ADDRESS_LOCATION;
    private static final String TAG = "GEOCODER";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locating);

        longitudeEdit = (EditText) findViewById(R.id.longitudeEdit);
        latitudeEdit = (EditText) findViewById(R.id.latitudeEdit);
        addressEdit = (EditText) findViewById(R.id.addressEdit);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        infoText = (TextView) findViewById(R.id.infoText);
        checkBox = (CheckBox) findViewById(R.id.checkbox);

        mResultReceiver = new AddressResultReceiver(null);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.radioAddress:
                if (checked) {
                    fetchAddress = false;
                    fetchType = LocContants.USE_ADDRESS_NAME;
                    longitudeEdit.setEnabled(false);
                    latitudeEdit.setEnabled(false);
                    addressEdit.setEnabled(true);
                    addressEdit.requestFocus();
                }
                break;
            case R.id.radioLocation:
                if (checked) {
                    fetchAddress = true;
                    fetchType = LocContants.USE_ADDRESS_LOCATION;
                    latitudeEdit.setEnabled(true);
                    latitudeEdit.requestFocus();
                    longitudeEdit.setEnabled(true);
                    addressEdit.setEnabled(false);
                }
                break;
        }
    }



    public void onButtonClicked(View view) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(LocContants.RECEIVER, mResultReceiver);
        intent.putExtra(LocContants.FETCH_TYPE_EXTRA, fetchType);
        if(fetchType == LocContants.USE_ADDRESS_NAME) {
            if(addressEdit.getText().length() == 0) {
                Toast.makeText(this, "Please enter an address name", Toast.LENGTH_LONG).show();
                return;
            }
            intent.putExtra(LocContants.LOCATION_NAME_DATA_EXTRA, addressEdit.getText().toString());
        }
        else {
            if(latitudeEdit.getText().length() == 0 || longitudeEdit.getText().length() == 0) {
                Toast.makeText(this,
                        "Please enter both latitude and longitude",
                        Toast.LENGTH_LONG).show();
                return;
            }
            intent.putExtra(LocContants.LOCATION_LATITUDE_DATA_EXTRA,
                    Double.parseDouble(latitudeEdit.getText().toString()));
            intent.putExtra(LocContants.LOCATION_LONGITUDE_DATA_EXTRA,
                    Double.parseDouble(longitudeEdit.getText().toString()));
        }
        infoText.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        Log.e(TAG, "Starting Service");
        startService(intent);
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, final Bundle resultData) {
            if (resultCode == LocContants.SUCCESS_RESULT) {
                final Address address = resultData.getParcelable(LocContants.RESULT_ADDRESS);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        infoText.setVisibility(View.VISIBLE);
                        infoText.setText("Latitude: " + address.getLatitude() + "\n" +
                                "Longitude: " + address.getLongitude() + "\n" +
                                "Address: " + resultData.getString(LocContants.RESULT_DATA_KEY));

                    }
                });
            }
            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        infoText.setVisibility(View.VISIBLE);
                        infoText.setText(resultData.getString(LocContants.RESULT_DATA_KEY));
                    }
                });
            }
        }
    }


}

