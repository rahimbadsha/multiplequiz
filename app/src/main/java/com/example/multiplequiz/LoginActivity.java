package com.example.multiplequiz;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText email, pass;
    private Button loginButton;
    private TextView forgotPassB, sighnUpB;

    private FirebaseAuth mAuth;

    private TextView dialogText;
    private Dialog progressDailog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.login_email_field);
        pass = findViewById(R.id.login_password_field);
        loginButton = findViewById(R.id.login_button);
        forgotPassB = findViewById(R.id.login_forgot_password);
        sighnUpB = findViewById(R.id.login_sign_up_text);

        progressDailog = new Dialog(LoginActivity.this);
        progressDailog.setContentView(R.layout.progressbar_dialog_layout);
        progressDailog.setCancelable(false);
        progressDailog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogText = progressDailog.findViewById(R.id.dialog_progress_text);
        dialogText.setText("Registering user...");

        mAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateData())
                {
                    login();
                }
            }
        });

        sighnUpB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void login() {
        String emails = email.getText().toString().trim();
        String passwords = pass.getText().toString().trim();
        progressDailog.show();

        mAuth.signInWithEmailAndPassword(emails, passwords)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(LoginActivity.this, "Successfully Login!", Toast.LENGTH_SHORT).show();

                            progressDailog.dismiss();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            progressDailog.dismiss();
                        }
                    }
                });


    }


    private boolean validateData() {
        if (email.getText().toString().isEmpty())
        {
            email.setError("Enter E-Mail ID");
            return false;
        }
        if (pass.getText().toString().isEmpty())
        {
            pass.setError("Enter Password");
            return false;
        }

        return true;
    }
}
