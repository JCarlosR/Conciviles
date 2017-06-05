package com.programacionymas.conciviles.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.programacionymas.conciviles.Global;
import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.io.MyApiAdapter;
import com.programacionymas.conciviles.io.sqlite.MyDbHelper;
import com.programacionymas.conciviles.model.Area;
import com.programacionymas.conciviles.model.CriticalRisk;
import com.programacionymas.conciviles.model.Report;
import com.programacionymas.conciviles.model.User;
import com.programacionymas.conciviles.model.WorkFront;
import com.programacionymas.conciviles.ui.adapter.ReportAdapter;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView recyclerView;
    private ReportAdapter adapter;

    private static int inform_id;
    private int author_inform_id;
    private boolean inform_editable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        inform_id = getIntent().getIntExtra("inform_id", 0);
        author_inform_id = getIntent().getIntExtra("author_inform_id", 0);
        inform_editable = getIntent().getBooleanExtra("inform_editable", false);

        setupRecyclerView();

        if (inform_id > 0) {
            reloadReportsForThisInformFromSQLite();
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

    public void reloadReportsForThisInformFromSQLite() {
        MyDbHelper myDbHelper = new MyDbHelper(this);
        ArrayList<Report> reports = myDbHelper.getReports(inform_id);
        if (reports.size() > 0)
            adapter.setReportsData(reports);
        else
            Global.showMessageDialog(this, "Error", "No se han podido obtener los reportes del informe seleccionado.");
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

        adapter = new ReportAdapter(this, inform_id, author_inform_id, inform_editable);
        recyclerView.setAdapter(adapter);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final int user_id = Global.getIntFromPreferences(this, "user_id");

        // only the on duty can add new reports to the inform (if it is active)
        if (inform_editable && author_inform_id == user_id) {
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                showDialogNewReport();
                break;
        }
    }

    private void showDialogNewReport() {

        // Empty report_id => Register new report
        Intent intent = new Intent(this, ReportFormActivity.class);
        intent.putExtra("inform_id", inform_id);
        intent.putExtra("report_id", 0);
        startActivityForResult(intent, 1); // 1 is the request code
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (Global.isConnected(this)) {
                // get updated data from the API
                // this method will update the UI when the data is ready
                getReportsForThisInformFromApi();
            } else {
                // fetch data from SQLite instantly (because it was registered in db locally)
                reloadReportsForThisInformFromSQLite();
            }
        }
    }

    private void getReportsForThisInformFromApi() {
        Call<ArrayList<Report>> call = MyApiAdapter.getApiService().getReportsByInform(inform_id);
        call.enqueue(new Callback<ArrayList<Report>>() {
            @Override
            public void onResponse(Call<ArrayList<Report>> call, Response<ArrayList<Report>> response) {
                if (response.isSuccessful()) {
                    ArrayList<Report> reports = response.body();

                    final MyDbHelper myHelper = new MyDbHelper(getApplicationContext());
                    myHelper.updateReportsForInform(reports, inform_id);

                    reloadReportsForThisInformFromSQLite();
                }
            }
            @Override
            public void onFailure(Call<ArrayList<Report>> call, Throwable t) {
                Toast.makeText(ReportsActivity.this, "No se han podido actualizar los reportes del informe actual.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Broadcast receiver

    private BroadcastReceiver updateReportsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int updated_inform_id = intent.getIntExtra("inform_id", 0);
            if (updated_inform_id == 0) return; // it should never happen

            if (updated_inform_id == inform_id) {
                // update the recyclerView only if the user is viewing the updated reports
                reloadReportsForThisInformFromSQLite();
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        // Register updateReportsReceiver to receive messages
        LocalBroadcastManager.getInstance(this).registerReceiver(
                updateReportsReceiver, new IntentFilter("event-update-reports")
        );
    }

    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReportsReceiver);

        super.onPause();
    }
}
