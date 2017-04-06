package com.programacionymas.conciviles.ui.activity;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.programacionymas.conciviles.Global;
import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.io.MyApiAdapter;
import com.programacionymas.conciviles.model.Report;
import com.programacionymas.conciviles.ui.adapter.InformAdapter;
import com.programacionymas.conciviles.ui.adapter.ReportAdapter;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsActivity extends AppCompatActivity implements Callback<ArrayList<Report>> {

    private RecyclerView recyclerView;
    private ReportAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        setupRecyclerView();

        final int inform_id = getIntent().getIntExtra("inform_id", 0);

        if (inform_id > 0) {
            Call<ArrayList<Report>> call = MyApiAdapter.getApiService().getReportsByInform(inform_id);
            call.enqueue(this);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Informe " + inform_id);
        }

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

        adapter = new ReportAdapter(this);
        recyclerView.setAdapter(adapter);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

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
}
