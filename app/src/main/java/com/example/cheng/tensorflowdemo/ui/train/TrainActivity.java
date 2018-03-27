package com.example.cheng.tensorflowdemo.ui.train;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.cheng.tensorflowdemo.R;
import com.example.cheng.tensorflowdemo.ui.camera.CameraActivity;
import com.example.cheng.tensorflowdemo.ui.main.MainActivity;
import com.example.cheng.tensorflowdemo.utils.FileUtils;
import com.example.cheng.tensorflowdemo.utils.TensorflowMobile;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class TrainActivity extends AppCompatActivity implements View.OnClickListener,TrainContract.View{
    private final int CHOOSE_IMAGE = 103;
    public static final int CAMERA_REQUEST = 104;
    private final int REQUEST_CONTACTS = 111;
    private static final String TAG = TrainActivity.class.getName();
    private final String MODEL_FILE = "file:///android_asset/inception_v3_2016_08_28_frozen.pb";
    private final String INPUT_NODE = "Mul";
    private final String OUTPUT_NODE = "final_result";
    private final int INPUT_SIZE = 299;

    private ImageView imageView;
    private Button switchButton;
    private Button recognizeButton;
    private Button caremaButton;
    private Button chooseImageButton;
    private String imagePath="";
    private TrainContract.Presenter presenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);
        init();
//        TensorflowMobile tensorflowMobile=new TensorflowMobile(this,MODEL_FILE,INPUT_NODE,OUTPUT_NODE,INPUT_SIZE);
//        float result=tensorflowMobile.getData(null);
//        Log.e(TrainActivity.class.getName(), "第" + "個" + result);
    }

    private void init() {
        imageView=findViewById(R.id.imageview);
        switchButton = findViewById(R.id.switch_button);
        recognizeButton = findViewById(R.id.recognize_button);
        caremaButton = findViewById(R.id.carema_button);
        chooseImageButton = findViewById(R.id.choose_image_button);
        switchButton.setOnClickListener(this);
        recognizeButton.setOnClickListener(this);
        caremaButton.setOnClickListener(this);
        chooseImageButton.setOnClickListener(this);
        presenter=new TrainPresenter(this,new TensorflowMobile(this,MODEL_FILE,INPUT_NODE,OUTPUT_NODE,INPUT_SIZE));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == switchButton.getId()) {
            Intent intent = new Intent(TrainActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (view.getId() == recognizeButton.getId()) {
            if(!imageView.equals("")){
                presenter.recognize(imagePath);
            }
        } else if (view.getId() == caremaButton.getId()) {
            Intent intent = new Intent(TrainActivity.this, CameraActivity.class);
            intent.putExtra("from", TrainActivity.class.getName());
            startActivityForResult(intent, CAMERA_REQUEST);
        } else if (view.getId() == chooseImageButton.getId()) {
            int permission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                //未取得權限，向使用者要求允許權限
                ActivityCompat.requestPermissions(TrainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CONTACTS);
            } else {
                //已有權限，可進行檔案存取
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), CHOOSE_IMAGE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_IMAGE && data != null) {
            if (data.getData() != null) {
                Uri selectedImageUri = data.getData();
                imagePath = FileUtils.getPath(TrainActivity.this, selectedImageUri);
                Glide.with(TrainActivity.this).load(imagePath).into(imageView);
            }
        }else if (requestCode == CAMERA_REQUEST&&data!=null) {
            if (data.getStringExtra("image") != null) {
                imagePath=data.getStringExtra("image");
                Glide.with(TrainActivity.this).load(imagePath).into(imageView);
            }
        }
    }

    @Override
    public void getData(ArrayList<HashMap> result) {
        for (int i=0;i<result.size();i++){
            Log.e(TrainActivity.class.getName()
                    ,"label:"+result.get(i).get("label").toString()+" output:"+result.get(i).get("output"));
        }
    }
}