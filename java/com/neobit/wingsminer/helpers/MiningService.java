package com.neobit.wingsminer.helpers;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.neobit.wingsminer.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MiningService extends Service {

    private MiningTask mMiningTask = null;
    private String URL;
    public MiningService(Context applicationContext) {
        super();
    }
    PowerManager.WakeLock screenWakeLock = null;

    public MiningService() {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Log.i("start", "Mining service is up and running.");
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, getString(R.string.channel_name))
                    .setSmallIcon(R.drawable.noticon)
                    .setContentText(getString(R.string.persistent_notification))
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            Notification note = mBuilder.build();
            note.flags |= Notification.FLAG_ONGOING_EVENT;
            //notificationManager.notify(324, note);
            startForeground(1348, note);

            SharedPreferences settings = getApplicationContext().getSharedPreferences("MisPreferencias", getApplicationContext().MODE_PRIVATE);
            JSONObject usuario = new JSONObject(settings.getString("jsonUsuario", ""));
            int periodicity = Integer.parseInt(usuario.getJSONObject("plan").getString("blocks"));
            periodicity = 3600 * 24 / periodicity;
            /*OneTimeWorkRequest compressionWork =
                    new OneTimeWorkRequest.Builder(MiningWorker.class)
                            .setInitialDelay(periodicity, TimeUnit.SECONDS)
                            .addTag(getString(R.string.channel_name))
                            .build();
            WorkManager.getInstance().enqueue(compressionWork);*/

            ScheduledExecutorService ses = Executors.newScheduledThreadPool(1000);
            ses.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        SharedPreferences settings = getApplicationContext().getSharedPreferences("MisPreferencias", getApplicationContext().MODE_MULTI_PROCESS);
                        if (settings.getInt("3g", 0) == 0 && NetworkUtils.isWifiConnected(getApplicationContext()) ||
                                settings.getInt("3g", 0) == 1 && NetworkUtils.isConnected(getApplicationContext())) {
                            int total = settings.getInt("total", 0) + 1;
                            JSONObject usuario = new JSONObject(settings.getString("jsonUsuario", ""));
                            String api_key = usuario.getString("api_key");
                            URL = getApplicationContext().getString(R.string.url_blocks);
                            mMiningTask = new MiningTask(URL, total, NetworkUtils.isWifiConnected(getApplicationContext()) ? "W" : "D", api_key);
                            mMiningTask.execute();
                            if (screenWakeLock == null) {
                                PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
                                screenWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                                        "wings:mining");
                            }
                            if (total % Integer.parseInt(usuario.getJSONObject("plan").getString("restart")) == 0) screenWakeLock.acquire();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 0, periodicity, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent(this, MiningBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class MiningTask extends AsyncTask<Void, Void, JSONObject> {

        private final String mURL;
        private final int mTotal;
        private final String mTipo;
        private final String mMApiKey;

        MiningTask(String URL, int total, String tipo, String api_key) {
            mURL = URL;
            mTotal = total;
            mTipo = tipo;
            mMApiKey = api_key;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject jsonOb = new JSONObject();
            try {
                HashMap<String, String> meMap = new HashMap<String, String>();
                meMap.put("total", String.valueOf(mTotal));
                meMap.put("tipo", mTipo);
                JSONParser jParser = new JSONParser();
                jsonOb = jParser.getJSONPOSTAuthFromUrl(mURL, meMap, mMApiKey);
            } catch(Exception e) {
                e.printStackTrace();
            }
            return jsonOb;
        }

        @Override
        protected void onPostExecute(final JSONObject response) {
            mMiningTask = null;
            try {
                SharedPreferences settings = getApplicationContext().getSharedPreferences("MisPreferencias", getApplicationContext().MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                switch (response.getString("response_code")) {
                    case "200":
                        editor.putInt("total", mTotal);
                        break;
                    case "201":
                        editor.putInt("total", 0);
                        editor.putString("total_eth", response.getString("total_eth"));
                        break;
                }
                editor.commit();
                if (screenWakeLock != null && screenWakeLock.isHeld()) screenWakeLock.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            mMiningTask = null;
        }
    }
}
