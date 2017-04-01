package com.programacionymas.conciviles.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

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
                int user_id = loginResponse.getUserId();
                Global.saveIntPreference(this, "user_id", user_id);
                Intent intent = new Intent(this, MenuActivity.class);
                startActivity(intent);
            } else {
                Global.showMessageDialog(this, "Alerta", "Los datos ingresados no coinciden con ningún usuario.");
            }
        }
    }

    @Override
    public void onFailure(Call<LoginResponse> call, Throwable t) {

    }
}
