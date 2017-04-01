package com.programacionymas.conciviles.ui.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.programacionymas.conciviles.Global;
import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.io.MyApiAdapter;
import com.programacionymas.conciviles.io.response.ProfileResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment implements Callback<ProfileResponse> {

    private EditText etName, etEmail, etRol, etDepartment, etPosition, etLocation;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fetchProfileData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        etName = (EditText) v.findViewById(R.id.etName);
        etEmail = (EditText) v.findViewById(R.id.etEmail);
        etRol = (EditText) v.findViewById(R.id.etRol);
        etDepartment = (EditText) v.findViewById(R.id.etDepartment);
        etPosition = (EditText) v.findViewById(R.id.etPosition);
        etLocation = (EditText) v.findViewById(R.id.etLocation);

        return v;
    }


    private void fetchProfileData() {
        final int user_id = Global.getIntFromPreferences(getActivity(), "user_id");

        Call<ProfileResponse> call = MyApiAdapter.getApiService().getProfile(user_id);
        call.enqueue(this);
    }

    @Override
    public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
        if (response.isSuccessful()) {
            ProfileResponse profileResponse = response.body();
            etName.setText(profileResponse.getName());
            etEmail.setText(profileResponse.getEmail());
            etRol.setText(profileResponse.getRol());
            etDepartment.setText(profileResponse.getDepartment());
            etPosition.setText(profileResponse.getPosition());
            etLocation.setText(profileResponse.getLocation());
        }
    }

    @Override
    public void onFailure(Call<ProfileResponse> call, Throwable t) {
        Global.showMessageDialog(getContext(), "Error", "Ocurrió un error al obtener sus datos.");
    }
}
