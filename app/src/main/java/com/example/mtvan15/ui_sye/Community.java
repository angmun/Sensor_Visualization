package com.example.mtvan15.ui_sye;

import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class Community extends AppCompatActivity {

    List<ImageUpload> imageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        imageList = new ArrayList<ImageUpload>();
        prepareImages();
    }

    private void prepareImages(){
        // We need firebase calls
        //https://www.androidhive.info/2016/01/android-working-with-recycler-view/
    }

}
