package com.example.demo;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImageView;
    private EditText nameEditText, classEditText, emailText, regNoText, rollEditText, deptEditText;
    private TextView classLabel, regNoLabel;
    private Bitmap selectedBitmap = null;
    private String userKey, userType;

    private static final String USER_DATA_PREFS = "UserData";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImageView = view.findViewById(R.id.imageView);
        nameEditText = view.findViewById(R.id.name);
        classEditText = view.findViewById(R.id.cls);
        rollEditText = view.findViewById(R.id.t4);
        emailText = view.findViewById(R.id.t2);
        regNoText = view.findViewById(R.id.t3);
        deptEditText = view.findViewById(R.id.t6);

        regNoLabel = view.findViewById(R.id.d3);

        Button logoutBtn = view.findViewById(R.id.logout);
        Button updateBtn = view.findViewById(R.id.update);

        SharedPreferences appPrefs = requireActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        userKey = appPrefs.getString("userKey", null);
        userType = appPrefs.getString("userType", "student");

        if ("staff".equalsIgnoreCase(userType)) {
            classEditText.setHint("Enter Designation");

            regNoLabel.setText("Staff ID:");
            regNoText.setHint("Enter Staff ID");
        }

        if (userKey != null) {
            SharedPreferences userPrefs = requireActivity().getSharedPreferences(USER_DATA_PREFS, MODE_PRIVATE);
            String userProfileJson = userPrefs.getString(userKey + "_profile", null);
            if (userProfileJson != null) {
                Map<String, String> profileData = new Gson().fromJson(userProfileJson, new TypeToken<Map<String, String>>() {}.getType());
                nameEditText.setText(profileData.getOrDefault("username", ""));
                classEditText.setText(profileData.getOrDefault("className", ""));
                rollEditText.setText(profileData.getOrDefault("rollno", ""));
                emailText.setText(profileData.getOrDefault("email", ""));
                regNoText.setText(profileData.getOrDefault("RegNo", ""));
                deptEditText.setText(profileData.getOrDefault("department", ""));

                String profileImageBase64 = profileData.get("profileImage");
                if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                    byte[] decodedBytes = Base64.decode(profileImageBase64, Base64.DEFAULT);
                    Bitmap bitmap = BitmapUtils.decodeByteArrayToBitmap(decodedBytes);
                    profileImageView.setImageBitmap(bitmap);
                }
            }
        }

        profileImageView.setOnClickListener(v -> openGallery());

        updateBtn.setOnClickListener(v -> {
            String nameInput = nameEditText.getText().toString().trim();
            String classInput = classEditText.getText().toString().trim();
            String rollnoInput = rollEditText.getText().toString().trim();
            String emailInput = emailText.getText().toString().trim();
            String regNoInput = regNoText.getText().toString().trim();
            String deptInput = deptEditText.getText().toString().trim();

            if (nameInput.isEmpty() || classInput.isEmpty() || emailInput.isEmpty()
                    || regNoInput.isEmpty() || rollnoInput.isEmpty() || deptInput.isEmpty()) {
                Toast.makeText(requireActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userKey != null) {
                Map<String, String> profileData = new HashMap<>();
                profileData.put("username", nameInput);
                profileData.put("className", classInput);
                profileData.put("rollno", rollnoInput);
                profileData.put("email", emailInput);
                profileData.put("RegNo", regNoInput);
                profileData.put("department", deptInput);

                if (selectedBitmap != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    selectedBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    String encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                    profileData.put("profileImage", encodedImage);
                }

                SharedPreferences userPrefs = requireActivity().getSharedPreferences(USER_DATA_PREFS, MODE_PRIVATE);
                userPrefs.edit().putString(userKey + "_profile", new Gson().toJson(profileData)).apply();

                Toast.makeText(requireActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            }
        });

        logoutBtn.setOnClickListener(v -> {
            SharedPreferences.Editor editor = appPrefs.edit();
            editor.clear();
            editor.apply();

            profileImageView.setImageResource(R.drawable.profile);

            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                profileImageView.setImageBitmap(selectedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class BitmapUtils {
        public static Bitmap decodeByteArrayToBitmap(byte[] data) {
            return android.graphics.BitmapFactory.decodeByteArray(data, 0, data.length);
        }
    }
}
