package com.example.familymapclient.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.familymapclient.Proxy.Client;
import com.example.familymapclient.Cache.DataCache;
import com.example.familymapclient.R;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.ArrayList;

import Models.EventModel;
import Models.PersonModel;

public class SearchActivity extends AppCompatActivity {
    private static final int VIEW_EVENTS = 0;
    private static final int VIEW_PERSON = 1;

    private ArrayList<EventModel> events;
    private ArrayList<PersonModel> persons;
    private DataCache dc;
    private SearchAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        dc = DataCache.getInstance();
        events = new ArrayList<>();
        persons = dc.configurePersons();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        for(String key : dc.configureEvents().keySet()){
            events.add(dc.getEvents().get(key));
        }
        RecyclerView recycler = findViewById(R.id.search_box);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchAdapter(events, persons);
        recycler.setAdapter(adapter);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu,menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    private class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> implements Filterable {
        private final ArrayList<EventModel> eventList;
        private final ArrayList<EventModel> eventsFull;
        private final ArrayList<PersonModel> personList;
        private final ArrayList<PersonModel> personsFull;
        SearchAdapter(ArrayList<EventModel> eventList, ArrayList<PersonModel> personList){
            this.eventList = eventList;
            this.eventsFull = new ArrayList<>(eventList);
            this.personList = personList;
            this.personsFull = new ArrayList<>(personList);
        }
        @NonNull
        @Override
        public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v;
            v = getLayoutInflater().inflate(R.layout.search_item, parent, false);
            return new SearchViewHolder(v, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
            if(position < eventList.size()){
                holder.bind(eventList.get(position));
            }else{
                holder.bind(personList.get(position - eventList.size()));
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position < eventList.size() ? VIEW_EVENTS : VIEW_PERSON;
        }

        @Override
        public int getItemCount() {
            return eventList.size() + personList.size();
        }

        @Override
        public Filter getFilter() {
            return searchFilter;
        }
        private Filter searchFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                ArrayList<EventModel> filteredEvents = new ArrayList<>();
                ArrayList<PersonModel> filteredPersons = new ArrayList<>();
                FilterResults results = new FilterResults();
                if(constraint == null || constraint.length() == 0){
                    filteredEvents.addAll(eventsFull);
                    filteredPersons.addAll(personsFull);
                }else{
                    String query = constraint.toString().toLowerCase().trim();
                    for(EventModel event : eventsFull){
                        //countries cities eventTypes years
                        if(event.getCountry().toLowerCase().contains(query) || event.getCity().toLowerCase().contains(query)
                                || event.getEventType().toLowerCase().contains(query) || Float.toString(event.getYear()).contains(query) ){
                            filteredEvents.add(event);

                        }
                    }
                    for(PersonModel person : personsFull){
                        //first name last name
                        if(person.getFirstName().toLowerCase().contains(query) || person.getLastName().toLowerCase().contains(query)){
                            filteredPersons.add(person);
                        }
                    }
                }
                ArrayList<Object> finalResults = new ArrayList<>();
                finalResults.addAll(filteredEvents);
                finalResults.addAll(filteredPersons);
                results.values = finalResults;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                eventList.clear();
                personList.clear();
                ArrayList<Object> returnedItems = (ArrayList<Object>) results.values;
                for(Object obj : returnedItems){
                    if(obj instanceof EventModel){
                        eventList.add((EventModel) obj);
                    }
                    else{
                        personList.add((PersonModel) obj);
                    }
                }
                //eventList.addAll((ArrayList<EventModel>) results.values);
                notifyDataSetChanged();
            }
        };
    }
    private class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView mItemDesc;
        private final ImageView mIcon;
        private PersonModel person;
        private EventModel event;
        private final int viewCount;
        public SearchViewHolder(@NonNull View itemView, int viewCount) {
            super(itemView);
            itemView.setOnClickListener(this);
            mItemDesc = itemView.findViewById(R.id.search_desc);
            mIcon = itemView.findViewById(R.id.search_icon);
            this.viewCount = viewCount;

        }
        private void bind(EventModel event){
            StringBuilder sb = new StringBuilder();
            String eventType = event.getEventType();
            eventType = eventType.toUpperCase();
            sb.append(eventType + ": " + event.getCity() + ", " + event.getCountry() + " (" + event.getYear() + ")");
            sb.append('\n');
            sb.append(dc.getPersons().get(event.getPersonID()).getFirstName() + " " + dc.getPersons().get(event.getPersonID()).getLastName());
            mItemDesc.setText(sb.toString());
            Drawable eventDrawable = new IconDrawable(SearchActivity.this, FontAwesomeIcons.fa_map_marker).
                    colorRes(R.color.black).sizeDp(70);
            mIcon.setImageDrawable(eventDrawable);
            this.event = event;
        }
        private void bind(PersonModel person){
            StringBuilder sb = new StringBuilder();
            sb.append(person.getFirstName() + " " + person.getLastName());
            mItemDesc.setText(sb.toString());
            Drawable genderIcon = null;
            switch(person.getGender()){
                case "m":
                    genderIcon = new IconDrawable(SearchActivity.this, FontAwesomeIcons.fa_male).
                            colorRes(R.color.maleBlue).sizeDp(70);
                    break;
                case "f":
                    genderIcon = new IconDrawable(SearchActivity.this, FontAwesomeIcons.fa_female).
                            colorRes(R.color.femalePink).sizeDp(70);
                    break;
            }
            mIcon.setImageDrawable(genderIcon);
            this.person = person;
        }

        @Override
        public void onClick(View v) {
            if(viewCount == VIEW_EVENTS){
                //intent events
                Intent intent = new Intent(SearchActivity.this, EventActivity.class);
                intent.putExtra("MARKER",
                        Client.serialize(event));
                startActivity(intent);

            }
            else{
                //intent person
                Intent intent = new Intent(SearchActivity.this, PersonActivity.class);
                intent.putExtra("SELECTED_EVENT", Client.serialize(person));
                startActivity(intent);
            }
        }
    }
}
