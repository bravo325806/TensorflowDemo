package com.example.cheng.tensorflowdemo.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by cheng on 2018/3/20.
 */

public class TensorflowMobile {
    private Interpreter tflite;
    private int[] intValues;
    private ByteBuffer inputBuffer;
    private float[] inputFloat;
    private Context context;
    private final int INPUT_SIZE;
    private final String INPUT_NODE;
    private final String OUTPUT_NODE;
    private ArrayList labelList;
    private int outputSize=0;

    public TensorflowMobile(Context context, String modelFile,String labelFile, String inputNode, String outputNode, int inputSize) {
        this.context = context;
        this.INPUT_NODE = inputNode;
        this.OUTPUT_NODE = outputNode;
        this.INPUT_SIZE = inputSize;
        intValues = new int[inputSize * inputSize];
        inputFloat = new float[inputSize * inputSize * 3];
        labelList=new ArrayList();
        try {
            File file = new File(context.getExternalFilesDir(null) + "/output_graph.lite");
            if (file.exists()) {
                tflite =new Interpreter(file);
            } else {
                AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelFile);
                FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
                FileChannel fileChannel = inputStream.getChannel();
                long startOffset = fileDescriptor.getStartOffset();
                long declaredLength = fileDescriptor.getDeclaredLength();
                tflite = new Interpreter(fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength));
            }
            File file1 = new File(context.getExternalFilesDir(null)+"/output_labels.txt");
            String mLine= null;
            if(file1.exists()){
                InputStream is = new FileInputStream(file1);
                BufferedReader reader =new BufferedReader(new InputStreamReader(is));
                while ((mLine = reader.readLine())!=null){
                    outputSize++;
                    labelList.add(mLine);
                }
            }else{
                BufferedReader reader =new BufferedReader(new InputStreamReader(context.getAssets().open(labelFile)));
                while ((mLine = reader.readLine())!=null){
                    outputSize++;
                    labelList.add(mLine);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        inputBuffer = ByteBuffer.allocateDirect(INPUT_SIZE * INPUT_SIZE * 3 * 4);
        inputBuffer.order(ByteOrder.nativeOrder());
        inputBuffer.rewind();
    }

    public ArrayList<HashMap> getData(Bitmap bitmap) {
//        convertBitmapToByteBuffer(getBitmapFromAsset(context, "bird299.png"));
        convertBitmapToByteBuffer(bitmap);

        ArrayList<HashMap> result = initList();
        float[][] resu = new float[1][outputSize];
        tflite.run(inputBuffer,resu);
        for (int i = 0; i < outputSize; i++) {
            if (resu[0][i] > (float) result.get(0).get("output")) {
                result.get(2).put("output", result.get(1).get("output"));
                result.get(2).put("label", result.get(1).get("label"));
                result.get(1).put("output", result.get(0).get("output"));
                result.get(1).put("label", result.get(0).get("label"));
                result.get(0).put("output", resu[0][i]);
                result.get(0).put("label", labelList.get(i));
            } else if (resu[0][i] > (float) result.get(1).get("output")) {
                result.get(2).put("output", result.get(1).get("output"));
                result.get(2).put("label", result.get(1).get("label"));
                result.get(1).put("output", resu[0][i]);
                result.get(1).put("label", labelList.get(i));
            } else if (resu[0][i] > (float) result.get(2).get("output")) {
                result.get(2).put("output", resu[0][i]);
                result.get(2).put("label", labelList.get(i));
            }
        }
        return result;
    }

    private void convertBitmapToByteBuffer(Bitmap bitmap) {
//        Log.d(TAG, "convertBitmapToByteBuffer: " + bitmap.getWidth());
//        Log.d(TAG, "convertBitmapToByteBuffer: " + bitmap.getHeight());
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // Convert the image to floating point.
        inputBuffer.clear();
        for (int i = 0; i < intValues.length; ++i) {
            final float r = ((intValues[i] >> 16) & 0xFF)/255.0f;
            final float g = ((intValues[i] >> 8) & 0xFF)/255.0f;
            final float b = (intValues[i] & 0xFF)/255.0f;
            inputBuffer.putFloat(r);
            inputBuffer.putFloat(g);
            inputBuffer.putFloat(b);
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

    private ArrayList<HashMap> initList() {
        ArrayList<HashMap> arrayList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            HashMap hashMap = new HashMap();
            hashMap.put("label", 0f);
            hashMap.put("output", 0f);
            arrayList.add(hashMap);
        }
        return arrayList;
    }
}
