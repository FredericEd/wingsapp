package com.neobit.wingsminer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import androidx.fragment.app.Fragment;

public class FragmentHome extends Fragment {

    private View rootView;
    private View mProgressView;
    private View mContentView;
    private JSONObject usuario;
    private Handler handler = new Handler();

    public FragmentHome(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mProgressView = rootView.findViewById(R.id.progressView);
        mContentView = rootView.findViewById(R.id.contentView);

        try {
            SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_MULTI_PROCESS);
            usuario = new JSONObject(settings.getString("jsonUsuario", ""));
            TextView textName = (TextView) rootView.findViewById(R.id.textName);
            textName.setText(usuario.getString("name"));
            TextView textSurname = (TextView) rootView.findViewById(R.id.textSurname);
            textSurname.setText(usuario.getString("last_name"));
            TextView textRaised = (TextView) rootView.findViewById(R.id.textRaised);
            textRaised.setText(String.valueOf(new DecimalFormat("#.#######").format(round(Float.parseFloat(settings.getString("total_eth", "0")), 6))) + " ETH");

            TextView textCountry = (TextView) rootView.findViewById(R.id.textCountry);
            textCountry.setText(usuario.getString("city"));
            TextView textState = (TextView) rootView.findViewById(R.id.textState);
            textState.setText(usuario.getJSONObject("state").getString("name"));
            TextView textCity = (TextView) rootView.findViewById(R.id.textCity);
            textCity.setText(usuario.getJSONObject("state").getJSONObject("country").getString("name"));

            TextView textPlan = (TextView) rootView.findViewById(R.id.textPlan);
            textPlan.setText(usuario.getJSONObject("plan").getString("name"));
            TextView textPotency = (TextView) rootView.findViewById(R.id.textPotency);
            textPotency.setText(usuario.getJSONObject("plan").getString("megahash") + " MHs");
            TextView textDaily = (TextView) rootView.findViewById(R.id.textDaily);
            textDaily.setText(usuario.getString("daily") + " ETH");
            TextView text3g = (TextView) rootView.findViewById(R.id.text3g);
            text3g.setText(settings.getInt("3g", 0) == 0 ? R.string.fragment_home_label_deactivated : R.string.fragment_home_label_activated);

            Switch switch3g = (Switch) rootView.findViewById(R.id.switch3g);
            switch3g.setChecked(settings.getInt("3g", 0) == 1);
            switch3g.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_MULTI_PROCESS);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt("3g", isChecked ? 1 : 0);
                    editor.commit();
                }
            });
            handler.post(runnableCode);

            ImageView gif = (ImageView) rootView.findViewById(R.id.gif);
            Glide.with(getActivity())
                    .load(R.drawable.mininggif)
                    .placeholder(R.drawable.logo_fondo)
                    .into(gif);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootView;
    }
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            try {
                if (isAdded()) {
                    SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_MULTI_PROCESS);
                    int blocks = Integer.parseInt(usuario.getJSONObject("plan").getString("blocks"));
                    int periodicity = 3600 * 24 / blocks;
                    int total = settings.getInt("total", 0);

                    //TextView textRaised = (TextView) rootView.findViewById(R.id.textRaised);
                    //textRaised.setText(String.valueOf(total));
                    int[] time = splitToComponentTimes(total * periodicity);
                    TextView textTodayTime = (TextView) rootView.findViewById(R.id.textTodayTime);
                    textTodayTime.setText(time[0] + "h" + (time[1] <= 9 ? "0" : "") + time[1] + "m");// + (time[2] <= 9 ? "0" : "") + time[2]);

                    TextView textTodayEth = (TextView) rootView.findViewById(R.id.textTodayEth);
                    textTodayEth.setText(String.valueOf(new DecimalFormat("#.#######").format(round(Float.parseFloat(usuario.getString("daily")) / blocks * total, 6))) + " ETH");
                    handler.postDelayed(this, periodicity * 1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    private static int[] splitToComponentTimes(long longVal)
    {
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        int[] ints = {hours , mins , secs};
        return ints;
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