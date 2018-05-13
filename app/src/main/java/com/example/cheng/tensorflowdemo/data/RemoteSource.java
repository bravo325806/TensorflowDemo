package com.example.cheng.tensorflowdemo.data;

import org.json.JSONException;

/**
 * Created by cheng on 2018/3/26.
 */

public interface RemoteSource {
    interface getpbModel{
        void onFinish() throws Exception;
        void onError() throws Exception;
    }
    interface getpbtxt{
        void onFinish() throws Exception;
        void onError() throws Exception;
    }
    interface uploadFile{
        void onFinish(String response) throws Exception;
        void onError() throws Exception;
    }
    interface getVersion{
        void onFinish(String response) throws Exception;
        void onError() throws Exception;
    }
}
