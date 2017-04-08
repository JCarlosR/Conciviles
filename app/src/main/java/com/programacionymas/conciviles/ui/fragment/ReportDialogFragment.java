package com.programacionymas.conciviles.ui.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import android.widget.Toast;

import com.programacionymas.conciviles.R;

import java.util.ArrayList;
import java.util.List;

public class ReportDialogFragment extends DialogFragment implements View.OnClickListener {

    private EditText etDescription;
    private EditText etPlannedDate, etDeadline;

    private TextInputLayout tilDescription;

    private ImageButton btnTakeImage, btnTakeImageAction;
    private Spinner spinnerWorkFront, spinnerArea, spinnerReponsible;

    private String report_id;

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

        // TextInputLayout tilPlannedDate = (TextInputLayout) view.findViewById(R.id.tilPlannedDate);
        tilDescription = (TextInputLayout) view.findViewById(R.id.tilDescription);

        etDescription = (EditText) view.findViewById(R.id.etDescription);

        etPlannedDate = (EditText) view.findViewById(R.id.etPlannedDate);
        etPlannedDate.setOnClickListener(this);

        etDeadline = (EditText) view.findViewById(R.id.etDeadline);
        etDeadline.setOnClickListener(this);

        fetchSpinnerDataFromServer();

        return view;
    }

    private void fetchSpinnerDataFromServer() {

    }

    /*private void poblarSpinner(ArrayList<Responsable> responsables) {
        List<String> list = new ArrayList<String>();
        for (Responsable r : responsables) {
            list.add(r.getNombre());
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, list);
        // spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerResponsible.setAdapter(spinnerArrayAdapter);
    }*/

    /*
        private void obtenerDatosResponsables() {
            Call<ResponsableResponse> call = RedemnorteApiAdapter.getApiService().getResponsables();
            call.enqueue(new ResponsablesCallback());
        }

        class ResponsablesCallback implements Callback<ResponsableResponse> {

            @Override
            public void onResponse(Call<ResponsableResponse> call, Response<ResponsableResponse> response) {
                if (response.isSuccessful()) {
                    ResponsableResponse responsableResponse = response.body();
                    if (! responsableResponse.isError()) {
                        poblarSpinnerResponsables(responsableResponse.getResponsables());
                    }
                } else {
                    Toast.makeText(getContext(), "Error en el formato de respuesta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponsableResponse> call, Throwable t) {
                Toast.makeText(getContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    */
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

        // get edit text values

        final String description = etDescription.getText().toString().trim();
        // final String ubicacion = etUbicacion.getText().toString().trim();

        // get spinner values

        // final String responsable = spinnerResponsible.getText().toString().trim();

        // If we have received an ID, we have to edit the data, else, we have to create a new record
  /*      if (report_id.isEmpty()) {
            final String inventariador = Global.getFromSharedPreferences(getActivity(), "username");

            Call<SimpleResponse> call = RedemnorteApiAdapter.getApiService().postRegistrarHoja(
                    id, local, ubicacion, responsable, cargo, oficina,
                    ambiente, area, activo, observacion, inventariador
            );
            call.enqueue(new RegistrarHojaCallback());
        } else {
            Call<SimpleResponse> call = RedemnorteApiAdapter.getApiService().postEditarHoja(
                    id, local, ubicacion, responsable, cargo, oficina,
                    ambiente, area, activo, observacion
            );
            call.enqueue(new EditarHojaCallback());
        }*/
    }
/*
    class RegistrarHojaCallback implements Callback<SimpleResponse> {

        @Override
        public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
            if (response.isSuccessful()) {
                SimpleResponse simpleResponse = response.body();
                if (simpleResponse.isError()) {
                    // Log.d("HeaderDialog", "messageError => " + simpleResponse.getMessage());
                    Toast.makeText(getContext(), simpleResponse.getMessage(), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getContext(), "Se ha registrado una nueva hoja", Toast.LENGTH_SHORT).show();

                    // Re-load the sheets
                    ((PanelActivity) getActivity()).cargarHojas();
                    dismiss();
                }
            } else {
                Toast.makeText(getContext(), "Error en el formato de respuesta", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<SimpleResponse> call, Throwable t) {
            Toast.makeText(getContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    class EditarHojaCallback implements Callback<SimpleResponse> {

        @Override
        public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
            if (response.isSuccessful()) {
                SimpleResponse simpleResponse = response.body();
                if (simpleResponse.isError()) {
                    Toast.makeText(getContext(), simpleResponse.getMessage(), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getContext(), "Se ha editado correctamente la hoja", Toast.LENGTH_SHORT).show();

                    // Re-load the sheets
                    ((PanelActivity) getActivity()).cargarHojas();
                    dismiss();
                }
            } else {
                Toast.makeText(getContext(), "Error en el formato de respuesta", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<SimpleResponse> call, Throwable t) {
            Toast.makeText(getContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchHeaderDataFromServer() {
        Call<HojaResponse> call = RedemnorteApiAdapter.getApiService().getHoja(report_id);
        call.enqueue(new ShowHeaderDataCallback());
    }

    class ShowHeaderDataCallback implements Callback<HojaResponse> {

        @Override
        public void onResponse(Call<HojaResponse> call, Response<HojaResponse> response) {
            if (response.isSuccessful()) {
                HojaResponse hojaResponse = response.body();
                if (hojaResponse.isError()) {
                    Toast.makeText(getContext(), hojaResponse.getMessage(), Toast.LENGTH_SHORT).show();
                }
                else {
                    showHeaderDataInFields(hojaResponse.getHoja());
                }
            } else {
                Toast.makeText(getContext(), "Error en el formato de respuesta", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<HojaResponse> call, Throwable t) {
            Toast.makeText(getContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

        private void showHeaderDataInFields(Hoja hoja) {
            etLocal.setText(hoja.getLocal());
            etUbicacion.setText(hoja.getUbicacion());
            etCargo.setText(hoja.getCargo());
            etOficina.setText(hoja.getOficina());
            etAmbiente.setText(hoja.getAmbiente());
            etArea.setText(hoja.getArea());

            spinnerResponsible.setText(hoja.getResponsable());

            if ( hoja.getActivo().equals("0") ) {
                // active==0 => pendiente
                checkPendiente.setChecked(true);
                // pendiente => show observation field
                tilObservation.setVisibility(View.VISIBLE);
                etObservation.setText(hoja.getObservacion());
            }

            setCheckPendienteOnChangeListener();
        }
    }
*/
}
