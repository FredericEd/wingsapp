package com.neobit.wingsminer.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.neobit.wingsminer.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;

import androidx.recyclerview.widget.RecyclerView;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    JSONArray values;
    Context contexto;


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textFecha;
        TextView textAmount;
        TextView textTime;

        public ViewHolder(View v) {
            super(v);
            textFecha = v.findViewById(R.id.textFecha);
            textAmount = v.findViewById(R.id.textAmount);
            textTime = v.findViewById(R.id.textTime);
        }
    }

    public HistoryAdapter(Context mContext, JSONArray values) {
        this.contexto = mContext;
        this.values = values;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_history, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        try {
            JSONObject temp = values.getJSONObject(position);
            String[] fecha = temp.getString("created_at").split(" ")[0].split("-");
            int blocks = Integer.parseInt(temp.getJSONObject("plan").getString("blocks"));
            int periodicity = 3600 * 24 / blocks;
            int total = Integer.valueOf(temp.getString("total"));
            int[] time = splitToComponentTimes(total * periodicity);
            holder.textFecha.setText(String.valueOf(position + 1));//fecha[0] + "/" + fecha[1] + "/" + fecha[2]);
            holder.textTime.setText(time[0] + "h" + (time[1] <= 9 ? "0" : "") + time[1] + "m");
            holder.textAmount.setText(String.valueOf(new DecimalFormat("#.#######").format(Double.parseDouble(temp.getString("daily_payment")))) + " ETH");
            holder.itemView.setTag(temp.toString());
        } catch(Exception e) {
            Log.e(contexto.getResources().getString(R.string.app_name), contexto.getResources().getString(R.string.error_tag), e);
        }
    }

    @Override
    public int getItemCount() {
        return values.length();
    }

    private static int[] splitToComponentTimes(long longVal) {
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        int[] ints = {hours , mins , secs};
        return ints;
    }

    public void updateList (JSONArray items) {
        values = items;
        notifyDataSetChanged();
    }
}