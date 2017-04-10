package com.programacionymas.conciviles.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;
import com.programacionymas.conciviles.Global;
import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.io.MyApiAdapter;
import com.programacionymas.conciviles.model.Area;
import com.programacionymas.conciviles.model.CriticalRisk;
import com.programacionymas.conciviles.model.Report;
import com.programacionymas.conciviles.model.User;
import com.programacionymas.conciviles.model.WorkFront;
import com.programacionymas.conciviles.ui.adapter.ReportAdapter;
import com.programacionymas.conciviles.ui.fragment.ReportDialogFragment;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsActivity extends AppCompatActivity implements Callback<ArrayList<Report>>, View.OnClickListener {

    private RecyclerView recyclerView;
    private ReportAdapter adapter;

    private static int inform_id;
    private boolean inform_editable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        inform_id = getIntent().getIntExtra("inform_id", 0);
        inform_editable = getIntent().getBooleanExtra("inform_editable", false);

        setupRecyclerView();

        if (inform_id > 0) {
            reloadReportsByInform();
        }

        final String title = "Informe " + inform_id;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        fetchSpinnerDataFromServer();
    }

    private void fetchSpinnerDataFromServer() {
        final int user_id = Global.getIntFromPreferences(this, "user_id");

        Call<ArrayList<WorkFront>> callWorkFronts = MyApiAdapter.getApiService()
                .getWorkFrontsByLocationOfUser(user_id);

        callWorkFronts.enqueue(new Callback<ArrayList<WorkFront>>() {
            @Override
            public void onResponse(Call<ArrayList<WorkFront>> call, Response<ArrayList<WorkFront>> response) {
                if (response.isSuccessful()) {
                    ArrayList<WorkFront> workFronts = response.body();

                    String workFrontsSerialized = new Gson().toJson(workFronts);
                    Global.saveStringPreference(ReportsActivity.this, "work_fronts", workFrontsSerialized);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<WorkFront>> call, Throwable t) {

            }
        });

        Call<ArrayList<Area>> callAreas = MyApiAdapter.getApiService().getAreas();
        callAreas.enqueue(new Callback<ArrayList<Area>>() {
            @Override
            public void onResponse(Call<ArrayList<Area>> call, Response<ArrayList<Area>> response) {
                if (response.isSuccessful()) {
                    ArrayList<Area> areas = response.body();

                    String areasSerialized = new Gson().toJson(areas);
                    Global.saveStringPreference(ReportsActivity.this, "areas", areasSerialized);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Area>> call, Throwable t) {

            }
        });

        Call<ArrayList<User>> callUsers = MyApiAdapter.getApiService()
                .getUsersByLocationOfUser(user_id);
        callUsers.enqueue(new Callback<ArrayList<User>>() {
            @Override
            public void onResponse(Call<ArrayList<User>> call, Response<ArrayList<User>> response) {
                if (response.isSuccessful()) {
                    ArrayList<User> responsibleUsers = response.body();

                    String responsibleUsersSerialized = new Gson().toJson(responsibleUsers);
                    Global.saveStringPreference(ReportsActivity.this, "responsible_users", responsibleUsersSerialized);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<User>> call, Throwable t) {

            }
        });

        Call<ArrayList<CriticalRisk>> callCriticalRisks = MyApiAdapter.getApiService()
                .getCriticalRisks();
        callCriticalRisks.enqueue(new Callback<ArrayList<CriticalRisk>>() {
            @Override
            public void onResponse(Call<ArrayList<CriticalRisk>> call, Response<ArrayList<CriticalRisk>> response) {
                if (response.isSuccessful()) {
                    ArrayList<CriticalRisk> criticalRisks = response.body();

                    String criticalRiskSerialized = new Gson().toJson(criticalRisks);
                    Global.saveStringPreference(ReportsActivity.this, "critical_risks", criticalRiskSerialized);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<CriticalRisk>> call, Throwable t) {

            }
        });
    }

    public void reloadReportsByInform() {
        Call<ArrayList<Report>> call = MyApiAdapter.getApiService().getReportsByInform(inform_id);
        call.enqueue(this);
    }

    @Override
    public void onBackPressed() {
        // disable the back button device
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

    private void setupRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewReports);

        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ReportAdapter(this, inform_id);
        recyclerView.setAdapter(adapter);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        if (inform_editable) {
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
            {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy)
                {
                    if (dy > 0 || dy < 0 && fab.isShown())
                    {
                        fab.hide();
                    }
                }
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState)
                {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    {
                        fab.show();
                    }
                    super.onScrollStateChanged(recyclerView, newState);
                }
            });

            fab.setOnClickListener(this);
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }


    }

    @Override
    public void onResponse(Call<ArrayList<Report>> call, Response<ArrayList<Report>> response) {
        if (response.isSuccessful()) {
            ArrayList<Report> reports = response.body();
            adapter.setReportsData(reports);
        }
    }

    @Override
    public void onFailure(Call<ArrayList<Report>> call, Throwable t) {
        Global.showMessageDialog(this, "Error", "No se han podido obtener los reportes del informe seleccionado.");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                showDialogNewReport();
                break;
        }
    }

    private void showDialogNewReport() {
        // Log.d("ReportsActivity", "showDialogNewReport fired");

        // Empty report_id => Register new report
        Intent intent = new Intent(this, ReportDialogFragment.class);
        intent.putExtra("inform_id", inform_id);
        intent.putExtra("report_id", 0);
        startActivityForResult(intent, 1); // 1 is the request code
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode==RESULT_OK) {
            reloadReportsByInform();
        }
    }
}
