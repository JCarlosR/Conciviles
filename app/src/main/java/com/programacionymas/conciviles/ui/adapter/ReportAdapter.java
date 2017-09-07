package com.programacionymas.conciviles.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.programacionymas.conciviles.Global;
import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.model.Report;
import com.programacionymas.conciviles.ui.activity.ShowReportActivity;
import com.programacionymas.conciviles.ui.activity.ReportFormActivity;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    private Activity activity;
    private int inform_id;
    private int author_inform_id;
    private boolean inform_editable;

    private ArrayList<Report> reports;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llClosedReport, llButtons;

        TextView tvReportId, tvDescription, tvAuthorAndCreatedAt,
                tvWorkFrontName, tvAreaName, tvResponsibleName,
                tvPlannedDate, tvDeadline, tvOpenedReport;

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

            llClosedReport = (LinearLayout) v.findViewById(R.id.llClosedReport);
            llButtons = (LinearLayout) v.findViewById(R.id.llButtons);
            tvOpenedReport = (TextView) v.findViewById(R.id.tvOpenedReport);

            ivImage = (ImageView) v.findViewById(R.id.ivImage);

            btnShowReport = (Button) v.findViewById(R.id.btnShowReport);
            btnEditReport = (Button) v.findViewById(R.id.btnEditReport);
        }
    }

    public ReportAdapter(Activity activity, final int inform_id, final int author_inform_id, final boolean inform_editable) {
        this.activity = activity;
        this.reports = new ArrayList<>();

        this.inform_id = inform_id;
        this.author_inform_id = author_inform_id;
        this.inform_editable = inform_editable;
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

        final int reportId = currentReport.getId();
        String reportTitle = "REPORTE ";
        if (reportId == 0)
            reportTitle += "OFFLINE";
        else reportTitle += String.valueOf(reportId);

        holder.tvReportId.setText(reportTitle);
        holder.tvDescription.setText(currentReport.getDescription());
        holder.tvAuthorAndCreatedAt.setText(currentReport.getCreatedAt()); // what user_id name
        holder.tvWorkFrontName.setText(currentReport.getWorkFrontName());
        holder.tvAreaName.setText(currentReport.getAreaName());
        holder.tvResponsibleName.setText(currentReport.getResponsibleName());
        holder.tvPlannedDate.setText(currentReport.getPlannedDate());
        holder.tvDeadline.setText(currentReport.getDeadline());

        if (currentReport.getState().equals("Abierto")) {
            holder.tvOpenedReport.setVisibility(View.VISIBLE);
            holder.llClosedReport.setVisibility(View.GONE);
        } else {
            holder.tvOpenedReport.setVisibility(View.GONE);
            holder.llClosedReport.setVisibility(View.VISIBLE);
        }

        // Log.d("ReportAdapter", "isConnected? => " + String.valueOf(Global.isConnected(activity)));

        if (currentReport.wasOfflineEdited() || currentReport.getId() == 0) { // offline edited/created
            // image action only is displayed in ReportActivity (when ONE report was selected in full view)

            // show image from base64 strings if it exists
            String base64 = currentReport.getImageBase64();
            if (base64 != null && !base64.isEmpty())
                holder.ivImage.setImageBitmap(Global.getBitmapFromBase64(base64));
            else
                loadImageUsingPicasso(currentReport, holder);
        } else {
            loadImageUsingPicasso(currentReport, holder);
        }

        if (inform_editable) {
            holder.llButtons.setVisibility(View.VISIBLE);

            holder.btnShowReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(activity, ShowReportActivity.class);
                    intent.putExtra("report", new Gson().toJson(currentReport));
                    intent.putExtra("inform_id", inform_id);
                    activity.startActivity(intent);
                }
            });

            final int authenticated_user_id = Global.getIntFromPreferences(activity, "user_id");
            final boolean is_admin = Global.getBooleanFromPreferences(activity, "is_admin");

            if ( is_admin  || authenticated_user_id == author_inform_id ||
                    authenticated_user_id == currentReport.getUserId() ||
                    authenticated_user_id == currentReport.getResponsibleId() ) {

                holder.btnEditReport.setEnabled(true);
                holder.btnEditReport.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Empty report_id => Register new report
                        Intent intent = new Intent(activity, ReportFormActivity.class);
                        intent.putExtra("inform_id", inform_id);
                        intent.putExtra("_id", currentReport.getRowId());
                        intent.putExtra("report_id", currentReport.getId());
                        // the report has no id assigned, but it will be edited locally
                        if (currentReport.getId() == 0)
                            intent.putExtra("local_edit", true);
                        activity.startActivityForResult(intent, 1); // 1 is the request code
                    }
                });
            } else {
                holder.btnEditReport.setEnabled(false);
                // Global.showMessageDialog(activity, "Alerta", "Solo puedes editar reportes que tú has creado o eres responsable.");
            }
        }


    }

    private void loadImageUsingPicasso(Report report, ViewHolder holder) {
        if (Global.isConnected(activity))
            Picasso.with(activity).load(report.getImage())
                    .placeholder(R.drawable.logo)
                    .into(holder.ivImage);
        else
            Picasso.with(activity).load(report.getImage())
                    .placeholder(R.drawable.logo)
                    .networkPolicy(NetworkPolicy.OFFLINE).into(holder.ivImage);
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }
}