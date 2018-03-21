package com.example.cheng.tensorflowdemo.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.cheng.tensorflowdemo.ui.train.TrainActivity;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by cheng on 2018/3/20.
 */

public class TensorflowMobile {
    private TensorFlowInferenceInterface inferenceInterface;
    private int[] intValues;
    private float[] inputFloat;
    private Context context;
    private final int INPUT_SIZE;
    private final String INPUT_NODE;
    private final String OUTPUT_NODE;
    public TensorflowMobile(Context context,String modelFile,String inputNode,String outputNode,int inputSize){
        this.context=context;
        this.INPUT_NODE=inputNode;
        this.OUTPUT_NODE=outputNode;
        this.INPUT_SIZE=inputSize;
        inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(),modelFile);
        intValues = new int[inputSize * inputSize];
        inputFloat = new float[inputSize * inputSize * 3];
    }
    public float getData(Bitmap bitmap){
        convertBitmapToByteBuffer(getBitmapFromAsset(context, "bird299.png"));
        inferenceInterface.feed(INPUT_NODE, inputFloat, 1, INPUT_SIZE, INPUT_SIZE, 3);
        final float[] resu = new float[1001];
        float result = 0;
        int num = 0;
        inferenceInterface.run(new String[]{OUTPUT_NODE});
        inferenceInterface.fetch(OUTPUT_NODE, resu);
        for (int i = 0; i < 1001; i++) {
            if (resu[i] > result) {
                num = i;
                result = resu[i];
            }
        }
        return result;
    }
    private void convertBitmapToByteBuffer(Bitmap bitmap) {
//        Log.d(TAG, "convertBitmapToByteBuffer: " + bitmap.getWidth());
//        Log.d(TAG, "convertBitmapToByteBuffer: " + bitmap.getHeight());
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // Convert the image to floating point.
        for (int i = 0; i < intValues.length; ++i) {
            final int r = ((intValues[i] >> 16) & 0xFF);
            final int g = ((intValues[i] >> 8) & 0xFF);
            final int b = (intValues[i] & 0xFF);
            inputFloat[i * 3 + 0] = r;
            inputFloat[i * 3 + 1] = g;
            inputFloat[i * 3 + 2] = b;
        }
    }
    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            // handle exception
        }
        return bitmap;
    }
}
