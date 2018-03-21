package com.example.cheng.tensorflowdemo.ui.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ViewTarget;
import com.example.cheng.tensorflowdemo.R;

import java.util.ArrayList;

/**
 * Created by cheng on 2018/3/20.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder>{
    private ArrayList<String> arrayPath;
    private MainContract.View myView;
    public ImageAdapter(ArrayList arrayPath,MainContract.View myView) {
        this.myView=myView;
        this.arrayPath=arrayPath;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.item_main_image_list, parent, false);
        ViewHolder viewHolder=new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        Bitmap bitmap= BitmapFactory.decodeFile(arrayPath.get(position));
//        holder.imageView.setImageBitmap(bitmap);
        Glide.with(holder.imageView.getContext()).load(arrayPath.get(position)).into(holder.imageView);
        holder.imageView.setTag(R.id.image_tag,position);
        holder.imageView.setOnLongClickListener(imageLongClickListener);
    }
    View.OnLongClickListener imageLongClickListener=new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            myView.itemLongClick(view);
            return true;
        }
    };
    @Override
    public int getItemCount() {
        return arrayPath.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageView;
        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView=itemView.findViewById(R.id.imageview);
        }
    }
}
