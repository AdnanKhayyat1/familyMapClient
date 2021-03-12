package com.example.familymapclient.Fragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.familymapclient.Proxy.Client;
import com.example.familymapclient.Cache.DataCache;
import com.example.familymapclient.Activity.PersonActivity;
import com.example.familymapclient.R;
import com.example.familymapclient.Activity.SearchActivity;
import com.example.familymapclient.Activity.SettingsActivity;
import com.example.familymapclient.Cache.SettingsCache;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import java.util.ArrayList;
import java.util.HashMap;

import Models.EventModel;
import Models.PersonModel;


public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    public static final String ARG_KEY = "event";
    private final float[] MARKER_HUES = new float[]{BitmapDescriptorFactory.HUE_AZURE, BitmapDescriptorFactory.HUE_CYAN,
            BitmapDescriptorFactory.HUE_MAGENTA, BitmapDescriptorFactory.HUE_ORANGE,
            BitmapDescriptorFactory.HUE_ROSE, BitmapDescriptorFactory.HUE_GREEN};


    private GoogleMap map;
    private DataCache dc;
    private SettingsCache sc;
    private TextView desc;
    private ImageView icon;
    private HashMap<String, Float> markerColors;
    private ArrayList<Polyline> lines = new ArrayList<Polyline>();
    private ArrayList<Marker> markerList;
    public EventModel currentEvent;
    private EventModel passedOnEvent;
    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Iconify.with(new FontAwesomeModule());
        setHasOptionsMenu(true);
        markerColors = new HashMap<String, Float>();
        markerList = new ArrayList<Marker>();
        currentEvent = null;
        dc = DataCache.getInstance();
        sc = SettingsCache.getInstance();
        if(getArguments() != null){
            passedOnEvent = dc.getEvents().get(getArguments().getString(ARG_KEY));
        }

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.search_icon);
        searchItem.setIcon(new IconDrawable(getActivity(),
                FontAwesomeIcons.fa_search)
                .colorRes(R.color.white)
                .actionBarSize());
        MenuItem settingsItem = menu.findItem(R.id.settings_icon);
        settingsItem.setIcon(new IconDrawable(getActivity(),
                FontAwesomeIcons.fa_gear)
                .colorRes(R.color.white)
                .actionBarSize());
        settingsItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            }
        });
        searchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
                return true;
            }
        });
        if(passedOnEvent != null){
            searchItem.setVisible(false);
            settingsItem.setVisible(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //dc.mapEventsPerPerson();

        View v = inflater.inflate(R.layout.fragment_map, container, false);
        desc = (TextView) v.findViewById(R.id.mapTextView);
        desc.setText("Click on a marker to see event's details");
        desc.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(getActivity(), PersonActivity.class);
                intent.putExtra("SELECTED_EVENT", Client.serialize(currentEvent));
                startActivity(intent);
            }
        });
        icon = (ImageView) v.findViewById(R.id.icon);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        
        mapFragment.getMapAsync(this);
        return v;
    }

    @Override
    public void onMapLoaded() {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLoadedCallback(this);
        setMarkerColors(dc.getEvents());

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                focusOnMarker(marker);
                return true;
            }
        });

        Drawable defaultIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_home).
                colorRes(R.color.blue).sizeDp(70);
        icon.setImageDrawable(defaultIcon);
        layEvents(dc.configureEvents());
        if(passedOnEvent != null){
            focusOnMarker(getEventMarker(dc.getEvents().get(getArguments().getString(ARG_KEY))));
        }

    }
    public void focusOnMarker(Marker marker){

        for (int i = 0; i < lines.size(); i++) {
            lines.get(i).remove();
        }
        lines.clear();
        EventModel e = (EventModel) marker.getTag();
        map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(e.getLatitude(),e.getLongitude())));
        currentEvent = e;
        String personID = e.getPersonID();
        PersonModel p = dc.getPersons().get(personID);
        StringBuilder sb = new StringBuilder();
        sb.append(p.getFirstName() + " " + p.getLastName());
        sb.append('\n');
        sb.append(e.getEventType() + ": " + e.getCity() + ", " + e.getCountry() + " (" + e.getYear() + ")");
        desc.setText(sb.toString());
        Drawable maleIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_male).
                colorRes(R.color.maleBlue).sizeDp(70);
        Drawable femaleIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_female).
                colorRes(R.color.femalePink).sizeDp(70);
        switch(p.getGender()){
            case "m":
                icon.setImageDrawable(maleIcon);
                break;
            case "f":
                icon.setImageDrawable(femaleIcon);
                break;
        }
        if(sc.showSpouseLines){
            drawSpouseLines(marker);
        }
        if(sc.showFamilyLines){
            drawFamilyGens((EventModel) marker.getTag(), 10);
        }
        if(sc.showLifeLines){
            drawLifeStory(marker);
        }
    }
    public Marker getEventMarker(EventModel e){
        for (int i = 0; i < markerList.size(); i++) {
            if(e.equals((EventModel)markerList.get(i).getTag())){
                return markerList.get(i);
            }
        }
        return null;
    }
    private void drawFamilyGens(EventModel event, int strokeWidth){
        PersonModel user = dc.getPersons().get(event.getPersonID());
        PersonModel mom = dc.getPersons().get(user.getMotherID());
        if(mom != null){
            EventModel curr = getFirstEvent(mom.getPersonID());
            if(curr != null && markerExists(curr)){
                Polyline currLine = map.addPolyline(new PolylineOptions().add(new LatLng(event.getLatitude(), event.getLongitude()),
                        new LatLng(curr.getLatitude(), curr.getLongitude()))
                        .color(getResources().getColor(R.color.darkGreen)).width(strokeWidth));
                lines.add(currLine);
                drawFamilyGens(curr, strokeWidth - 3);
            }
        }
        PersonModel dad = dc.getPersons().get(user.getFatherID());
        if(dad != null){
            EventModel curr = getFirstEvent(dad.getPersonID());
            if(curr != null && markerExists(curr)){
                Polyline currLine = map.addPolyline(new PolylineOptions().add(new LatLng(event.getLatitude(), event.getLongitude()),
                        new LatLng(curr.getLatitude(), curr.getLongitude()))
                        .color(getResources().getColor(R.color.darkGreen)).width(strokeWidth));
                lines.add(currLine);
                drawFamilyGens(curr, strokeWidth - 3);
            }
        }

    }
    private EventModel getFirstEvent(String id){
        ArrayList<EventModel> events = dc.getEventsPerPerson().get(id);
        if(events.size() > 0){
            return events.get(0);
        }
        return null;
    }
    private void drawSpouseLines(Marker marker){
        EventModel event = (EventModel) marker.getTag();
        String personID = event.getPersonID();
        String spouseID = dc.getPersons().get(personID).getSpouseID();
        EventModel target = null;
        if(!spouseID.isEmpty()){
            ArrayList<EventModel> spouseEvents= dc.getEventsPerPerson().get(spouseID);
            for (int i = 0; i < spouseEvents.size(); i++) {
                if(spouseEvents.get(i).getEventType().equals("birth")){
                    target = spouseEvents.get(i);
                    break;
                }
            }
            if(target == null && spouseEvents.size() > 0){
                target = spouseEvents.get(0);
            }
            if(target != null && markerExists(target)){
                Polyline currLine = map.addPolyline(new PolylineOptions().add(new LatLng(event.getLatitude(), event.getLongitude()),
                        new LatLng(target.getLatitude(), target.getLongitude()))
                        .color(getResources().getColor(R.color.orange)));
                lines.add(currLine);
            }
        }
    }
    private boolean markerExists(EventModel e){
        for (int i = 0; i < markerList.size(); i++) {
            EventModel currentEvent = (EventModel) markerList.get(i).getTag();
            if(currentEvent.getEventID().equals(e.getEventID())){
                return true;
            }
        }
        return false;
    }
    private void drawLifeStory(Marker marker){
        EventModel event = (EventModel) marker.getTag();
        HashMap<String, ArrayList<EventModel>> models = dc.getEventsPerPerson();
        ArrayList<EventModel> currEvents =  models.get(event.getPersonID());
        EventModel temp = null;
        float tempLat = 0f;
        float tempLong = 0f;
        for (int i = 0; i < currEvents.size(); i++) {
            if(i == 0){
                temp = currEvents.get(i);
            }
            else{
                tempLat = temp.getLatitude();
                tempLong = temp.getLongitude();
                float lat = currEvents.get(i).getLatitude();
                float lon = currEvents.get(i).getLongitude();
                Polyline currLine = map.addPolyline(new PolylineOptions().add(new LatLng(tempLat, tempLong), new LatLng(lat, lon))
                        .color(getResources().getColor(R.color.purple)));
                lines.add(currLine);
                temp = currEvents.get(i);
            }
        }


    }

    private void layEvents(HashMap<String, EventModel> events){
        for(String key: events.keySet()){
            LatLng marker = new LatLng(events.get(key).getLatitude(), events.get(key).getLongitude());

            Marker curr = map.addMarker(new MarkerOptions().position(marker)
                    .icon(BitmapDescriptorFactory
                    .defaultMarker(getMakerColor(events.get(key).getEventType()))));
            curr.setTag(events.get(key));
            markerList.add(curr);
        }
    }
    private float getMakerColor(String type){
        return markerColors.get(type);
    }
    private void setMarkerColors(HashMap<String, EventModel> events){
        int mapIndex = 0;
        for(String key: events.keySet()){
            if(!markerColors.containsKey(events.get(key).getEventType())){
                markerColors.put(events.get(key).getEventType(), MARKER_HUES[mapIndex]);
                mapIndex++;
            }
            if(mapIndex >= MARKER_HUES.length){
                mapIndex = 0;
            }
        }
    }

}
