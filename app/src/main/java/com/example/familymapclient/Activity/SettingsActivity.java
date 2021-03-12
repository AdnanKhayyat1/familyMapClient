package com.example.familymapclient.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.familymapclient.R;
import com.example.familymapclient.Cache.SettingsCache;

public class SettingsActivity extends AppCompatActivity {
    private SettingsCache sc;
    private Switch mLifeStory;
    private Switch mFamilyLines;
    private Switch mSpouseLines;
    private Switch mFatherLines;
    private Switch mMotherLines;
    private Switch mMaleEvents;
    private Switch mFemaleEvents;
    private Button mLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        sc = SettingsCache.getInstance();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mLifeStory = findViewById(R.id.LS_switch);
        mLifeStory.setChecked(sc.showLifeLines);
        mLifeStory.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sc.showLifeLines = isChecked;
            }
        });

        mFamilyLines = findViewById(R.id.FT_switch);
        mFamilyLines.setChecked(sc.showFamilyLines);
        mFamilyLines.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sc.showFamilyLines = isChecked;
            }
        });

        mSpouseLines = findViewById(R.id.SpL_switch);
        mSpouseLines.setChecked(sc.showSpouseLines);
        mSpouseLines.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sc.showSpouseLines = isChecked;
            }
        });

        mFatherLines = findViewById(R.id.FL_switch);
        mFatherLines.setChecked(sc.showFatherSide);
        mFatherLines.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sc.showFatherSide = isChecked;
            }
        });

        mMotherLines = findViewById(R.id.ML_switch);
        mMotherLines.setChecked(sc.showMotherSide);
        mMotherLines.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sc.showMotherSide = isChecked;
            }
        });

        mMaleEvents= findViewById(R.id.ME_switch);
        mMaleEvents.setChecked(sc.showMaleEvents);
        mMaleEvents.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sc.showMaleEvents = isChecked;
            }
        });

        mFemaleEvents = findViewById(R.id.FE_switch);
        mFemaleEvents.setChecked(sc.showFemaleEvents);
        mFemaleEvents.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sc.showFemaleEvents = isChecked;
            }
        });

        mLogout = findViewById(R.id.logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sc.resetSettings();
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("START_MAP", true);
            startActivity(intent);
        }
        return true;
    }
}