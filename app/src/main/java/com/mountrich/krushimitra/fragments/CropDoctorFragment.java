package com.mountrich.krushimitra.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.mountrich.krushimitra.R;

public class CropDoctorFragment extends Fragment {

    private ImageView imgLeaf;
    private Button btnUpload, btnScan;
    private TextView txtResult;

    private Bitmap selectedBitmap;

    private static final int IMAGE_REQUEST = 100;



    public CropDoctorFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_crop_doctor_fragement,
                container,
                false);

        imgLeaf = view.findViewById(R.id.imgLeaf);
        btnUpload = view.findViewById(R.id.btnUpload);
        btnScan = view.findViewById(R.id.btnScan);
        txtResult = view.findViewById(R.id.txtResult);

        btnUpload.setOnClickListener(v -> openGallery());

        btnScan.setOnClickListener(v -> scanDisease());

        return view;
    }

    private void openGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        startActivityForResult(intent, IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST
                && resultCode == Activity.RESULT_OK
                && data != null) {

            Uri imageUri = data.getData();

            try {

                selectedBitmap =
                        MediaStore.Images.Media.getBitmap(
                                requireActivity().getContentResolver(),
                                imageUri);

                imgLeaf.setImageBitmap(selectedBitmap);

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    private void scanDisease() {

        if (selectedBitmap == null) {
            txtResult.setText("Please upload image first");
            return;
        }

        try {

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            byte[] byteArray = stream.toByteArray();

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                            "images",
                            "leaf.jpg",
                            RequestBody.create(byteArray, MediaType.parse("image/jpeg"))
                    )
                    .addFormDataPart("organs", "leaf")
                    .build();

            Request request = new Request.Builder()
                    .url("https://my-api.plantnet.org/v2/identify/all?api-key=2b10gIhHNx1Y0KKYdCdMinjp6e")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {

                    requireActivity().runOnUiThread(() ->
                            txtResult.setText("API Error: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String responseData = response.body().string();

                    requireActivity().runOnUiThread(() ->
                            parseResult(responseData));
                }
            });

        } catch (Exception e) {

            txtResult.setText("Error: " + e.getMessage());
        }
    }
    private String bitmapToBase64(Bitmap bitmap) {

        ByteArrayOutputStream byteArrayOutputStream =
                new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG,
                90,
                byteArrayOutputStream);

        byte[] byteArray =
                byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(
                byteArray,
                Base64.DEFAULT);
    }

    private void parseResult(String json) {

        try {

            JSONObject jsonObject = new JSONObject(json);

            JSONArray results = jsonObject.getJSONArray("results");

            if (results.length() > 0) {

                JSONObject plant = results.getJSONObject(0);

                JSONObject species = plant.getJSONObject("species");

                String name = species.getString("scientificNameWithoutAuthor");

                txtResult.setText("Plant Detected: " + name);

            } else {

                txtResult.setText("Plant not detected");
            }

        } catch (Exception e) {

            txtResult.setText("Parsing Error");
        }
    }

}