package com.neobit.wingsminer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.neobit.wingsminer.helpers.NetworkUtils;

import org.json.JSONObject;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class FragmentProfile extends Fragment {

    private View rootView;
    private View mProgressView;
    private View mContentView;

    public FragmentProfile(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        mProgressView = rootView.findViewById(R.id.progressView);
        mContentView = rootView.findViewById(R.id.contentView);

        try {
            SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);
            JSONObject usuario = new JSONObject(settings.getString("jsonUsuario", ""));
            TextView textName = (TextView) rootView.findViewById(R.id.textName);
            textName.setText(usuario.getString("name"));
            TextView textSurname = (TextView) rootView.findViewById(R.id.textSurname);
            textSurname.setText(usuario.getString("last_name"));
            TextView textPlan = (TextView) rootView.findViewById(R.id.textPlan);
            textPlan.setText(usuario.getJSONObject("plan").getString("name"));
            TextView textEmail = (TextView) rootView.findViewById(R.id.textEmail);
            textEmail.setText(usuario.getString("email"));
            TextView textAddress = (TextView) rootView.findViewById(R.id.textAddress);
            textAddress.setText(usuario.getString("address"));
            TextView textCountry = (TextView) rootView.findViewById(R.id.textCountry);
            textCountry.setText(usuario.getJSONObject("state").getJSONObject("country").getString("name"));
            TextView textState = (TextView) rootView.findViewById(R.id.textState);
            textState.setText(usuario.getJSONObject("state").getString("name"));
            TextView textCity = (TextView) rootView.findViewById(R.id.textCity);
            textCity.setText(usuario.getString("city"));

            Button btnProfile = (Button) rootView.findViewById(R.id.btnProfile);
            btnProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!NetworkUtils.isConnected(getActivity())) {
                        Toast.makeText(getActivity(), R.string.no_conexion, Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        Fragment fragment = new FragmentUpdate();
                        FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                        fragTransaction.replace(R.id.frame_container, fragment);
                        fragTransaction.commit();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootView;
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
}