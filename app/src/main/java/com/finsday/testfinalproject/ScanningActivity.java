package com.finsday.testfinalproject;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.finsday.testfinalproject.ml.MedicalDiagnosisCnnModelLast;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ScanningActivity extends AppCompatActivity {
    private Button btn_TakePicture,btn_GetPicture,btn_Predict;
    private ImageView imgXray;
    private TextView tvResult;

    public static final int REQUEST_IMAGE_CAPTURE = 10;
    private Uri uri;
    private int imageSize = 256;
    private int checkImageImported = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);
        Initiate();

        btn_TakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickCamera(0);
            }
        });
        btn_GetPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickCamera(1);
            }
        });

        btn_Predict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkImageImported !=0){
                    classify();
                }
            }
        });

        
    }

    private void Initiate(){
        btn_TakePicture = findViewById(R.id.btn_TakePicture);
        btn_GetPicture = findViewById(R.id.btn_GetPicture);
        btn_Predict = findViewById(R.id.btn_Predict);
        imgXray = findViewById(R.id.imgXray);
        tvResult = findViewById(R.id.tvResults);
    }


    public void pickCamera(int app) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 110);
        } else {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
            uri = getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

            //startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE); // OLD WAY
            if (app == 0){
                startCamera.launch(cameraIntent);
            }else{
                mGetContent.launch("image/*");
            }
            checkImageImported = 1;
        }

    }

    private void classify(){
        try {
            MedicalDiagnosisCnnModelLast model = MedicalDiagnosisCnnModelLast.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 256, 256, 3}, DataType.FLOAT32);

            Bitmap image = MediaStore.Images.Media.getBitmap(ScanningActivity.this.getContentResolver(),uri);
            int dimension = Math.min(image.getWidth(), image.getHeight());
            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for(int i = 0; i < imageSize; i ++){
                for(int j = 0; j < imageSize; j++){
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 1));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            MedicalDiagnosisCnnModelLast.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence.
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
                Toast.makeText(this, ""+confidences[i], Toast.LENGTH_SHORT).show();
            }
            String[] classes = {"COVID", "NORMAL","VIRAL PNEUMONIA"};

            String forText = classes[maxPos] +"_"+ confidences[maxPos];

            if (classes[maxPos] == classes[0]){
               forText = classes[maxPos] +" !! \n\nYou must go to the hospital as soon as possible,stay away from people and wear a mask!!";
            }else if (classes[maxPos] == classes[2]){
                forText = classes[maxPos] +" !! \n\nYou must go to the hospital as soon as possible and stay at home !!";
            }else {
                forText = classes[maxPos] +" \n\nYou are fine to go Nothing Wrong with you :)";
            }


            tvResult.setText(forText);

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    private final ActivityResultLauncher<Intent> startCamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        // There are no request codes
                        imgXray.setImageURI(uri);
                    }
                }
            });
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    ScanningActivity.this.uri = uri;
                    // Handle the returned Uri
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(ScanningActivity.this.getContentResolver(),uri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Glide.with(ScanningActivity.this).load(bitmap).error(R.drawable.bot).placeholder(R.drawable.bot).into(imgXray);
                }
            });

    }