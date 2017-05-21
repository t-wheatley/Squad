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

/**
 * This class acts as a manager activty that is created to handle the BottomNavagationView used
 * through the majority of the app. If an activity uses the menu it needs to extend this class
 * and implement the two abstract methods which this manager requires.
 */
public abstract class BaseActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener
{
    // No modifier as they're used within the package and class
    BottomNavigationView navigationView;
    FirebaseUser firebaseUser;

    /**
     * onCreate packages the changes in UI (setContentView()) and stores other variables used within the package
     * firebaseUser is accessed throughout the program when user details are displayed or manipulated.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        navigationView = (BottomNavigationView) findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(this);
    }

    /**
     * onStart from FragmentActivity overrides AppCompatActivity onStart to ensure the
     * navigation bar changes without it being visible to the user
     */
    @Override
    protected void onStart()
    {
        super.onStart();
        updateNavigationBarState();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    /**
     * This method checks which menu item is clicked and outputs it onto the screen.
     * @param item the item which has been selected by the user.
     * @return the new xml view using startActivity() and returns true to show it definitely has
     *         changed.
     *         Note: If a new menu item is added it needs to be added below and as a /menu/navigation item
     *         The new java class must also extend BaseActivity and implement the abstract methods.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        Intent intent;
        switch (item.getItemId())
        {
            case R.id.menu_squads:
                intent = new Intent(this, SquadListActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_meetups:
                intent = new Intent(this, MeetupsListActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_places:
                intent = new Intent(this, PlacesListActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_map:
                intent = new Intent(this, MapActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_profile:
                if (firebaseUser != null)
                {
                    //Sends the user's id to the profile activity
                    intent = new Intent(this, ProfileActivity.class);
                    intent.putExtra("uId", firebaseUser.getUid());
                    startActivity(intent);
                }
                break;
        }
        return true;
    }

    /**
     * This method packages the methods included to change the Nav bar.
     * selectBottomNavigationItem(int itemId)
     * getNavigationMenuItemId()
     */
    private void updateNavigationBarState()
    {
        int actionId = getNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
    }

    /**
     * This method is used to check the relevant menu item as declared in the
     * getNavigationMenuItemId method. Loops through each menuitem and determines
     * if the item should be highlighted.
     * @param itemId
     */
    private void selectBottomNavigationBarItem(int itemId)
    {
        Menu menu = navigationView.getMenu();
        for (int i = 0, size = menu.size(); i < size; i++)
        {
            MenuItem item = menu.getItem(i);
            boolean shouldBeChecked = item.getItemId() == itemId;
            if (shouldBeChecked)
            {
                item.setChecked(true);
                break;
            }
        }
    }

    /**
     * This method retrieves the id values associated with the required layout.
     * @return the int layout value ID which is linked to the xml of the required class
     */
    abstract int getContentViewId();

    /**
     * This method retrieves the id value needed to change the highlighted menu icon.
     * @return  the final int value of the icon which is highlighted according to the
     *          available menu items within the onNavigationItemSelected method.
     *          Note: An item must be highlighted.
     */
    abstract int getNavigationMenuItemId();

}
