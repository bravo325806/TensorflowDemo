package com.example.cheng.tensorflowdemo.ui.main;

import android.os.Parcelable;
import android.util.Log;

import com.example.cheng.tensorflowdemo.data.RemoteReponsitory;
import com.example.cheng.tensorflowdemo.data.RemoteSource;

import java.util.ArrayList;

/**
 * Created by cheng on 2018/3/26.
 */

public class MainPresenter implements MainContract.Presenter {
    private MainContract.View myView;
    private RemoteReponsitory remoteReponsitory;
    private int count=0;
    public MainPresenter(MainContract.View myView, RemoteReponsitory remoteReponsitory) {
        this.myView = myView;
        this.remoteReponsitory = remoteReponsitory;
    }

    @Override
    public void getModel() {
        remoteReponsitory.setGetpbModelRequset();
        remoteReponsitory.setGetpbModelFinish(getpbModel);
        remoteReponsitory.setGetpbtxtRequset();
        remoteReponsitory.setGetpbtxtFinish(getpbtxt);
    }

    @Override
    public void upload(String tag, ArrayList<String> file, String id) {
        remoteReponsitory.setUploadFileRequset(tag, file, id);
        remoteReponsitory.setUploadFileFinish(uploadFile);
    }

    RemoteSource.uploadFile uploadFile = new RemoteSource.uploadFile() {
        @Override
        public void onFinish(String response) throws Exception {
            if (response.equals("0")) {
                myView.uploadFinish();
            }
        }
    };
    RemoteSource.getpbModel getpbModel = new RemoteSource.getpbModel() {
        @Override
        public void onFinish() throws Exception {
            count++;
            updateModelFinish();

        }
    };
    RemoteSource.getpbtxt getpbtxt = new RemoteSource.getpbtxt() {
        @Override
        public void onFinish() throws Exception {
            count++;
            updateModelFinish();
        }
    };
    private void updateModelFinish(){
        if(count==2){
            count=0;
            myView.modelUpdateFinish();
        }
    }
}
