package com.programacionymas.conciviles.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.model.Inform;
import com.programacionymas.conciviles.ui.activity.ReportsActivity;

import java.util.ArrayList;

public class InformAdapter extends RecyclerView.Adapter<InformAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Inform> informs;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInformId, tvUserName, tvCreatedAt, tvFromDate, tvToDate;
        Button btnGoToReports, btnEditInform;

        public ViewHolder(View v) {
            super(v);

            tvInformId = (TextView) v.findViewById(R.id.tvInformId);
            tvUserName = (TextView) v.findViewById(R.id.tvUserName);
            tvCreatedAt = (TextView) v.findViewById(R.id.tvCreatedAt);
            tvFromDate = (TextView) v.findViewById(R.id.tvFromDate);
            tvToDate = (TextView) v.findViewById(R.id.tvToDate);

            btnGoToReports = (Button) v.findViewById(R.id.btnGoToReports);
            btnEditInform = (Button) v.findViewById(R.id.btnEditInform);
        }
    }

    public InformAdapter(Context context) {
        this.context = context;
        this.informs = new ArrayList<>();
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

        final Inform currentInform = informs.get(position);

        final String informTitle = "INFORME " + currentInform.getId();
        holder.tvInformId.setText(informTitle);
        holder.tvUserName.setText(currentInform.getUserName());
        holder.tvCreatedAt.setText(currentInform.getCreatedAt());
        holder.tvFromDate.setText(currentInform.getFromDate());
        holder.tvToDate.setText(currentInform.getToDate());

        holder.btnGoToReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ReportsActivity.class);
                intent.putExtra("inform_id", currentInform.getId());
                intent.putExtra("inform_editable", currentInform.isEditable());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return informs.size();
    }
}