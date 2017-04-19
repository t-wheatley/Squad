package uk.ac.tees.donut.squad.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import uk.ac.tees.donut.squad.R;

public class SquadsActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squads2);
    }

    public void buttonOnClick(View v){
        ImageButton test = (ImageButton)findViewById(R.id.videoGamesBtn);
        TextView test2 =(TextView)findViewById(R.id.textviewAS);
        test2.setText(String.valueOf(test.getMeasuredWidth()));
    }
}
