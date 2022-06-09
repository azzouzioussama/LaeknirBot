package com.finsday.testfinalproject;

import android.app.NotificationManager;
import android.content.Context;
import android.widget.Toast;

import com.finsday.testfinalproject.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class UtilsModel {
    private Context mContext;

    public UtilsModel(Context mContext) {
        this.mContext = mContext;
    }

    public float[] Classifier(ArrayList<Integer> arrayList){

        try {
            Model model = Model.newInstance(mContext);

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 1, 123}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 * arrayList.size());
            byteBuffer.order(ByteOrder.nativeOrder());

            for (int i = 0; i < arrayList.size(); i++) {
                byteBuffer.putFloat((arrayList.get(i)) * (1.f));
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // Releases model resources if no longer used.
            model.close();

            return confidences;
        } catch (IOException e) {
            // TODO Handle the exception
            Toast.makeText(mContext, "ERROR", Toast.LENGTH_SHORT).show();
        }
        return new float[0];
    }

    public ArrayList<Integer> convertPythonArrToJavaArr(String ar) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (char s: ar.toCharArray()){
            if (s == '0'){
                arrayList.add(0);
            }else if (s == '1'){
                arrayList.add(1);
            }
        }
        return arrayList;
    }

    public static int countWords(String s){

        int wordCount = 0;

        boolean word = false;
        int endOfLine = s.length() - 1;

        for (int i = 0; i < s.length(); i++) {
            // if the char is a letter, word = true.
            if (Character.isLetter(s.charAt(i)) && i != endOfLine) {
                word = true;
                // if char isn't a letter and there have been letters before,
                // counter goes up.
            } else if (!Character.isLetter(s.charAt(i)) && word) {
                wordCount++;
                word = false;
                // last word of String; if it doesn't end with a non letter, it
                // wouldn't count without this.
            } else if (Character.isLetter(s.charAt(i)) && i == endOfLine) {
                wordCount++;
            }
        }
        return wordCount;
    }




}
