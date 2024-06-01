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

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // handles process when log in button is clicked
    public void loggingIn(View view) {
        EditText usernameText = findViewById(R.id.userText);
        String username = usernameText.getText().toString();
        EditText passwordText = findViewById(R.id.pwText);
        String password = passwordText.getText().toString();

        // check if entries are empty
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Invalid Inputs", Toast.LENGTH_SHORT).show();
            usernameText.setText("");
            passwordText.setText("");
            return;
        }

        // check if username already exists
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        Intent myIntent = new Intent(LoginActivity.this, DashboardActivity.class);
        if (sharedPreferences.contains(username)) {
            // validate password
            String storedPw = sharedPreferences.getString(username + "_password", null);
            if (password.equals(storedPw)) {  // correct pw
                Toast.makeText(this, "Logging In", Toast.LENGTH_SHORT).show();
                myIntent.putExtra("username", username);
                startActivity(myIntent);
            } else {  // incorrect pw
                Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                usernameText.setText("");
                usernameText.setHint("Username");
                passwordText.setText("");
                passwordText.setHint("Password");
            }
        } else {
            Toast.makeText(this, "User Does Not Exist", Toast.LENGTH_SHORT).show();
        }
    }

    // go to sign up page
    public void signingUp(View view) {
        EditText usernameText = findViewById(R.id.userText);
        String username = usernameText.getText().toString();
        Intent myIntent = new Intent(LoginActivity.this, SignupActivity.class);
        myIntent.putExtra("username", username);
        startActivity(myIntent);
    }
}