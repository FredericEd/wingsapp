package com.neobit.wingsminer.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.legacy.content.WakefulBroadcastReceiver;

public class MiningBroadcastReceiver extends WakefulBroadcastReceiver {

    PowerManager.WakeLock screenWakeLock = null;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i("started", "Mining broadcast received a call.");
        /*PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "WingsMiner::KeepWorkRunning");
        wakeLock.acquire();*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //startWakefulService(context, new Intent(context, MiningService.class));
            context.startForegroundService(new Intent(context, MiningService.class));
        } else {
            context.startService(new Intent(context, MiningService.class));
        }
        /*if (screenWakeLock == null)
        {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            screenWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "wings:mining");
            screenWakeLock.acquire();
        }
        Intent service = new Intent(context, MiningService.class);
        startWakefulService(context, service);
        if (screenWakeLock != null)
            screenWakeLock.release();*/
    }
}
