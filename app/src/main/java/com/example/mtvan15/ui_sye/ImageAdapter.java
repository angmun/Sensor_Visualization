package com.example.mtvan15.ui_sye;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {

    // Keep a reference to the activity
    private Activity activity;

    // Keep a reference to a Bitmap
    private Bitmap placeHolder;

    // List for the ImageUpload Objects
    private List<ImageUpload> imageUploadList;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // Inflate our own imageView for the Recycler View
        View imageView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rv_item, viewGroup, false);
        return new MyViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        ImageUpload anImage = imageUploadList.get(i);
        myViewHolder.image.setImageBitmap(anImage.getBitmap());
        myViewHolder.title.setText(anImage.getTitle());
        myViewHolder.description.setText(anImage.getDescription());
    }

    @Override
    public int getItemCount() {
        return imageUploadList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title;
        TextView description;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.rv_item_image);
            title = itemView.findViewById(R.id.rv_item_title);
            description = itemView.findViewById(R.id.rv_item_description);
        }
    }

    public ImageAdapter(List<ImageUpload> imageUploadList, Activity activity){
        this.activity = activity;
        this.placeHolder = BitmapFactory.decodeResource(this.activity.getResources(), R.mipmap.place_holder);
        this.imageUploadList = imageUploadList;
    }
}
