package com.example.cheng.tensorflowdemo.ui.recognition;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import com.example.cheng.tensorflowdemo.utils.FileUtils;
import com.example.cheng.tensorflowdemo.utils.TensorflowMobile;

import java.util.ArrayList;

/**
 * Created by cheng on 2018/3/26.
 */

public class RecognitionPresenter implements RecognitionContract.Presenter{
    private RecognitionContract.View myView;
    private TensorflowMobile tensorflowMobile;
    public RecognitionPresenter(RecognitionContract.View myView, TensorflowMobile tensorflowMobile){
        this.myView=myView;
        this.tensorflowMobile=tensorflowMobile;
    }

    @Override
    public void recognize(String path) {
        Bitmap bitmap = FileUtils.getBitmapRotation(path);
        Matrix matrix = new Matrix();
        matrix.postScale(299/(float)bitmap.getWidth(),299/(float)bitmap.getHeight());
        Bitmap zoomBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        Log.e("width",zoomBitmap.getWidth()+"");
        Log.e("height",zoomBitmap.getHeight()+"");
        ArrayList result = tensorflowMobile.getData(zoomBitmap);
        myView.getData(result);
    }
}
