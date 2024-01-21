package com.example.mobile_semantic_image_search_frontend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LogIn extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mAuth = FirebaseAuth.getInstance();
        Activity this_act = this;

        Button btnSignIn = findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String edtSignInEmail = ((TextInputEditText)findViewById(R.id.edtSignInEmail)).getText().toString();
                String edtSignInPassword = ((TextInputEditText)findViewById(R.id.edtSignInPassword)).getText().toString();

                mAuth.signInWithEmailAndPassword(edtSignInEmail, edtSignInPassword)
                        .addOnCompleteListener(this_act, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("SIGNIN", "signInWithEmail:success");
                                    Toast.makeText(getBaseContext(), "Sign in succeeded.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(this_act, MainActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("SIGNIN", "signInWithEmail:failure", task.getException());
                                    Toast.makeText(getBaseContext(), "Sign in failed.", Toast.LENGTH_SHORT).show();
                                    Intent intent = getIntent();
                                    // test UI nen comment lai
//                                    finish();
//                                    startActivity(intent);
                                    finish();
                                    startActivity(new Intent(LogIn.this, MainActivity.class));
                                }
                            }
                        });
            }
        });




        TextView txtSignUp = findViewById(R.id.txtSignUp);

        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LogIn.this, SignUp.class);
                startActivity(intent);
                finish();
            }
        });
    }
}