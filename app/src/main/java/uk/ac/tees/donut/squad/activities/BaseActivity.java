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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch(item.getItemId()){
            case R.id.menu_squads:
                intent = new Intent(this,SquadListActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_meetups:
                System.out.println("----------------------Booting up meetups");
                intent = new Intent(this,MeetupsListActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_places:
                System.out.println("-----------------------Booting up places");
                intent = new Intent(this, PlacesListActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_map:
                System.out.println("--------------------Booting up map");
                intent = new Intent(this, MapActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_profile:
                if (firebaseUser != null) {
                    //Sends the user's id to the profile activity
                    System.out.println("---------------------------------Booting up profile");
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

    abstract int getContentViewId();

    abstract int getNavigationMenuItemId();

}
