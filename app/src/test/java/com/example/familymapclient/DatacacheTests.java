package com.example.familymapclient;

import com.example.familymapclient.Cache.DataCache;
import com.example.familymapclient.Cache.SettingsCache;
import com.example.familymapclient.Proxy.Client;

import org.json.JSONException;
import org.junit.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;

import Models.EventModel;
import Models.PersonModel;
import Request.LoginReq;
import Result.EventAllRes;
import Result.LoginRes;
import Result.PersonAllRes;

/**
 * REQUIRED: Load in Sheila Parker data
 */
public class DatacacheTests {
    private static final int SHIELA_MARRIAGE_INDEX = 1;
    private static boolean isInit = false;
    private Client proxy;
    private DataCache dc;
    private SettingsCache sc;
    private String PORT = "8080";
    private String IP_ADDRESS = "127.0.0.1";
    private String authToken;
    @Before
    public void getInstance() throws IOException, JSONException {
        if(!isInit){
            setUp();
            isInit = true;
        }
        dc = DataCache.getInstance();
        sc = SettingsCache.getInstance();
        sc.resetSettings();

    }
    public void setUp() throws JSONException, IOException {
        proxy = new Client(IP_ADDRESS, PORT);
        dc = DataCache.getInstance();
        sc = SettingsCache.getInstance();
        LoginUser();

    }
    private void LoginUser() throws JSONException, IOException {
        LoginReq req = new LoginReq("sheila", "parker");
        LoginRes res = proxy.Login(req);
        authToken = res.getAuthToken();
        PersonAllRes personRes = proxy.GetFamilyData(authToken);
        dc.mapPersons(personRes.getData());
        EventAllRes eventsRes = proxy.GetEventsData(authToken);
        dc.mapEvents(eventsRes.getData());
        dc.setUser(dc.getPersons().get("Sheila_Parker"));
        dc.mapEventsPerPerson();
        dc.mapUserFamily();

    }
    @Test
    public void CalculateUserFamily(){
        dc.mapUserFamily();
        assertNotNull(dc.getUserMom());
        assertEquals(dc.getUserMom().getPersonID(),"Betty_White");
        assertNotNull(dc.getUserDad());
        assertEquals(dc.getUserDad().getPersonID(),"Blaine_McGary");
        assertNotNull(dc.getUserSpouse());
        assertEquals(dc.getUserSpouse().getPersonID(),"Davis_Hyer");
        assertEquals(dc.getUserSpouse().getSpouseID(),"Sheila_Parker");
    }
    @Test
    public void ShowPaternalSide(){
        //Show father's side only
        sc.showMotherSide = false;
        ArrayList<PersonModel> result = dc.configurePersons();
        //Check the entire array
        assertEquals(5,result.size());
        assertTrue(result.contains(dc.getUser()));
        assertTrue(result.contains(dc.getUserSpouse()));
        assertTrue(result.contains(dc.getUserDad()));
        assertTrue(result.contains(dc.getPersons().get("Blaine_McGary")));
        assertTrue(result.contains(dc.getPersons().get("Ken_Rodham")));
        assertTrue(result.contains(dc.getPersons().get("Mrs_Rodham")));
        assertFalse(result.contains(dc.getUserMom()));
    }
    @Test
    public void ShowMaternalSide(){
        //Show mother's side only
        sc.showMotherSide = true;
        sc.showFatherSide = false;
        ArrayList<PersonModel> results2 = dc.configurePersons();
        //Check the entire array
        assertEquals(5,results2.size());
        assertTrue(results2.contains(dc.getUser()));
        assertTrue(results2.contains(dc.getUserSpouse()));
        assertTrue(results2.contains(dc.getUserMom()));
        assertTrue(results2.contains(dc.getPersons().get("Betty_White")));
        assertTrue(results2.contains(dc.getPersons().get("Frank_Jones")));
        assertTrue(results2.contains(dc.getPersons().get("Mrs_Jones")));
        assertFalse(results2.contains(dc.getUserDad()));
    }
    @Test
    public void ShowMaleGender(){
        //Show male events only
        sc.showMaleEvents = true;
        sc.showFemaleEvents = false;
        ArrayList<PersonModel> males = dc.configurePersons();
        assertEquals(4,males.size());
        assertTrue(males.contains(dc.getPersons().get("Davis_Hyer")));
        assertTrue(males.contains(dc.getPersons().get("Frank_Jones")));
        assertTrue(males.contains(dc.getPersons().get("Blaine_McGary")));
        assertTrue(males.contains(dc.getPersons().get("Ken_Rodham")));

        HashMap<String, EventModel> maleEvents = dc.configureEvents();
        for(String key: maleEvents.keySet()){
            assertTrue(dc.getPersons().get(maleEvents.get(key).getPersonID()).getGender().equals("m"));
        }

    }
    @Test
    public void ShowFemaleGender(){
        //Show male events only
        sc.showFemaleEvents = true;
        sc.showMaleEvents = false;
        ArrayList<PersonModel> females = dc.configurePersons();
        assertEquals(4,females.size());
        assertTrue(females.contains(dc.getPersons().get("Sheila_Parker")));
        assertTrue(females.contains(dc.getPersons().get("Betty_White")));
        assertTrue(females.contains(dc.getPersons().get("Mrs_Jones")));
        assertTrue(females.contains(dc.getPersons().get("Mrs_Rodham")));

        HashMap<String, EventModel> femaleEvents = dc.configureEvents();
        for(String key: femaleEvents.keySet()){
            assertTrue(dc.getPersons().get(femaleEvents.get(key).getPersonID()).getGender().equals("f"));
        }
    }
    @Test
    public void AbnormalGenderOptions(){
        sc.showFemaleEvents = false;
        sc.showMaleEvents = false;
        ArrayList<PersonModel> persons = dc.configurePersons();
        assertEquals(persons.size(),0);
        HashMap<String, EventModel> events = dc.configureEvents();
        assertEquals(events.size(),0);
    }
    @Test
    public void AbnormalPaternalOptions(){
        sc.showMotherSide = false;
        sc.showFatherSide = false;
        ArrayList<PersonModel> noSides = dc.configurePersons();
        assertEquals(noSides.size(), 2);
        assertTrue(noSides.contains(dc.getPersons().get("Sheila_Parker")));
        assertTrue(noSides.contains(dc.getPersons().get("Davis_Hyer")));
    }

    /** Test if:
     * Birth event is first, Death is last (if they exist)
     * AND Marriage event is logical for both persons
     *
     */
    @Test
    public void LogicalEventTimeLine(){
        EventModel shielaBirth = dc.getEventsPerPerson().get("Sheila_Parker").get(0);
        EventModel shielaDeath = dc.getEventsPerPerson().get("Sheila_Parker").get(dc.getEventsPerPerson().get("Sheila_Parker").size() - 1);
        EventModel shielaMarraige = dc.getEventsPerPerson().get("Sheila_Parker").get(SHIELA_MARRIAGE_INDEX);
        EventModel davisBirth = dc.getEventsPerPerson().get("Davis_Hyer").get(0);
        assertEquals("birth", shielaBirth.getEventType());
        assertEquals("death", shielaDeath.getEventType());
        assertEquals("birth", davisBirth.getEventType());
        assertTrue(shielaBirth.getYear() < shielaDeath.getYear());
        assertTrue(davisBirth.getYear() < shielaMarraige.getYear());
    }

    /** Test if:
     * Check for ascending sequence of years
     */
    @Test
    public void EventsSortedByYear(){
        ArrayList<EventModel> sortedEvents = dc.getEventsPerPerson().get("Sheila_Parker");
        float temp = sortedEvents.get(0).getYear();
        for (int i = 1; i < sortedEvents.size(); i++) {
            assertTrue(temp <= sortedEvents.get(i).getYear());
            temp = sortedEvents.get(i).getYear();
        }
    }


}
