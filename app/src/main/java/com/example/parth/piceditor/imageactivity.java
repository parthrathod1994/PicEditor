package com.example.parth.piceditor;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by parth on 11-02-2018.
 */

public class imageactivity extends AppCompatActivity {
    Bitmap image;
    ImageView imageView;

    TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        overridePendingTransition(0, 0);

        imageView = (ImageView) findViewById(R.id.image2);
        downloadimage();
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
}
