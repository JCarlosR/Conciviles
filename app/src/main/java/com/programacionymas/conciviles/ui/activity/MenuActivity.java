package com.programacionymas.conciviles.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.programacionymas.conciviles.Global;
import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.io.MyApiAdapter;
import com.programacionymas.conciviles.io.service.CheckForUpdatesService;
import com.programacionymas.conciviles.ui.fragment.InformsFragment;
import com.programacionymas.conciviles.ui.fragment.ProfileFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Global.checkForUpdates(context);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onPause() {
        unregisterReceiver(networkStateReceiver);
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        final int user_id = Global.getIntFromPreferences(this, "user_id");

        // For special exceptions (the user removes manually the shared preferences)
        if (user_id == 0)
            finish();

        // Start service to download the informs if it's possible and needed
        Global.checkForUpdates(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Usa el menú lateral izquierdo ...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Update the FCM token in the server when needed
        updateFcmTokenInServer();
    }

    private void updateFcmTokenInServer() {
        if (!Global.isConnected(this)) {
            return; // stop impossible update action
        }

        final boolean shouldSendToken = Global.getBooleanFromPreferences(this, "should_send_fcm_token");
        if (shouldSendToken) {
            final int user_id = Global.getIntFromPreferences(this, "user_id");
            final String token = Global.getStringFromPreferences(this, "fcm_token");
            Call<Boolean> call = MyApiAdapter.getApiService().updateUserToken(user_id, token);
            call.enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    if (response.isSuccessful()) {
                        Boolean tokenUpdated = response.body();
                        if (tokenUpdated)
                            Global.saveBooleanPreference(getApplicationContext(), "should_send_fcm_token", false);
                    } else {
                        Toast.makeText(MenuActivity.this, R.string.error_retrofit_response, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    Toast.makeText(MenuActivity.this, R.string.error_retrofit_callback, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }/* else {
            // We are ignoring the back button
            super.onBackPressed();
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        // clear preferences
        Global.saveIntPreference(this, "user_id", 0);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.nav_profile) {
            fragment = new ProfileFragment();
        } else if (id == R.id.nav_informs) {
            fragment = new InformsFragment();
        } else if (id == R.id.nav_sync) {
            syncData();
        } else if (id == R.id.nav_stop_sync) {
            stopSyncData();
        }

        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_menu, fragment)
                    .commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void syncData() {
        if (Global.isConnected(this)) {
            Global.showConfirmationDialog(this, "¿Está seguro que desea sincronizar manualmente?", "En caso que no actualice adecuadamente, use la opción Detener actualizaciones, e intente nuevamente.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Start service to download the informs if it's possible and needed
                    Global.checkForUpdates(getApplicationContext());
                }
            });
        } else {
            Toast.makeText(this, "Esta acción requiere de conexión a internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopSyncData() {
        Global.showConfirmationDialog(this, "Confirmar", "¿Está seguro que desea detener la sincronización actual?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Force and stop CheckForUpdates service
                stopService(new Intent(MenuActivity.this, CheckForUpdatesService.class));
            }
        });
    }
}
