package com.neobit.wingsminer.helpers;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.neobit.wingsminer.R;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class MiningService extends Service {

    public MiningService(Context applicationContext) {
        super();
    }

    public MiningService() {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
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

            Log.i("start", "Mining service is up and running.");
            SharedPreferences settings = getApplicationContext().getSharedPreferences("MisPreferencias", getApplicationContext().MODE_PRIVATE);
            JSONObject usuario = new JSONObject(settings.getString("jsonUsuario", ""));
            int periodicity = Integer.parseInt(usuario.getJSONObject("plan").getString("blocks"));
            periodicity = 3600 * 24 / periodicity;
            OneTimeWorkRequest compressionWork =
                    new OneTimeWorkRequest.Builder(MiningWorker.class)
                            .setInitialDelay(periodicity, TimeUnit.SECONDS)
                            .addTag(getString(R.string.channel_name))
                            .build();
            WorkManager.getInstance().enqueue(compressionWork);
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
}
