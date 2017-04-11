package com.programacionymas.conciviles.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
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
import com.programacionymas.conciviles.ui.activity.ReportActivity;
import com.programacionymas.conciviles.ui.fragment.ReportDialogFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    private Activity activity;
    private int inform_id;
    private int author_inform_id;

    private ArrayList<Report> reports;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llClosedReport;

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
            tvOpenedReport = (TextView) v.findViewById(R.id.tvOpenedReport);

            ivImage = (ImageView) v.findViewById(R.id.ivImage);

            btnShowReport = (Button) v.findViewById(R.id.btnShowReport);
            btnEditReport = (Button) v.findViewById(R.id.btnEditReport);
        }
    }

    public ReportAdapter(Activity activity, final int inform_id, final int author_inform_id) {
        this.activity = activity;
        this.reports = new ArrayList<>();

        this.inform_id = inform_id;
        this.author_inform_id = author_inform_id;
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

        if (currentReport.getState().equals("Abierto")) {
            holder.tvOpenedReport.setVisibility(View.VISIBLE);
            holder.llClosedReport.setVisibility(View.GONE);
        } else {
            holder.tvOpenedReport.setVisibility(View.GONE);
            holder.llClosedReport.setVisibility(View.VISIBLE);
        }


        Picasso.with(activity).load(currentReport.getImage()).into(holder.ivImage);

        holder.btnShowReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, ReportActivity.class);
                intent.putExtra("report", new Gson().toJson(currentReport));
                intent.putExtra("inform_id", inform_id);
                activity.startActivity(intent);
            }
        });

        final int authenticated_user_id = Global.getIntFromPreferences(activity, "user_id");

        holder.btnEditReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (authenticated_user_id == currentReport.getUserId() || authenticated_user_id == author_inform_id) {

                    // Empty report_id => Register new report
                    Intent intent = new Intent(activity, ReportDialogFragment.class);
                    intent.putExtra("inform_id", inform_id);
                    intent.putExtra("report_id", currentReport.getId());
                    activity.startActivityForResult(intent, 1); // is just a dummy request code
                } else {
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