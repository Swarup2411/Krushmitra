package com.mountrich.krushimitra.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mountrich.krushimitra.R;
import com.mountrich.krushimitra.crop_diseast_detection_api.HuggingFaceClient;
import com.mountrich.krushimitra.crop_diseast_detection_api.PlantDiseaseApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CropDoctorFragment extends Fragment {

    private ImageView ivCropImg;
    private TextView tvResult;
    private Button btnSelectImg, btnAnalyze;

    private static final int PICK_IMAGE = 1;
    private Uri imageUri;

    public CropDoctorFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_crop_doctor_fragement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnSelectImg = view.findViewById(R.id.btnSelectImage);
        btnAnalyze = view.findViewById(R.id.btnAnalyze);
        ivCropImg = view.findViewById(R.id.imagePreview);
        tvResult = view.findViewById(R.id.txtDiseaseName);

        btnSelectImg.setOnClickListener(v -> openGallery());

        btnAnalyze.setOnClickListener(v -> {

            if (imageUri == null) {
                tvResult.setText("Please select an image first");
                return;
            }

            try {

                File file = uriToFile(imageUri);

                callDiseaseApi(file);

            } catch (Exception e) {

                e.printStackTrace();
                tvResult.setText("Error processing image");

            }

        });
    }

    private void openGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {

            imageUri = data.getData();

            ivCropImg.setImageURI(imageUri);
        }
    }

    private RequestBody imageToRequestBody(File file) {

        return RequestBody.create(
                MediaType.parse("application/octet-stream"),
                file
        );
    }

    private void callDiseaseApi(File imageFile) {

        PlantDiseaseApi api =
                HuggingFaceClient.getClient().create(PlantDiseaseApi.class);

        RequestBody body = imageToRequestBody(imageFile);

        Call<Object> call = api.detectDisease(
                "Bearer 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",
                body
        );

        call.enqueue(new Callback<Object>() {

            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {

                if (response.isSuccessful() && response.body() != null) {

                    String result = response.body().toString();

                    tvResult.setText(result);

                } else {

                    tvResult.setText("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {

                tvResult.setText("Failed: " + t.getMessage());
            }
        });
    }

    private File uriToFile(Uri uri) throws Exception {

        InputStream inputStream =
                requireActivity().getContentResolver().openInputStream(uri);

        File file = new File(requireActivity().getCacheDir(), "image.jpg");

        FileOutputStream outputStream = new FileOutputStream(file);

        byte[] buffer = new byte[1024];
        int length;

        while ((length = inputStream.read(buffer)) > 0) {

            outputStream.write(buffer, 0, length);
        }

        outputStream.close();
        inputStream.close();

        return file;
    }
}