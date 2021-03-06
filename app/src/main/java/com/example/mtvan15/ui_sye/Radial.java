package com.example.mtvan15.ui_sye;

import android.graphics.Point;

import java.util.ArrayList;

/***
 * The radial class generates coordinates for drawing a radial pattern on the canvas if a user
 * selects the 'Radial Motion' special mode in preferences.
 */
public class Radial {
    // Instance variables
    private double angleStep;
    private double currAngle1;
    private double currAngle2;
    private double currAngle3;
    private double currAngle4;
    private int dimX;
    private int dimY;
    private int maxRadius;
    private int radius;
    private int radiusStep;

    /***
     * Radial class constructor.
     * @param dim1 window width
     * @param dim2 window height
     */
    public Radial(int dim1, int dim2){
        // Keep track of the width and height of the window.
        dimX = dim1;
        dimY = dim2;

        // Keep track of the minimum dimension so that it will always draw on the screen.
        if(dimX < dimY) {
            maxRadius = dimX / 2;
        }else{
            maxRadius = dimY / 2;
        }

        // The initial angles for the four radial paths as well as the change when next is called
        // to find the next coordinates.
        currAngle1 = 0.0;
        currAngle2 = Math.PI/2;
        currAngle3 = Math.PI;
        currAngle4 = 3*Math.PI/2;

        angleStep = Math.PI / 64;

        // The initial radius value as well as the change when next is called to find the next
        // coordinates.
        radiusStep = 1;
        radius = 1;

    }

    /***
     * Generate the next points at which to draw on the canvas to continue the radial pattern.
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
        // Update the radius and angles you want to use in this situation.
        radius += radiusStep;
        currAngle1 += angleStep;
        currAngle2 += angleStep;
        currAngle3 += angleStep;
        currAngle4 += angleStep;

        // Now get and return the coordinates as expressed by Sin() and Cos().
        Point p1 = new Point((int) ((dimX/2) + radius * Math.cos(currAngle1)), (int) ((dimY/2) + radius * Math.sin(currAngle1)));
        Point p2 = new Point((int) ((dimX/2) + radius * Math.cos(currAngle2)), (int) ((dimY/2) + radius * Math.sin(currAngle2)));
        Point p3 = new Point((int) ((dimX/2) + radius * Math.cos(currAngle3)), (int) ((dimY/2) + radius * Math.sin(currAngle3)));
        Point p4 = new Point((int) ((dimX/2) + radius * Math.cos(currAngle4)), (int) ((dimY/2) + radius * Math.sin(currAngle4)));

        return new int[]{p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y};
    }
}
