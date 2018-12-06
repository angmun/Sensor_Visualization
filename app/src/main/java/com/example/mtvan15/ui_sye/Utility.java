package com.example.mtvan15.ui_sye;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.renderscript.ScriptGroup;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Utility {

    private static final String TAG = Utility.class.getSimpleName();

    public static Bitmap downloadBitmap(String aURL, int rw, int rh){
        // we're going to sample the image, and not take all of the pixel
        // first query the image and see how big it is
        BitmapFactory.Options options = downloadBitMapOptions(aURL);
        if(options == null) return null;
        URL url = null;
        HttpURLConnection connection = null;
        try {
            url = new URL(aURL);
            connection = (HttpURLConnection) url.openConnection();

            // TODO check response code for HTTP_OK

            InputStream inputStream = new BufferedInputStream(connection.getInputStream());

            Bitmap bitmap = decodeSampledBitmap(inputStream, options, rw, rh);

            inputStream.close();
            return bitmap;

        } catch (MalformedURLException e) {
            e.printStackTrace();
            //TODO error log
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            //TODO error log
            return null;
        }
        finally{
            if(connection != null) connection.disconnect();
        }
    }

    private static BitmapFactory.Options downloadBitMapOptions(String url){
        HttpURLConnection connection = null;
        URL aURL;
        try {
            aURL = new URL(url);
            connection = (HttpURLConnection) aURL.openConnection();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                Log.e(TAG, "Error while openingurl");
                return null;

            }

            InputStream inputStream = null;
            // efficiently downloads data more efficiently than a classic input stream
            inputStream = new BufferedInputStream(connection.getInputStream());

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            return options;

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG, "malformed URL " + url);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        finally{
            if(connection!= null) connection.disconnect();
        }
    }

    public static Bitmap decodeSampledBitmap(InputStream inputStream, BitmapFactory.Options option, int rw, int rh){

        option.inSampleSize = calculateInSampleSize(option, rw, rh);
        option.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(inputStream, null, option);
    }

    //https://developer.android.com/topic/performance/graphics/load-bitmap#java
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}