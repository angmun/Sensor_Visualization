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
import android.util.Base64;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/***
 * The Community activity displays images saved by the community through the application and provides the user with the option to download an image copy to modify as they desire, known as 'remixing' in or application.
 */
public class Community extends AppCompatActivity {
    // A list of images from Firebase to display in a recycler view on the Community activity.
    List<ImageUpload> imageList;

    // A view that allows us to display images as we download them from the Firebase database where they are saved.
    RecyclerView recyclerView;

    // An image adapter required to manage view holder objects for images to be displayed in the recycler view.
    ImageAdapter adapter;

    // An integer value that allows us to track the number of images left to prepare for display in the recycler view.
    int imageNum = -1;

    // The required data for a single image downloaded from the Firebase database for its display in the recycler view.
    String title;
    String description;
    Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        // Initialize the variables required to set up the recycler view for image display from the Firebase database.
        imageList = new ArrayList<ImageUpload>();
        recyclerView = findViewById(R.id.recyclerView);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new ImageAdapter(imageList, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        // Set up the images accordingly for display.
        prepareImages();
    }

    /***
     * Sets up saved images for display on the recycler view. An image, its title and description are 
     */
    private void prepareImages(){
        // We need firebase calls
        //Citation: https://www.androidhive.info/2016/01/android-working-with-recycler-view/

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
                int iterations = imageNum;
                Log.d("imageNumCom", "Value is: " + imageNum);


                DatabaseReference imageRef = database.getReference(String.valueOf(imageNum));


                // Read from the database

                for(int i = 0; i < iterations; i ++){
                    imageRef = database.getReference(String.valueOf(imageNum));
                    imageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            ArrayList<String> data = new ArrayList<>();
                            for(DataSnapshot imageSnapshot : dataSnapshot.getChildren()){
                                String newData = imageSnapshot.getValue(String.class);
                                data.add(newData);
                            }
                            description = data.get(0);
                            title = data.get(2);
                            String stringImage = data.get(1);
                            byte[] decodedString = Base64.decode(stringImage, Base64.DEFAULT);
                            image = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                            // Make our ImageUpload Object
                            imageList.add(new ImageUpload(title, description, image));

                            adapter.notifyDataSetChanged();

                            Log.d("imageTitle", title);
                            Log.d("imageTitle", description);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    imageNum--;
                }

                /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        Log.d("ImageList", String.valueOf(imageList.size()));
    }

}
