package uk.ac.tees.donut.squad.debug;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import uk.ac.tees.donut.squad.squads.Interest;

/**
 * Created by q5071488 on 21/04/2017.
 */

public class InterestDebug {

    public static void addInterest(String name)
    {
        // Getting the reference for the Firebase Realtime Database
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creating a new interest node and getting the key value
        String interestId = mDatabase.child("interests").push().getKey();

        // Creating a interest object
        Interest interest = new Interest(interestId, name);

        // Pushing the interest to the "interests" node using the interestId
        mDatabase.child("interests").child(interestId).setValue(interest);

    }
}
