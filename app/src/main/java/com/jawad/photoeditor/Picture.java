package com.jawad.photoeditor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Picture{
    MainActivity main;

    public Picture(MainActivity main){
        super();
        this.main = main;
    }

    public void takePicture() {

        //Intent to take picture, Action Image Capture is for capturing an photo
        final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // if it is different then Null, it will create an image file
        if(takePictureIntent.resolveActivity(main.getPackageManager())!= null){
            //Created a file for the photo that was just taken
            final File photoFile = createImageFile();
            //Assigning the photo file to the imageUri
            main.imageUri = Uri.fromFile(photoFile);
            final SharedPreferences myPrefs = main.getSharedPreferences(main.appId, 0 );
            myPrefs.edit().putString("path", photoFile.getAbsolutePath()).apply();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, main.imageUri);
            main.startActivityForResult(takePictureIntent, main.REQUEST_IMAGE_CAPTURE);
        } else {
            //Showing user that users camera application is not compatible
            Toast.makeText(main, "Uw camera voldoet niet aan de gewenste eisen.", Toast.LENGTH_LONG).show();
        }
    }

    //Creating an image file.
    public static File createImageFile(){
        //Getting the current date and assigning it to the file
        final String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.FRANCE).format(new Date());
        //Assigning the imageFileName to .JPEG + timestamp + .jpg
        final String imageFileName = "/JPEG_" + timeStamp + ".jpg";
        //Creating the image file, and storing it in the directory in which the photo will be saved
        final File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //Returning new file pathname including the storageDir and imageFileName
        return new File(storageDir + imageFileName);
    }
}
