package com.finsday.testfinalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    private ImageView btnScan,btnCovid,btnVaccine, btnAbout, accountInfo, homeScreenPP;
    private TextView homeScreenUsername;
    private ArrayList<User> users;

    public static final String USERNAME_ROOMMATE="username_of_roommate";
    public static final String EMAIL_ROOMMATE="email_of_roommate";
    public static final String PICTURE_ROOMMATE="picture_of_roommate";
    public static final String MY_IMAGE="my_image";

    private String imgUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Init();
        importUser();
        accountInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this,ProfileActivity.class));
            }
        });

        btnVaccine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this,VaccineActivity.class));
            }
        });

        btnCovid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this,Medical_Chatbot_Activity.class).putExtra(MY_IMAGE,imgUrl));
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ScanningActivity.class));
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, AboutActivity.class));
            }
        });



    }

    private void Init() {
        btnScan = findViewById(R.id.btnScanChat);
        btnCovid = findViewById(R.id.btnCovidChat);
        btnVaccine = findViewById(R.id.btnVaccine);
        btnAbout = findViewById(R.id.btnAbout);
        accountInfo = findViewById(R.id.Account);
        homeScreenPP = findViewById(R.id.homeScreenPP);
        homeScreenUsername = findViewById(R.id.homeScreenUsername);
    }

    private void importUser(){
        FirebaseDatabase.getInstance().getReference("users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(HomeActivity.this)
                        .load(user.getPicture())
                        .error(R.drawable.ic_account)
                        .placeholder(R.drawable.ic_account).into(homeScreenPP);
                homeScreenUsername.setText(user.getUsername());
                imgUrl =  user.getPicture();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}