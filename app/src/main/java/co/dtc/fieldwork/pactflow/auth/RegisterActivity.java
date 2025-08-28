package co.dtc.fieldwork.pactflow.auth;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import co.dtc.fieldwork.pactflow.HomeActivity;
import co.dtc.fieldwork.pactflow.R;

public class RegisterActivity extends AppCompatActivity {

    private android.widget.TextView textViewLogin;
    private Button buttonReg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        textViewLogin = findViewById(R.id.textViewLogin);
        buttonReg = findViewById(R.id.buttonRegister);


        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });


        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
