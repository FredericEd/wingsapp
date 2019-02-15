package com.neobit.wingsminer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.neobit.wingsminer.helpers.ConnectionDetector;
import com.neobit.wingsminer.helpers.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class FragmentUpdate extends Fragment {

    private EditText mNameView;
    private EditText mSurnameView;
    private EditText mEmailView;
    private EditText mCityView;
    private EditText mAddressView;
    private EditText mWalletView;
    private View rootView;
    private View mProgressView;
    private View mContentView;

    private UserRegisterTask mAuthTask = null;
    private GetCountriesJSON countriesTask = null;
    private GetStatesJSON statesTask = null;
    private JSONObject usuario;

    private int pickedCountry = 0, pickedState = 0;
    private JSONArray countries, states;
    private String URL, URL2, api_key;

    public FragmentUpdate(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_login, container, false);
        mContentView = rootView.findViewById(R.id.contentView);
        mProgressView = rootView.findViewById(R.id.progressView);

        URL = getString(R.string.url_countries);
        URL2 = getString(R.string.url_phones);

        mNameView = (EditText) rootView.findViewById(R.id.name);
        mSurnameView = (EditText) rootView.findViewById(R.id.lastname);
        mEmailView = (EditText) rootView.findViewById(R.id.email);
        mCityView = (EditText) rootView.findViewById(R.id.city);
        mAddressView = (EditText) rootView.findViewById(R.id.address);
        mWalletView = (EditText) rootView.findViewById(R.id.wallet);
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
        try {
            ConnectionDetector cd = new ConnectionDetector(getActivity());
            if (!cd.isConnectingToInternet()) {
                Toast.makeText(getActivity(), R.string.no_conexion, Toast.LENGTH_LONG).show();
                getActivity().onBackPressed();
            }

            SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);
            usuario = new JSONObject(settings.getString("jsonUsuario", ""));
            api_key = usuario.getString("api_key");
            mNameView.setText(usuario.getString("name"));
            mSurnameView.setText(usuario.getString("last_name"));
            mEmailView.setText(usuario.getString("email"));
            mCityView.setText(usuario.getString("city"));
            mAddressView.setText(usuario.getString("address"));
            mWalletView.setText(usuario.getString("wallet"));

            Button btnSave = (Button) rootView.findViewById(R.id.btnSave);
            btnSave.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptRegister();
                }
            });
            countriesTask = new GetCountriesJSON(URL);
            countriesTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  rootView;
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
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                mAuthTask = new UserRegisterTask(URL2, name, surname, email, city, address, wallet, states.getJSONObject(pickedState).getString("id_state"));
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
        private boolean success;

        UserRegisterTask(String URL, String name, String surname, String email, String city, String address, String wallet, String state) {
            mURL = URL;
            mNombre = name;
            mApellido = surname;
            mEmail = email;
            mCity = city;
            mAddress = address;
            mWallet = wallet;
            mState = state;
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

                Log.i("URL", mURL);
                JSONParser jParser = new JSONParser();
                jsonOb = jParser.getJSONPUTAuthFromUrl(mURL, meMap, api_key);
                Log.i("response", jsonOb.toString());
                if (jsonOb.getString("error").equals("false")) success = true;
            } catch(Exception e) {
                Log.e(FragmentUpdate.this.getResources().getString(R.string.app_name), FragmentUpdate.this.getResources().getString(R.string.error_tag), e);
            }
            return jsonOb;
        }

        @Override
        protected void onPostExecute(final JSONObject response) {
            mAuthTask = null;
            showProgress(false);

            try {
                if (success) {
                    Toast.makeText(getActivity(), response.getString("message"), Toast.LENGTH_LONG).show();
                    SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias",getActivity().MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("jsonUsuario", response.getJSONObject("data").toString());
                    editor.commit();
                    getActivity().onBackPressed();
                } else {
                    Snackbar.make(mContentView, response.getString("message"), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            } catch (Exception e) {
                Log.e(FragmentUpdate.this.getResources().getString(R.string.app_name), FragmentUpdate.this.getResources().getString(R.string.error_tag), e);
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
                    final EditText editCountry = (EditText) rootView.findViewById(R.id.country);
                    final List<String> countriesString = new ArrayList<>();
                    for (int x = 0; x < countries.length(); x++) {
                        if (countries.getJSONObject(x).getString("id_country").equals(usuario.getJSONObject("state").getJSONObject("country").getString("id_country"))) {
                            pickedCountry = x;
                            editCountry.setText(countries.getJSONObject(x).getString("name"));
                        }
                        countriesString.add(countries.getJSONObject(x).getString("name"));
                    }
                    editCountry.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                    final EditText editState = (EditText) rootView.findViewById(R.id.state);
                    final List<String> statesString = new ArrayList<>();
                    for (int x = 0; x < states.length(); x++) {
                        if (states.getJSONObject(x).getString("id_state").equals(usuario.getJSONObject("state").getString("id_state"))) {
                            pickedState = x;
                            editState.setText(states.getJSONObject(x).getString("name"));
                        }
                        statesString.add(states.getJSONObject(x).getString("name"));
                    }
                    editState.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

    public void backPressed () {
        Fragment fragment = new FragmentProfile();
        FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
        fragTransaction.replace(R.id.frame_container, fragment);
        fragTransaction.commit();
    }
}

