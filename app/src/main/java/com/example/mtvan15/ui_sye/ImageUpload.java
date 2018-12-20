package com.example.mtvan15.ui_sye;

import android.graphics.Bitmap;

/***
 * This class represents a single image upload from Firebase presented in a recycler view on our Community activity. Each entry on Firebase contains the saved bitmap as a base64 string and the title and description under which the image was saved.
 */
public class ImageUpload {

    private String title;
    private String description;
    private Bitmap bitmap;

    /***
     * Constructs a new ImageUpload object.
     * @param title the saved title of an image entry in Firebase.
     * @param description the saved description of an image entry in Firebase.
     *      * @param description
     * @param bitmap the saved base64 string representation of an image in Firebase decoded into a bitmap.
     */
    public ImageUpload(String title, String description, Bitmap bitmap){
        this.title = title;
        this.description = description;
        this.bitmap = bitmap;
    }

    /***
     * Returns the title of an ImageUpload object.
     * @return string
     */
    public String getTitle() {
        return title;
    }

    /***
     * Sets the title of an ImageUpload object.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /***
     * Returns the description of an ImageUpload object.
     * @return string
     */
    public String getDescription() {
        return description;
    }

    /***
     * Sets the title of an ImageUpload object.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /***
     * Returns the bitmap of an ImageUpload object.
     * @return string
     */
    public Bitmap getBitmap() {
        return bitmap;
    }

    /***
     * Sets the bitmap of an ImageUpload object.
     */
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
