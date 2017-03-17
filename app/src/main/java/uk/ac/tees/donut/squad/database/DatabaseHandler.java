package uk.ac.tees.donut.squad.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import uk.ac.tees.donut.squad.posts.Meetup;

/**
 * Created by q5273202 on 07/02/2017.
 */
public class DatabaseHandler extends SQLiteOpenHelper
{
    //Database name
    private static final String DATABASE_NAME = "squad.db";
    //Contacts table name
    private static final String TABLE_NAME = "meetups";
    //Contacts table columns names
    private static final String COL_ID = "_id"; //primary key column
    private static final String COL_NAME = "name";
    private static final String COL_INTEREST = "interest";
    private static final String COL_DESCRIPTION = "description";
    //columns for temporary location variables
    private static final String COL_ADDRESS = "address";
    private static final String COL_POSTCODE = "postCode";

    public DatabaseHandler(Context context)
    {
        super(context, DATABASE_NAME, null, 1);

        Log.d("Database", "Handler Created.");
    }


    @Override
    public void onCreate(SQLiteDatabase db)
    {
        //generate Create SQL statement
        String CREATE_CONTACTS_TABLE = "CREATE TABLE "
                + TABLE_NAME
                + "(" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_NAME + " TEXT,"
                + COL_INTEREST + " TEXT,"
                + COL_DESCRIPTION + " TEXT,"
                //temporary location columns
                + COL_ADDRESS + " TEXT,"
                + COL_POSTCODE + " TEXT"
                + ")";

        //execute/run create SQL statement
        db.execSQL(CREATE_CONTACTS_TABLE);

        Log.d("Database", "Database Created.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //Drop older table if exists and create fresh (deletes all data)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    //Data Handling
    //adding
    public long addEvent(Meetup m)
    {
        //open database connection (for write)
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d("Database", "Opened connection to database (for writing).");

        ContentValues values = new ContentValues();
        values.put(COL_NAME, m.getName());
       // values.put(COL_INTEREST, m.getInterest());
        values.put(COL_DESCRIPTION, m.getDescription());
        //temporary location values
        values.put(COL_ADDRESS, m.getAddress());
        values.put(COL_POSTCODE, m.getPostCode());
        //id gets autoincremented

        //Add record to database and get id of new record (must long integer)
        long id = db.insert(TABLE_NAME, null, values);

        Log.d("Database", "Inserted: " + m.getName() + ", " + m.getInterest());

        db.close();
        Log.d("Database", "Connection closed.");

        return id;
    }

    //getting
    public List<Meetup> getAll()
    {
        //create empty list (in memory)
        List<Meetup> list = new ArrayList<>();
        //connect to the database to read data
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d("Database", "Opened connection to database (for reading).");

        //Generate SQL SELECT statement
        String selectQuery = "SELECT * FROM " + TABLE_NAME;

        //execute SELECT statement
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst())
        {
            //get position (index) of each of the column names
            int idIdx = cursor.getColumnIndex(COL_ID);
            int nameIdx = cursor.getColumnIndex(COL_NAME);
            int interestIdx = cursor.getColumnIndex(COL_INTEREST);

            do {
                //create Lecturer object for current database record

                String nid= cursor.getString(idIdx);
                String nname = cursor.getString(nameIdx);
                String ninterest = cursor.getString(interestIdx);



                Meetup meetup = new Meetup(

        //                cursor.getInt(idIdx),
        //                cursor.getString(nameIdx),
          //              cursor.getString(interestIdx)

                        cursor.getString(idIdx),
                        cursor.getString(nameIdx),
                        cursor.getString(interestIdx)

                );
                //add lecturer to the list
                list.add(meetup);
            } while (cursor.moveToNext());
        }

        return list;
    }


}
