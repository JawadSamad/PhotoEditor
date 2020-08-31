package com.jawad.photoeditor;

import android.content.Intent;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.Objects;

public class Share {
    MainActivity main;

    public Share(MainActivity main){
        super();
        this.main = main;
    }

    public void sharedMedia() {

        try {
            // Assigning the imageUri path to a new file
            File file = new File(main.imageUri.getPath());
            //Creating an intent which has the Action Send params
            Intent intent = new Intent(Intent.ACTION_SEND);
            FileProvider.getUriForFile(Objects.requireNonNull(main), BuildConfig.APPLICATION_ID + ".provider", file);
            intent.setDataAndType(main.imageUri, "*/*");
            //For whatsapp only;
            //intent.setPackage("com.whatsapp");
            intent.putExtra(Intent.EXTRA_STREAM, main.imageUri);
            main.startActivity(Intent.createChooser(intent, "Delen via"));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
