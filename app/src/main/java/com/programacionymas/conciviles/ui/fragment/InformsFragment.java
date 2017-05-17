package com.programacionymas.conciviles.ui.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.programacionymas.conciviles.Global;
import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.io.MyApiAdapter;
import com.programacionymas.conciviles.io.sqlite.MyDbHelper;
import com.programacionymas.conciviles.model.Inform;
import com.programacionymas.conciviles.ui.activity.InformActivity;
import com.programacionymas.conciviles.ui.activity.MenuActivity;
import com.programacionymas.conciviles.ui.adapter.InformAdapter;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InformsFragment extends Fragment {

    private RecyclerView recyclerView;
    private InformAdapter adapter;

    public InformsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_informs, container, false);

        setupRecyclerView(v);
        setupFloatingActionButton(v);

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readInformsFromSQLite();
    }

    private void readInformsFromSQLite() {
        MyDbHelper myDbHelper = new MyDbHelper(getContext());
        adapter.setInformsData(myDbHelper.getInforms());
    }

    private void setupRecyclerView(View v) {
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerViewInforms);

        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new InformAdapter(getContext());
        recyclerView.setAdapter(adapter);

        final FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);

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

    private void setupFloatingActionButton(View v) {
        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (! Global.isConnected(getContext())) {
                    Toast.makeText(getContext(), "El registro de informes solo está disponible de modo online.", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(getContext(), InformActivity.class);
                startActivity(intent);
            }
        });
    }

    // Broadcast receiver

    private BroadcastReceiver updateInformReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            readInformsFromSQLite();
            // optional we can read parameters from intent extras
            // String message = intent.getStringExtra("message");
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        // Register updateInformReceiver to receive messages
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                updateInformReceiver, new IntentFilter("event-update-informs")
        );
    }

    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(updateInformReceiver);

        super.onPause();
    }
}
