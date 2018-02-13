package com.example.parth.piceditor;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    Button button,button1,button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        button1 = (Button) findViewById(R.id.button3);
        button2 = (Button) findViewById(R.id.button4);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
    }

    private void openGallery()
    {
        Intent intent = new Intent(getApplicationContext(),edit.class);
        startActivity(intent);
    }

    public void openOriginal(View view)
    {
        Intent intent = new Intent(getApplicationContext(),originalimage.class);
        startActivity(intent);
    }

    public void openImage(View view)
    {
        Intent intent = new Intent(getApplicationContext(),imageactivity.class);
        startActivity(intent);
    }
}
