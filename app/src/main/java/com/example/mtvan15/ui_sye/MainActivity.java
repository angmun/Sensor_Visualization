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


    float magnitude = 3.14f;
    float colorMagnitude = 0f;
    float lightMagnitude = 0f;

    private Button button;
    private Canvas canvas;
    private Paint paint;
    private Bitmap bitmap;
    private ImageView imageView;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private Sensor gyroscopeSensor;
    private SilenceDetector silenceDetector;
    private int sizeRadius = 0;
    private double threshold;

    private Rect rect = new Rect();

    private TextView audioInfo;

    private int color[] = {0, 0, 0};
    private int opacity = 0;
    private int x = -1;
    private int y = -1;

    private boolean begin = false;

    // Permissions Code!/////////////////////////////////////
    private final static int PERMISSION_REQUEST_CODE = 999;
    private boolean permissions_granted;
    private final static String LOGTAG =
            MainActivity.class.getSimpleName();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.sensorDraw:
                    enableSensor();
                    return true;
                case R.id.touchDraw:
                    return true;
                case R.id.settings:
                    toSettings();
                    return true;
            }
            return false;
        }
    };

    /**
     * OnStart method to setup the listeners for the sensorManager sensors that are in use
     */
    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // we have only asked for FINE LOCATION
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.permissions_granted = true;
                Log.i(LOGTAG, "Audio Recording Allowed");
            }
            else {
                this.permissions_granted = false;
                Log.i(LOGTAG, "Audio Recording Not Allowed");
            }
        }
    }

    private void toSettings(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void enableSensor(){
        this.begin = !begin;
        //Initialize bitmap
        this.bitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register a listener for the proximity sensory
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_GAME);
        // Register a listener for the gyroscope sensor
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
        // Now we are ready to start
    }

    /**
     * OnCreate: setup bottomNavigationView, sensorManager, lightSensor, and other sensors
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] { Manifest.permission.RECORD_AUDIO },
                    PERMISSION_REQUEST_CODE
            );
        }

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        imageView = findViewById(R.id.imageView);

        this.x = imageView.getWidth() /2;
        this.y = imageView.getHeight() / 2;

        paint = new Paint();

        paint.setColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


        // Audio Stuff For now
        // Audio Instance Variables
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
        }

        return -1;
    }

    /**
     * DrawSomething Function
     * @param x location to draw a rectangle (x)
     * @param y location to draw rectangle (y)
     */
    public void drawSomething(int x, int y, Bitmap bitmap) {

        int width = imageView.getWidth();
        //int width = 300;
        int height = imageView.getHeight();
        //int height = 300;

        if (width > 0) {
            //int rectWidth = width / 6;
            //int rectHeight = height / 6;
            int radius = height/9;

            this.audioInfo.setText(String.format("%d %d %d", this.color[0], this.color[1], this.color[2]));

            paint.setARGB(this.opacity, this.color[0], this.color[1],this.color[2]);

            Log.i("Main_Activity", String.valueOf(width));
//
//            int locX = mod(x , (width - rectWidth));
//            int locY = mod(y , (height - rectHeight));

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
                    float[] gyroscopeValues = sensorEvent.values;

                    // float values between 0 and 2pi
                    Log.i("Main_Activity", String.valueOf(gyroscopeValues[0]));

                    this.x = mapRange(0,0, imageView.getWidth(), gyroscopeValues[1]);
                    this.y = mapRange(0,0, imageView.getHeight(), gyroscopeValues[0]);

                    drawSomething(this.x, this.y, this.bitmap);

                    break;
                case Sensor.TYPE_LIGHT:
                    float currentValue = sensorEvent.values[0];
                    this.opacity = 255 - mapRange(2, 0, 255, currentValue);
                    break;
            }


            // Add switch statement if we had more sensors to take care of
            // Scale this value between 0 and 255


//        Log.i("Sensor", String.valueOf(currentValue));
//        // Pass draw something a view
//        drawSomething(this.imageView);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public int[] soundToColor(float hertz){

        if (hertz > 0) {
            // Map Hz from 25.4 and 1760 -----> 0 and 1530
            int color = mapRange(1, 0, 1530, hertz);
            int[] returnColor = new int[3];
            int mod = color % 255;

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
            // See if this doesn't crash
        }
    }
    @Override
    public void processingFinished() {

    }
}
