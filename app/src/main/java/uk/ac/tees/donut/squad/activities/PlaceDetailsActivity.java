package uk.ac.tees.donut.squad.activities;

import android.media.Image;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;

import android.widget.ImageSwitcher;
import android.widget.RelativeLayout;
import android.widget.TextView;

import uk.ac.tees.donut.squad.R;

public class PlaceDetailsActivity extends AppCompatActivity {

    TextView placeName;
    TextView description;
    TextView noPic;

    ImageSwitcher gallery;
    RelativeLayout galleryLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);

        //getting UI Elements
        placeName = (TextView) findViewById(R.id.placeNameText);
        description = (TextView) findViewById(R.id.descriptionText);
        noPic = (TextView) findViewById(R.id.noPic);

        gallery = (ImageSwitcher) findViewById(R.id.placeGallery);
        galleryLayout = (RelativeLayout) findViewById(R.id.galleryLayout);

        //if there are no pictures
        boolean pics = false; //TEMPORARY TILL WE CAN ATTEMPT AT LOADING PICS
        if(pics)
        {
            //keeps the noPic text, and changes the height of the layout so it's not too big
            ViewGroup.LayoutParams params = galleryLayout.getLayoutParams();
            params.height = 35;
            galleryLayout.setLayoutParams(params);
        }
        else
            //gets rid of the noPic text
            noPic.setVisibility(View.GONE);
        
    }

    private void openMapLocation(View view)
    {

    }

    private void openMapDirections(View view)
    {

    }
}
