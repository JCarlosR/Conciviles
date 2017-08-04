package com.programacionymas.conciviles.ui.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.programacionymas.conciviles.Global;
import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.io.MyApiAdapter;
import com.programacionymas.conciviles.io.response.ProfileResponse;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment implements Callback<ProfileResponse> {

    private EditText etName, etEmail, etRol, etDepartment, etPosition, etLocation;
    private ImageView imageView;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        imageView = (ImageView) v.findViewById(R.id.imageView);

        return v;
    }


    private void fetchProfileData() {
        if (Global.isConnected(getContext())) {
            final int user_id = Global.getIntFromPreferences(getActivity(), "user_id");

            Call<ProfileResponse> call = MyApiAdapter.getApiService().getProfile(user_id);
            call.enqueue(this);
        } else {
            readProfileFromPreferences();
        }
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

            Picasso.with(getContext())
                    .load(profileResponse.getImage())
                    .placeholder(R.drawable.logo)
                    .into(imageView);

            storeProfileInPreferences(profileResponse);
        }
    }

    @Override
    public void onFailure(Call<ProfileResponse> call, Throwable t) {
        Global.showMessageDialog(getContext(), "Error", "Ocurrió un error al obtener sus datos.");
    }

    private void storeProfileInPreferences(ProfileResponse profileResponse) {
        Global.saveStringPreference(getActivity(), "profile_name", profileResponse.getName());
        Global.saveStringPreference(getActivity(), "profile_email", profileResponse.getEmail());
        Global.saveStringPreference(getActivity(), "profile_rol", profileResponse.getRol());
        Global.saveStringPreference(getActivity(), "profile_department", profileResponse.getDepartment());
        Global.saveStringPreference(getActivity(), "profile_position", profileResponse.getPosition());
        Global.saveStringPreference(getActivity(), "profile_location", profileResponse.getLocation());
        Global.saveStringPreference(getActivity(), "profile_image", profileResponse.getImage());
    }

    private void readProfileFromPreferences() {
        final String name = Global.getStringFromPreferences(getActivity(), "profile_name");
        final String email = Global.getStringFromPreferences(getActivity(), "profile_email");
        final String rol = Global.getStringFromPreferences(getActivity(), "profile_rol");
        final String department = Global.getStringFromPreferences(getActivity(), "profile_department");
        final String position = Global.getStringFromPreferences(getActivity(), "profile_position");
        final String location = Global.getStringFromPreferences(getActivity(), "profile_location");
        final String image = Global.getStringFromPreferences(getActivity(), "profile_image");

        etName.setText(name);
        etEmail.setText(email);
        etRol.setText(rol);
        etDepartment.setText(department);
        etPosition.setText(position);
        etLocation.setText(location);

        Picasso.with(getContext()).load(image)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.logo).into(imageView);
    }
}
