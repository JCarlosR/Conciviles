package com.programacionymas.conciviles.ui.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.programacionymas.conciviles.Global;
import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.io.MyApiAdapter;
import com.programacionymas.conciviles.model.Area;
import com.programacionymas.conciviles.model.CriticalRisk;
import com.programacionymas.conciviles.model.User;
import com.programacionymas.conciviles.model.WorkFront;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportDialogFragment extends DialogFragment implements View.OnClickListener {

    private EditText etDescription, etActions, etInspections, etObservations;
    private EditText etPlannedDate, etDeadline;

    private TextInputLayout tilDescription, tilActions, tilInspections, tilObservations;

    private ImageButton btnTakeImage, btnTakeImageAction;
    private Spinner spinnerWorkFront, spinnerArea, spinnerResponsible, spinnerCriticalRisk;

    // Selected report to edit (or empty for new reports)
    private String report_id;

    // Spinner options
    private ArrayList<WorkFront> workFronts;
    private ArrayList<Area> areas;
    private ArrayList<User> responsibles;
    private ArrayList<CriticalRisk> criticalRisks;

    public static ReportDialogFragment newInstance(String report_id) {
        ReportDialogFragment f = new ReportDialogFragment();

        Bundle args = new Bundle();
        args.putString("report_id", report_id);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        report_id = getArguments().getString("report_id");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_report, container, false);

        // etId = (EditText) view.findViewById(R.id.etId);

        String title;
        if (report_id.isEmpty())
            title = "Nuevo reporte";
        else {
            title = "Editar reporte";
            // fetchReportDataFromServer();
        }

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        }
        setHasOptionsMenu(true);

        tilDescription = (TextInputLayout) view.findViewById(R.id.tilDescription);
        tilInspections = (TextInputLayout) view.findViewById(R.id.tilInspections);
        // tilObservations = (TextInputLayout) view.findViewById(R.id.tilObservations);

        etDescription = (EditText) view.findViewById(R.id.etDescription);
        etActions = (EditText) view.findViewById(R.id.etActions);
        etInspections = (EditText) view.findViewById(R.id.etInspections);
        etObservations = (EditText) view.findViewById(R.id.etObservations);

        // date fields references
        etPlannedDate = (EditText) view.findViewById(R.id.etPlannedDate);
        etPlannedDate.setOnClickListener(this);
        etDeadline = (EditText) view.findViewById(R.id.etDeadline);
        etDeadline.setOnClickListener(this);

        // spinner references
        spinnerWorkFront = (Spinner) view.findViewById(R.id.spinnerWorkFront);
        spinnerArea = (Spinner) view.findViewById(R.id.spinnerArea);
        spinnerResponsible = (Spinner) view.findViewById(R.id.spinnerResponsible);
        spinnerCriticalRisk = (Spinner) view.findViewById(R.id.spinnerCriticalRisk);

        // load spinner data
        fetchSpinnerDataFromServer();

        // buttons to capture photos or pick images from gallery
        btnTakeImage = (ImageButton) view.findViewById(R.id.btnTakeImage);
        btnTakeImageAction = (ImageButton) view.findViewById(R.id.btnTakeImageAction);

        return view;
    }

    private void fetchSpinnerDataFromServer() {
        final int user_id = Global.getIntFromPreferences(getActivity(), "user_id");

        Call<ArrayList<WorkFront>> callWorkFronts = MyApiAdapter.getApiService()
                .getWorkFrontsByLocationOfUser(user_id);
        callWorkFronts.enqueue(new Callback<ArrayList<WorkFront>>() {
            @Override
            public void onResponse(Call<ArrayList<WorkFront>> call, Response<ArrayList<WorkFront>> response) {
                if (response.isSuccessful()) {
                    workFronts = response.body();
                    populateWorkFrontSpinner(workFronts);
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
                    areas = response.body();
                    populateAreaSpinner(areas);
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
                    responsibles = response.body();
                    populateResponsibleSpinner(responsibles);
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
                    criticalRisks = response.body();
                    populateCriticalRiskSpinner(criticalRisks);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<CriticalRisk>> call, Throwable t) {

            }
        });
    }

    private void populateWorkFrontSpinner(ArrayList<WorkFront> workFronts) {
        List<String> options = new ArrayList<String>();
        for (WorkFront workFront : workFronts) {
            options.add(workFront.getName());
        }

        populateSpinner(options, spinnerWorkFront);
    }

    private void populateAreaSpinner(ArrayList<Area> areas) {
        List<String> options = new ArrayList<String>();
        for (Area area : areas) {
            options.add(area.getName());
        }

        populateSpinner(options, spinnerArea);
    }

    private void populateResponsibleSpinner(ArrayList<User> users) {
        List<String> options = new ArrayList<String>();
        for (User user : users) {
            options.add(user.getName());
        }

        populateSpinner(options, spinnerResponsible);
    }

    private void populateCriticalRiskSpinner(ArrayList<CriticalRisk> criticalRisks) {
        List<String> options = new ArrayList<String>();
        for (CriticalRisk criticalRisk : criticalRisks) {
            options.add(criticalRisk.getName());
        }

        populateSpinner(options, spinnerCriticalRisk);
    }

    private void populateSpinner(List<String> options, Spinner spinnerTarget) {
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, options);
        spinnerTarget.setAdapter(spinnerArrayAdapter);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.save_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.save) {
            validateForm();
            return true;
        } else if (id == android.R.id.home) {
            getDialog().dismiss();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.etPlannedDate:
                showDatePickerDialog(etPlannedDate);
                break;

            case R.id.etDeadline:
                showDatePickerDialog(etDeadline);
                break;
        }
    }

    private void showDatePickerDialog(final EditText editText) {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because january is zero
                final String selectedDate = twoDigits(day) + " / " + twoDigits(month+1) + " / " + year;
                editText.setText(selectedDate);
            }
        });
        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }

    private String twoDigits(int n) {
        return (n<=9) ? ("0"+n) : String.valueOf(n);
    }

    private boolean validateEditText(EditText editText, TextInputLayout textInputLayout, int errorString) {
        if (editText.getText().toString().trim().isEmpty()) {
            textInputLayout.setError(getString(errorString));
            return false;
        } else {
            textInputLayout.setErrorEnabled(false);
        }

        return true;
    }

    private void validateForm() {
        if (! validateEditText(etDescription, tilDescription, R.string.error_description)) {
            return;
        }

        if (! validateEditText(etInspections, tilInspections, R.string.error_inspections)) {
            return;
        }

        // get edit text values

        final String description = etDescription.getText().toString().trim();
        final String actions = etActions.getText().toString().trim();
        final String inspections = etInspections.getText().toString().trim();
        final String observations = etObservations.getText().toString().trim();

        // get spinner values

        final int workFrontIndex = Global.getSpinnerIndex(spinnerWorkFront);
        final int workFront = workFronts.get(workFrontIndex).getId();

        final int areaIndex = Global.getSpinnerIndex(spinnerArea);
        final int area = areas.get(workFrontIndex).getId();

        final int responsibleIndex = Global.getSpinnerIndex(spinnerResponsible);
        final int responsible = responsibles.get(responsibleIndex).getId();

        final int criticalRiskIndex = Global.getSpinnerIndex(spinnerCriticalRisk);
        final int criticalRisk= criticalRisks.get(criticalRiskIndex).getId();

        // If the report ID is empty, create a new record
        if (report_id.isEmpty()) {
            final int user_id = Global.getIntFromPreferences(getActivity(), "user_id");

            /*Call<SimpleResponse> call = MyApiAdapter.getApiService().postStoreReport(
                    local, ubicacion, responsable, cargo, oficina,
                    ambiente, area, activo, observacion, inventariador
            );
            // call.enqueue(new RegistrarHojaCallback());*/
        } /*else { // edit the selected report
            Call<SimpleResponse> call = MyApiAdapter.getApiService().postUpdateReport(
                    id, local, ubicacion, responsable, cargo, oficina,
                    ambiente, area, activo, observacion
            );
            // call.enqueue(new EditarHojaCallback());
        }*/
    }

}
