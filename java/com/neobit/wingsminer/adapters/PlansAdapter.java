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

import androidx.recyclerview.widget.RecyclerView;

public class PlansAdapter extends RecyclerView.Adapter<PlansAdapter.ViewHolder> {

    JSONArray values;
    Context contexto;


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textName;
        TextView textDescripcion;
        TextView textVelocidad;

        public ViewHolder(View v) {
            super(v);
            textName = v.findViewById(R.id.textName);
            textDescripcion = v.findViewById(R.id.textDescripcion);
            textVelocidad = v.findViewById(R.id.textVelocidad);
        }
    }

    public PlansAdapter(Context mContext, JSONArray values) {
        this.contexto = mContext;
        this.values = values;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_plan, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        try {
            JSONObject temp = values.getJSONObject(position);
            holder.textName.setText(temp.getString("name"));
            holder.textDescripcion.setText(temp.getString("description"));
            holder.textVelocidad.setText(temp.getString("megahash") + " MHs");
            holder.itemView.setTag(temp.toString());
        } catch(Exception e) {
            Log.e(contexto.getResources().getString(R.string.app_name), contexto.getResources().getString(R.string.error_tag), e);
        }
    }

    @Override
    public int getItemCount() {
        return values.length();
    }

    public void updateList (JSONArray items) {
        values = items;
        notifyDataSetChanged();
    }
}