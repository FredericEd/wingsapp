package com.neobit.wingsminer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.neobit.wingsminer.helpers.ConnectionDetector;
import com.neobit.wingsminer.helpers.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActivityLogin extends AppCompatActivity {

    private EditText mNameView;
    private EditText mSurnameView;
    private EditText mEmailView;
    private EditText mCityView;
    private EditText mAddressView;
    private EditText mWalletView;
    private View mProgressView;
    private View mContentView;

    private UserRegisterTask mAuthTask = null;
    private GetCountriesJSON countriesTask = null;
    private GetStatesJSON statesTask = null;

    private int pickedCountry = 0, pickedState = 0;
    private JSONArray countries, states;
    private String URL, URL2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContentView = findViewById(R.id.contentView);
        mProgressView = findViewById(R.id.progressView);

        URL = getString(R.string.url_countries);
        URL2 = getString(R.string.url_phones);

        mNameView = (EditText) findViewById(R.id.name);
        mSurnameView = (EditText) findViewById(R.id.lastname);
        mEmailView = (EditText) findViewById(R.id.email);
        mCityView = (EditText) findViewById(R.id.city);
        mAddressView = (EditText) findViewById(R.id.address);
        mWalletView = (EditText) findViewById(R.id.wallet);
        mWalletView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        Button btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        ConnectionDetector cd = new ConnectionDetector(ActivityLogin.this);
        if (!cd.isConnectingToInternet()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityLogin.this);
            builder.setMessage(R.string.no_conexion)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            return;
        }
        countriesTask = new GetCountriesJSON(URL);
        countriesTask.execute();
    }

    private void attemptRegister() {
        try {
            if (mAuthTask != null) return;

            // Reset errors.
            mNameView.setError(null);
            mSurnameView.setError(null);
            mEmailView.setError(null);
            mCityView.setError(null);
            mAddressView.setError(null);
            mWalletView.setError(null);

            String name = mNameView.getText().toString();
            String surname = mSurnameView.getText().toString();
            String email = mEmailView.getText().toString();
            String city = mCityView.getText().toString();
            String address = mAddressView.getText().toString();
            String wallet = mWalletView.getText().toString();

            boolean cancel = false;
            View focusView = null;

            if (!isFieldValid(name)) {
                mNameView.setError(getString(R.string.error_field_required));
                focusView = mNameView;
                cancel = true;
            }
            if (!isFieldValid(surname)) {
                mSurnameView.setError(getString(R.string.error_field_required));
                focusView = mSurnameView;
                cancel = true;
            }
            if (!isFieldValid(city)) {
                mCityView.setError(getString(R.string.error_field_required));
                focusView = mCityView;
                cancel = true;
            }
            if (!isFieldValid(address)) {
                mAddressView.setError(getString(R.string.error_field_required));
                focusView = mAddressView;
                cancel = true;
            }
            if (!isFieldValid(wallet)) {
                mWalletView.setError(getString(R.string.error_field_required));
                focusView = mWalletView;
                cancel = true;
            }
            if (!isFieldValid(email)) {
                mEmailView.setError(getString(R.string.error_field_required));
                focusView = mEmailView;
                cancel = true;
            }
            if (!isEmailValid(email)) {
                mEmailView.setError(getString(R.string.error_invalid_email));
                focusView = mEmailView;
                cancel = true;
            }

            if (cancel) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView.requestFocus();
            } else {
                String androidId = Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                mAuthTask = new UserRegisterTask(URL2, name, surname, email, city, address, wallet, states.getJSONObject(pickedState).getString("id_state"), androidId);
                mAuthTask.execute((Void) null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isFieldValid(String field) {
        return field.length() >= 3;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mContentView.setVisibility(show ? View.GONE : View.VISIBLE);
            mContentView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mContentView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mContentView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    public class UserRegisterTask extends AsyncTask<Void, Void, JSONObject> {

        private final String mURL;
        private final String mNombre;
        private final String mApellido;
        private final String mEmail;
        private final String mAddress;
        private final String mCity;
        private final String mWallet;
        private final String mState;
        private final String mAndroidID;
        private boolean success;

        UserRegisterTask(String URL, String name, String surname, String email, String city, String address, String wallet, String state, String androidID) {
            mURL = URL;
            mNombre = name;
            mApellido = surname;
            mEmail = email;
            mCity = city;
            mAddress = address;
            mWallet = wallet;
            mState = state;
            mAndroidID = androidID;
            showProgress(true);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject jsonOb = new JSONObject();
            try {
                HashMap<String, String> meMap = new HashMap<String, String>();
                meMap.put("name", mNombre);
                meMap.put("last_name", mApellido);
                meMap.put("email", mEmail);
                meMap.put("address", mAddress);
                meMap.put("city", mCity);
                meMap.put("id_state", mState);
                meMap.put("wallet", mWallet);
                meMap.put("uuid", mAndroidID);

                Log.i("URL", mURL);
                JSONParser jParser = new JSONParser();
                jsonOb = jParser.getJSONPOSTFromUrl(mURL, meMap);
                Log.i("response", jsonOb.toString());
                if (jsonOb.getString("error").equals("false")) success = true;
            } catch(Exception e) {
                Log.e(ActivityLogin.this.getResources().getString(R.string.app_name), ActivityLogin.this.getResources().getString(R.string.error_tag), e);
            }
            return jsonOb;
        }

        @Override
        protected void onPostExecute(final JSONObject response) {
            mAuthTask = null;
            showProgress(false);

            try {
                if (success) {
                    Toast.makeText(ActivityLogin.this, response.getString("message"), Toast.LENGTH_LONG).show();
                    SharedPreferences settings = getApplicationContext().getSharedPreferences("MisPreferencias", getApplicationContext().MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("jsonUsuario", response.getJSONObject("data").toString());
                    editor.commit();

                    ActivityLogin.this.finish();
                    Intent mainIntent = new Intent(ActivityLogin.this, ActivityMain.class);
                    ActivityLogin.this.startActivity(mainIntent);
                } else {
                    Snackbar.make(mContentView, response.getString("message"), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            } catch (Exception e) {
                Log.e(ActivityLogin.this.getResources().getString(R.string.app_name), ActivityLogin.this.getResources().getString(R.string.error_tag), e);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public class GetCountriesJSON extends AsyncTask<Void, Void, JSONObject> {

        private final String URL;
        private Boolean success = false;

        GetCountriesJSON(String URL) {
            this.URL = URL;
            showProgress(true);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject jsonOb = new JSONObject();
            try {
                JSONParser jParser = new JSONParser();
                jsonOb = jParser.getJSONFromUrl(URL);
                success = jsonOb.getJSONArray("countries").length() > 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsonOb;
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            countriesTask = null;
            showProgress(false);
            if (success) {
                try {
                    countries = response.getJSONArray("countries");
                    final EditText editCountry = (EditText) findViewById(R.id.country);
                    final List<String> countriesString = new ArrayList<>();
                    for (int x = 0; x < countries.length(); x++) {
                        if (x == 0)
                            editCountry.setText(countries.getJSONObject(x).getString("name"));
                        countriesString.add(countries.getJSONObject(x).getString("name"));
                    }
                    editCountry.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityLogin.this);
                            builder.setTitle(R.string.prompt_country2)
                                    .setSingleChoiceItems(countriesString.toArray(new String[countriesString.size()]), pickedCountry, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int selectedIndex) {
                                            try {
                                                pickedCountry = selectedIndex;
                                                editCountry.setText(countries.getJSONObject(pickedCountry).getString("name"));
                                                statesTask = new GetStatesJSON(URL + "/" + countries.getJSONObject(pickedCountry).getString("id_country") + "/states");
                                                statesTask.execute();
                                            } catch (Exception e) {
                                                Log.e(getResources().getString(R.string.app_name), getResources().getString(R.string.error_tag), e);
                                            }
                                        }
                                    }).setPositiveButton(R.string.ok, null);
                            builder.create().show();
                        }
                    });
                    statesTask = new GetStatesJSON(URL + "/" + countries.getJSONObject(pickedCountry).getString("id_country") + "/states");
                    statesTask.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onCancelled() {
            countriesTask = null;
            showProgress(false);
        }
    }

    public class GetStatesJSON extends AsyncTask<Void, Void, JSONObject> {

        private final String URL;
        private Boolean success = false;

        GetStatesJSON(String URL) {
            this.URL = URL;
            showProgress(true);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject jsonOb = new JSONObject();
            try {
                JSONParser jParser = new JSONParser();
                jsonOb = jParser.getJSONFromUrl(URL);
                success = jsonOb.getJSONArray("states").length() > 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsonOb;
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            statesTask = null;
            showProgress(false);
            if (success) {
                try {
                    states = response.getJSONArray("states");
                    final EditText editState = (EditText) findViewById(R.id.state);
                    final List<String> statesString = new ArrayList<>();
                    for (int x = 0; x < states.length(); x++) {
                        if (x == 0) editState.setText(states.getJSONObject(x).getString("name"));
                        statesString.add(states.getJSONObject(x).getString("name"));
                    }
                    editState.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityLogin.this);
                            builder.setTitle(R.string.prompt_state2)
                                    .setSingleChoiceItems(statesString.toArray(new String[statesString.size()]), pickedState, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int selectedIndex) {
                                            try {
                                                pickedState = selectedIndex;
                                                editState.setText(states.getJSONObject(pickedState).getString("name"));
                                            } catch (Exception e) {
                                                Log.e(getResources().getString(R.string.app_name), getResources().getString(R.string.error_tag), e);
                                            }
                                        }
                                    }).setPositiveButton(R.string.ok, null);
                            builder.create().show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onCancelled() {
            statesTask = null;
            showProgress(false);
        }
    }
}

