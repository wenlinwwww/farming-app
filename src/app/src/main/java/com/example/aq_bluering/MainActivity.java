package com.example.aq_bluering;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;


import com.example.aq_bluering.Connection.ApiConnection;
import com.example.aq_bluering.ui.Dashboard.Dashboard;
import com.example.aq_bluering.ui.Profile.Profile;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aq_bluering.databinding.ActivityMainBinding;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private UsernameShare usernameShare;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    public String userName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Retrieve the username passed from the LoginActivity
        userName = getIntent().getStringExtra("USERNAME");
        // Initialize ViewModel
        usernameShare = new ViewModelProvider(this).get(UsernameShare.class);
        usernameShare.setUsername(userName);
        
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.mainContent.header.toolbar);
        TextView menu_username=(TextView) binding.navView.getHeaderView(0).findViewById(R.id.menu_username);
        menu_username.setText(userName);
        ImageButton profileButton = (ImageButton) binding.navView.getHeaderView(0).findViewById(R.id.open_profile);
        profileButton.setOnClickListener(view -> {
            Profile profile = new Profile(userName);
            profile.show(getSupportFragmentManager(), "profile");
        });


        //set main application navigation
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.dashboard, R.id.farm_Field, R.id.gateway, R.id.sensor, R.id.irrigation)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        NavigationUI.setupWithNavController(navigationView, navController);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notification, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}