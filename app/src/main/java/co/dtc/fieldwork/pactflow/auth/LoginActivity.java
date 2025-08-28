package co.dtc.fieldwork.pactflow.auth;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import co.dtc.fieldwork.pactflow.R;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private android.widget.TextView textViewForgotPassword, textViewRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

// Inside onCreate(), after setContentView:
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
//            Window window = getWindow();
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
//        }


        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
        textViewRegister = findViewById(R.id.textViewRegister);

//        buttonLogin.setOnClickListener(v -> {
//            String email = editTextEmail.getText().toString();
//            String password = editTextPassword.getText().toString();
//            if (email.isEmpty() || password.isEmpty()) {
//                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "Login clicked", Toast.LENGTH_SHORT).show();
//
//            }
//        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        textViewForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Forgot password clicked", Toast.LENGTH_SHORT).show()
        );



        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

//        textViewRegister.setOnClickListener(v ->
//                Toast.makeText(this, "Register clicked", Toast.LENGTH_SHORT).show()
//        );

    }
}
