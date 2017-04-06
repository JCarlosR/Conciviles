package com.programacionymas.conciviles.ui.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.programacionymas.conciviles.Global;
import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.model.Report;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<Report> reports;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvReportId, tvDescription, tvAuthorAndCreatedAt,
                tvWorkFrontName, tvAreaName, tvResponsibleName,
                tvPlannedDate, tvDeadline, tvState;

        Button btnShowReport, btnEditReport;

        ImageView ivImage;

        public ViewHolder(View v) {
            super(v);

            tvReportId = (TextView) v.findViewById(R.id.tvReportId);
            tvDescription = (TextView) v.findViewById(R.id.tvDescription);
            tvAuthorAndCreatedAt = (TextView) v.findViewById(R.id.tvAuthorAndCreatedAt);
            tvWorkFrontName = (TextView) v.findViewById(R.id.tvWorkFrontName);
            tvAreaName = (TextView) v.findViewById(R.id.tvAreaName);
            tvResponsibleName = (TextView) v.findViewById(R.id.tvResponsibleName);
            tvPlannedDate = (TextView) v.findViewById(R.id.tvPlannedDate);
            tvDeadline = (TextView) v.findViewById(R.id.tvDeadline);
            tvState = (TextView) v.findViewById(R.id.tvState);

            ivImage = (ImageView) v.findViewById(R.id.ivImage);

            btnShowReport = (Button) v.findViewById(R.id.btnShowReport);
            btnEditReport = (Button) v.findViewById(R.id.btnEditReport);
        }
    }

    public ReportAdapter(Activity activity) {
        this.activity = activity;
        this.reports = new ArrayList<>();
    }

    public void setReportsData(ArrayList<Report> reports) {
        this.reports = reports;
        this.notifyDataSetChanged();
    }

    @Override
    public ReportAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Report currentReport = reports.get(position);

        final String reportTitle = "REPORTE " + currentReport.getId();
        holder.tvReportId.setText(reportTitle);
        holder.tvDescription.setText(currentReport.getDescription());
        holder.tvAuthorAndCreatedAt.setText(currentReport.getCreatedAt()); // what user_id name
        holder.tvWorkFrontName.setText(currentReport.getWorkFrontName());
        holder.tvAreaName.setText(currentReport.getAreaName());
        holder.tvResponsibleName.setText(currentReport.getResponsibleName());
        holder.tvPlannedDate.setText(currentReport.getPlannedDate());
        holder.tvDeadline.setText(currentReport.getDeadline());
        holder.tvState.setText(currentReport.getState());

        Picasso.with(activity).load(currentReport.getImage()).into(holder.ivImage);

        holder.btnShowReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        final int authenticated_user_id = Global.getIntFromPreferences(activity, "user_id");

        holder.btnEditReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (authenticated_user_id != currentReport.getUserId()) {
                    Global.showMessageDialog(activity, "Alerta", "Solo puedes editar reportes que tú mismo has creado.");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }
}