package com.programacionymas.conciviles.ui.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.programacionymas.conciviles.Global;
import com.programacionymas.conciviles.R;
import com.programacionymas.conciviles.io.MyApiAdapter;
import com.programacionymas.conciviles.io.response.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements Callback<LoginResponse> {

    private EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Used to debug SQLite dbs
        Stetho.initializeWithDefaults(this);

        if (Global.getIntFromPreferences(this, "user_id") > 0)
            goToMenuActivity();

        setContentView(R.layout.activity_main);

        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);

        // Last used email in login
        etEmail.setText(Global.getStringFromPreferences(this, "login_email"));
    }

    public void onClickLogin(View v)
    {
        final String email = etEmail.getText().toString();
        final String password = etPassword.getText().toString();

        // Store as the last used email for login purposes
        Global.saveStringPreference(this, "login_email", email);

        Call<LoginResponse> call = MyApiAdapter.getApiService().postLogin(email, password);
        call.enqueue(this);
    }

    @Override
    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
        if (response.isSuccessful()) {
            LoginResponse loginResponse = response.body();
            if (loginResponse.isSuccess()) {

                final int user_id = loginResponse.getUserId();
                final boolean is_admin = loginResponse.isAdmin();

                Global.saveIntPreference(this, "user_id", user_id);
                Global.saveBooleanPreference(this, "is_admin", is_admin);

                goToMenuActivity();
            } else {
                Global.showMessageDialog(this, "Alerta", "Los datos ingresados no coinciden con ningún usuario.");
            }
        }
    }

    @Override
    public void onFailure(Call<LoginResponse> call, Throwable t) {
        Toast.makeText(this, "Verifique su conexión a internet", Toast.LENGTH_SHORT).show();
    }

    private void goToMenuActivity() {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}
