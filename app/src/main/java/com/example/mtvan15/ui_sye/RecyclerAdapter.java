package com.example.mtvan15.ui_sye;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

// Takes the type of the viewHolder object
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ImageAdapterViewHolder> {

    // ImageAdapterViewHolder Class
    class ImageAdapterViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView textView;

        public ImageAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            // finding the appropriate imageView and textView in
            // tbese need to refer to the views in the layout file
            // just like we've been doing all semester
            // just running these once for each ViewHolder
            this.imageView = itemView.findViewById(R.id.rv_item_image);
            this.textView = itemView.findViewById(R.id.rv_item_text);
        }
    }

    // This is the connection to the data source
    private ImageURLInterface images;

    // Keep a reference to the activity
    private Activity act;

    // Keep a reference to a Bitmap
    private Bitmap placeHolder;

    public RecyclerAdapter(ImageURLInterface images, Activity act){
        this.images = images;
        this.act = act;
        this.placeHolder = BitmapFactory.decodeResource(this.act.getResources(), R.drawable.sv_chip_weaving);
    }

    // get's called only a handful of times
    // ViewGroup for the linearlayout
    // The amount that fit on the screen, and a couple extras
    @NonNull
    @Override
    public RecyclerAdapter.ImageAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        // Terminology
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.rv_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        // Parent is the RecyclerView, and we don't want to pass it immediately
        ImageAdapterViewHolder viewHolder = new ImageAdapterViewHolder(view);
        return viewHolder;
    }

    // get's called an unlimited number of times
    // get's passed "i" which refers to the dataItem "i"
    // get's called when a view holder comes into the display area
    // that ViewHolder might get reused
    // position of the ViewHolder in the entire list
    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.ImageAdapterViewHolder imageAdapterViewHolder, int i) {
        String url = images.get(i);
        // DownloadBitmapTask next!!!!

        // need to figure out if there is already a task downloading an image for this viewholder
        // this view holder could already be downloading an image... how to check for that

        if(cancelCurrentTask(url, imageAdapterViewHolder.imageView)){
            // we're ready to start downloading this...
            DownloadBitmapTask task = new DownloadBitmapTask(imageAdapterViewHolder);
            // call url to get this going!
            // place holder aSyncDrawable
            AsyncDrawable asyncDrawable = new AsyncDrawable(this.act.getResources(), this.placeHolder, task);
            // attaching this async drawable to the view holder
            imageAdapterViewHolder.imageView.setImageDrawable(asyncDrawable);
            task.execute(url);
        }

        //DownloadBitmapTask t = new DownloadBitmapTask(imageAdapterViewHolder);
        // call url to get this going!
        //t.execute(url);
    }

    // private helper method
    private boolean cancelCurrentTask(String url, ImageView imageView){
        // get the task for the ImageView if it exists
        DownloadBitmapTask bitmapTask = AsyncDrawable.getDownloadBitmapTaskReference(imageView);

        // check to see if bitmapTask is null. If not, then you should be all good.
        if(bitmapTask != null){

            // how to check if it's still running when onBindViewHolder is called again
            // check to see if the URL in the ViewHolder is the same as the URL in the bitmapTask
            //TODO what if getURL is null
            String taskUrl = bitmapTask.getUrl();
            if(taskUrl == null || !url.equals(taskUrl)){
                bitmapTask.cancel(true);
                return true;
            }else{
                return false;
            }

        }

        return true; //TODO Why?
    }

    @Override
    public int getItemCount() {
        return images.count();
    }
}