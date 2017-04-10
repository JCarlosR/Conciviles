package com.programacionymas.conciviles.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.model.Report;
import com.squareup.picasso.Picasso;

public class ReportActivity extends AppCompatActivity {

    private TextView tvDescription, tvAuthorAndCreatedAt,
            tvWorkFrontName, tvAreaName, tvResponsibleName,
            tvPlannedDate, tvDeadline, tvState;

    private TextView tvActions, tvAspect, tvPotential,
            tvInspections, tvCriticalRisk, tvObservations;

    private ImageView ivImage, ivImageAction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        getViewReferences();
        getReportDataFromExtras();
    }

    @Override
    public void onBackPressed() {
        // disable the back button device
    }

    private void getViewReferences() {
        tvDescription = (TextView) findViewById(R.id.tvDescription);
        tvAuthorAndCreatedAt = (TextView) findViewById(R.id.tvAuthorAndCreatedAt);

        tvWorkFrontName = (TextView) findViewById(R.id.tvWorkFrontName);
        tvAreaName = (TextView) findViewById(R.id.tvAreaName);
        tvResponsibleName = (TextView) findViewById(R.id.tvResponsibleName);

        tvPlannedDate = (TextView) findViewById(R.id.tvPlannedDate);
        tvDeadline = (TextView) findViewById(R.id.tvDeadline);
        tvState = (TextView) findViewById(R.id.tvState);

        tvActions = (TextView) findViewById(R.id.tvActions);
        tvAspect = (TextView) findViewById(R.id.tvAspect);
        tvPotential = (TextView) findViewById(R.id.tvPotential);
        tvInspections = (TextView) findViewById(R.id.tvInspections);
        tvCriticalRisk = (TextView) findViewById(R.id.tvCriticalRisk);
        tvObservations = (TextView) findViewById(R.id.tvObservations);

        ivImage = (ImageView) findViewById(R.id.ivImage);
        ivImageAction = (ImageView) findViewById(R.id.ivImageAction);
   }

    private void getReportDataFromExtras() {
        final String reportJson = getIntent().getStringExtra("report");
        final Report report = new Gson().fromJson(reportJson, Report.class);

        setReportIdInActionBar(report.getId());

        Picasso.with(this).load(report.getImage()).into(ivImage);
        Picasso.with(this).load(report.getImageAction()).into(ivImageAction);

        tvDescription.setText(report.getDescription());
        tvAuthorAndCreatedAt.setText(report.getCreatedAt());
        tvWorkFrontName.setText(report.getWorkFrontName());
        tvAreaName.setText(report.getAreaName());
        tvResponsibleName.setText(report.getResponsibleName());
        tvPlannedDate.setText(report.getPlannedDate());
        tvDeadline.setText(report.getDeadline());
        tvState.setText(report.getState());

        tvActions.setText(report.getActions());
        tvAspect.setText(report.getAspect());
        tvPotential.setText(report.getPotential());
        tvInspections.setText(String.valueOf(report.getInspections()));
        tvCriticalRisk.setText(report.getCriticalRisksName());
        tvObservations.setText(report.getObservations());
    }

    private void setReportIdInActionBar(final int report_id) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Reporte " + report_id);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
