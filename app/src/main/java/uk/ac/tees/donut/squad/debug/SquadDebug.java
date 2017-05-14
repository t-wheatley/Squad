package uk.ac.tees.donut.squad.debug;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import uk.ac.tees.donut.squad.squads.Squad;

/**
 * Class that contains debug methods for Squads.
 */
public class SquadDebug
{

    /**
     * Method that creates a new Squad.
     *
     * @param name Name of the new Squad.
     * @param desc Description of the new Squad.
     */
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
