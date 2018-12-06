package com.example.mtvan15.ui_sye;

import android.graphics.Canvas;

public class Spiral {

    // Keep track of the coordinates you want to draw at
    private int x;
    private int y;

    private double angleStep;
    private double currAngle;
    private int dimX;
    private int dimY;
    private int maxRadius;
    private int radius;
    private int radiusStep;

    public Spiral(int dim1, int dim2){
        // Keep track of the width and height of the window
        dimX = dim1;
        dimY = dim2;

        // Keep track of the minimum dimension so that it will always draw on the screen
        if(dimX < dimY) {
            maxRadius = dimX;
        }else{
            maxRadius = dimY;
        }

        currAngle = 0.0;
        angleStep = Math.PI / 32;

        this.x = 0;
        this.y = 0;

        radiusStep = 1;
        radius = 1;

    }

    public int[] next(){
        // Check to see if the maximum radius has been exceeded
        if(radius + radiusStep >= maxRadius){
            if(radiusStep > 0){
                radiusStep = -1;
                radius += radiusStep;
            }
        }else if (radius + radiusStep <= 1){
            if(radiusStep < 0){
                radiusStep = 1;
                radius += radiusStep;

            }
        }else{
            // Update the angle you want to use in this situation
            radius += radiusStep;
            currAngle += angleStep;
        }

//        // Check to see if the maximum angle has been exceeded
//        if(angleStep > 0 && currAngle + angleStep >= 2 * Math.PI){
//            angleStep = -angleStep;
//        }else if(radiusStep < 0 && currAngle + angleStep <= 0){
//            angleStep = -angleStep;
//        }



        // Now get the coordinates as expressed by Sin() and Cos()
        return new int[]{(int) ((dimX/2) + radius * Math.cos(currAngle)), (int) ((dimY/2) + radius * Math.sin(currAngle))};
    }
}
