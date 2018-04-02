package com.example.cheng.tensorflowdemo.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.cheng.tensorflowdemo.ui.main.MainPresenter;
import com.example.cheng.tensorflowdemo.utils.FileUtils;
import com.example.cheng.tensorflowdemo.utils.InputStreamVolleyRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cheng on 2018/3/26.
 */

public class RemoteReponsitory {
    private Context context;
    private RequestQueue queue;
    private RemoteSource.getpbModel getpbModel;
    private RemoteSource.uploadFile uploadFile;
    private RemoteSource.getpbtxt getpbtxt;
    public RemoteReponsitory(Context context){
        this.context=context;
        queue = SingleRequestQueue.getQueue(context);
    }
    private static final class SingleRequestQueue {
        private volatile static RequestQueue queue;

        private SingleRequestQueue() {
        }

        private static RequestQueue getQueue(Context context) {
            if (queue == null) {
                synchronized (SingleRequestQueue.class) {
                    if (queue == null) {
                        queue = Volley.newRequestQueue(context);
                    }
                }
            }
            return queue;
        }
    }
    public void setUploadFileFinish(RemoteSource.uploadFile uploadFile){
        this.uploadFile=uploadFile;
    }
    public void setUploadFileRequset(final String tag,final ArrayList<String> file,final String id){
        new Thread(new Runnable() {
            StringBuilder s= new StringBuilder();
            @Override
            public void run() {
                try {
                    HttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
                    HttpClient httpClient = new DefaultHttpClient(httpParams);
                    HttpPost postRequest = new HttpPost("http://10.21.20.38:5000/upload");
                    MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                    try {
                        for (int i=0;i<file.size();i++){
                            Bitmap bitmap= FileUtils.getBitmapRotation(file.get(i));
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, bos);
                            byte[] data = bos.toByteArray();
                            String[] x=file.get(i).split("/");
                            ByteArrayBody bab = new ByteArrayBody(data, x[x.length-1]);
                            reqEntity.addPart("file[]", bab);
                        }
                        reqEntity.addPart("tag", new StringBody(tag));
                        reqEntity.addPart("id", new StringBody(id));
                    } catch (Exception e) {
                    }
                    postRequest.setEntity(reqEntity);

                    HttpResponse response = httpClient.execute(postRequest);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    String sResponse;

                    while ((sResponse = reader.readLine()) != null) {
                        s = s.append(sResponse);
                    }
                }catch(Exception e){
                    e.getStackTrace();
                }
                setTrainModelRequset(id);
            }
        }).start();
    }
    public void setTrainModelRequset(final String name){
        String url = "http://10.21.20.38:5000/train";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    uploadFile.onFinish(response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    uploadFile.onError();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e("請求錯誤：",error.toString());
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap map=new HashMap();
                map.put("name",name);
                return map;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }
    public void setGetpbModelFinish(RemoteSource.getpbModel getpbModel) {
        this.getpbModel=getpbModel;
    }

    public void setGetpbModelRequset() {
        String url = "http://10.21.20.38:5000/file";
        InputStreamVolleyRequest postRequest = new InputStreamVolleyRequest(Request.Method.GET, url,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response)  {
                        try {
                            FileOutputStream output = new FileOutputStream(context.getExternalFilesDir(null)+"/output_graph.pb");
                            output.write(response);
                            output.close();
                            getpbModel.onFinish();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            getpbModel.onError();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.e("請求錯誤：",error.toString());
//                        Toast.makeText(context, "帳號或密碼錯誤", Toast.LENGTH_SHORT).show();
                    }
                },null
        );
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);
    }
    public void setGetpbtxtFinish(RemoteSource.getpbtxt getpbtxt) {
        this.getpbtxt=getpbtxt;
    }

    public void setGetpbtxtRequset() {
        String url = "http://10.21.20.38:5000/file1";
        InputStreamVolleyRequest postRequest = new InputStreamVolleyRequest(Request.Method.GET, url,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response)  {
                        try {
                            FileOutputStream output = new FileOutputStream(context.getExternalFilesDir(null)+"/output_labels.txt");
                            output.write(response);
                            output.close();
                            getpbtxt.onFinish();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            getpbtxt.onError();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.e("請求錯誤：",error.toString());
//                        Toast.makeText(context, "帳號或密碼錯誤", Toast.LENGTH_SHORT).show();
                    }
                },null
        );
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);
    }
}
