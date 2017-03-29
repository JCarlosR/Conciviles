package com.programacionymas.conciviles;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
    }

    public void onClickLogin(View v)
    {
        final String email = etEmail.getText().toString();
        final String password = etPassword.getText().toString();

        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }
}
