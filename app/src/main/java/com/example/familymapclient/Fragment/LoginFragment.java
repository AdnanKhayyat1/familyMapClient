package com.example.familymapclient.Fragment;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.familymapclient.Proxy.Client;
import com.example.familymapclient.Cache.DataCache;
import com.example.familymapclient.Activity.MainActivity;
import com.example.familymapclient.R;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import Models.EventModel;
import Models.PersonModel;
import Request.LoginReq;
import Request.RegisterReq;
import Result.EventAllRes;
import Result.LoginRes;
import Result.PersonAllRes;
import Result.RegisterRes;

public class LoginFragment extends Fragment {
    public static final String LOCAL_HOST = "10.0.2.2";
    public static final String PORT_NUMBER = "8080";

    private DataCache dc;

    private EditText mServerHost;
    private EditText mPort;
    private EditText mUserName;
    private EditText mPassword;
    private EditText mFirstName;
    private EditText mLastName;
    private EditText mEmail;
    private RadioButton mMaleGender;
    private RadioButton mFemaleGender;



    private Button mRegister;
    private Button mLogin;

    String ipAddress;
    String portNum;

    public LoginFragment(){}

    public LoginFragment newInstance(){
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.login_fragment, container, false);
        mServerHost = v.findViewById(R.id.serverHost);
        mPort = v.findViewById(R.id.serverPort);

        dc = DataCache.getInstance();

        mServerHost.setText(LOCAL_HOST);
        mPort.setText(PORT_NUMBER);
        System.out.println(ipAddress);
        mUserName = v.findViewById(R.id.username);
        mPassword = v.findViewById(R.id.password);
        mFirstName = v.findViewById(R.id.firstName);
        mLastName = v.findViewById(R.id.lastName);
        mEmail = v.findViewById(R.id.email);
        mMaleGender = v.findViewById(R.id.maleBtn);
        mFemaleGender = v.findViewById(R.id.FemaleBtn);
        mRegister = v.findViewById(R.id.register);
        mLogin = v.findViewById(R.id.signIn);

        final RadioGroup mRadioGroup = v.findViewById(R.id.gender);

        mLogin.setEnabled(false);
        mRegister.setEnabled(false);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ipAddress = mServerHost.getText().toString();
                portNum = mPort.getText().toString();
                LoginReq req = new LoginReq(mUserName.getText().toString(), mPassword.getText().toString());

                UserTasks task = new UserTasks();
                task.execute(req);
            }
        });
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ipAddress = mServerHost.getText().toString();
                portNum = mPort.getText().toString();
                RegisterReq req = new RegisterReq();
                req.userName = mUserName.getText().toString();
                req.password = mPassword.getText().toString();
                req.email = mEmail.getText().toString();
                req.firstName = mFirstName.getText().toString();
                req.lastName = mLastName.getText().toString();
                int selectedId = mRadioGroup.getCheckedRadioButtonId();
                if(selectedId == 0){
                    req.gender = "m";
                }else{
                    req.gender = "f";
                }
                RegisterTask task = new RegisterTask();
                task.execute(req);
            }
        });
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                LoginChecker();
                RegisterChecker();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
            public void LoginChecker(){
                if(isNotEmpty(mUserName) && isNotEmpty(mPassword) && isNotEmpty(mServerHost) && isNotEmpty(mPort)){
                    mLogin.setEnabled(true);
                }
                else{
                    mLogin.setEnabled(false);
                }
            }
            public void RegisterChecker(){
                if(isNotEmpty(mUserName) && isNotEmpty(mPassword) &&
                        isNotEmpty(mServerHost) && isNotEmpty(mPort) && isNotEmpty(mFirstName)
                            && isNotEmpty(mLastName) && isNotEmpty(mEmail) && eitherGenderIsChecked(mMaleGender, mFemaleGender)){
                    mRegister.setEnabled(true);
                } else{
                    mRegister.setEnabled(false);
                }
            }
        };
        mServerHost.addTextChangedListener(watcher);
        mPort.addTextChangedListener(watcher);
        mUserName.addTextChangedListener(watcher);
        mPassword.addTextChangedListener(watcher);
        mFirstName.addTextChangedListener(watcher);
        mLastName.addTextChangedListener(watcher);
        mEmail.addTextChangedListener(watcher);
        mMaleGender.addTextChangedListener(watcher);
        mFemaleGender.addTextChangedListener(watcher);

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(isNotEmpty(mUserName) && isNotEmpty(mPassword) &&
                        isNotEmpty(mServerHost) && isNotEmpty(mPort) && isNotEmpty(mFirstName)
                        && isNotEmpty(mLastName) && isNotEmpty(mEmail) && eitherGenderIsChecked(mMaleGender, mFemaleGender)){
                    mRegister.setEnabled(true);
                } else{
                    mRegister.setEnabled(false);
                }
            }
        });

        return v;
    }
    private boolean isNotEmpty(EditText text){
        return text.getText().toString().trim().length() != 0;
    }
    private boolean eitherGenderIsChecked(RadioButton male, RadioButton female){
        return male.isChecked() || female.isChecked();
    }

    /**
     * Register user thread
     */
    private class RegisterTask extends AsyncTask<RegisterReq, Integer, RegisterRes>{

        @Override
        protected RegisterRes doInBackground(RegisterReq... registerReqs) {
            Client client = new Client(ipAddress, portNum);
            RegisterRes regRes = null;
            try {
                regRes = client.RegisterUser(registerReqs[0]);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return regRes;
        }

        @Override
        protected void onPostExecute(RegisterRes registerRes) {
            if(registerRes == null){
                FailedRegister();
            }
            else if(!registerRes.isSuccess()){
                FailedRegister();
            }
            else{
                FamilyDataTask task = new FamilyDataTask();
                task.execute(registerRes.getAuthToken(), registerRes.getPersonID());
            }
        }
    }

    /**
     * Login a user
     */
    private class UserTasks extends AsyncTask<LoginReq, Integer, LoginRes>{

        @Override
        protected LoginRes doInBackground(LoginReq... loginReqs) {
            Client client = new Client(ipAddress, portNum);
            LoginRes response = null;
            try {
                response = client.Login(loginReqs[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return response;
        }
        @Override
        protected void onProgressUpdate(Integer... progress) {

        }
        @Override
        protected void onPostExecute(LoginRes result) {
            if(result != null){
                 String authToken = result.getAuthToken();
                 String personID = result.getPersonID();

                FamilyDataTask task = new FamilyDataTask();
                task.execute(authToken, personID);
            }
            else{
                FailedLogin();
            }
        }
    }
    private class EventsDataTask extends AsyncTask<String, Integer, EventAllRes>{
        @Override
        protected EventAllRes doInBackground(String... params) {
            try {
                Client client = new Client(ipAddress, portNum);
                return client.GetEventsData(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(EventAllRes result){
            if(result == null){
                Log.e("LoginFragment", "Could not obtain event data, check EventDataTask asyncTask");
                FailedLogin();
            }
            else{
                List<EventModel> events = result.getData();
                dc.mapEvents(events);
                dc.mapEventsPerPerson();
            }
        }
    }
    private class FamilyDataTask extends AsyncTask<String, Integer, PersonAllRes>{
        private String personID;
        private String auth;
        @Override
        protected PersonAllRes doInBackground(String... params) {
            personID = params[1];
            auth = params[0];
            try{
                Client client = new Client(ipAddress, portNum);
                return client.GetFamilyData(auth);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //[0] -> auth , [1] -> personID
            return null;
        }
        @Override
        protected void onPostExecute(PersonAllRes result){
            if(result == null){
                Log.e("LoginFragment", "Could not obtain family data, check FamilyData asyncTask");
                FailedLogin();
            }
            else{
                PersonModel user = null;
                List<PersonModel> persons = result.getData();
                dc.mapPersons(persons);
                EventsDataTask task = new EventsDataTask();
                task.execute(auth);
                for (int i = 0; i < persons.size(); i++) {
                    if(persons.get(i).getPersonID().equals(personID)){
                        user = persons.get(i);
                        dc.setUser(user);
                        break;
                    }
                }
                SuccessfulLogin(user.getFirstName(), user.getLastName());
            }
        }
    }
    private void FailedRegister(){
        dc.setLoggedIn(false);
        Toast.makeText(getActivity().getApplicationContext(), "Register Failed (username taken/ already exists)", Toast.LENGTH_SHORT).show();
    }
    private void FailedLogin(){
        dc.setLoggedIn(false);
        Toast.makeText(getActivity().getApplicationContext(), "no account for given username", Toast.LENGTH_SHORT).show();
    }
    private void SuccessfulLogin(String first, String last){
        MainActivity activity = (MainActivity) getActivity();
        activity.switchFragments();
        String msg = "Welcome " + first + " " + last;
        Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

}
