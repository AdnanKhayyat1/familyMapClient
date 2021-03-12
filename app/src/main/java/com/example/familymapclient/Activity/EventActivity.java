package com.example.familymapclient.Activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.familymapclient.Proxy.Client;
import com.example.familymapclient.Cache.DataCache;
import com.example.familymapclient.Fragment.MapFragment;
import com.example.familymapclient.R;
import com.google.android.gms.maps.model.Marker;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import Models.EventModel;

public class EventActivity extends AppCompatActivity {
    private DataCache dc;
    private MapFragment mapFragment;
    private EventModel event;
    private Marker eventMarker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        Iconify.with(new FontAwesomeModule());
        dc = DataCache.getInstance();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        event = Client.deserialize(getIntent().getStringExtra("MARKER"), EventModel.class);
        FragmentManager fm = this.getSupportFragmentManager();
        mapFragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(mapFragment.ARG_KEY,event.getEventID());
        mapFragment.setArguments(args);
        fm.beginTransaction().add(R.id.main_frame, mapFragment).commit();

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return true;
    }
}
