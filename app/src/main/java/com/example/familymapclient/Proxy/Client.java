package com.example.familymapclient.Proxy;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import Request.LoginReq;
import Request.RegisterReq;
import Result.EventAllRes;
import Result.LoginRes;
import Result.PersonAllRes;
import Result.RegisterRes;

public class Client {

    String ipAddress;
    String portNum;
    public Client(String ipAddress, String portNum){
        this.ipAddress = ipAddress;
        this.portNum = portNum;
    }

    public LoginRes Login(LoginReq loginReq) throws JSONException {
        try {

            URL url = new URL("http://" + ipAddress + ":" + portNum + "/user/login");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setDoOutput(true);
            http.setRequestMethod("POST");
            OutputStream req = http.getOutputStream();
            JSONObject json = new JSONObject();
            json.put("userName", loginReq.userName);
            json.put("password", loginReq.password);
            String str = Client.serialize(loginReq);

            req.write(str.getBytes());
            http.connect();
            InputStream res = http.getInputStream();
            String responseString = readString(res);
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                LoginRes resObj = Client.deserialize(responseString,LoginRes.class);
                JSONObject returnJson = new JSONObject(responseString);
                return resObj;
            } else {
                Log.e("Client", "Response is not 200");
                Log.e("Client", "Response is not 200");
            }
        }
        catch(IOException e){
            System.out.println(e.toString());
            Log.e("Client", e.toString());
        }
        return null;
    }
    public RegisterRes RegisterUser(RegisterReq req) throws IOException, JSONException {
        URL url =  new URL("http://" + ipAddress + ":" + portNum + "/user/register");
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("POST");
        String jsonStr = serialize(req);
        http.setDoOutput(true);
        OutputStream os = http.getOutputStream();
        os.write(jsonStr.getBytes());
        http.connect();
        if(http.getResponseCode() == HttpURLConnection.HTTP_OK){
            InputStream res = http.getInputStream();
            String responseString = readString(res);
            return deserialize(responseString, RegisterRes.class);
        }
        else{
            Log.e("Client", "/user/register did not return 200");
        }
        return null;

    }
    public PersonAllRes GetFamilyData(String auth) throws IOException {
        URL url =  new URL("http://" + ipAddress + ":" + portNum + "/person");
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("GET");
        http.setRequestProperty("Authorization", auth);

        http.connect();
        if(http.getResponseCode() == HttpURLConnection.HTTP_OK){
            InputStream res = http.getInputStream();
            String responseString = readString(res);
            return deserialize(responseString, PersonAllRes.class);
        }
        else{
            Log.e("Client", "/persons did not return 200");
        }
        return null;
    }
    public EventAllRes GetEventsData(String auth) throws IOException{
        URL url =  new URL("http://" + ipAddress + ":" + portNum + "/event");
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("GET");
        http.setRequestProperty("Authorization", auth);

        http.connect();
        if(http.getResponseCode() == HttpURLConnection.HTTP_OK){
            InputStream res = http.getInputStream();
            String responseString = readString(res);
            return deserialize(responseString, EventAllRes.class);
        }
        else{
            Log.e("Client", "/event did not return 200");
        }
        return null;
    }
    public void ClearData() throws IOException {
        URL url = new URL("http://" + ipAddress + ":" + portNum + "/clear");
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("POST");
        http.connect();
        if(http.getResponseCode() == HttpURLConnection.HTTP_OK){
            System.out.println("Data cleared");
        }
    }
    private static String readString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader sr = new InputStreamReader(is);
        char[] buf = new char[1024];
        int len;
        while ((len = sr.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }
        return sb.toString();
    }
    public static <T> T deserialize(String value, Class<T> returnType) {
        return (new Gson()).fromJson(value, returnType);
    }
    public static <T> String serialize(T object) {
        return (new Gson()).toJson(object);
    }
}
