package com.example.parth.piceditor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by parth on 04-02-2018.
 */

public class editorwindow extends AppCompatActivity {

    ImageView imageView;

    private int PICK_IMAGE = 1;

    Uri imageUri;

    float scale=1f;

    float angle=0;

    Spinner spinner;

    Bitmap image;

    ScaleGestureDetector scaleGestureDetector;

    InputStream inputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        overridePendingTransition(0, 0);

        scaleGestureDetector = new ScaleGestureDetector(this,new ScaleListener());

        List<String> categories = new ArrayList<>();
        categories.add("Select Option");
        categories.add("Rotate +");
        categories.add("Rotate -");
        categories.add("Crop");
        categories.add("Zoom In");
        categories.add("Zoom Out");
        spinner = (Spinner) findViewById(R.id.spinner2);
        imageView = (ImageView) findViewById(R.id.imgMain);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,categories);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                if(item == "Rotate +") {
                    angle += 90;
                    Matrix matrix = new Matrix();
                    matrix.postRotate((float) angle);
                    Bitmap rotated = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(),
                            matrix, true);
                    imageView.setImageBitmap(rotated);
                    image = rotated;
                    //Toast.makeText(getApplicationContext(), item, Toast.LENGTH_SHORT).show();
                }
                if(item == "Rotate -") {
                    angle -= 90;
                    Matrix matrix = new Matrix();
                    matrix.postRotate((float) angle);
                    Bitmap rotated = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(),
                            matrix, true);
                    imageView.setImageBitmap(rotated);
                    image = rotated;
                    //Toast.makeText(getApplicationContext(), item, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        openGallery();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        scaleGestureDetector.onTouchEvent(ev);

        return true;

    }

    private void openGallery()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,PICK_IMAGE);
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
                //imageView.setImageURI(imageUri);
                //imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            } catch (Exception e) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                        .show();
            }
        }
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

            // Multiply scale factor

            scale*= sgd.getScaleFactor();

            // Scale or zoom the imageview

            imageView.setScaleX(scale);

            imageView.setScaleY(scale);

            Log.i("Main",String.valueOf(scale));

            return true;

        }
    }

    //Rotating Image
    public static Bitmap rotateImage(Bitmap sourceImage, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(sourceImage, 0, 0, sourceImage.getWidth(), sourceImage.getHeight(), matrix, true);
    }
}
