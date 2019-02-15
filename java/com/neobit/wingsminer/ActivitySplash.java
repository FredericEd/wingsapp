package com.neobit.wingsminer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;


public class ActivitySplash extends Activity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        SharedPreferences settings = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        Intent mainIntent = new Intent(ActivitySplash.this, settings.getString("jsonUsuario", "").equals("") ? ActivityPreview.class : ActivityMain.class);
        startActivity(mainIntent);
        finish();
    }
}