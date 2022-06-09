package com.finsday.testfinalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private EditText edtUserName,edtEmail,edtPassword,edtConfirmPassword;
    private TextInputLayout userName,confirmPassword;
    private Button btnSubmit;
    private TextView btnSwitchLogIn,btnSwitchSignUp;
    private ImageView botImg;
    private boolean isSubmit,clicked;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initiate();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this,HomeActivity.class));
            finish();
        }

        changeUiElements();

        mAuth = FirebaseAuth.getInstance();

        btnSwitchLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!clicked) {
                    changeUiElements();
                    clicked= true;
                }
            }
        });
        btnSwitchSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clicked) {
                    changeUiElements();
                    clicked= false;
                }
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtEmail.getText().toString().isEmpty() || edtPassword.getText().toString().isEmpty()){
                    if (isSubmit && edtUserName.getText().toString().isEmpty()){
                        Toast.makeText(MainActivity.this, "Insufficient Inputs, make sure all information are inserted ", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Toast.makeText(MainActivity.this, "Insufficient Inputs, make sure all information are inserted ", Toast.LENGTH_LONG).show();
                    return;
                }
                if (isSubmit) {
                    if (!edtPassword.getText().toString().equals(edtConfirmPassword.getText().toString())){
                        Toast.makeText(MainActivity.this, "Make sure that you entered the right Password", Toast.LENGTH_LONG).show();
                        return;
                    }
                    forSignUp();
                }else{
                    forLogIn();
                }
                SystemClock.sleep(1000);
                finish();
            }
        });


    }

    private void forSignUp() {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "Signed up successfully!", Toast.LENGTH_SHORT).show();
                            FirebaseDatabase.getInstance().getReference("users/"+FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(new User(edtUserName.getText().toString(),edtEmail.getText().toString(),"",""));
                            startActivity(new Intent(MainActivity.this,HomeActivity.class));
                        }else {
                            Toast.makeText(MainActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void forLogIn() {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Logged In Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this,HomeActivity.class));
                        }else{
                            Toast.makeText(MainActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initiate(){
        edtUserName = findViewById(R.id.edtUserName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnSubmit = findViewById(R.id.Submit);
        btnSwitchLogIn = findViewById(R.id.btnSwitchLogIn);
        btnSwitchSignUp = findViewById(R.id.btnSwitchSignUp);
        userName = findViewById(R.id.userName);
        confirmPassword = findViewById(R.id.confirmPassword);
        botImg = findViewById(R.id.botImg);
        isSubmit=false;
        clicked = false;
    }

    private void changeUiElements(){
        if (!isSubmit) {
            edtUserName.setVisibility(View.VISIBLE);
            userName.setVisibility(View.VISIBLE);
            edtConfirmPassword.setVisibility(View.VISIBLE);
            confirmPassword.setVisibility(View.VISIBLE);
            btnSwitchLogIn.setBackground(AppCompatResources.getDrawable(MainActivity.this,R.drawable.path_3));
            btnSwitchLogIn.setTextColor(getColor(R.color.secondaryColor));
            btnSwitchSignUp.setBackground(AppCompatResources.getDrawable(MainActivity.this,R.drawable.path_6));
            btnSwitchSignUp.setTextColor(getColor(R.color.white));
            btnSwitchSignUp.setElevation(5.0f);
            btnSwitchLogIn.setElevation(0.0f);
            botImg.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this,R.drawable.image_2));
            btnSubmit.setText("Sign Up");
            isSubmit = true;
        }else{
            edtConfirmPassword.setVisibility(View.GONE);
            confirmPassword.setVisibility(View.GONE);
            edtUserName.setVisibility(View.GONE);
            userName.setVisibility(View.GONE);
            btnSwitchLogIn.setBackground(AppCompatResources.getDrawable(MainActivity.this,R.drawable.path_6));
            btnSwitchLogIn.setTextColor(getColor(R.color.white));
            btnSwitchSignUp.setBackground(AppCompatResources.getDrawable(MainActivity.this,R.drawable.path_3));
            btnSwitchSignUp.setTextColor(getColor(R.color.secondaryColor));
            btnSwitchSignUp.setElevation(0.0f);
            btnSwitchLogIn.setElevation(5.0f);
            botImg.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this,R.drawable.image_1));
            btnSubmit.setText("Log In");
            isSubmit = false;
        }
    }



}