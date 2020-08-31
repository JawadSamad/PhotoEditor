package com.jawad.photoeditor;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Save{
    MainActivity main;

    //Constructor which has 1 parameter and that's the MainActivity
    public Save(MainActivity main){
        super();
        this.main = main;
    }

    //Created an saveMedia method, which saves the image.
    public void saveMedia() {
            //Created an dialog which asks for confirmation if the user wants to save the image
            //In case user accidently presses on save.
            final AlertDialog.Builder builder = new AlertDialog.Builder(main);
            //Created an DialogInterface to detect the clicks on the dialog
            final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                //Has 2 parameters that detects which button has been pressed
                public void onClick(DialogInterface dialog, int which) {
                    //If button is posetive than it will create the image file
                    if(which == DialogInterface.BUTTON_POSITIVE) {
                        //Creating the image file.
                        final File outFile = Picture.createImageFile();
                        try(FileOutputStream out = new FileOutputStream(outFile)){
                            //This will compress the image to an .JPEG file.
                            main.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            //Updating the imageUri path
                            main.imageUri = Uri.parse("file://" + outFile.getAbsolutePath());
                            //Informs the app that an new image has been created
                            main.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, main.imageUri));
                            //Toast to confirm that the image has been saved to the gallery
                            Toast.makeText(main, "Uw foto is opgeslagen", Toast.LENGTH_LONG).show();
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            };
            //Setting the message on the Dialog and assigning the posetive and negative buttons
            builder.setMessage("Foto opslaan in je gallerij?")
                    .setPositiveButton("Ja", dialogClickListener)
                    .setNegativeButton("Nee", dialogClickListener)
                    .show();
    }
}