package com.example.mtvan15.ui_sye;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Random;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class MainActivity extends AppCompatActivity implements SensorEventListener, AudioProcessor {

    // Values to keep track of the largest magnitudes seen so far
    private float magnitude = 3.14f;
    private float colorMagnitude = 1f;
    private float lightMagnitude = 1f;
    private float sizeMagnitude = 1f;

    // Check whether certain sensors are enabled
    private boolean gyroIsEnabled = true;
    private boolean touchIsEnabled = false;
    private boolean settings = false;
    private boolean audioThread = false;

    // !! Spiral Code !!
    private boolean spiralEnabled = true;

    // Setup UI View Elements
    private Button button;
    private Canvas canvas;
    private Paint paint;
    private Bitmap bitmap;
    private ImageView imageView;
    private TextView audioInfo;

    // Sensor Manager and the Sensors
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private Sensor gyroscopeSensor;
    private SilenceDetector silenceDetector;
    private double threshold;

    // Keep a reference to our touchListener class
    private TouchListener touchListener;

    // Keep a reference to a Spiral Class
    private Spiral spiral;

    // Important drawing elements for the canvas (parameters)
    private int color[] = {0, 0, 0};
    private int sizeRadius = 1;
    private int opacity = 0;
    private int x = -1;
    private int y = -1;
    private int width;
    private int height;

    // Determine whether sensors can start writing values
    private boolean begin = false;

    // Setup Firebase Cloud Storage
    private StorageReference mStorageRef;
    FirebaseDatabase database = FirebaseDatabase.getInstance();


    // Keep track of permissions for the application
    private final static int PERMISSION_REQUEST_CODE = 999;
    private boolean permissions_granted;
    private final static String LOGTAG =
            MainActivity.class.getSimpleName();

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * onStart( ) life cycle method
     */
    @Override
    protected void onStart() {
        super.onStart();

    }

    /**
     * onResume( ) life cycle method. Register the sensorListeners in here assuring they are ready for data.
     */
    @Override
    protected void onResume() {
        super.onResume();

        Log.i("MagResume", String.valueOf(this.height));

        if (settings) {
            begin = true;
            settings = false;
        }

        if (!audioThread && checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            this.permissions_granted = true;
            createAudioThread();
        }
        // Register a listener for the proximity sensory
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_GAME);
        // Register a listener for the gyroscope sensor
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
        // Now we are ready to start
    }

    /**
     * onCreate( ) life cycle method. Handles UI elements and checks for permissions.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check all permissions
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    this,
                    new String[] { Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE
            );
        }

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Setup Firebase Storage
        mStorageRef = FirebaseStorage.getInstance().getReference();
        DatabaseReference myRef = database.getReference("message");

        imageView = findViewById(R.id.imageView);
        this.touchListener = new TouchListener();
        imageView.setOnTouchListener(this.touchListener);

        this.x = imageView.getWidth() /2;
        this.y = imageView.getHeight() / 2;

        paint = new Paint();

        paint.setColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

    }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * BottomNavigationView is the primary navigation element in our application.
     * This method handles events based on which bottom navigation tab was selected.
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.sensorDraw:
                    enableSensor(true);
                    return true;
                case R.id.touchDraw:
                    enableSensor(false);
                    return true;
                case R.id.settings:
                    begin = false;
                    toSettings();
                    return true;
                case R.id.save:
                    saveImage();
                    return true;
            }
            return false;
        }
    };

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Checks the permissions for AUDIO, READ/WRITE_EXTERNAL_STORAGE
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            Log.i("SENSORP", String.valueOf(grantResults[0]));
            Log.i("SENSORP", String.valueOf(grantResults[1]));
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                this.permissions_granted = true;
                Log.i(LOGTAG, "Audio and External Storage Allowed");
                createAudioThread();
            }
            else {
                this.permissions_granted = false;
                Log.i(LOGTAG, "Audio and External Storage Not Allowed");
            }
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Method calling an intent to the Setting Activity
     */
    private void toSettings(){
        settings = true;
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Enables sensors in our application based on the option selected.
     * The main sensor we toggle ON/OFF based on the navigation bar option is the GYROSCOPE
     * @param gyro
     */
    private void enableSensor(boolean gyro){
        if (gyro) {
            gyroIsEnabled = true;
            touchIsEnabled = false;
        } else {
            gyroIsEnabled = false;
            touchIsEnabled = true;
        }
        this.bitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);
        this.begin = true;
        width = imageView.getWidth();
        height = imageView.getHeight();
        this.spiral = new Spiral(imageView.getWidth(), imageView.getHeight());
    }

    /**
     * Saves the current bitmap to the phone/tablet's gallery
     */
    private void saveImage(){
        // Save image to gallery
        MediaStore.Images.Media.insertImage(getContentResolver(), this.bitmap, "", "");
        // Push image to firebase if there is an internet connection
    }

    /**
     * Create an AudioThread for tarosDSP such that we can detect PITCH and LOUDNESS.
     */
    public void createAudioThread(){
        // Audio Stuff For now
        // Audio Instance Variables
        audioThread = true;
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);

        //Setup loudness detection.
        this.threshold = SilenceDetector.DEFAULT_SILENCE_THRESHOLD;
        silenceDetector = new SilenceDetector(threshold, false);
        dispatcher.addAudioProcessor(silenceDetector);
        dispatcher.addAudioProcessor(this);


        this.audioInfo = findViewById(R.id.audioInfo);
        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processPitch(pitchInHz);
                    }
                });
            }
        };

        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(pitchProcessor);

        Thread audioThread = new Thread(dispatcher, "Audio Thread");
        audioThread.start();

    }

    /**
     * Process incoming Hz sound data and convert it to a unique color.
     * @param pitchInHz
     */
    public void processPitch(float pitchInHz) {

        if(pitchInHz != -1) {

            this.color = soundToColor(pitchInHz);
        }
    }

    /**
     * MapRange Function
     * @param min destination value
     * @param max destination value
     * @param val value to scale that is within range magnitude
     * @return scaled number between min and max
     */
    public int mapRange(int aCase, int min, int max, float val) {

        // REFORMAT THIS. THIS KIND OF LOOKS LIKE CRAP....

        switch(aCase){
            case 0:
                if (Math.abs(val) > magnitude) {
                    magnitude = Math.abs(val);
                }

                return min + (((int)(val * 1000) + (int)(magnitude*1000))*(max - min) / (int)(magnitude*2000));
            case 1:
                if (Math.abs(val) > colorMagnitude) {
                    colorMagnitude = Math.abs(val);
                }

                return min + (((int)(val * 1000) + (int)(colorMagnitude*1000))*(max - min) / (int)(colorMagnitude*2000));
            case 2:
                if (Math.abs(val) > lightMagnitude) {
                    lightMagnitude = Math.abs(val);
                }

                return min + (((int)(val * 1000) + (int)(lightMagnitude*1000))*(max - min) / (int)(lightMagnitude*2000));
            case 3:
                if (Math.abs(val) > sizeMagnitude) {
                    sizeMagnitude = Math.abs(val);
                }

                return min + (((int)(val * 1000) + (int)(sizeMagnitude*1000))*(max - min) / (int)(sizeMagnitude*2000));
        }

        return -1;
    }

    /**
     * DrawSomething Function
     * @param x location to draw a rectangle (x)
     * @param y location to draw rectangle (y)
     */
    public void drawSomething(int x, int y, Bitmap bitmap) {
        //int height = 300;

        if (width > 0) {
            int radius = this.sizeRadius;

            this.audioInfo.setText(String.format("%d %d %d", this.color[0], this.color[1], this.color[2]));

            paint.setARGB(this.opacity, this.color[0], this.color[1],this.color[2]);

            int locX = mod(x, width);
            int locY = mod(y, height);

            //Associate imageview with the bitmap
            imageView.setImageBitmap(bitmap);
            //Initialize canvas
            canvas = new Canvas(bitmap);
            //Make a rectangle with dimensions
            //rect.set(locX + rectWidth, locY + rectHeight, locX, locY);
            //Draw the rectangle
            //canvas.drawRect(rect, paint);
            canvas.drawCircle(locX, locY, radius, paint);
            //Invalidate
            imageView.invalidate();
        }
    }

    /**
     * A true mod function that computes the modulus similar to Python
     * @param x
     * @param y
     * @return
     */
    public int mod(int x, int y){
        if(y == 0) return 0;
        if(x > 0) return x % y;
        return y - (Math.abs(x) % y);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // Get the type of the event triggering this method call
        int sensorType = sensorEvent.sensor.getType();

        if(begin) {

            switch (sensorType) {

                case Sensor.TYPE_GYROSCOPE:
                    // x y z in indices 0 1 2
                    if(this.spiralEnabled){
                        this.x = spiral.next()[0];
                        this.y = spiral.next()[1];
                    }else if(gyroIsEnabled) {
                        float[] gyroscopeValues = sensorEvent.values;

                        // float values between 0 and 2pi
                        Log.i("Main_Activity", String.valueOf(gyroscopeValues[0]));

                        this.x = mapRange(0, 0, imageView.getWidth(), gyroscopeValues[1]);
                        this.y = mapRange(0, 0, imageView.getHeight(), gyroscopeValues[0]);
                    }else if (this.touchIsEnabled){
                        float[] coordinates = this.touchListener.getCoordinates();
                        this.x = (int)coordinates[0];
                        this.y = (int)coordinates[1];
                    }
                    drawSomething(this.x, this.y, this.bitmap);


                    break;
                case Sensor.TYPE_LIGHT:
                    float currentValue = sensorEvent.values[0];
                    this.opacity = 75 - mapRange(2, 0, 70, currentValue);
                    break;
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public int[] soundToColor(float hertz){

        if (hertz > 0) {
            // Map Hz from 25.4 and 1760 -----> 0 and 1530
            int color = mapRange(1, 200, 1530, hertz);
            int[] returnColor = new int[3];
            int mod = mod(color, 255);

            if (color <= 255) {
                returnColor[0] = 255;
                returnColor[1] = mod;
                returnColor[2] = 0;
            } else if (color <= 255 * 2) {
                returnColor[0] = 255 - mod;
                returnColor[1] = 255;
                returnColor[2] = 0;
            } else if (color <= 255 * 3) {
                returnColor[0] = 0;
                returnColor[1] = 255;
                returnColor[2] = mod;
            } else if (color <= 255 * 4) {
                returnColor[0] = 0;
                returnColor[1] = 255 - mod;
                returnColor[2] = 255;
            } else if (color <= 255 * 5) {
                returnColor[0] = mod;
                returnColor[1] = 0;
                returnColor[2] = 255;
            } else {
                returnColor[0] = 255;
                returnColor[1] = 0;
                returnColor[2] = 255 - mod;
            }

            return returnColor;

        }

        return new int[]{0, 0, 0};
    }

    @Override
    public boolean process(AudioEvent audioEvent) {
        handleSound();
        return true;
    }

    private void handleSound(){
        if(silenceDetector.currentSPL() > threshold){
            this.sizeRadius = mapRange(3, 5, height, (float) silenceDetector.currentSPL());

            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    audioInfo.setText(audioInfo.getText() + String.valueOf(sizeRadius));
                }
            });
        }
    }
    @Override
    public void processingFinished() {

    }
}
