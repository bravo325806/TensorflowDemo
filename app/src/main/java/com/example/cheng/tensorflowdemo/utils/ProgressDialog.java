package com.example.cheng.tensorflowdemo.utils;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cheng.tensorflowdemo.R;

/**
 * Created by cheng on 2018/3/26.
 */

public class ProgressDialog extends DialogFragment{
    private TextView textView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.dialog_progress,container,false);
        textView=view.findViewById(R.id.text);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.setCancelable(false);
        if(getArguments().getString("text")!=null){
            textView.setText(getArguments().getString("text"));
        }
    }
}
