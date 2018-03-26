package com.example.cheng.tensorflowdemo.ui.main;

import android.view.View;

import java.util.ArrayList;

/**
 * Created by cheng on 2018/3/20.
 */

public interface MainContract {
    interface View{
        void itemLongClick(android.view.View view);
        void uploadFinish();
        void modelUpdateFinish();
    }
    interface Presenter{
        void getModel();
        void upload(String tag, ArrayList<String> file,String id);
    }
}
