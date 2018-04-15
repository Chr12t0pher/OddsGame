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

import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.util.ExtraConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Container extends AppCompatActivity {
    private DrawerLayout menuDrawer;
    private Toolbar toolbar;
    private NavigationView navView;

    public static Intent createIntent(Context context, IdpResponse idpResponse) {
        return new Intent().setClass(context, Container.class)
                .putExtra(ExtraConstants.IDP_RESPONSE, idpResponse);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                // todo
            case R.id.menu_history:
                // todo
            case R.id.menu_settings:
                // todo
            case R.id.menu_user_guide:
                // todo
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
