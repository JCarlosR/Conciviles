package com.programacionymas.conciviles.ui.activity;

import android.app.DatePickerDialog;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.programacionymas.conciviles.Global;
import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.io.MyApiAdapter;
import com.programacionymas.conciviles.io.response.NewInformResponse;
import com.programacionymas.conciviles.ui.fragment.DatePickerFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InformActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etStartDate, etEndDate;
    private TextInputLayout tilStartDate, tilEndDate;

    private boolean storing = false;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inform);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        }

        etStartDate = (EditText) findViewById(R.id.etStartDate);
        etStartDate.setOnClickListener(this);

        etEndDate = (EditText) findViewById(R.id.etEndDate);
        etEndDate.setOnClickListener(this);

        tilStartDate = (TextInputLayout) findViewById(R.id.tilStartDate);
        tilEndDate = (TextInputLayout) findViewById(R.id.tilEndDate);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        menu.findItem(R.id.save).setEnabled(!storing);

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.save) {
            validateForm();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.etStartDate:
                showDatePickerDialog(etStartDate);
                break;

            case R.id.etEndDate:
                showDatePickerDialog(etEndDate);
                break;
        }
    }

    private void validateForm() {
        if (! validateEditText(etStartDate, tilStartDate, R.string.error_inform_start)) {
            return;
        }

        if (! validateEditText(etEndDate, tilEndDate, R.string.error_inform_end)) {
            return;
        }

        // perform request to store in the database

        final int user_id = Global.getIntFromPreferences(this, "user_id");
        if (user_id == 0)
            finish();

        final String fromDate = etStartDate.getText().toString().trim();
        final String toDate = etEndDate.getText().toString().trim();

        startStoringState();

        Call<NewInformResponse> call = MyApiAdapter.getApiService().postNewInform(user_id, fromDate, toDate);
        call.enqueue(new Callback<NewInformResponse>() {
            @Override
            public void onResponse(Call<NewInformResponse> call, Response<NewInformResponse> response) {
                String errorMessage = "Ocurrió un error inesperado";
                if (response.isSuccessful()) {
                    NewInformResponse newInformResponse = response.body();
                    if (newInformResponse.isSuccess()) {
                        Toast.makeText(getApplicationContext(), "El informe se ha registrado correctamente!", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    } else {
                        errorMessage = newInformResponse.getFirstError();
                    }
                }

                stopStoringState();
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<NewInformResponse> call, Throwable t) {
                stopStoringState();
                Toast.makeText(getApplicationContext(), "No se ha obtenido respuesta del servidor", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean validateEditText(EditText editText, TextInputLayout textInputLayout, int errorString) {
        if (editText.getText().toString().length() < 1) {
            textInputLayout.setError(getString(errorString));
            return false;
        } else {
            textInputLayout.setErrorEnabled(false);
        }

        return true;
    }

    private void showDatePickerDialog(final EditText editText) {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because january is zero
                final String selectedDate = year + "-" + Global.twoDigits(month+1) + "-" + Global.twoDigits(day);
                editText.setText(selectedDate);
            }
        });
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private void startStoringState() {
        storing = true;
        invalidateOptionsMenu();

        progressBar.setVisibility(View.VISIBLE);
    }

    private void stopStoringState() {
        storing = false;
        invalidateOptionsMenu();

        progressBar.setVisibility(View.GONE);
    }
}
