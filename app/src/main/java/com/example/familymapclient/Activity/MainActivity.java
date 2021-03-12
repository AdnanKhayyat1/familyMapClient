package com.example.familymapclient.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import com.example.familymapclient.Fragment.LoginFragment;
import com.example.familymapclient.Fragment.MapFragment;
import com.example.familymapclient.R;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

public class MainActivity extends AppCompatActivity {
    private LoginFragment loginFragment;
    private MapFragment map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Iconify.with(new FontAwesomeModule());
        setContentView(R.layout.activity_main);
        FragmentManager fm = this.getSupportFragmentManager();
        if(savedInstanceState == null){
            loginFragment = new LoginFragment();
            fm.beginTransaction().add(R.id.main_frame, loginFragment).commit();
        }
        if(getIntent().getBooleanExtra("START_MAP", false)){
            switchFragments();
        }

    }
    public void switchFragments(){
        FragmentManager fm = this.getSupportFragmentManager();
        map = new MapFragment();
        fm.beginTransaction().replace(R.id.main_frame, map).commit();
    }
}
