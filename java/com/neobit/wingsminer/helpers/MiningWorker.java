package com.neobit.wingsminer.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.neobit.wingsminer.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class MiningWorker extends Worker {

    private String URL;
    private MiningTask mMiningTask = null;

    public MiningWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        try {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Log.i("MINING", "Cheking internet connection.");
                        SharedPreferences settings = getApplicationContext().getSharedPreferences("MisPreferencias", getApplicationContext().MODE_PRIVATE);
                        JSONObject usuario = new JSONObject(settings.getString("jsonUsuario", ""));
                        int periodicity = Integer.parseInt(usuario.getJSONObject("plan").getString("blocks"));
                        periodicity = 3600 * 24 / periodicity;
                        OneTimeWorkRequest compressionWork =
                                new OneTimeWorkRequest.Builder(MiningWorker.class)
                                        .setInitialDelay(periodicity, TimeUnit.SECONDS)
                                        .addTag(getApplicationContext().getString(R.string.channel_name))
                                        .build();
                        WorkManager.getInstance().enqueue(compressionWork);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            SharedPreferences settings = getApplicationContext().getSharedPreferences("MisPreferencias", getApplicationContext().MODE_PRIVATE);
            if (settings.getInt("3g", 0) == 0 && NetworkUtils.isWifiConnected(getApplicationContext()) ||
                    settings.getInt("3g", 0) == 1 && NetworkUtils.isConnected(getApplicationContext())) {
                JSONObject usuario = new JSONObject(settings.getString("jsonUsuario", ""));
                String api_key = usuario.getString("api_key");
                URL = getApplicationContext().getString(R.string.url_blocks);
                mMiningTask = new MiningTask(URL, settings.getInt("total", 0) + 1, api_key);
                mMiningTask.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.success();
        // (Returning Result.retry() tells WorkManager to try this task again
        // later; Result.failure() says not to try again.)
    }

    public class MiningTask extends AsyncTask<Void, Void, JSONObject> {

        private final String mURL;
        private final int mTotal;
        private final String mMApiKey;

        MiningTask(String URL, int total, String api_key) {
            mURL = URL;
            mTotal = total;
            mMApiKey = api_key;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject jsonOb = new JSONObject();
            try {
                HashMap<String, String> meMap = new HashMap<String, String>();
                meMap.put("total", String.valueOf(mTotal));
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
                        editor.putString("total_final", response.getString("total_eth"));
                        break;
                }
                editor.commit();
                Log.i("response", response.getString("message"));
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