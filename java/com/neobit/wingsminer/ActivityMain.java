package com.neobit.wingsminer;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.neobit.wingsminer.helpers.MiningService;
import com.neobit.wingsminer.helpers.NetworkUtils;

import org.json.JSONObject;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.work.WorkManager;

public class ActivityMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Intent mServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(getString(R.string.channel_name), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

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
            SharedPreferences settings = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
            JSONObject usuario = new JSONObject(settings.getString("jsonUsuario", ""));
            TextView textName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textName);
            textName.setText(usuario.getString("name"));
            TextView textPlan = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textPlan);
            textPlan.setText(usuario.getJSONObject("plan").getString("name"));
            TextView textVelocidad = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textVelocidad);
            textVelocidad.setText(usuario.getJSONObject("plan").getString("megahash"));

            MiningService mSensorService = new MiningService(this);
            mServiceIntent = new Intent(this, mSensorService.getClass());
            if (!isMyServiceRunning(mSensorService.getClass())) {
                WorkManager.getInstance().cancelAllWorkByTag("mining");
                startService(mServiceIntent);
            }
            /*int periodicity = Integer.parseInt(usuario.getJSONObject("plan").getString("blocks"));
            periodicity = 3600 * 24 / periodicity;
            OneTimeWorkRequest compressionWork =
                    new OneTimeWorkRequest.Builder(MiningWorker.class)
                            .setInitialDelay(periodicity, TimeUnit.SECONDS)
                            .addTag(getString(R.string.channel_name))
                            .build();
            WorkManager.getInstance().enqueue(compressionWork);*/
            /*NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, getString(R.string.channel_name))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText(getString(R.string.persistent_notification))
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            Notification note = mBuilder.build();
            note.flags |= Notification.FLAG_ONGOING_EVENT;
            notificationManager.notify(324, note);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onDestroy() {
        WorkManager.getInstance().cancelAllWorkByTag("mining");
        stopService(mServiceIntent);
        super.onDestroy();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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
            displayView(3);
        /*} else if (id == R.id.nav_plans) {
            displayView(3);*/
        } else if (id == R.id.nav_history) {
            displayView(4);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

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
                if (!NetworkUtils.isConnected(ActivityMain.this)) {
                    Toast.makeText(ActivityMain.this, R.string.no_conexion, Toast.LENGTH_LONG).show();
                } else fragment = new FragmentPlans();
                invalidateOptionsMenu();
                break;
            case 4:
                if (!NetworkUtils.isConnected(ActivityMain.this)) {
                    Toast.makeText(ActivityMain.this, R.string.no_conexion, Toast.LENGTH_LONG).show();
                } else fragment = new FragmentHistory();
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
                                ActivityMain.this.finish();
                                //android.os.Process.killProcess(android.os.Process.myPid());
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
