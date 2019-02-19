package com.neobit.wingsminer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.neobit.wingsminer.adapters.PlansAdapter;
import com.neobit.wingsminer.helpers.JSONParser;
import com.neobit.wingsminer.helpers.NetworkUtils;

import org.json.JSONObject;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FragmentPlans extends Fragment {

    private String URL;
    private View rootView;
    private View mProgressView;
    private View mContentView;
    private GetJSON jsonTask;

    private String api_key;

    public FragmentPlans(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_recycler2, container, false);
        mProgressView = rootView.findViewById(R.id.progressView);
        mContentView = rootView.findViewById(R.id.contentView);

        URL = getString(R.string.url_plans);
        TextView textSuperior = rootView.findViewById(R.id.textSuperior);
        textSuperior.setVisibility(View.VISIBLE);
        textSuperior.setText(getActivity().getText(R.string.fragment_recycler_label_plans_title));
        if (!NetworkUtils.isConnected(getActivity())) {
            Toast.makeText(getActivity(), R.string.no_conexion, Toast.LENGTH_LONG).show();
            getActivity().onBackPressed();
            return rootView;
        }
        try {
            SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);
            JSONObject usuario = new JSONObject(settings.getString("jsonUsuario", ""));
            api_key = usuario.getString("api_key");

            jsonTask = new GetJSON(URL);
            jsonTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootView;
    }

    public class GetJSON extends AsyncTask<Void, Void, JSONObject> {

        private final String URL;
        private Boolean success = false;

        GetJSON(String URL) {
            this.URL = URL;
            showProgress(true);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject jsonOb = new JSONObject();
            try {
                Log.i("URL", URL);
                JSONParser jParser = new JSONParser();
                jsonOb = jParser.getJSONAuthFromUrl(URL, api_key);
                if (jsonOb.getJSONArray("data").length() > 0) success = true;
                Log.i("DATA", jsonOb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsonOb;
        }

        @Override
        protected void onPostExecute(final JSONObject response) {
            jsonTask = null;
            if (isAdded()) {
                showProgress(false);
                RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
                if (success) {
                    try {
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                        mRecyclerView.setLayoutManager(mLayoutManager);
                        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                        PlansAdapter mAdapter = new PlansAdapter(getActivity(), response.getJSONArray("data"));
                        mRecyclerView.setAdapter(mAdapter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        protected void onCancelled() {
            jsonTask = null;
            showProgress(false);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
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
}