package com.example.cheng.tensorflowdemo.ui.main;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cheng.tensorflowdemo.R;
import com.example.cheng.tensorflowdemo.data.RemoteReponsitory;
import com.example.cheng.tensorflowdemo.ui.camera.CameraActivity;
import com.example.cheng.tensorflowdemo.ui.recognition.RecognitionActivity;
import com.example.cheng.tensorflowdemo.utils.FileUtils;
import com.example.cheng.tensorflowdemo.utils.ProgressDialog;

import java.util.ArrayList;

/**
 * Created by cheng on 2018/3/19.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MainContract.View {
    private final int CHOOSE_IMAGE = 3;
    public static final int CAMERA_REQUEST = 4;
    private final int REQUEST_CONTACTS = 11;
    private final String id = "story";
    private Button switchButton;
    private Button updateModelButton;
    private RecyclerView imageList;
    private Button chooseImageButton;
    private Button caremaButton;
    private EditText labelEditText;
    private Button uploadButton;
    private ArrayList<String> arrayPath;
    private ImageAdapter imageAdapter;
    private MainContract.Presenter myPresenter;
    private ProgressDialog progressDialog;
    private Toast toast;
    private DownloadDialog downloadDialog;
    private String version;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        switchButton = findViewById(R.id.switch_button);
        updateModelButton = findViewById(R.id.update_model_button);
        imageList = findViewById(R.id.image_list);
        chooseImageButton = findViewById(R.id.choose_image_button);
        caremaButton = findViewById(R.id.carema_button);
        labelEditText = findViewById(R.id.label_editText);
        uploadButton = findViewById(R.id.upload_button);
        switchButton.setOnClickListener(this);
        updateModelButton.setOnClickListener(this);
        chooseImageButton.setOnClickListener(this);
        caremaButton.setOnClickListener(this);
        uploadButton.setOnClickListener(this);
        arrayPath = new ArrayList<>();
        setRecycleView();
        myPresenter = new MainPresenter(this, new RemoteReponsitory(getApplicationContext()));
        progressDialog = new ProgressDialog();
        downloadDialog = new DownloadDialog();
        preferences = getSharedPreferences("demo", 0);
        editor = preferences.edit();
    }

    private void setRecycleView() {
        StaggeredGridLayoutManager linearManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL);
        imageList.setLayoutManager(linearManager);
        imageAdapter = new ImageAdapter(arrayPath, this);
        imageList.setAdapter(imageAdapter);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == switchButton.getId()) {
            Intent intent = new Intent(MainActivity.this, RecognitionActivity.class);
            startActivity(intent);
            finish();
        } else if (view.getId() == updateModelButton.getId()) {
            myPresenter.getVersion();
        } else if (view.getId() == chooseImageButton.getId()) {
            int permission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                //未取得權限，向使用者要求允許權限
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CONTACTS);
            } else {
                //已有權限，可進行檔案存取
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), CHOOSE_IMAGE);
            }
        } else if (view.getId() == caremaButton.getId()) {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            intent.putExtra("from", MainActivity.class.getName());
            startActivityForResult(intent, CAMERA_REQUEST);
        } else if (view.getId() == uploadButton.getId()) {
            if (!labelEditText.getText().toString().equals("")) {
                Bundle bundle = new Bundle();
                bundle.putString("text", "上傳中...");
                progressDialog.setArguments(bundle);
                progressDialog.show(getFragmentManager(), "dialog");
                myPresenter.upload(labelEditText.getText().toString(), arrayPath, id);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_IMAGE && data != null) {
            if (data.getClipData() != null) {
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri uri = clipData.getItemAt(i).getUri();
                    String selectedImagePath = FileUtils.getPath(MainActivity.this, uri);
                    arrayPath.add(selectedImagePath);
                    imageAdapter.notifyDataSetChanged();
                }
            } else {
                if (data.getData() != null) {
                    Uri selectedImageUri = data.getData();
                    String selectedImagePath = FileUtils.getPath(MainActivity.this, selectedImageUri);
                    arrayPath.add(selectedImagePath);
                    imageAdapter.notifyDataSetChanged();

                }
            }
        } else if (requestCode == CAMERA_REQUEST && data != null) {
            if (data.getStringExtra("image") != null) {
                arrayPath.add(data.getStringExtra("image"));
                imageAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void getVersionFinish(String version) {
        this.version = version;
        Bundle bundle = new Bundle();
        bundle.putString("version", version);
        downloadDialog.setArguments(bundle);
        downloadDialog.show(getFragmentManager(), DownloadDialog.class.getName());
        downloadDialog.setOnClickListener(onClick);
    }

    @Override
    public void itemLongClick(View view) {
        arrayPath.remove((int) view.getTag(R.id.image_tag));
        imageAdapter.notifyDataSetChanged();
    }

    @Override
    public void uploadFinish() {
        progressDialog.dismiss();
        arrayPath.clear();
        imageAdapter.notifyDataSetChanged();
    }

    @Override
    public void uploadError() {
        progressDialog.dismiss();
        showToast("上傳失敗");
    }

    @Override
    public void modelUpdateFinish() {
        editor.putString("version", version);
        editor.commit();
        progressDialog.dismiss();
    }

    @Override
    public void modelUpdateError() {
        progressDialog.dismiss();
        showToast("下載失敗");
    }

    private DownloadDialog.onClick onClick = new DownloadDialog.onClick() {
        @Override
        public void onClick(View view) {
            downloadDialog.dismiss();
            Bundle bundle = new Bundle();
            bundle.putString("text", "下載中...");
            progressDialog.setArguments(bundle);
            progressDialog.show(getFragmentManager(), ProgressDialog.class.getName());
            myPresenter.getModel();
        }
    };

    private void showToast(String msg) {
        if (toast == null) {
            toast = Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }
}
