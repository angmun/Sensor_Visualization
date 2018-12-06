package com.example.mtvan15.ui_sye;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class AsyncDrawable extends BitmapDrawable {

    private final DownloadBitmapTask downloadBitmapTaskReference;

    // bitmap is a placeholder image until its loaded
    public AsyncDrawable(Resources resources, Bitmap bitmap, DownloadBitmapTask downloadBitmapTaskReference){
        // placeholder bitmap
        super(resources, bitmap);
        this.downloadBitmapTaskReference = downloadBitmapTaskReference;
    }

    public DownloadBitmapTask getDownloadBitmapTaskReference() {
        return downloadBitmapTaskReference;
    }

    // given an ImageView as a parameter, want to get underlying drawable
    // check to see if it's an AsyncDrawable
    // if it is, then return that reference

    // Helper method to get the task reference given an ImageView

    /**
     * Helper method tp get the task reference given an ImageView
     * @param imageView - getting reference to task on this view
     * @return - DownloadBitmapTaskReference that ImageView referred to or null
     */
    public static DownloadBitmapTask getDownloadBitmapTaskReference(ImageView imageView){
        // returns a reference to the Async Drawable in the ImageView if it exists
        if(imageView != null){
            Drawable drawable = imageView.getDrawable();
            if(drawable instanceof AsyncDrawable){
                return ((AsyncDrawable) drawable).getDownloadBitmapTaskReference();
            }
        }
        return null;
    }
}