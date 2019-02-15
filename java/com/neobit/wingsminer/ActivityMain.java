package com.neobit.wingsminer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.neobit.wingsminer.helpers.MiningWorker;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class ActivityMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
        onNavigationItemSelected(navigationView.getMenu().getItem(0));
        try {
            WorkManager.getInstance().cancelAllWorkByTag("mining");

            SharedPreferences settings = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
            JSONObject usuario = new JSONObject(settings.getString("jsonUsuario", ""));
            TextView textName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textName);
            textName.setText(usuario.getString("name"));
            TextView textPlan = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textPlan);
            textPlan.setText(usuario.getJSONObject("plan").getString("name"));
            TextView textVelocidad = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textVelocidad);
            textVelocidad.setText(usuario.getJSONObject("plan").getString("megahash"));

            int periodicity = Integer.parseInt(usuario.getJSONObject("plan").getString("blocks"));
            periodicity = 3600 * 24 / periodicity;
            OneTimeWorkRequest compressionWork =
                    new OneTimeWorkRequest.Builder(MiningWorker.class)
                            .setInitialDelay(periodicity, TimeUnit.SECONDS)
                            .addTag("mining")
                            .build();
            WorkManager.getInstance().enqueue(compressionWork);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            displayView(0);
        } else if (id == R.id.nav_profile) {
            displayView(1);
        } else if (id == R.id.nav_upgrade) {
            displayView(2);
        } else if (id == R.id.nav_plans) {
            displayView(3);
        } else if (id == R.id.nav_history) {
            displayView(4);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Diplaying fragment view for selected nav drawer list item
     */
    private void displayView(int position) {
        Fragment fragment = null;
        Bundle bundl = new Bundle();
        switch (position) {
            case 0:
                fragment = new FragmentHome();
                invalidateOptionsMenu();
                break;
            case 1:
                fragment = new FragmentProfile();
                invalidateOptionsMenu();
                break;
            case 2:
                fragment = new FragmentUpgrade();
                invalidateOptionsMenu();
                break;
            case 3:
                fragment = new FragmentPlans();
                invalidateOptionsMenu();
                break;
            case 4:
                fragment = new FragmentHistory();
                invalidateOptionsMenu();
                break;
            default:
                break;
        }
        if (fragment != null) {
            fragment.setArguments(bundl);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
        } else Log.e("MainActivity", "Error in creating fragment");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Fragment anonymousFragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);
            if (anonymousFragment instanceof FragmentHome) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMain.this);
                builder.setMessage(R.string.salir_prompt)
                        .setCancelable(false)
                        .setPositiveButton(R.string.afirmacion, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                android.os.Process.killProcess(android.os.Process.myPid());
                            }
                        })
                        .setNegativeButton(R.string.negacion, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //NA
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            } else if (anonymousFragment instanceof FragmentUpdate) {
                FragmentUpdate activeFragment = (FragmentUpdate) anonymousFragment;
                activeFragment.backPressed();
            } else {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }
    }
}
