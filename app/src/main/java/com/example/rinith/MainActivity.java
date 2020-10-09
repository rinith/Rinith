package com.example.rinith;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.truecaller.android.sdk.ITrueCallback;
import com.truecaller.android.sdk.TrueError;
import com.truecaller.android.sdk.TrueProfile;
import com.truecaller.android.sdk.TruecallerSDK;
import com.truecaller.android.sdk.TruecallerSdkScope;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

}