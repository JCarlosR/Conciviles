package com.programacionymas.conciviles.io.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.programacionymas.conciviles.Global;
import com.programacionymas.conciviles.io.MyApiAdapter;
import com.programacionymas.conciviles.io.sqlite.MyDbContract;
import com.programacionymas.conciviles.io.sqlite.MyDbHelper;
import com.programacionymas.conciviles.model.Inform;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyService extends Service {
    public Runnable mRunnable = null;
    public MyService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static int tries = 0;

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        final Handler mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (intent == null) return; // some strange cases

                MyDbHelper myHelper = new MyDbHelper(getApplicationContext());
                boolean isInfoAvailable = myHelper.isAnyInfoAvailable(MyDbContract.InformEntry.TABLE_NAME);

                // Toast.makeText(getApplicationContext(), String.valueOf(isInfoAvailable), Toast.LENGTH_LONG).show();

                if (!isInfoAvailable || hasPassedAtLeast2Mins())
                    downloadInforms(intent.getIntExtra("user_id", 0));

                mHandler.postDelayed(mRunnable, 20 * 1000); // next delays are 20 seconds
            }
        };
        mHandler.postDelayed(mRunnable, 10 * 1000); // first time with 10 seconds delay

        return super.onStartCommand(intent, flags, startId);
    }

    private boolean hasPassedAtLeast2Mins() {
        long currentTime = new Date().getTime();
        // Last time saved in shared preference
        final long lastTime = Global.getLongFromPreferences(getApplicationContext(), "lastTime");

        if (lastTime == 0) {
            return true; // perform the first download
        } else {
            // Difference in seconds
            double diff = TimeUnit.MILLISECONDS.toSeconds(currentTime - lastTime);

            return diff >= 2 * 60; // has passed 2 minutes or more
        }
    }

    private void downloadInforms(final int user_id) {
        if (user_id == 0) return;

        Log.d("MyService", "Going to download informs");

        Call<ArrayList<Inform>> call = MyApiAdapter.getApiService().getInformsByLocationOfUser(user_id);
        call.enqueue(new Callback<ArrayList<Inform>>() {
            @Override
            public void onResponse(Call<ArrayList<Inform>> call, Response<ArrayList<Inform>> response) {
                if (response.isSuccessful()) {
                    Log.d("MyService", "HTTP Request performed");
                    ArrayList<Inform> informs = response.body();
                    MyDbHelper myHelper = new MyDbHelper(getApplicationContext());
                    myHelper.updateInformsTable(informs);
                    notifyInformsUiTobeUpdated();

                    // Save the time for the last download
                    long currentTime = new Date().getTime();
                    Global.saveLongPreference(getApplicationContext(), "lastTime", currentTime);

                    // and stop the service
                    stopSelf();
                } else {
                    // try 3 times
                    ++tries;
                    if (tries == 3)
                        stopSelf();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Inform>> call, Throwable t) {
                Toast.makeText(MyService.this, "No se ha podido actualizar la lista de informes.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void notifyInformsUiTobeUpdated() {
        Intent intent = new Intent("event-update-informs");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d("MyService", "Broadcast to update informs sent");
    }
}
