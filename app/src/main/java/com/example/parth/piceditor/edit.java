package com.example.parth.piceditor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    int flag = 0;

    ImageButton bt1,bt2;

    FirebaseDatabase database;
    DatabaseReference myRef;

    public static final int REQUEST_CODE = 1014;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        overridePendingTransition(0, 0);

        scaleGestureDetector = new ScaleGestureDetector(this,new ScaleListener());

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("angle");

        List<String> categories = new ArrayList<>();
        categories.add("Select Option");
        categories.add("Rotate");
        categories.add("Crop");
        categories.add("Save Image");
        categories.add("Upload File");
        categories.add("Download File");
        categories.add("Download File Original");

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
                else if( item == "Save Image")
                {
                    storeImage(image);
                    /*Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_STREAM, getImageUri(getApplicationContext(),image));
                    startActivity(intent);*/

                }
                else if(item == "Upload File")
                {
                    uploadimage();
                }
                else if(item == "Download File")
                {
                    downloadimage();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        openGallery();

    }

    public void uploadimage()
    {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

        StorageReference riversRef = mStorageRef.child("images/myimg.jpg");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        myRef.setValue(angle);
        image = rotateImage(image,angle);
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        riversRef.putBytes(byteArray)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        // Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = 100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                progressDialog.setMessage((int) progress + "% Uploaded");
            }
        });
    }

    public void downloadimage()
    {
        try {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Downloading...");
            progressDialog.show();

            StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

            StorageReference pathReference = mStorageRef.child("images/myimg.jpg");
            final File localFile = File.createTempFile("images", "jpg");

            pathReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            progressDialog.dismiss();
                            image = decodeFile(localFile);
                            imageView.setImageBitmap(image);

                        } //Successfully downloaded data to local file
                        // ...
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle failed download
                    // ...
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    double progress = 100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount();
                    progressDialog.setMessage((int)progress + "% Download");
                }
            });
        }
        catch (Exception e){}
    }

    private Bitmap decodeFile(File f){
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }

    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            //Log.d(TAG,"Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            //Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            //Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
        Toast.makeText(getApplicationContext(),"File Saved At\n"+pictureFile.getPath().toString(),Toast.LENGTH_SHORT).show();
    }

    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm HH:mm:ss").format(new Date());
        File mediaFile;
        String mImageName="PI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
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
        angle = angle%360;
        imageView.setRotation(angle);
        /*if (flag == 0) {
            flag = 1;
            angle += 90;
            image = rotateImage(image,angle);
            imageView.setImageBitmap(image);
            myRef.setValue(angle);
            flag = 0;
        }*/
    }

    public void rotateminus(View view)
    {
        angle -= 90;
        angle = angle%360;
        imageView.setRotation(angle);
        /*
        if (flag == 0) {
            flag = 1;
            angle -= 90;
            image = rotateImage(image, angle);
            imageView.setImageBitmap(image);
            myRef.setValue(angle);
            flag = 0;
        }*/
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
