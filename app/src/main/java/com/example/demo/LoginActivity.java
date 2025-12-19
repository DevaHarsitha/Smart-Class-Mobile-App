package com.example.demo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText, emailEditText, regNoEditText;
    private RadioGroup userTypeGroup;
    private Button loginButton;

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String USER_DATA_PREFS = "UserData";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        usernameEditText = findViewById(R.id.edit1);
        passwordEditText = findViewById(R.id.edit2);
        emailEditText = findViewById(R.id.edit3);
        userTypeGroup = findViewById(R.id.userTypeGroup);
        loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();


        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = userTypeGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Select user type", Toast.LENGTH_SHORT).show();
            return;
        }

        String userType = (selectedId == R.id.radioStaff) ? "Staff" : "Student";

        String userKey = username + "_" + password;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.putString("email", email);
        editor.putString("login", "true");
        editor.putString("userType", userType);
        editor.putString("userKey", userKey);
        editor.apply();

        SharedPreferences userPrefs = getSharedPreferences(USER_DATA_PREFS, MODE_PRIVATE);
        if (userPrefs.contains(userKey)) {
            Toast.makeText(this, "Welcome back! Data restored.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "New session started.", Toast.LENGTH_SHORT).show();
        }
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
