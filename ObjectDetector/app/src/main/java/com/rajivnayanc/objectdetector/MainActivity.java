package com.rajivnayanc.objectdetector;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ImageView imageHolder;
    private Button btn;
    private int REQUEST_CODE = 1;
    private String URL;// = "http://192.168.122.1/";
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageHolder = findViewById(R.id.selectedPhoto);
        btn = findViewById(R.id.selectPhoto);
        editText = findViewById(R.id.ip_address);
        checkPermission();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                URL = "http://"+editText.getText().toString()+"/";
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent.createChooser(intent,"Select a Photo"),REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==REQUEST_CODE){

                Uri imageUri = data.getData();

                try {

                    InputStream is = getContentResolver().openInputStream(data.getData());

                    uploadImage(getBytes(is));

                } catch (IOException e) {
                    e.printStackTrace();
                }

//                imageHolder.setImageURI(imageUri);
            }
        }
    }

    public byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();

        int buffSize = 1024;
        byte[] buff = new byte[buffSize];

        int len = 0;
        while ((len = is.read(buff)) != -1) {
            byteBuff.write(buff, 0, len);
        }

        return byteBuff.toByteArray();
    }

    private void uploadImage(byte[] imageBytes) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        placeholderAPI retrofitInterface = retrofit.create(placeholderAPI.class);

        RequestBody requestFile = RequestBody.create(MediaType.parse("*/*"), imageBytes);

        MultipartBody.Part body = MultipartBody.Part.createFormData("image", "image.jpg", requestFile);
        Call<ResponseBody> call = retrofitInterface.objectDetect(body);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("API CALL","SUCCESSFUL");
                Bitmap decodedByte;
                try {
                    byte[] encodedString = response.body().bytes();
                    decodedByte = BitmapFactory.decodeByteArray(encodedString, 0, encodedString.length);
                    imageHolder.setImageBitmap(decodedByte);
                }catch (IOException e){
                    Log.e("API CALL",e.getMessage());
                }



            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("API CALL","UNSUCCESSFUL, "+t.getMessage());
            }
        });

    }



    private boolean checkPermission(){
        int camera = ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA);
        int mediaStorage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_MEDIA_LOCATION);
        int writeStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int mediaContentControl = ContextCompat.checkSelfPermission(this, Manifest.permission.MEDIA_CONTENT_CONTROL);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if(camera!= PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(android.Manifest.permission.CAMERA);
        }

        if(mediaStorage!= PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_MEDIA_LOCATION);
        }

        if(writeStorage!= PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if(readStorage!= PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if(mediaContentControl!= PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(Manifest.permission.MEDIA_CONTENT_CONTROL);
        }

        if(!listPermissionsNeeded.isEmpty()){
            ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray(
                    new String[listPermissionsNeeded.size()]),1);
            return false;
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
