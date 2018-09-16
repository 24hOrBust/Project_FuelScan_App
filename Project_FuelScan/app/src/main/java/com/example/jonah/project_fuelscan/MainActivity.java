package com.example.jonah.project_fuelscan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    Uri file;
    double latitude;
    double longitude;
    private static final double DEFAULT_LATITUDE = 42.730171;
    private static final double DEFAULT_LONGITUDE = -73.678802;
    Bitmap photo, file_upload;
    private static final String url = "http://projectfuelscan.mybluemix.net/api/v1/measurement";
    private static final int PICK_IMAGE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageview);

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);


    }

    public void takePicture(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1002);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1002) {
            if (resultCode == RESULT_OK) {
               // String str = MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME;
               // file = MediaStore.Images.Media.getContentUri(str);
                photo = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(photo);

                new UploadImage(photo, longitude, latitude).execute();
            }

        }else if(requestCode == 100 && resultCode == RESULT_OK){

            file = data.getData();
            imageView.setImageURI(file);

            try {
                file_upload = MediaStore.Images.Media.getBitmap(this.getContentResolver(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            new UploadImage(file_upload, DEFAULT_LONGITUDE, DEFAULT_LATITUDE).execute();
        }

    }



    public void upload_photo(View view) {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);

    }

    private class UploadImage extends AsyncTask<Void, Void, Void>{

        Bitmap image;
        Double lng, lat;

        public UploadImage(Bitmap image, Double lng, Double lat){
            this.image = image;
            this.lng = lng;
            this.lat = lat;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
            String encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("lat", lat.toString())
                    .add("lng", lng.toString())
                    .add("image_data", encodedImage)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                Log.v("here", "gotclhebvcywqguywqgyuqwgfkyuweqg");
                // Do something with the response.
            } catch (IOException e) {
                Log.v("Here too", e.toString());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(MainActivity.this, "Upload success", Toast.LENGTH_LONG).show();
        }
    }
}
