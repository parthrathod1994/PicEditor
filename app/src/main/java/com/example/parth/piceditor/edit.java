package com.example.parth.piceditor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by parth on 05-02-2018.
 */

public class edit extends AppCompatActivity {

    final int CAMERA_CAPTURE = 1;
    final int PIC_CROP = 2;
    private Uri picUri;

    ScaleGestureDetector scaleGestureDetector;

    ImageView imageView;
    private int PICK_IMAGE = 1;
    Uri imageUri;
    float scale=1f;
    float angle=0;
    Spinner spinner;
    Bitmap image;
    InputStream inputStream;
    Matrix matrix;

    ImageButton bt1,bt2;

    public static final int REQUEST_CODE = 1014;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        overridePendingTransition(0, 0);

        scaleGestureDetector = new ScaleGestureDetector(this,new ScaleListener());

        List<String> categories = new ArrayList<>();
        categories.add("Select Option");
        categories.add("Rotate");
        categories.add("Crop");
        categories.add("Send Image");
        categories.add("Send File");

        spinner = (Spinner) findViewById(R.id.spinner2);
        imageView = (ImageView) findViewById(R.id.imgMain);
        bt1 = (ImageButton) findViewById(R.id.button1);
        bt2 = (ImageButton) findViewById(R.id.button2);

        bt1.setVisibility(View.GONE);
        bt2.setVisibility(View.GONE);

        //spinner data
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,categories);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String item = adapterView.getItemAtPosition(i).toString();
                if(item == "Rotate") {
                    bt1.setVisibility(View.VISIBLE);
                    bt2.setVisibility(View.VISIBLE);
                }
                else if (item == "Crop") {
                    bt1.setVisibility(View.GONE);
                    bt2.setVisibility(View.GONE);
                    cropperdemo();
                }
                else if( item == "Send Image")
                {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_STREAM, getImageUri(getApplicationContext(),image));
                    startActivity(intent);
                }
                else if(item == "Send File")
                {
                    File f = null;
                    try {
                        f = File.createTempFile("instructions", ".txt", getExternalCacheDir());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    FileWriter fw = null;
                    try {
                        fw = new FileWriter(f);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    BufferedWriter w = new BufferedWriter(fw);
                    try {
                        w.write("rorate by 180 degree");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        w.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("*/*");
                    intent.putExtra(intent.EXTRA_STREAM, Uri.fromFile(f));
                    startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        openGallery();

    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public void cropperdemo()
    {
        try {
            Uri sourceUri = imageUri;
            CropImage.activity(sourceUri).setActivityTitle("Crop").setGuidelines(CropImageView.Guidelines.ON).start(this);
        }
        catch (Exception e){}
    }

    private void openGallery()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,PICK_IMAGE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        scaleGestureDetector.onTouchEvent(ev);

        return true;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            try {
                imageUri = data.getData();
                inputStream = getContentResolver().openInputStream(imageUri);
                image=BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(image);
            } catch (Exception e) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                        .show();
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                try {
                    imageUri = result.getUri();
                    inputStream = getContentResolver().openInputStream(imageUri);
                    image = BitmapFactory.decodeStream(inputStream);
                    imageView.setImageBitmap(image);

                } catch (Exception e) {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                            .show();
                }
            }
        }
    }

    public void rotateplus(View view)
    {
        angle += 90;
        Bitmap rotatedImage=rotateImage(image,angle);
        imageView.setImageBitmap(rotatedImage);
    }

    public void rotateminus(View view)
    {
        angle -= 90;
        Bitmap rotatedImage=rotateImage(image,angle);
        imageView.setImageBitmap(rotatedImage);
    }

    //Rotating Image
    public static Bitmap rotateImage(Bitmap sourceImage, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(sourceImage, 0, 0, sourceImage.getWidth(), sourceImage.getHeight(), matrix, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener{
        public boolean onScaleBegin(ScaleGestureDetector sgd)
        {
            return true;
        }
        public void onScaleEnd(ScaleGestureDetector sgd){

        }

        public boolean onScale(ScaleGestureDetector sgd){

            scale*= sgd.getScaleFactor();
            imageView.setScaleX(scale);
            imageView.setScaleY(scale);
            return true;
        }
    }

}
