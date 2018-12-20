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
 * The Community activity displays images saved by the community through the application and
 * provides the user with the option to download an image copy to modify as they desire, known as
 * 'remixing' in or application.
 */
public class Community extends AppCompatActivity {
    // A list of images from Firebase to display in a recycler view on the Community activity.
    List<ImageUpload> imageList;

    // A view that allows us to display images as we download them from the Firebase database where
    // they are saved.
    RecyclerView recyclerView;

    // An image adapter required to manage view holder objects for images to be displayed in the
    // recycler view.
    ImageAdapter adapter;

    // An integer value that allows us to track the saved image's id in Firebase to prepare for
    // display in the recycler view.
    int imageNum = -1;

    // The required data for a single image downloaded from the Firebase database for its display
    // in the recycler view.
    String title;
    String description;
    Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        // Initialize the variables required to set up the recycler view for image display from the
        // Firebase database.
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
     * Sets up saved images for display on the recycler view. An image, its title and description
     * are gotten from the respective entry in the Firebase database, and this information is used
     * to create ImageUpload objects that are added to a list. The list is passed to our image
     * adapter, which handles image display onto the recycler view.
     */
    private void prepareImages(){
        // Get the image count in the Firebase database.
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("imageNum");

        // Read saved image entries from the database.
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                // Update the image count if modified.
                long value = dataSnapshot.getValue(Long.class);
                imageNum = (int) value;

                // Save the initial value of imageNum for use in the for loop condition.
                int iterations = imageNum;

                DatabaseReference imageRef = database.getReference(String.valueOf(imageNum));


                // Read from the database.
                for(int i = 0; i < iterations; i ++){
                    // Get the respective data entry with an id corresponding to imageNum's value.
                    imageRef = database.getReference(String.valueOf(imageNum));
                    // We only need to read data once after which we do not require continuous
                    // listening for data changes.
                    imageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Initialize an arraylist to store values of a single image entry's
                            // properties.
                            ArrayList<String> data = new ArrayList<>();
                            for(DataSnapshot imageSnapshot : dataSnapshot.getChildren()){
                                String newData = imageSnapshot.getValue(String.class);
                                data.add(newData);
                            }
                            description = data.get(0);
                            title = data.get(2);
                            String stringImage = data.get(1);

                            // Convert the base64 string into a bitmap we can display.
                            byte[] decodedString = Base64.decode(stringImage, Base64.DEFAULT);
                            image = BitmapFactory.decodeByteArray(decodedString, 0,
                                    decodedString.length);

                            // Make our ImageUpload Object and add it to our list of images to be
                            // displayed on the recylcer view.
                            imageList.add(new ImageUpload(title, description, image));

                            // Notify the adapter to update the recycler view as we have added to
                            // the list of images to display.
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    // Move to a previous data entry in the Firebase database.
                    imageNum--;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
