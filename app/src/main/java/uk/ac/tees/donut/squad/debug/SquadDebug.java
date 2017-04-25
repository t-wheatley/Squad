package uk.ac.tees.donut.squad.debug;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import uk.ac.tees.donut.squad.squads.Squad;

/**
 * Created by q5071488 on 21/04/2017.
 */

public class SquadDebug {

    public static void addSquad(String name, String desc)
    {
        // Getting the reference for the Firebase Realtime Database
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        // Creating a new squad node and getting the key value
        String squadId = mDatabase.child("squads").push().getKey();

        // Creating a squad object
        Squad squad = new Squad(squadId, name, desc);

        // Pushing the squad to the "squads" node using the squadId
        mDatabase.child("squads").child(squadId).setValue(squad);

    }
}
