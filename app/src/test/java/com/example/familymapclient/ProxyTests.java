package com.example.familymapclient;

import com.example.familymapclient.Proxy.Client;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;


import java.io.IOException;
import Request.LoginReq;
import Request.RegisterReq;
import Result.EventAllRes;
import Result.LoginRes;
import Result.PersonAllRes;
import Result.RegisterRes;
import static org.junit.Assert.*;

/**
 * For each test run, make sure you load shiela-parker data.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ProxyTests {
    private final static String IP_ADDRESS = "127.0.0.1";
    private final static String PORT_NUM = "8080";
    private Client proxy;
    private String authToken;
    @Before
    public  void setUp() throws IOException {
        proxy = new Client(IP_ADDRESS, PORT_NUM);
    }
    private void LoginUser() throws JSONException {
        LoginReq req = new LoginReq("sheila", "parker");
        LoginRes res = proxy.Login(req);
        authToken = res.getAuthToken();
    }
    @Test
    public void LogInPass() throws JSONException {
        LoginReq req = new LoginReq("sheila", "parker");
        LoginRes res = proxy.Login(req);
        assertNotNull(res);
        assertEquals(res.getPersonID(),"Sheila_Parker");
        assertEquals(res.getUserName(),"sheila");
        assertNotNull(res.getAuthToken());
        authToken = res.getAuthToken();
        assertTrue(res.isSuccess());
    }
    @Test
    public void RegisterPass() throws IOException, JSONException {
        RegisterRes res = null;
        RegisterReq regReq = new RegisterReq();
        regReq.userName = "user_123";
        regReq.password = "password_1234";
        regReq.firstName = "Student";
        regReq.lastName = "AtBYU";
        regReq.gender = "f";
        regReq.email = "student@byu.edu";
        res = proxy.RegisterUser(regReq);
        assertNotNull(res);
        assertEquals("user_123", res.getUserName());
        assertNotNull(res.getAuthToken());

        assertTrue(res.isSuccess());
    }
    @Test
    public void RegisterFail() throws IOException, JSONException {
        RegisterRes res = null;
        RegisterReq regReq = new RegisterReq();
        regReq.userName = null;
        regReq.password = "password_1234";
        regReq.firstName = "Student";
        regReq.lastName = "AtBYU";
        regReq.gender = "f";
        regReq.email = "student@byu.edu";
        res = proxy.RegisterUser(regReq);
        assertNull(res);
    }
    @Test
    public void LogInFail() throws JSONException {
        JSONObject json = null;
        LoginReq logReq  = new LoginReq("non-existent", "user");
        LoginRes res = proxy.Login(logReq);
        assertNull(res);
    }
    @Test
    public void GetPersonsPass() throws IOException, JSONException {
        LoginUser();
        String successMsg = "Successfully added 2 users,\n" +
                "\t 11 persons,\n" +
                "\t and 19 events to the database.";
        PersonAllRes res = proxy.GetFamilyData(authToken);
        assertNotNull(res);
        assertTrue(res.isSuccess());
        assertNotNull(res.getData());
        assertEquals(res.getData().size(),8);
    }
    @Test
    public void GetEventsPass() throws IOException, JSONException {
        LoginUser();
        EventAllRes res = proxy.GetEventsData(authToken);
        assertNotNull(res);
        assertTrue(res.isSuccess());
        assertNotNull(res.getData());
        assertEquals(res.getData().size(),16);
    }
    @Test
    public void GetEventsFail() throws IOException, JSONException {
        LoginUser();
        EventAllRes res = proxy.GetEventsData("fake_token");
        assertNull(res);
    }
    @Test
    public void GetPersonFail() throws IOException, JSONException {
        LoginUser();
        PersonAllRes res = proxy.GetFamilyData("fake_token");
        assertNull(res);
    }

}