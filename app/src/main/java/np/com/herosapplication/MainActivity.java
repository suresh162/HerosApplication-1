package np.com.herosapplication;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import api.HeroAPI;
import model.ImageResponse;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import url.Url;

public class MainActivity extends AppCompatActivity {
    private EditText etName, etDescription;
    private Button btnSave;
    private ImageView imgProfile;
    String imagePath;
    String imageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etName = findViewById(R.id.etName);
        imgProfile = findViewById(R.id.imgProfile);
        etDescription = findViewById(R.id.etDesc);
        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveImageOnly();
            }
        });
        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BrowseImage();
            }
        });

    }

    private void BrowseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (data == null) {

                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            }
        }
        Uri uri = data.getData();
        imagePath = getRealPathFromUri(uri);
        previewImage(imagePath);

    }

    private void previewImage(String imagePath) {

        File imgFile = new File(imagePath);
        if (imgFile.exists()) {

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imgProfile.setImageBitmap(myBitmap);
        }
    }

    private String getRealPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), uri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int colIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(colIndex);
        cursor.close();
        return result;

    }


    private void StrictMode() {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    private void SaveImageOnly() {
        File file = new File(imagePath);
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("imageFile", file.getName(), requestBody);
        HeroAPI heroAPI = Url.getInstance().create(HeroAPI.class);
        Call<ImageResponse> responseBodyCall = heroAPI.uploadImage(body);
        StrictMode();

        try {

            Response<ImageResponse> imageResponseResponse = responseBodyCall.execute();
            imageName = imageResponseResponse.body().getFilename();
            Save();
        } catch (IOException e) {

            Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void Save() {
        String name = etName.getText().toString();
        String desc = etDescription.getText().toString();


        HeroAPI heroAPI = Url.getInstance().create(HeroAPI.class);
        Call<Void> heroesCall = heroAPI.addHero(name, desc, imageName);

        heroesCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Code" + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(MainActivity.this, "Successfully added", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "error" + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
