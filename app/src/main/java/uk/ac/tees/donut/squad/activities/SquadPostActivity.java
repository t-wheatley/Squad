package uk.ac.tees.donut.squad.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import uk.ac.tees.donut.squad.R;

public class SquadPostActivity extends AppCompatActivity {

    private Button btnPost;
    private MultiAutoCompleteTextView txtbox;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squad_post);

        txtbox = (MultiAutoCompleteTextView) findViewById(R.id.txtboxPost);

    }
}
