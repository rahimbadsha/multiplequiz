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

public class SignupActivity extends AppCompatActivity {

    private EditText name, email, pass, confirmPass;
    private Button signUpB;
    private TextView signinButton;
    private String emailStr, passStr, confirmPassStr, nameStr;
    private TextView dialogText;
    private Dialog progressDailog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        name = findViewById(R.id.signup_fullname_field);
        email = findViewById(R.id.signup_email_field);
        pass = findViewById(R.id.signup_password_field);
        confirmPass = findViewById(R.id.signup_confrim_pass_field);
        signUpB = findViewById(R.id.signup_button);
        signinButton = findViewById(R.id.signup_login_text);

        progressDailog = new Dialog(SignupActivity.this);
        progressDailog.setContentView(R.layout.progressbar_dialog_layout);
        progressDailog.setCancelable(false);
        progressDailog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogText = progressDailog.findViewById(R.id.dialog_progress_text);
        dialogText.setText("Registering user...");

        mAuth = FirebaseAuth.getInstance();

        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        signUpB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validate())
                {
                    signupNewUser();
                }
            }
        });

    }

    private boolean validate()
    {
        nameStr = name.getText().toString().trim();
        passStr = pass.getText().toString().trim();
        emailStr = email.getText().toString().trim();
        confirmPassStr = confirmPass.getText().toString().trim();

        if (nameStr.isEmpty())
        {
            name.setError("Enter Name Here");
            Toast.makeText(SignupActivity.this, "Enter your name here", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (emailStr.isEmpty())
        {
            email.setError("Enter EMail ID");
            Toast.makeText(SignupActivity.this, "Enter valid email here", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (passStr.isEmpty())
        {
            pass.setError("Enter Password");
            Toast.makeText(SignupActivity.this, "Set a valid password", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(passStr.length() < 6)
        {
            Toast.makeText(SignupActivity.this, "Password at least 6 digit", Toast.LENGTH_SHORT).show();
            pass.setError("Check the length of the password");
            return false;
        }
        if (confirmPassStr.isEmpty())
        {
            confirmPass.setError("Confirm your Password again");
            return false;
        }
        if (passStr.compareTo(confirmPassStr) != 0)
        {
            Toast.makeText(SignupActivity.this, "Password Did not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void signupNewUser() {

        progressDailog.show();
        mAuth.createUserWithEmailAndPassword(emailStr, passStr)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Successfully Signup!", Toast.LENGTH_SHORT).show();
                            progressDailog.dismiss();
                            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                            startActivity(intent);
                            SignupActivity.this.finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDailog.dismiss();
                            Toast.makeText(SignupActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
