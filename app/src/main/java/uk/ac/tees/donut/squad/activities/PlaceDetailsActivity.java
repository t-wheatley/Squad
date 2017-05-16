package uk.ac.tees.donut.squad.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mvc.imagepicker.ImagePicker;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import uk.ac.tees.donut.squad.R;
import uk.ac.tees.donut.squad.posts.LocPlace;

/**
 * Activity which allows the user to view the details of a Place.
 */

public class PlaceDetailsActivity extends BaseActivity {
    // Firebase
    DatabaseReference mDatabase;
    FirebaseUser firebaseUser;
    FirebaseStorage firebaseStorage;
    StorageReference placeStorage;

    // Loading Overlay
    RelativeLayout loadingOverlay;
    TextView loadingText;
    ProgressBar imageLoading;

    // Activity UI
    TextView placeName;
    TextView description;
    TextView noPic;
    TextView address;
    TextView squad;
    Button mapBtn;
    Button meetupsBtn;
    RelativeLayout galleryLayout;
    ImageSwitcher gallery;

    boolean burger = false;
    FloatingActionButton fab;
    LinearLayout burgerMenu;
    LinearLayout hostMenu;

    // Variables
    String placeId;
    LocPlace place;
    double latitude;
    double longitude;
    ArrayList<String> images;
    int imagePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);

        // Initialising loading overlay and displaying
        loadingOverlay = (RelativeLayout) this.findViewById(R.id.loading_overlay);
        loadingText = (TextView) this.findViewById(R.id.loading_overlay_text);
        imageLoading = (ProgressBar) findViewById(R.id.placeDetails_imageProgress);
        loadingText.setText("Loading Place...");
        loadingOverlay.setVisibility(View.VISIBLE);

        // Initialising UI Elements
        placeName = (TextView) findViewById(R.id.placeDetails_placeName);
        description = (TextView) findViewById(R.id.placeDetails_description);
        noPic = (TextView) findViewById(R.id.noPic);
        address = (TextView) findViewById(R.id.placeDetails_address);
        squad = (TextView) findViewById(R.id.placeDetails_squadName);
        fab = (FloatingActionButton) findViewById(R.id.placeDetails_fab);
        burgerMenu = (LinearLayout) findViewById(R.id.placeDetails_burgerMenu);
        hostMenu = (LinearLayout) findViewById(R.id.placeDetails_hostMenu);
        galleryLayout = (RelativeLayout) findViewById(R.id.placeDetails_galleryLayout);
        mapBtn = (Button) findViewById(R.id.mapButton);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMapLocation();
            }
        });
        meetupsBtn = (Button) findViewById(R.id.meetupsButton);
        meetupsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMeetups();
            }
        });

        gallery = (ImageSwitcher) findViewById(R.id.placeDetails_gallery);
        gallery.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                return new ImageView(PlaceDetailsActivity.this);
            }
        });

        //if there are no pictures
        boolean noPics = true; //TEMPORARY TILL WE CAN ATTEMPT AT LOADING PICS
        if (noPics) {
            //keeps the noPic text, and changes the height of the layout so it's not too big
            gallery.setVisibility(View.GONE);
        } else {
            //gets rid of the noPic text
            noPic.setVisibility(View.GONE);
            galleryLayout.setVisibility(View.VISIBLE);
            gallery.setVisibility(View.VISIBLE);
        }

        // Getting the current user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Gets extras passd from last activity
        Intent detail = getIntent();
        Bundle b = detail.getExtras();
        if (b != null) {
            placeId = (String) b.get("placeId");
            this.setTitle("Place Details");
        } else {
            new AlertDialog.Builder(PlaceDetailsActivity.this)
                    .setTitle("Error")
                    .setMessage("The place went missing somewhere, please try again")
                    .setPositiveButton("Back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        // Getting the reference for the Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Getting the reference for the Firebase Storage
        firebaseStorage = FirebaseStorage.getInstance();

        images = new ArrayList<String>();
        imagePosition = 0;

        // Starts the loading chain
        // loadMeetup -> loadSquad -> loadPictures
        loadPlace();
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_place_details;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.menu_places;
    }

    /**
     * Uses the placeId to create a Place object and display its details.
     */
    public void loadPlace() {
        // Reads the data from the placeId in Firebase
        mDatabase.child("places").child(placeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Gets the data from Firebase and stores it in a Place class
                place = dataSnapshot.getValue(LocPlace.class);

                // Displays the found place's attributes
                placeName.setText(place.getName());
                description.setText(place.getDescription());
                address.setText(place.fullAddress());
                longitude = place.getLocLong();
                latitude = place.getLocLat();

                // If signed-in user is the Host of the Meetup
                if (firebaseUser.getUid().equals(place.getHost())) {
                    // Display editing controls
                    editMode();
                }

                // Load the name of the Squad
                loadSquad();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Uses the squadId of the Meetup to load the name of the Squad it belongs to.
     */
    public void loadSquad() {
        // Setting the loading text
        loadingText.setText("Getting the Place's Squad...");

        // Get Squad name from id
        mDatabase.child("squads").child(place.getSquad()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Displays the Squad's name
                squad.setText(dataSnapshot.child("name").getValue(String.class));

                loadPictures();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Method to download and display the Place's pictures.
     */
    public void loadPictures() {
        HashMap<String, String> pictures = place.getPictures();

        if (pictures != null) {
            noPic.setVisibility(View.GONE);
            galleryLayout.setVisibility(View.VISIBLE);
            gallery.setVisibility(View.VISIBLE);

            for (final String pictureUrl : pictures.values()) {
                images.add(pictureUrl);

            }

            displayImage(images.get(0));


            // Hiding loading overlay
            loadingOverlay.setVisibility(View.GONE);
        } else {
            // No pictures
            // Hiding loading overlay
            loadingOverlay.setVisibility(View.GONE);
        }
    }

    public void displayImage(String pictureUrl) {
        imageLoading.setVisibility(View.VISIBLE);

        // Download and display using Glide
        Glide.with(PlaceDetailsActivity.this)
                .load(pictureUrl)
                .asBitmap()
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        Toast.makeText(PlaceDetailsActivity.this, "Something went wrong loading the image", Toast.LENGTH_SHORT);
                        imageLoading.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        gallery.setImageDrawable(new BitmapDrawable(getResources(), resource));
                        imageLoading.setVisibility(View.GONE);
                        return true;
                    }
                })
                .into((ImageView) gallery.getCurrentView());
    }

    public void nextImage(View view) {
        imagePosition++;
        if (imagePosition == images.size()) {
            imagePosition = 0;
        }
        displayImage(images.get(imagePosition));
    }

    public void previousImage(View view) {
        imagePosition--;
        if (imagePosition == -1) {
            imagePosition = images.size() - 1;
        }
        displayImage(images.get(imagePosition));
    }


    /**
     * Method called on result of the ImagePicker Intent.
     *
     * @param requestCode The request code that was passed to startActivityForResult().
     * @param resultCode  The result code, RESULT_OK if successful, RESULT_CANCELED if not.
     * @param data        An Intent that carries the data of the result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Getting a bitmap from the Intent
        final Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);

        // The user's id and current time in millis is used to create a unique id
        final String uniqueId = firebaseUser.getUid() + System.currentTimeMillis();

        // Creating the reference for the meetup's Firebase Storage, used to store pictures,
        placeStorage = firebaseStorage.getReference().child("places/" + place.getPlaceId() + "/" +
                uniqueId);

        // If Bitmap exists
        if (bitmap != null) {
            loadingText.setText("Uploading photo...");
            loadingOverlay.setVisibility(View.VISIBLE);

            // Creating a ByteArray from the bitmap
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bytes = baos.toByteArray();

            // Upload to Firebase Storage
            UploadTask uploadTask = placeStorage.putBytes(bytes);

            // Upload Listener
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // If upload failed
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(PlaceDetailsActivity.this, "Upload failed, please try again!"
                            , Toast.LENGTH_SHORT);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // If upload successful
                    // Push the download URL to the Place's pictures
                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    mDatabase.child("places").child(placeId).child("pictures").child(uniqueId).setValue(downloadUrl.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // Notify user
                            loadingOverlay.setVisibility(View.GONE);
                            Toast.makeText(PlaceDetailsActivity.this, "Photo uploaded!", Toast.LENGTH_SHORT);

                            if (images.isEmpty()) {
                                gallery.setVisibility(View.VISIBLE);
                                galleryLayout.setVisibility(View.VISIBLE);
                                noPic.setVisibility(View.GONE);

                                images.add(downloadUrl.toString());
                                displayImage(downloadUrl.toString());
                            } else {
                                images.add(downloadUrl.toString());
                            }
                        }
                    });
                }
            });
        }
    }

    /**
     * Method that displays an ImagePicker intent.
     *
     * @param view The ImageView holding the Meetup's picture.
     */
    public void selectImage(View view) {
        // Click on image button
        ImagePicker.pickImage(this, "Select your image:");

        fab(view);
    }

    /**
     * Method to display the Place on the PlaceMapsActivity.
     */
    private void openMapLocation() {
        Intent newDetail = new Intent(PlaceDetailsActivity.this, PlaceMapsActivity.class);
        newDetail.putExtra("latitude", latitude);
        newDetail.putExtra("longitude", longitude);
        newDetail.putExtra("placeName", placeName.getText().toString());
        newDetail.putExtra("placeDescription", description.getText().toString());
        startActivity(newDetail);
    }

    /**
     * Method to load the SquadDetailActivity of the selected squad.
     *
     * @param view The TextEdit holding the Squad's name.
     */
    public void viewSquad(View view) {
        //Sends the id to the details activity
        Intent detail = new Intent(PlaceDetailsActivity.this, SquadDetailActivity.class);
        detail.putExtra("squadId", place.getSquad());
        startActivity(detail);
    }

    /**
     * Method to view the Place's Meetups.
     */
    public void viewMeetups() {
        // Loads the MeetupsList activity displaying the Meetups that are at this place
        Intent intent = new Intent(this, MeetupsListActivity.class);
        intent.putExtra("placeId", placeId);
        startActivity(intent);
    }

    /**
     * Method that displays editing controls for the Place if the signed-in User is the Host.
     */
    public void editMode() {
        hostMenu.setVisibility(View.VISIBLE);
    }

    public void fab(View view) {
        if (burger == false) {
            burger = true;
            burgerMenu.setVisibility(View.VISIBLE);
            fab.setImageResource(R.drawable.ic_cross);
        } else {
            burger = false;
            burgerMenu.setVisibility(View.GONE);
            fab.setImageResource(R.drawable.ic_burger);
        }
    }

    /**
     * Method that displays a dialog for the User to enter a new name.
     *
     * @param view The button that was pressed.
     */
    public void editName(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Name:");

        // EditText for input
        final EditText editTextName = new EditText(this);
        // Sets the expected input types, text, long message, auto correct and multi line
        editTextName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE |
                InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
        // Sets the maximum characters to 50
        editTextName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50)});
        builder.setView(editTextName);

        // Buttons on the Dialog
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String newName = editTextName.getText().toString();
                placeName.setText(newName);
                updateName(newName);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        // Displays the Dialog
        builder.show();
    }

    /**
     * Method to update the Places's name in the Firebase Database.
     *
     * @param newName The new name entered in the previous dialog.
     */
    public void updateName(String newName)
    {
        // Closing the burgerMenu
        fab(fab);

        if (place.getPlaceId() != null) {
            loadingText.setText("Updating the description");
            loadingOverlay.setVisibility(View.VISIBLE);

            // Pushing the new description to the Firebase Database
            mDatabase.child("places").child(place.getPlaceId()).child("name").setValue(newName).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(PlaceDetailsActivity.this, "Name updated!", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            // No user is signed in
            Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method that displays a dialog for the User to enter a new description.
     *
     * @param view The button that was pressed.
     */
    public void editDescription(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Description:");

        // EditText for input
        final EditText editTextDesc = new EditText(this);
        // Sets the expected input types, text, long message, auto correct and multi line
        editTextDesc.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE |
                InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
        // Sets the maximum characters to 120
        editTextDesc.setFilters(new InputFilter[]{new InputFilter.LengthFilter(120)});
        builder.setView(editTextDesc);

        // Buttons on the Dialog
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String newDesc = editTextDesc.getText().toString();
                description.setText(newDesc);
                updateDescription(newDesc);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        // Displays the Dialog
        builder.show();
    }


    /**
     * Method to update the Places's description in the Firebase Database.
     *
     * @param newDesc The new description entered in the previous dialog.
     */
    public void updateDescription(String newDesc)
    {
        // Closing the burgerMenu
        fab(fab);

        if (place.getPlaceId() != null)
        {
            loadingText.setText("Updating the description");
            loadingOverlay.setVisibility(View.VISIBLE);

            // Pushing the new description to the Firebase Database
            mDatabase.child("places").child(place.getPlaceId()).child("description").setValue(newDesc).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(PlaceDetailsActivity.this, "Description updated!", Toast.LENGTH_SHORT).show();
                }
            });

        } else
        {
            // No user is signed in
            Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
