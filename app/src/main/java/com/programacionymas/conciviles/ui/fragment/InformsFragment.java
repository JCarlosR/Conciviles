package com.programacionymas.conciviles.ui.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.programacionymas.conciviles.Global;
import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.io.MyApiAdapter;
import com.programacionymas.conciviles.model.Inform;
import com.programacionymas.conciviles.ui.activity.MenuActivity;
import com.programacionymas.conciviles.ui.adapter.InformAdapter;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InformsFragment extends Fragment implements Callback<ArrayList<Inform>> {

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

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final int user_id = Global.getIntFromPreferences(getActivity(), "user_id");
        Call<ArrayList<Inform>> call = MyApiAdapter.getApiService().getInformsByLocationOfUser(user_id);
        call.enqueue(this);
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

    @Override
    public void onResponse(Call<ArrayList<Inform>> call, Response<ArrayList<Inform>> response) {
        if (response.isSuccessful()) {
            ArrayList<Inform> informs = response.body();
            adapter.setInformsData(informs);
        }
    }

    @Override
    public void onFailure(Call<ArrayList<Inform>> call, Throwable t) {
        Global.showMessageDialog(getContext(), "Error", "No se ha podido obtener la lista de informes de su localización.");
    }
}
