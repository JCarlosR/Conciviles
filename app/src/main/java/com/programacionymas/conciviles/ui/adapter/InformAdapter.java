package com.programacionymas.conciviles.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.model.Inform;

import java.util.ArrayList;

public class InformAdapter extends RecyclerView.Adapter<InformAdapter.ViewHolder> {
    private ArrayList<Inform> informs;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInformId, tvUserName, tvCreatedAt, tvFromDate, tvToDate;

        public ViewHolder(View v) {
            super(v);

            tvInformId = (TextView) v.findViewById(R.id.tvInformId);
            tvUserName = (TextView) v.findViewById(R.id.tvUserName);
            tvCreatedAt = (TextView) v.findViewById(R.id.tvCreatedAt);
            tvFromDate = (TextView) v.findViewById(R.id.tvFromDate);
            tvToDate = (TextView) v.findViewById(R.id.tvToDate);
        }
    }

    public InformAdapter() {
        informs = new ArrayList<>();
    }

    public void setInformsData(ArrayList<Inform> informs) {
        this.informs = informs;
        this.notifyDataSetChanged();
    }

    @Override
    public InformAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inform, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Inform currentInform = informs.get(position);
        holder.tvInformId.setText(String.valueOf(currentInform.getId()));
        holder.tvCreatedAt.setText(currentInform.getCreatedAt());
        holder.tvFromDate.setText(currentInform.getFromDate());
        holder.tvToDate.setText(currentInform.getToDate());
    }

    @Override
    public int getItemCount() {
        return informs.size();
    }
}