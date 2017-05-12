package uk.ac.tees.donut.squad.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import uk.ac.tees.donut.squad.R;

public abstract class BaseActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    protected BottomNavigationView navigationView;
    FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        navigationView = (BottomNavigationView) findViewById(R.id.navigation);
        System.out.println(this);
        navigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom navigation items
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    //Handles navigation between the nav bar
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch(item.getItemId()){
            case R.id.menu_home:
                intent = new Intent(this,MenuActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_squads:
                intent = new Intent(this,SquadListActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_meetups:
                intent = new Intent(this,MeetupsListActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_profile:
                System.out.println("Opening ProfileActivity!");
                if (firebaseUser != null) {
                    //Sends the user's id to the profile activity
                    intent = new Intent(this, ProfileActivity.class);
                    intent.putExtra("uId", firebaseUser.getUid());
                    startActivity(intent);
                }
                break;
        }
        return true;
    }

    private void updateNavigationBarState(){
        int actionId = getNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
    }

    void selectBottomNavigationBarItem(int itemId) {
        Menu menu = navigationView.getMenu();
        for (int i = 0, size = menu.size(); i < size; i++) {
            MenuItem item = menu.getItem(i);
            boolean shouldBeChecked = item.getItemId() == itemId;
            if (shouldBeChecked) {
                item.setChecked(true);
                break;
            }
        }
    }

    //Returns the layout id of the xml required
    abstract int getContentViewId();
    // returns the menu id for which icon to highlight
    abstract int getNavigationMenuItemId();

}
