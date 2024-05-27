package com.example.kim_j_project4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // set initial text value
        Intent myIntent = getIntent();
        String username = myIntent.getStringExtra("username");
        EditText userText = findViewById(R.id.usernameText);
        if (!username.isEmpty()) {
            userText.setText(username);
        }
    }

    // handles process when sign up button is clicked
    public void registerUser(View view) {
        EditText userText = findViewById(R.id.usernameText);
        EditText pwText = findViewById(R.id.passwordText);
        EditText heightText = findViewById(R.id.heightText);
        EditText weightText = findViewById(R.id.weightText);
        EditText ageText = findViewById(R.id.ageText);
        String username = userText.getText().toString();
        String password = pwText.getText().toString();
        String height = heightText.getText().toString();
        String weight = weightText.getText().toString();
        String age = ageText.getText().toString();

        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        Intent nextIntent = new Intent(SignupActivity.this, MainPageActivity.class);

        // check if username already exists
        if (sharedPreferences.contains(username)) {
            Toast.makeText(this, "Username Taken", Toast.LENGTH_SHORT).show();
            return;
        }

        // check for valid inputs
        if (username.isEmpty() || password.isEmpty() || height.isEmpty() || weight.isEmpty() || age.isEmpty()) {
            Toast.makeText(this, "Invalid Inputs", Toast.LENGTH_SHORT).show();
            return;
        }

        // save user info
        Toast.makeText(this, "Registered", Toast.LENGTH_SHORT).show();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(username, "username");
        editor.putString(username + "_password", password);
        editor.putString(username + "_height", height);
        editor.putString(username + "_weight", weight);
        editor.putString(username + "_age", age);
        editor.apply();
        // pass data to next intent/activity
        nextIntent.putExtra("username", username);
        Log.i("HERE", "registered");
        startActivity(nextIntent);
    }
}