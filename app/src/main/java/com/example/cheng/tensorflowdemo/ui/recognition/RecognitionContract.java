package com.example.cheng.tensorflowdemo.ui.recognition;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by cheng on 2018/3/20.
 */

public interface RecognitionContract {
    interface View{
        void getData(ArrayList<HashMap> result);
    }
    interface Presenter{
        void recognize(String path);
    }
}
