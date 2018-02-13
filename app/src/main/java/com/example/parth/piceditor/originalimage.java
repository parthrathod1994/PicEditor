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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

public class originalimage extends AppCompatActivity {

    Bitmap image;
    ImageView imageView;
    float angle = 0;
    FirebaseDatabase database;
    DatabaseReference myRef;

    TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_original);
        overridePendingTransition(0, 0);

        imageView = (ImageView) findViewById(R.id.image1);
        //textView = (TextView) findViewById(R.id.tw);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        myRef.keepSynced(true);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                firebasedata firebasedt = dataSnapshot.getValue(firebasedata.class);
                angle = firebasedt.getAngle();
                //textView.setText(String.valueOf(angle));
                downloadimage();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //downloadimage();
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
                            image = rotateImage(image,-angle);
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

    public static Bitmap rotateImage(Bitmap sourceImage, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(sourceImage, 0, 0, sourceImage.getWidth(), sourceImage.getHeight(), matrix, true);
    }
}
