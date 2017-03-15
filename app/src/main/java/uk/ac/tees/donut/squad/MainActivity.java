package uk.ac.tees.donut.squad;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity
{
    Button btnNewMeetup;
    Button btnViewMeetups;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnNewMeetup = (Button) findViewById(R.id.main_buttonNewMeetup);
        btnViewMeetups = (Button) findViewById(R.id.main_buttonViewMeetups);

        btnNewMeetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NewMeetup.class));
            }
        });

        btnViewMeetups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ViewMeetups.class));
            }
        });
    }
}
