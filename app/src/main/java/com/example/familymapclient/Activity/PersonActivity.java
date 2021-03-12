package com.example.familymapclient.Activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.familymapclient.Proxy.Client;
import com.example.familymapclient.Cache.DataCache;
import com.example.familymapclient.R;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.ArrayList;
import java.util.HashMap;

import Models.EventModel;
import Models.PersonModel;

public class PersonActivity extends AppCompatActivity {
    private TextView mFirstName;
    private TextView mLastName;
    private TextView mGender;

    private EventModel event;
    private PersonModel person;
    private DataCache dc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        dc = DataCache.getInstance();
        event = Client.deserialize(getIntent().getStringExtra("SELECTED_EVENT"), EventModel.class);
        person = dc.getPersons().get(event.getPersonID());
        ExpandableListView expandableListView = findViewById(R.id.expEvents);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mFirstName = findViewById(R.id.fNameVar);
        mLastName = findViewById(R.id.lNameVar);
        mGender = findViewById(R.id.genderVar);
        mFirstName.setText(person.getFirstName());
        mLastName.setText(person.getLastName());
        switch (person.getGender()){
            case "m":
                mGender.setText("Male");
                break;
            case "f":
                mGender.setText("Female");
                break;
        }
        expandableListView.setAdapter(new ExpandableListAdapter(dc.getEventsPerPerson().get(person.getPersonID()), person));
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




    private class ExpandableListAdapter extends BaseExpandableListAdapter{
        private final int EVENTS_GROUP_POSITION = 0;
        private final int PERSONS_GROUP_POSITION = 1;
        private final String[] familyConns;
        private final ArrayList<EventModel> events;
        private final PersonModel person;
        private HashMap<String, PersonModel> personFamily; // family connection -> person
        private ArrayList<PersonModel> finalPersons;
        ExpandableListAdapter(ArrayList<EventModel> events, PersonModel person){
            familyConns = new String[]{"Father", "Mother", "Spouse", "Child"};
            this.events = events;
            this.person = person;
            this.personFamily = new HashMap<String, PersonModel>();
            this.finalPersons = new ArrayList<PersonModel>();
            rearrangeEvents();
            mapFamily();

        }
        private void mapFamily(){
            if(person.getFatherID() != null){
                personFamily.put("Father", dc.getPersons().get(person.getFatherID()));
            }
            if(person.getMotherID() != null){
                personFamily.put("Mother", dc.getPersons().get(person.getMotherID()));
            }
            if(person.getSpouseID() != null){
                personFamily.put("Spouse", dc.getPersons().get(person.getSpouseID()));
            }
            for(String key : dc.getPersons().keySet()){
                if(person.getGender().equals("m")){
                    if(dc.getPersons().get(key).getFatherID() != null && dc.getPersons().get(key).getFatherID().equals(person.getPersonID())){
                        personFamily.put("Child", dc.getPersons().get(key));
                        break;
                    }
                }
                else{
                    if(dc.getPersons().get(key).getMotherID() != null && dc.getPersons().get(key).getMotherID().equals(person.getPersonID())){
                        personFamily.put("Child", dc.getPersons().get(key));
                        break;
                    }
                }

            }
            for(String key : personFamily.keySet()){
                finalPersons.add(personFamily.get(key));
            }
        }
        private void rearrangeEvents(){
            for (int i = 0; i < events.size(); i++) {
                if(events.get(i).getEventType().equals("death")){
                    events.add(events.size() - 1, events.get(i));
                    events.remove(i);
                }
                if(events.get(i).getEventType().equals("birth")){
                    events.add(0, events.get(i));
                    events.remove(i);
                }

            }
        }
        @Override
        public int getGroupCount() {
            return 2;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch(groupPosition){
                case EVENTS_GROUP_POSITION:
                    return events.size();
                case PERSONS_GROUP_POSITION:
                    return personFamily.keySet().size();
                default:
                    throw new IllegalArgumentException("Unrecognized group position " + groupPosition);
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            switch(groupPosition){
                case EVENTS_GROUP_POSITION:
                    return "Events:";
                case PERSONS_GROUP_POSITION:
                    return "Family:";
                default:
                    throw new IllegalArgumentException("Unrecognized group position " + groupPosition);
            }
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            switch(groupPosition){
                case EVENTS_GROUP_POSITION:
                    return events.get(childPosition);
                case PERSONS_GROUP_POSITION:
                    return finalPersons.get(childPosition);
                default:
                    throw new IllegalArgumentException("Unrecognized group position " + groupPosition);
            }
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_title, parent, false);
            }
            TextView titleView = convertView.findViewById(R.id.list_title);
            switch(groupPosition){
                case EVENTS_GROUP_POSITION:
                    titleView.setText("Events");
                    break;
                case PERSONS_GROUP_POSITION:
                    titleView.setText("Family");
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position " + groupPosition);
            }

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View itemView;


            itemView = getLayoutInflater().inflate(R.layout.person_list_item, parent, false);
            switch (groupPosition) {
                case EVENTS_GROUP_POSITION:
                    initializeEventView(itemView, childPosition);
                    break;
                case PERSONS_GROUP_POSITION:
                    initializePersonView(itemView, childPosition);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position " + groupPosition);
            }

            return itemView;
        }
        private String getConnection(PersonModel p){
            for(String key: personFamily.keySet()){
                if(p.getPersonID().equals(personFamily.get(key).getPersonID())){
                    return key;
                }
            }
            return null;
        }
        private void initializePersonView(View eventView, final int childPos){
            PersonModel p = finalPersons.get(childPos);
            ImageView itemIcon = eventView.findViewById(R.id.item_icon);
            switch(p.getGender()){
                case "m":
                    Drawable maleIcon = new IconDrawable(PersonActivity.this, FontAwesomeIcons.fa_male).
                            colorRes(R.color.maleBlue).sizeDp(70);
                    itemIcon.setImageDrawable(maleIcon);
                    break;
                case "f":
                    Drawable femaleIcon = new IconDrawable(PersonActivity.this, FontAwesomeIcons.fa_female).
                            colorRes(R.color.femalePink).sizeDp(70);
                    itemIcon.setImageDrawable(femaleIcon);
                    break;
            }
            TextView itemDesc = eventView.findViewById(R.id.item_desc);
            StringBuilder sb = new StringBuilder();

            sb.append(p.getFirstName() + " " + p.getLastName());
            sb.append('\n');

            sb.append(getConnection(p));
            itemDesc.setText(sb.toString());
            eventView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PersonActivity.this, PersonActivity.class);
                    intent.putExtra("SELECTED_EVENT",
                            Client.serialize(dc.getEventsPerPerson().get(finalPersons.get(childPos).getPersonID()).toArray()[0]));
                    startActivity(intent);
                }
            });
        }
        private void initializeEventView(View eventView, final int childPos){
            ImageView itemIcon = eventView.findViewById(R.id.item_icon);
            Drawable defaultIcon = new IconDrawable(PersonActivity.this, FontAwesomeIcons.fa_map_marker).
                    colorRes(R.color.black).sizeDp(70);
            itemIcon.setImageDrawable(defaultIcon);
            TextView itemDesc = eventView.findViewById(R.id.item_desc);
            StringBuilder sb = new StringBuilder();
            final EventModel initalEvent = events.get(childPos);
            String personID = initalEvent.getPersonID();
            PersonModel p = dc.getPersons().get(personID);
            sb.append(p.getFirstName() + " " + p.getLastName());
            sb.append('\n');
            sb.append(initalEvent.getEventType() + ": " + initalEvent.getCity() + ", " + initalEvent.getCountry() + " (" + initalEvent.getYear() + ")");
            itemDesc.setText(sb.toString());

            eventView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PersonActivity.this, EventActivity.class);
                    intent.putExtra("MARKER",
                            Client.serialize(initalEvent));
                    startActivity(intent);
                }
            });
        }


        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
