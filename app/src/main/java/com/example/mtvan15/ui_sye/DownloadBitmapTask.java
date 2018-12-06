package com.example.mtvan15.ui_sye;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

// Type of Data to do it's job
// Type of Data for progress updates
// Type of Data to return
public class DownloadBitmapTask extends AsyncTask<String, Void, Bitmap> {

    // need to keep track of the ViewHolder
    private RecyclerAdapter.ImageAdapterViewHolder viewHolder;
    private String path;

    public DownloadBitmapTask(RecyclerAdapter.ImageAdapterViewHolder viewHolder){
        this.viewHolder = viewHolder;
    }

    // variable number of arguments
    // doInBackground runs in a separate thread
    // than the UI thread. Worker thread.
    // WORKER THREAD ANDROID DOCUMENTATION
    @Override
    protected Bitmap doInBackground(String... path) {
        Bitmap bm = Utility.downloadBitmap(path[0], viewHolder.imageView.getMaxWidth(), viewHolder.imageView.getMaxHeight());
        this.path = path[0];
        // not in the UI thread, that cannot work, that will crash hard!
        //this.viewHolder.textView.setText(url[0]);
        return bm;
    }

    // onPostExecute always runs in the UI thread.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        // might have gotten cancelled
        if(this.isCancelled()){
            return;
        }

        // check to see if the current task is the same task that is downloading the viewholder task
        ImageView iv = viewHolder.imageView;
        DownloadBitmapTask iv_task = AsyncDrawable.getDownloadBitmapTaskReference(iv);

        // now we need to check a couple of things!
        if(this == iv_task && iv != null){
            this.viewHolder.imageView.setImageBitmap(bitmap);
            this.viewHolder.textView.setText(this.path);
        }

    }

    public String getPath() {
        return path;
    }
}
