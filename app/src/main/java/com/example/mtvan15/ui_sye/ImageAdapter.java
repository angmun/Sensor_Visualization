package com.example.mtvan15.ui_sye;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
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
        final ImageUpload anImage = imageUploadList.get(i);
        myViewHolder.image.setImageBitmap(anImage.getBitmap());
        myViewHolder.title.setText(anImage.getTitle());
        myViewHolder.description.setText(anImage.getDescription());


        myViewHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                anImage.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
                byte[] byteArray = baos.toByteArray();
                String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
                sharedPreferences.edit().putString("background", encodedImage).apply();
                Log.i("ImageSaved",encodedImage);
                activity.finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUploadList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title;
        TextView description;
        Button button;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.rv_item_image);
            title = itemView.findViewById(R.id.rv_item_title);
            description = itemView.findViewById(R.id.rv_item_description);
            button = itemView.findViewById(R.id.saveButton);
        }
    }

    public ImageAdapter(List<ImageUpload> imageUploadList, Activity activity){
        this.activity = activity;
        this.imageUploadList = imageUploadList;
    }
}
