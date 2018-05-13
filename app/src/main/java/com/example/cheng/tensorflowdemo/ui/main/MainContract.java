package com.example.cheng.tensorflowdemo.ui.main;

import android.view.View;

import java.util.ArrayList;

/**
 * Created by cheng on 2018/3/20.
 */

public interface MainContract {
    interface View{
        void getVersionFinish(String version);
        void itemLongClick(android.view.View view);
        void uploadFinish();
        void uploadError();
        void modelUpdateFinish();
        void modelUpdateError();
    }
    interface Presenter{
        void getVersion();
        void getModel();
        void upload(String tag, ArrayList<String> file,String id);
    }
}
