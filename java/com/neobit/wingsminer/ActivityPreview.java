package com.neobit.wingsminer;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ActivityPreview extends AppCompatActivity {

    private View mProgressView;
    private View mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        mContentView = findViewById(R.id.contentView);
        mProgressView = findViewById(R.id.progressView);

        Button btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityPreview.this.finish();
                Intent mainIntent = new Intent(ActivityPreview.this, ActivityLogin.class);
                ActivityPreview.this.startActivity(mainIntent);
            }
        });
    }
}

