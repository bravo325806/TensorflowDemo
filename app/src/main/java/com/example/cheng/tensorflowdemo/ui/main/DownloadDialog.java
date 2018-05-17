package com.example.cheng.tensorflowdemo.ui.main;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cheng.tensorflowdemo.R;

/**
 * Created by cheng on 2018/4/23.
 */

public class DownloadDialog extends DialogFragment implements View.OnClickListener{
    private TextView currentVersion;
    private TextView lastestVersion;
    private TextView updateButton;
    private TextView cancelButton;
    private SharedPreferences preferences;
    private onClick onClick;
    public interface onClick{
        void onClick(View view);
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_download, container, false);
        currentVersion = view.findViewById(R.id.current_version);
        lastestVersion = view.findViewById(R.id.lastest_version);
        updateButton = view.findViewById(R.id.update_button);
        cancelButton = view.findViewById(R.id.cancel_button);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferences=getActivity().getSharedPreferences("demo",0);
        currentVersion.setText(preferences.getString("version","0"));
        lastestVersion.setText(getArguments().getString("version"));
        this.getDialog().setCanceledOnTouchOutside(true);
        updateButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==updateButton.getId()){
            onClick.onClick(view);
        } else if(view.getId()==cancelButton.getId()){
            DownloadDialog.this.dismiss();
        }
    }
    public void setOnClickListener(onClick onClick){
        this.onClick=onClick;
    }
}