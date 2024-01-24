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

import com.example.mobile_semantic_image_search_frontend.DAO.UserDAO;
import com.example.mobile_semantic_image_search_frontend.Object.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class SignUp extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users");
        String userID = userRef.push().getKey();
        Activity this_act = this;

        Button btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String edtSignUpEmail = ((TextInputEditText)findViewById(R.id.edtSignUpEmail)).getText().toString();
                String edtSignUpPassword = ((TextInputEditText)findViewById(R.id.edtSignUpPassword)).getText().toString();
                String edtSignUpConfirmPassword = ((TextInputEditText)findViewById(R.id.edtSignUpConfirmPassword)).getText().toString();
                String userPhone = ((TextInputEditText)findViewById(R.id.edtSignUpMobile)).getText().toString();
                String userName = ((TextInputEditText)findViewById(R.id.edtSignUpFullName)).getText().toString();
                System.out.println(edtSignUpEmail + " " + edtSignUpPassword + " " + edtSignUpConfirmPassword);
                System.out.println(userPhone + " " + userName );

                if(!edtSignUpPassword.equals(edtSignUpConfirmPassword)){
                    CharSequence text = "Password not match!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(getBaseContext(), text, duration);
                    toast.show();
                    return;
                }
                if(edtSignUpPassword.length() < 6){
                    CharSequence text = "Password must be at least 6 characters!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(getBaseContext(), text, duration);
                    toast.show();
                    return;
                }
                mAuth.createUserWithEmailAndPassword(edtSignUpEmail, edtSignUpPassword)
                        .addOnCompleteListener(this_act, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("SIGNUP", "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (userID != null) {
                                        User m_user = new User(user.getUid(),userPhone, userName);
                                        userRef.child(userID).setValue(m_user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Intent intent = new Intent(SignUp.this,LogIn.class);
                                                        CharSequence text = "Sign up successfully! Please log in!";
                                                        int duration = Toast.LENGTH_SHORT;

                                                        Toast toast = Toast.makeText(getBaseContext(), text, duration);
                                                        toast.show();
                                                        finish();
                                                        startActivity(intent);
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        CharSequence text = "Sign up user failed!";
                                                        int duration = Toast.LENGTH_SHORT;

                                                        Toast toast = Toast.makeText(getBaseContext(), text, duration);
                                                        toast.show();
                                                        UserDAO.deleteUser(user);
                                                    }
                                                });;
                                    } else {
                                        UserDAO.deleteUser(user);
                                        Log.e("ADDUSER", "Failed to generate userID");
                                    }
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.e("SIGNUP", "createUserWithEmail:failure", task.getException());
                                    CharSequence text = "Sign up failed!" + task.getException().getMessage();
                                    int duration = Toast.LENGTH_SHORT;
                                    Toast toast = Toast.makeText(getBaseContext(), text, duration);
                                    toast.show();
                                    return;

                                }
                            }


                        });
            }
        });


        TextView txtSignIn = findViewById(R.id.txtSignIn);
        txtSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUp.this,LogIn.class);
                startActivity(intent);
                finish();
            }
        });
    }
}