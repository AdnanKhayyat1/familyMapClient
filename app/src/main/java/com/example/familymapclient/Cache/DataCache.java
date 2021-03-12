package com.example.familymapclient.Cache;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import Models.EventModel;
import Models.PersonModel;

public class DataCache {

    private static DataCache instance;
    private SettingsCache sc;
    private HashMap<String, PersonModel> persons;
    private ArrayList<String> eventTypes;
    private HashMap<String, EventModel> events;
    private HashMap<String, EventModel> maleEvents;
    private HashMap<String, EventModel> femaleEvents;
    private HashMap<String, ArrayList<EventModel>> eventsPerPerson;
    private HashMap<String, EventModel> userFatherSide;
    private HashMap<String, EventModel> userMotherSide;
    private ArrayList<PersonModel> paternalPersons;
    private ArrayList<PersonModel> maternalPersons;
    private boolean isLoggedIn;
    private PersonModel user;
    private PersonModel userMom;
    private PersonModel userDad;
    private PersonModel userSpouse;
    private HashMap<String, PersonModel> children;


    public static DataCache getInstance() {
        if(instance == null) {
            instance = new DataCache();
        }

        return instance;
    }
    private DataCache(){
        sc = SettingsCache.getInstance();
        persons = new HashMap<String, PersonModel>();
        events = new HashMap<String, EventModel>();
        maleEvents = new HashMap<String, EventModel>();
        femaleEvents = new HashMap<String, EventModel>();
        userFatherSide = new HashMap<String, EventModel>();
        userMotherSide = new HashMap<String, EventModel>();
        paternalPersons = new ArrayList<>();
        maternalPersons = new ArrayList<>();
        eventTypes = new ArrayList<String>();
        eventsPerPerson = new HashMap<String, ArrayList<EventModel>>();
        children = new HashMap<String, PersonModel>();
    }
    public void mapUserFamily(){
        if(user.getSpouseID() != null){
            userSpouse = persons.get(user.getSpouseID());
        }
        if(user.getMotherID() != null){
            userMom = persons.get(user.getMotherID());
        }
        if(user.getFatherID() != null){
            userDad = persons.get(user.getFatherID());
        }
        for(String key : persons.keySet()){
            if(user.getGender().equals("m")){
                if(persons.get(key).getFatherID() != null && persons.get(key).getFatherID().equals(user.getPersonID())){
                    children.put(persons.get(key).getPersonID(), persons.get(key));
                }
            }
            else{
                if(persons.get(key).getMotherID() != null && persons.get(key).getMotherID().equals(user.getPersonID())){
                    children.put(persons.get(key).getPersonID(), persons.get(key));
                }
            }

        }
    }
    public void mapPersons (List<PersonModel> personList){
        for (int i = 0; i < personList.size(); i++) {
            persons.put(personList.get(i).getPersonID(), personList.get(i));
        }
    }
    public HashMap<String, ArrayList<EventModel>> getEventsPerPerson() {
        return eventsPerPerson;
    }

    public ArrayList<String> getEventTypes() {
        return eventTypes;
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void mapEventsPerPerson(){
        for(String personID : persons.keySet()){
            eventsPerPerson.put(personID, new ArrayList<EventModel>());
            for(String eventID : events.keySet()){
                if(events.get(eventID).getPersonID().equals(personID)){
                    eventsPerPerson.get(personID).add(events.get(eventID));
                }
            }
            eventsPerPerson.get(personID).sort(new EventSorter());
        }
        if(user.getMotherID() != null){
            PersonModel mainMother = persons.get(user.getMotherID());
            mapParentalSides(mainMother, userMotherSide, maternalPersons);
        }
        if(user.getFatherID() != null){
            PersonModel mainFather = persons.get(user.getFatherID());
            mapParentalSides(mainFather, userFatherSide, paternalPersons);
        }


    }
    public void mapEvents (List<EventModel> eventList){
        for (int i = 0; i < eventList.size(); i++) {
            EventModel e = eventList.get(i);
            e.setEventType(e.getEventType().toLowerCase());
            mapEventTypes(e);
            events.put(eventList.get(i).getEventID(), eventList.get(i));
            mapGenderedEvents(eventList.get(i));
        }
    }
    private void mapParentalSides(PersonModel person, HashMap<String, EventModel> sideEvents, ArrayList<PersonModel> sidePersons){
        if(person.getMotherID() != null){
            PersonModel mum = persons.get(person.getMotherID());
            sidePersons.add(mum);
            ArrayList<EventModel> mumEvents = eventsPerPerson.get(person.getMotherID());
            for (int i = 0; i < mumEvents.size(); i++) {
                sideEvents.put(mumEvents.get(i).getEventID(), mumEvents.get(i));
            }
            mapParentalSides(mum, sideEvents, sidePersons);
        }
        if(person.getFatherID() != null){
            PersonModel dad = persons.get(person.getFatherID());
            sidePersons.add(dad);
            ArrayList<EventModel> dadEvents = eventsPerPerson.get(person.getFatherID());
            for (int i = 0; i < dadEvents.size(); i++) {
                sideEvents.put(dadEvents.get(i).getEventID(), dadEvents.get(i));
            }
            mapParentalSides(dad, sideEvents, sidePersons);
        }
    }
    public ArrayList<PersonModel> configurePersons(){
        ArrayList<PersonModel> temp = new ArrayList<>();
        ArrayList<PersonModel> personsToRemove = new ArrayList<>();
        temp.add(user);
        if(user.getSpouseID() != null){
            temp.add(persons.get(user.getSpouseID()));
        }
        if(sc.showMotherSide){
            if(user.getMotherID() != null){
                temp.add(persons.get(user.getMotherID()));
            }
            temp.addAll(maternalPersons);
        }
        if(sc.showFatherSide){
            if(user.getFatherID() != null){
                temp.add(persons.get(user.getFatherID()));
            }
            temp.addAll(paternalPersons);
        }
        if(!sc.showMaleEvents){
            for (int i = 0; i < temp.size(); i++) {
                if(temp.get(i).getGender().equals("m")){
                    personsToRemove.add(temp.get(i));
                }
            }
        }
        if(!sc.showFemaleEvents){
            for (int i = 0; i < temp.size(); i++) {
                if(temp.get(i).getGender().equals("f")){
                    personsToRemove.add(temp.get(i));
                }
            }
        }
        temp.removeAll(personsToRemove);
        return temp;
    }
    public HashMap<String, EventModel> configureEvents(){
        HashMap<String, EventModel> temp = new HashMap<String, EventModel>();
        ArrayList<String> eventsToRemove = new ArrayList<String>();
        ArrayList<EventModel> userEvents = eventsPerPerson.get(user.getPersonID());
        for (int i = 0; i < userEvents.size(); i++) {
            temp.put(userEvents.get(i).getEventID(), userEvents.get(i));
        }
        if(user.getSpouseID() != null){
            ArrayList<EventModel> spouseEvents = eventsPerPerson.get(user.getSpouseID());
            for (int i = 0; i < spouseEvents.size(); i++) {
                temp.put(spouseEvents.get(i).getEventID(), spouseEvents.get(i));
            }
        }
        if(sc.showFatherSide){
            if(user.getFatherID() != null){
                ArrayList<EventModel> fatherEvents = eventsPerPerson.get(user.getFatherID());
                for (int i = 0; i < fatherEvents.size(); i++) {
                    temp.put(fatherEvents.get(i).getEventID(), fatherEvents.get(i));
                }
            }
            temp.putAll(userFatherSide);
        }
        if(sc.showMotherSide){
            if(user.getMotherID() != null){
                ArrayList<EventModel> motherEvents = eventsPerPerson.get(user.getMotherID());
                for (int i = 0; i < motherEvents.size(); i++) {
                    temp.put(motherEvents.get(i).getEventID(), motherEvents.get(i));
                }
            }
            temp.putAll(userMotherSide);
        }
        if(!sc.showMaleEvents){
            for(String id: temp.keySet()){
                if(persons.get(temp.get(id).getPersonID()).getGender().equals("m")){
                    eventsToRemove.add(id);
                }
            }
        }
        if(!sc.showFemaleEvents){
            for(String id: temp.keySet()){
                if(persons.get(temp.get(id).getPersonID()).getGender().equals("f")){
                    eventsToRemove.add(id);
                }
            }
        }
        for (int i = 0; i < eventsToRemove.size(); i++) {
            temp.remove(eventsToRemove.get(i));
        }


        return temp;
    }
    private void mapGenderedEvents(EventModel e){
        switch (persons.get(e.getPersonID()).getGender()) {
            case "m":
                maleEvents.put(e.getEventID(), e);
                break;
            case "f":
                femaleEvents.put(e.getEventID(), e);
                break;
        }
    }
    private void mapEventTypes(EventModel event){
        if(!eventTypes.contains(event.getEventType().toLowerCase())){
            eventTypes.add(event.getEventType().toLowerCase());
        }
    }
    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public PersonModel getUser() {
        return user;
    }

    public void setUser(PersonModel user) {
        this.user = user;
    }

    public HashMap<String, PersonModel> getPersons() {
        return persons;
    }

    public HashMap<String, EventModel> getEvents() {
        return events;
    }
    public HashMap<String, EventModel> getMaleEvents() {
        return maleEvents;
    }

    public HashMap<String, EventModel> getFemaleEvents() {
        return femaleEvents;
    }
    public PersonModel getUserMom() {
        return userMom;
    }

    public PersonModel getUserDad() {
        return userDad;
    }

    public PersonModel getUserSpouse() {
        return userSpouse;
    }
    private class EventSorter implements Comparator<EventModel>{

        @Override
        public int compare(EventModel o1, EventModel o2) {
            return Integer.compare(o1.getYear(), o2.getYear());
        }
    }



}
