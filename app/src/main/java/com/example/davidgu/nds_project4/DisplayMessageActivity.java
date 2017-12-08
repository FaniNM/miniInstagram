package com.example.davidgu.nds_project4;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

public class DisplayMessageActivity extends AppCompatActivity {
    private static final String TAG = "Display";

    private int GET_FROM_GALLERY = 1;
    private ImageView imageContainer;
    private ProgressBar progressBar;
    private Button uploadButton;
    //    private Button galleryButton;
    private Button searchButton;
    private EditText description;

    private CheckBox imagePublic;
    //private TextView downloadUrl;

    //private StorageReference mStorageRef;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        email = getIntent().getStringExtra("user_email");
        Log.d(TAG,email);

        setContentView(R.layout.activity_display_message);

        imageContainer = findViewById(R.id.image_container);
        uploadButton = (Button)findViewById(R.id.upload_button);
        uploadButton.setOnClickListener (new UploadOnClickListener());
//        galleryButton = (Button)findViewById(R.id.image_gallery);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
//        progressBar.setVisibility(View.GONE);
        description = findViewById(R.id.image_description);
        imagePublic = (CheckBox)findViewById(R.id.image_public);

        searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener (new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sendMessage(v);
            }
        });
        //downloadUrl = (TextView) findViewById(R.id.download_url);


    }




    private class UploadOnClickListener implements View.OnClickListener {
        @Override
        public void onClick (View view) {
//            imageContainer.setDrawingCacheEnabled (true);
//            imageContainer.buildDrawingCache();
//            Bitmap bitmap = imageContainer.getDrawingCache();
            BitmapDrawable bitmapDrawable = (BitmapDrawable) imageContainer.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] data = stream.toByteArray();

            String img_path = "firememes/" + UUID.randomUUID() + ".png";
            StorageReference firememeRef = storage.getReference(img_path);

            StorageMetadata metadata = new StorageMetadata.Builder()
                    .build();

            progressBar.setVisibility(View.VISIBLE);
            uploadButton.setEnabled(false);

            final Uri[] img_Url = new Uri[1];

            UploadTask uploadTask = firememeRef.putBytes (data, metadata);
            uploadTask.addOnSuccessListener(DisplayMessageActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressBar.setVisibility(View.GONE);
                    uploadButton.setEnabled(true);
                    String temp = img_Url[0].toString();
                    Log.d(TAG, temp);
                    img_Url[0] = taskSnapshot.getDownloadUrl();
//                    downloadUrl.setText(url.toString());
////                    downloadUrl.setVisibility(View.VISIBLE);
                }
            });

            int availability;
            //mDatabase.setValue();
            if (imagePublic.isChecked()){
                availability = 1;
            }
            else{
                availability = 0;
            }

            String image_description = description.getText().toString();

            writeNewUser(availability, email, img_Url[0], image_description);
        }
    }

    public void select_from_gallery(View view) {
        startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                imageContainer.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    private void writeNewUser(int availability, String email, Uri img_Url, String Discription) {

        String imgURL = img_Url.toString();
        mDatabase.child("users").setValue(email);
        mDatabase.child("users").child(email).child("img_Url").setValue(imgURL);
        mDatabase.child("users").child(email).child("img_Url").child(imgURL).child("available").setValue(availability);
        mDatabase.child("users").child(email).child("img_Url").child(imgURL).child("discription").setValue(Discription);
    }


    public void sendMessage(View view) {
        Log.d(TAG,"In Send Message");
        Log.d(TAG, email);
        Intent intent = new Intent(this, SearchActivity.class);
        //EditText editText = (EditText) findViewById(R.id.editText);
        intent.putExtra("user_email", email);
        startActivity(intent);
    }
}


//Reference:
//https://www.youtube.com/watch?time_continue=148&v=7puuTDSf3pk



