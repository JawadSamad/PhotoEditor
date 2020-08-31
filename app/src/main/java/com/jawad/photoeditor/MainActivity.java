package com.jawad.photoeditor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    /*Instantiated 5 objects which all hold their own functionality. See classes of the objects to understand
    what the functionality is.*/
   Save saveButton = new Save(this);
   Picture pictureButton = new Picture(this);
   Image imageButton = new Image(this);
   Month monthButton = new Month(this);
   Share shareButtonMedia = new Share(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    @SuppressLint("NewApi")
    //Request permissions
    private static final int REQUEST_PERMISSIONS = 1234;
    //defining the permissions using an String array
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //Requesting 2 permissions
    private static final int PERMISSIONS_COUNT = 2;

    private boolean noPermissions() {
        for(int i = 0; i < PERMISSIONS_COUNT; i++){
            if(checkSelfPermission(PERMISSIONS[i])!= PackageManager.PERMISSION_GRANTED){
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Checking if android is at least Marshmallow or greater.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && noPermissions()){
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
        }
    }

    @Override
    //Overriding method which checks if permissions has been granted or denied.
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] PERMISSIONS, @NonNull int[] grantResult){
        super.onRequestPermissionsResult(requestCode, PERMISSIONS, grantResult);
        //Checking if user granted or denied permissions
        if(requestCode == REQUEST_PERMISSIONS && grantResult.length > 0) {
            //If the app does not get permission, it will clear the application data after closing the app
            if(noPermissions()) {
                try {
                    ((ActivityManager)(Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE)))).clearApplicationUserData();
                    recreate();
                } catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
        }
    }


    public final int REQUEST_PICK_IMAGE = 12345;
    private ImageView imageView;

    //Only initializes if user grants permission
    private void init() {

        //Assigned various buttons to the layout buttons and added an onclick listener for each button
        imageView = findViewById(R.id.imageView);
        final Button selectImageButton = findViewById(R.id.selectImageButton);
        final Button takePhotoButton = findViewById(R.id.takePhotoButton);
        final Button saveImage = findViewById(R.id.saveButton);
        final Button shareButton = findViewById(R.id.shareButton);
        final CheckBox checkBox = findViewById(R.id.mvdm);
        final TextView monthText = findViewById(R.id.MVDM);

        //Overcoming Android Nougat security restriction policy regarding the camera. Without this the
        // take picture button will cause an crash. Just checking if the version is greater than Nougat
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        //Check if users device has an camera. Otherwise it will hide the take photo button
        if(!MainActivity.this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
            findViewById(R.id.takePhotoButton).setVisibility(View.GONE);
        }

        //Created an select image button that redirects the user to their gallery/downloads, see Image class for further functionality
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageButton.saveImage();
            }
        });

        //Created an take photo button, which redirect user to their camera app, see Picture class for further functionality
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pictureButton.takePicture();
            }
        });

        //Created an save button which saves the media, inspect Save class for further functionality
        saveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButton.saveMedia();
            }
        });

        //Created an checkbox which show an message when pressed, see Month class for further funtionality
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monthButton.date();
                monthText.setText(monthButton.date());
            }
        });

        //Created an share button which share to other app, see Share class for further functionality
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareButtonMedia.sharedMedia();
            }
        });
    }

    //Created an save method which saves the image when the "save" button has been pressed.
    public void pressedSaveButton(View view) {
        //Created an intent to perform the activity
        Intent toSaveButton = new Intent(MainActivity.this, Save.class);
        //Launching the activity
        startActivity(toSaveButton);
    }

    public final int REQUEST_IMAGE_CAPTURE = 1012;
    public final String appId = "Medewerker van de maand";
    public Uri imageUri;

    public boolean editMode = false;

    public Bitmap bitmap;

    private int width = 0;
    private int height = 0;
    //Maximum resolution to 2048 in width and 2048 in height
    private static final int MAX_PIXEL_COUNT = 2048;
    //Storing all the pixels into an int array
    private int[] pixels;
    private int pixelCount = 0;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Checking if, selecting an image went succesfully otherwise it will return
        if(resultCode != RESULT_OK) {
            return;
        }
        //Checking if the request code is equal to an image capture
        if(requestCode == REQUEST_IMAGE_CAPTURE) {
            //Checking if image has been lost in the process
            if(imageUri == null) {
                //Getting the image back from the sharedPrefences
                final SharedPreferences p = getSharedPreferences(appId, 0);
                //Getting the path string.
                final String path = p.getString("path", "");
                //Checking if the path is null
                if(path.length() < 1) {
                    //Relaunching application, because it's not recoverable
                    recreate();
                    return;
                }
                //if path is greater than 1, than the path will be assigned to the imageUri
                imageUri = Uri.parse("file://" + path);
            }
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));
        } else if(data == null){
            //Relaunching application
            recreate();
            return;
        } else if(requestCode == REQUEST_PICK_IMAGE){
            imageUri = data.getData();
        }
        //Dialog which shows that the image is loading
        final ProgressDialog dialog = ProgressDialog.show(MainActivity.this, "Laden", "Even geduld aub", true);
        //after getting the image succesfully, the application will be set to editMode = true and switched to xml EdtiScreen, see activity_main.xml for
        //further functionality
        editMode = true;

        findViewById(R.id.welcomeScreen).setVisibility(View.GONE);
        findViewById(R.id.editScreen).setVisibility(View.VISIBLE);

        //Using a Thread in order to quicken the process of loading the image.
        new Thread() {
            public void run() {
                //Checks the state of the bitmap
                bitmap = null;
                //Opens the settings/options of bitmap
                final BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                //Enables the inBitmap features, so that I can change the content later on
                bitmapOptions.inBitmap = bitmap;
                bitmapOptions.inJustDecodeBounds = true;
                //Using the imageUri to load the image
                try(InputStream input = getContentResolver().openInputStream(imageUri)) {
                    bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                bitmapOptions.inJustDecodeBounds = false;
                //Setting the width and height to the outWidth and outHeight
                width = bitmapOptions.outWidth;
                height = bitmapOptions.outHeight;

                //Resizing the image so it fits on the screen
                int resizeScale = 1;
                if(width > MAX_PIXEL_COUNT) {
                    resizeScale = width / MAX_PIXEL_COUNT;
                } else if (height > MAX_PIXEL_COUNT) {
                    resizeScale = height / MAX_PIXEL_COUNT;
                }
                //In any of these cases, the scale will be resized and get a bit larger
                if(width / resizeScale > MAX_PIXEL_COUNT || height / resizeScale > MAX_PIXEL_COUNT) {
                    resizeScale++;
                }

                //Setting the bitmapoptions to the resizingscale
                bitmapOptions.inSampleSize = resizeScale;
                InputStream input = null;

                try {
                    input = getContentResolver().openInputStream(imageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    recreate();
                    return;
                }

                //Reading the bitmap that i;m editing
                bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
                //In order to make changes to the screen, i'm running this on the UiThread.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Setting bitmap to the ImageView so that it is showing the image
                        imageView.setImageBitmap(bitmap);
                        //Cancelling the progressdialog
                        dialog.cancel();
                    }
                });

                width = bitmap.getWidth();
                height = bitmap.getHeight();
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                //Creating 5 imageButtons, which all places an sticker over the imageView/image, all do basically the same, but have
                // different location values
                final ImageButton decoButton = findViewById(R.id.buttonDeco);
                decoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bitmap deco = BitmapFactory.decodeResource(getResources(), R.drawable.sticker_deco);
                        //Assigning width and height values to the deco Bitmap
                        Bitmap resizedDeco = Bitmap.createScaledBitmap(deco, 250, 250, true);
                        //Creating a new canvas which holds the image as an bitmap
                        Canvas canvas = new Canvas(bitmap);
                        //Drawing the deco bitmap over the image/photo bitmap. Using hardcoded values for the location of the stickers for now.
                        canvas.drawBitmap(resizedDeco,100,100,new Paint());
                        imageView.setImageBitmap(bitmap);
                    }
                });

                final ImageButton fireButton = findViewById(R.id.buttonFire);
                fireButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bitmap fire = BitmapFactory.decodeResource(getResources(), R.drawable.sticker_fire);
                        Bitmap resizedFire = Bitmap.createScaledBitmap(fire, 250, 250, true);
                        Canvas canvas = new Canvas(bitmap);
                        canvas.drawBitmap(resizedFire,(width)/1000,650,new Paint());
                        imageView.setImageBitmap(bitmap);
                    }
                });

                final ImageButton rainbowButton = findViewById(R.id.buttonRainbow);
                rainbowButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bitmap rainbow = BitmapFactory.decodeResource(getResources(), R.drawable.sticker_rainbow);
                        Bitmap resizedRainbow = Bitmap.createScaledBitmap(rainbow, 150, 150, true);
                        Canvas canvas = new Canvas(bitmap);
                        canvas.drawBitmap(resizedRainbow,800,850,new Paint());
                        imageView.setImageBitmap(bitmap);
                    }
                });

                final ImageButton glassesButton = findViewById(R.id.buttonGlasses);
                glassesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bitmap glasses = BitmapFactory.decodeResource(getResources(), R.drawable.sticker_sunglasses);
                        Bitmap resizedGlasses = Bitmap.createScaledBitmap(glasses, 250, 250, true);
                        Canvas canvas = new Canvas(bitmap);
                        canvas.drawBitmap(resizedGlasses,500,650,new Paint());
                        imageView.setImageBitmap(bitmap);
                    }
                });

                final ImageButton crownButton = findViewById(R.id.buttonCrown);
                crownButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bitmap crown = BitmapFactory.decodeResource(getResources(), R.drawable.sticker_crown);
                        Bitmap resizedCrown = Bitmap.createScaledBitmap(crown, 250, 250, true);
                        Canvas canvas = new Canvas(bitmap);
                        canvas.drawBitmap(resizedCrown, 500,250,new Paint());
                        imageView.setImageBitmap(bitmap);
                    }
                });

                pixelCount = width * height;
                pixels = new int [pixelCount];
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            }
        }.start();
    }
}
