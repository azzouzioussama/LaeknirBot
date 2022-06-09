package com.finsday.testfinalproject;

import static com.finsday.testfinalproject.HomeActivity.EMAIL_ROOMMATE;
import static com.finsday.testfinalproject.HomeActivity.MY_IMAGE;
import static com.finsday.testfinalproject.HomeActivity.PICTURE_ROOMMATE;
import static com.finsday.testfinalproject.HomeActivity.USERNAME_ROOMMATE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import pl.droidsonroids.gif.GifImageView;


public class Medical_Chatbot_Activity extends AppCompatActivity {

    private GifImageView pfpMsg;
    private ImageView imgSend;
    private TextView tvMsg;
    private RecyclerView RvMsg;
    private EditText edtMsg;
    private ProgressBar progressBar;
    private ArrayList<Message> messages;
    UtilsModel myUtils ;
    private int waitSec = 0;

    private MessageAdapter adapter;

    private String chatRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_chatbot);
        initiate();
        myUtils = new UtilsModel(Medical_Chatbot_Activity.this);



        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableEditText(edtMsg);

                FirebaseDatabase.getInstance().getReference("Messages/"+chatRoomId).push()
                        .setValue(new Message(FirebaseAuth.getInstance().getCurrentUser().getEmail(),"laeknir",edtMsg.getText().toString()))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()){
                                    Toast.makeText(Medical_Chatbot_Activity.this, "error", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(Medical_Chatbot_Activity.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                sendMsg(edtMsg.getText().toString());
                edtMsg.setText("");
            }
        });

        adapter = new MessageAdapter(messages,getIntent().getStringExtra(MY_IMAGE),Medical_Chatbot_Activity.this);
        Glide.with(Medical_Chatbot_Activity.this).load(R.drawable.bot).centerCrop().error(R.drawable.bot).placeholder(R.drawable.bot).into(pfpMsg);
        RvMsg.setAdapter(adapter);
        RvMsg.setLayoutManager(new LinearLayoutManager(this));

        createRoom();
    }

    private void createRoom(){
        FirebaseDatabase.getInstance().getReference("users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User Me = snapshot.getValue(User.class);

                chatRoomId = Me.getUsername() +"_"+ FirebaseAuth.getInstance().getCurrentUser().getUid();

                attachMessageListener(chatRoomId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void attachMessageListener(String chatRoomId){
        FirebaseDatabase.getInstance().getReference("Messages/"+chatRoomId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    messages.add(dataSnapshot.getValue(Message.class));
                }
                adapter.notifyDataSetChanged();
                RvMsg.scrollToPosition(messages.size()-1);
                RvMsg.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initiate(){
        pfpMsg = findViewById(R.id.pfpMessage);
        imgSend = findViewById(R.id.imgSend);
        tvMsg = findViewById(R.id.tvChattingWith);
        RvMsg = findViewById(R.id.RvMsg);
        edtMsg = findViewById(R.id.edtText);
        progressBar = findViewById(R.id.progressMsg);
        messages = new ArrayList<>();
    }

    private void sendMsg(String msg){
//        SystemClock.sleep(2000);

//        if (UtilsModel.countWords(msg)< 2 && !msg.toLowerCase(Locale.ROOT).equals("hello")&& !msg.toLowerCase(Locale.ROOT).equals("hi")&& !msg.toLowerCase(Locale.ROOT).equals("bye")&& !msg.toLowerCase(Locale.ROOT).equals("thnx")&& !msg.toLowerCase(Locale.ROOT).equals("thanks")){
//
//            FirebaseDatabase.getInstance().getReference("Messages/"+chatRoomId).push()
//                    .setValue(new Message("laeknir",FirebaseAuth.getInstance().getCurrentUser().getEmail(),"i don't understand"));
//            edtMsg.setEnabled(true);
//            return;
//        }

        new Thread(new Runnable() {
            public void run() {
                if (waitSec>0){
                    SystemClock.sleep(2200);
                }
                if (! Python.isStarted()) {
                    Python.start(new AndroidPlatform(Medical_Chatbot_Activity.this));
                }

                Python py = Python.getInstance();
                PyObject pyObject= py.getModule("chatbot");

                PyObject obj  = pyObject.callAttr("BagOfWords", msg);

                ArrayList<Integer> arrayList = myUtils.convertPythonArrToJavaArr(obj.toString());

                float[] arr2 = myUtils.Classifier(arrayList);

//        tvResults.setText(arrayList.toString());


                PyObject obj2  = pyObject.callAttr("chatRoom", Arrays.toString(arr2));
//        textView2.setText(obj2.toString());



                FirebaseDatabase.getInstance().getReference("Messages/"+chatRoomId).push()
                        .setValue(new Message("laeknir",FirebaseAuth.getInstance().getCurrentUser().getEmail(),obj2.toString()))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()){
                                    Toast.makeText(Medical_Chatbot_Activity.this, "error", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(Medical_Chatbot_Activity.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                                }
                                enableEditText(edtMsg);
                                waitSec ++;
                            }
                        });
            }
        }).start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Thread thread = new Thread(){
            public void run() {
                while (!isInterrupted()) {
                    try {
                        Thread.sleep(1000);  //1000ms = 1 sec
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                nMgr.cancelAll();
                            }
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.start();
        Log.d("hi there", "onResume: ");
    }

    private void disableEditText(EditText editText) {
//        editText.setFocusable(false);
        editText.setEnabled(false);
//        editText.setCursorVisible(false);
//        editText.setKeyListener(null);
        editText.setHint("Laeknir is Typing...");
//        editText.setBackgroundColor(Color.TRANSPARENT);
    }
    private void enableEditText(EditText editText) {
//        editText.setFocusable(true);
        editText.setEnabled(true);
//        editText.setCursorVisible(true);
//        editText.setKeyListener(true);
        editText.setHint("Type here...");
    }
}