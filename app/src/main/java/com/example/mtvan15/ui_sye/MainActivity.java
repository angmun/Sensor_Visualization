package com.example.mtvan15.ui_sye;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    float magnitude = 3.14f;

    private Button button;
    private Canvas canvas;
    private Paint paint;
    private Bitmap bitmap;
    private ImageView imageView;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private Sensor gyroscopeSensor;

    private Rect rect = new Rect();

    private int color = 0;
    private int x = -1;
    private int y = -1;

    private boolean begin = false;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.sensorDraw:
                    return true;
                case R.id.touchDraw:
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
        // Register a listener for the proximity sensory
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_GAME);
        // Register a listener for the gyroscope sensor
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
        // Now we are ready to start
        this.begin = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * OnCreate: setup bottomNavigationView, sensorManager, lightSensor, and other sensors
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Log.i("Scaling", String.valueOf(mapRange(0, 255, -3.14f)));
        Log.i("Scaling", String.valueOf(mapRange(0, 255, 0f)));
        Log.i("Scaling", String.valueOf(mapRange(0, 255, 3.14f)));

        Log.i("Scaling", String.valueOf(mapRange(0, 255, -6.28f)));
        Log.i("Scaling", String.valueOf(mapRange(0, 255, 3.14f)));

        imageView = findViewById(R.id.imageView);

        this.x = imageView.getWidth() /2;
        this.y = imageView.getHeight() / 2;

        paint = new Paint();

        paint.setColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

    }

    /**
     * MapRange Function
     * @param min destination value
     * @param max destination value
     * @param val value to scale that is within range magnitude
     * @return scaled number between min and max
     */
    public int mapRange(int min, int max, float val) {
        if (Math.abs(val) > magnitude) {
            magnitude = Math.abs(val);
        }

        return min + (((int)(val * 1000) + (int)(magnitude*1000))*(max - min) / (int)(magnitude*2000));
    }

    /**
     * DrawSomething Function
     * @param x location to draw a rectangle (x)
     * @param y location to draw rectangle (y)
     */
    public void drawSomething(int x, int y) {

        int width = imageView.getWidth();
        //int width = 300;
        int height = imageView.getHeight();
        //int height = 300;

        if (width > 0) {

            int color = this.color;
            int rectWidth = width / 6;
            int rectHeight = height / 6;

            paint.setARGB(color, color, color, 1);

            Log.i("Main_Activity", String.valueOf(width));

            int locX = mod(x , (width - rectWidth));
            int locY = mod(y , (height - rectHeight));

            //Initialize bitmap
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            //Associate imageview with the bitmap
            imageView.setImageBitmap(bitmap);
            //Initialize canvas
            canvas = new Canvas(bitmap);
            //Make a rectangle with dimensions
            rect.set(locX + rectWidth, locY + rectHeight, locX, locY);
            //Draw the rectangle
            canvas.drawRect(rect, paint);
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

//                // scale the x value
//                this.x = (int)((imageView.getWidth())*(gyroscopeValues[0] + 2 * Math.PI)/(4 * Math.PI));
//
//                // scale the y value
//                this.y = (int)((imageView.getHeight())*(gyroscopeValues[1] + 2 * Math.PI)/(4 * Math.PI));

                    this.x = (int) (this.x + (20 * gyroscopeValues[1]));
                    this.y = (int) (this.y + (20 * gyroscopeValues[0]));

                    drawSomething(this.x, this.y);

                    break;
                case Sensor.TYPE_LIGHT:
                    float currentValue = sensorEvent.values[0];
                    this.color = ((int) currentValue) % 255;
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
}
