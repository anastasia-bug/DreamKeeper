package com.example.dreamkeeper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordActivity extends AppCompatActivity {

    private EditText pinEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        pinEditText = findViewById(R.id.pin_edit_text);
        pinEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        findViewById(R.id.confirm_button).setOnClickListener(v -> verifyPin());
    }

    private void verifyPin() {
        String inputPin = pinEditText.getText().toString();

        if (inputPin.length() != 6) {
            Toast.makeText(this, "Введите 6 цифр", Toast.LENGTH_SHORT).show();
            return;
        }

        String inputHash = hashPin(inputPin);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String savedHash = prefs.getString("password_hash", null);

        if (savedHash != null && savedHash.equals(inputHash)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("from_password_activity", true);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Неверный PIN", Toast.LENGTH_SHORT).show();
        }
    }

    public static String hashPin(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pin.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 не поддерживается", e);
        }
    }
}