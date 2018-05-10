package biz.cstevens.oddsgame;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.util.ExtraConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import biz.cstevens.oddsgame.Util.DownloadImageTask;


public class Container extends AppCompatActivity {
    private DrawerLayout menuDrawer;
    private ImageView userImage;
    private TextView userName;
    private TextView userEmail;

    private Toolbar toolbar;
    private NavigationView navView;

    public static Intent createIntent(Context context, IdpResponse idpResponse) {
        return new Intent().setClass(context, Container.class)
                .putExtra(ExtraConstants.IDP_RESPONSE, idpResponse);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) savedInstanceState = new Bundle();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { // If there's no logged in user, show the login screen and don't continue loading.
            startActivity(SignIn.createIntent(this));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Setup the toolbar.
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

        // Setup the menu drawer.
        menuDrawer = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.navigation_view);
        navView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        selectDrawerItem(item);
                        return true;
                    }
                }
        );
        View header = navView.getHeaderView(0);
        userImage = header.findViewById(R.id.header_image);
        userName = header.findViewById(R.id.header_name);
        userEmail = header.findViewById(R.id.header_email);
        new DownloadImageTask(userImage).execute(user.getPhotoUrl().toString());
        userName.setText(user.getDisplayName());
        userEmail.setText(user.getEmail());

        Bundle extras = getIntent().getExtras();
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (extras != null && extras.get("type") != null && extras.getString("type").equals("new_odds")) { // If there's data from FCM...
            Fragment fragment = InGameFragment.newInstance(extras.getString("oddsId"), false);

            savedInstanceState.putInt("fragment", Main.IN_GAME_FRAGMENT);
            savedInstanceState.putString("oddsId", extras.getString("oddsId"));
            savedInstanceState.putBoolean("isCreator", false);

            // Switch over the fragments.
            fragmentManager.beginTransaction().replace(R.id.frag_content, fragment).addToBackStack("").commit();

        } else if (savedInstanceState.get("fragment") != null) {
            savedInstanceState.putInt("fragment", Main.NEW_GAME_FRAGMENT);
            resumeFragment(savedInstanceState.getInt("fragment"), savedInstanceState);

        } else {
            navView.getMenu().getItem(0).setChecked(true);
            fragmentManager.beginTransaction().replace(R.id.frag_content, NewGameFragment.newInstance()).commit();

        }
    }

    private void resumeFragment(int fragmentId, Bundle savedInstanceState) {
        Fragment fragment = null;
        switch (fragmentId) {
            case Main.NEW_GAME_FRAGMENT:
                navView.getMenu().getItem(0).setChecked(true);
                fragment = NewGameFragment.newInstance();
                break;

            case Main.IN_GAME_FRAGMENT:
                fragment = InGameFragment.newInstance(
                        savedInstanceState.getString("oddsId"),
                        savedInstanceState.getBoolean("isCreator")
                );
                break;

            case Main.GAME_REQUEST_FRAGMENT:
                navView.getMenu().getItem(1).setChecked(true);
                fragment = GameRequestFragment.newInstance();
                break;

            case Main.GAME_HISTORY_FRAGMENT:
                navView.getMenu().getItem(2).setChecked(true);
                fragment = GameHistoryFragment.newInstance();
                break;

            case Main.USER_GUIDE_FRAGMENT:
                navView.getMenu().getItem(4).setChecked(true);
                fragment = UserGuideFragment.newInstance();
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frag_content, fragment).commit();

    }

    public void selectDrawerItem(MenuItem item) {
        Fragment fragment = null;
        Class fragmentClass;

        // Get the relevant class for the selected page.
        switch(item.getItemId()) {
            case R.id.menu_new_game:
                fragmentClass = NewGameFragment.class;
                break;
            case R.id.menu_requests:
                fragmentClass = GameRequestFragment.class;
                break;
            case R.id.menu_history:
                fragmentClass = GameHistoryFragment.class;
                break;
            case R.id.menu_logout: // If logging out...
                FirebaseAuth.getInstance().signOut(); // Sign out.
                startActivity(SignIn.createIntent(this)); // Start the sign-in activity.
                return;
            case R.id.menu_user_guide:
                fragmentClass = UserGuideFragment.class;
                break;
            default:
                fragmentClass = NewGameFragment.class;
        }

        try {
            fragment = (Fragment)fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Switch over the fragments.
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frag_content, fragment).commit();

        item.setChecked(true); // Select item to keep highlighting
        setTitle(item.getTitle());
        menuDrawer.closeDrawers(); // Close drawer after item is selected
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            menuDrawer.openDrawer(GravityCompat.START);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
