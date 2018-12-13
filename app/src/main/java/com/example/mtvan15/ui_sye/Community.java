package com.example.mtvan15.ui_sye;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class Community extends AppCompatActivity {

    List<ImageUpload> imageList;
    RecyclerView recyclerView;
    ImageAdapter adapter;
    int imageNum = -1;
    String title;
    String description;
    Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        imageList = new ArrayList<ImageUpload>();
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new ImageAdapter(imageList, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        prepareImages();
    }

    private void prepareImages(){
        // We need firebase calls
        //https://www.androidhive.info/2016/01/android-working-with-recycler-view/

        // Get the count of the last picture in the data base
        // Write a message to the database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("imageNum");

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                long value = dataSnapshot.getValue(Long.class);
                imageNum = (int) value;
                Log.d("imageNum", "Value is: " + imageNum);

                while(imageNum >= 1){
                    DatabaseReference imageRef = database.getReference(String.valueOf(imageNum));


                    // Read from the database
                    imageRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            ArrayList<String> data = new ArrayList<>();
                            for(DataSnapshot imageSnapshot : dataSnapshot.getChildren()){
                                String newData = imageSnapshot.getValue(String.class);
                                data.add(newData);
                            }

                            title = data.get(1);
                            description = data.get(0);
                            Log.d("imageTitle", title);
                            Log.d("imageTitle", description);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

                /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        FirebaseStorage storageRef = FirebaseStorage.getInstance();

        StorageReference storageReference = storageRef.getReference();

        StorageReference pathReference = storageReference.child("images/placeHolder.jpg");

        final long ONE_MEGABYTE = 1024 * 1024;

        pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }
        });

        //image = getBitmapFromURL("https://firebasestorage.googleapis.com/v0/b/cs450-synesthesia.appspot.com/o/2.jpg?alt=media&token=82a2e7f3-6d3d-4aeb-9d00-de47a56914a1");

//        imageStorageRef.getBytes(1024*1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//            @Override
//            public void onSuccess(byte[] bytes) {
//                Log.d("byteArray", String.valueOf(bytes.length));
//                image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//            }
//
//        });

        Log.d("imageSize", String.valueOf(image.getByteCount()));
        // Make our ImageUpload Object
        imageList.add(new ImageUpload(title, description, image));

        imageNum--;

        adapter.notifyDataSetChanged();

    }

    public Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
