package com.programacionymas.conciviles.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.programacionymas.conciviles.Global;
import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.model.Report;
import com.squareup.picasso.Picasso;

public class ShowReportActivity extends AppCompatActivity {

    private TextView tvDescription, tvAuthorAndCreatedAt,
            tvWorkFrontName, tvAreaName, tvResponsibleName,
            tvPlannedDate, tvDeadline, tvState;

    private TextView tvActions, tvAspect, tvPotential,
            tvInspections, tvCriticalRisk, tvObservations;

    private ImageView ivImage, ivImageAction;

    private FloatingActionButton fab;

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

        fab = (FloatingActionButton) findViewById(R.id.fab);
   }

    private void getReportDataFromExtras() {
        final String reportJson = getIntent().getStringExtra("report");
        final int inform_id = getIntent().getIntExtra("inform_id",0);

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

        final int authenticated_user_id = Global.getIntFromPreferences(this, "user_id");
        final boolean is_admin = Global.getBooleanFromPreferences(this, "is_admin");

        if (is_admin || authenticated_user_id == inform_id ||
                authenticated_user_id == report.getUserId() ||
                authenticated_user_id == report.getResponsibleId()) {

            NestedScrollView nestedScrollView = (NestedScrollView) findViewById(R.id.nestedScrollView);
            nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (scrollY > oldScrollY) {
                        fab.hide();
                    } else {
                        fab.show();
                    }
                }
            });

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Empty report_id => Register new report
                    Intent intent = new Intent(getApplicationContext(), ReportFormActivity.class);
                    intent.putExtra("inform_id", inform_id);
                    intent.putExtra("_id", report.getRowId());
                    intent.putExtra("report_id", report.getId());
                    // the report has no id assigned, but it will be edited locally
                    if (report.getId() == 0)
                        intent.putExtra("local_edit", true);
                    startActivity(intent);
                }
            });

        } else {
            fab.setVisibility(View.GONE);
        }
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
