package com.example.mtvan15.ui_sye;

import android.graphics.Canvas;

/***
 * The spiral class generates coordinates for drawing a spiral pattern on the canvas if a user
 * selects the 'Spiral Motion' special mode in preferences.
 */
public class Spiral {
        // Instance variables
        private double angleStep;
        private double currAngle;
        private int dimX;
        private int dimY;
        private int maxRadius;
        private int radius;
        private int radiusStep;

    /***
     * Spiral class constructor.
      * @param dim1 window width
     * @param dim2 window height
     */
    public Spiral(int dim1, int dim2){
        // Keep track of the width and height of the window.
        dimX = dim1;
        dimY = dim2;

        // Keep track of the minimum dimension so that it will always draw on the screen.
        if(dimX < dimY) {
            maxRadius = dimX / 2;
        }else{
            maxRadius = dimY / 2;
        }

        // The initial angle as well as the change when next is called to find the next coordinates.
        currAngle = 0.0;
        angleStep = Math.PI / 32;

        // The initial radius value as well as the change when next is called to find the next
        // coordinates.
        radiusStep = 1;
        radius = 1;

    }

    /***
     * Generate the next points at which to draw on the canvas to continue the spiral pattern.
     * @return an array of points at which to draw on the canvas
     */
    public int[] next(){
        // Check to see if the maximum radius has been exceeded.
        if(radius + radiusStep >= maxRadius){
            if(radiusStep > 0){
                radiusStep = -1;
            }
        }else if (radius + radiusStep <= 1){
            if(radiusStep < 0){
                radiusStep = 1;
            }
        }
        // Update the radius and angle you want to use in this situation.
        radius += radiusStep;
        currAngle += angleStep;

        // Now get and return the coordinates as expressed by Sin() and Cos().
        return new int[]{(int) ((dimX/2) + radius * Math.cos(currAngle)), (int) ((dimY/2) + radius * Math.sin(currAngle))};
    }
}
