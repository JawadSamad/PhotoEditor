package com.jawad.photoeditor;

import android.content.Intent;
import android.provider.MediaStore;

public class Image{
    public MainActivity main;

    public Image(MainActivity main){
        super();
        this.main = main;
    }

    public void saveImage() {
        //Created an intent and assigned an Action get content
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //Specified which kind of files the intent can open
        intent.setType("image/*");

        // Created another intent and assigned Action pick, this will give the user options that the user can use,, uploading from downloads, gallery etc.
        final Intent pickIntent = new Intent(Intent.ACTION_PICK);
        //Setting data and type
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

        //Enables the user to choose a image
        final Intent chooserIntent = Intent.createChooser(intent, "Kies een afbeelding");
        main.startActivityForResult(chooserIntent, main.REQUEST_PICK_IMAGE);
    }
}
