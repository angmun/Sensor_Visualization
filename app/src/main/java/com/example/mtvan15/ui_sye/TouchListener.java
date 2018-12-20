package com.example.mtvan15.ui_sye;

import android.view.MotionEvent;
import android.view.View;

/***
 * A class that provide the methods required to handle touch events in the main activity when a user is on 'Sensor Draw' mode.
 */
public class TouchListener implements View.OnTouchListener {
    float x = -1;
    float y = -1;

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        this.x = motionEvent.getX();
        this.y = motionEvent.getY();

        return true;
    }

    public float[] getCoordinates(){
        return new float[]{this.x, this.y};
    }
}
